package se.moneymaker.serviceprovider;

import com.betfair.aping.exceptions.APINGException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.betfair.enums.ErrorCode;
import se.betfair.util.JsonConverter;
import se.main.application.PriceReader;
import se.moneymaker.container.BetOfferIdsContainer;
import se.moneymaker.db.DBServices;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.ReadReason;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.model.Bookmaker;
import se.moneymaker.util.Log;
import se.moneymaker.util.Utils;

public class ReadPrice extends HttpServlet {

    private static final String CLASSNAME = ReadPrice.class.getName();
    private PriceReader priceReader;
    private final DBServices services;

    public ReadPrice(DBServices services) {
        this.services = services;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String METHOD = "doPost";

        response.setCharacterEncoding(ServiceProviderConstants.CHARACTER_ENCODING);
        response.setContentType(ServiceProviderConstants.CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();

        String sessionToken = request.getHeader(ServiceProviderConstants.SESSION_TOKEN);
        if (sessionToken != null) {
            StringBuilder sb = ServiceProviderUtility.readRequest(request.getReader());
            BetOfferIdsContainer betOfferIDs = JsonConverter.convertFromJson(sb.toString(), BetOfferIdsContainer.class);
            String accountName = betOfferIDs.getAccount().getAccountName();
            
            synchronized (this) {
                if (priceReader == null) {
                    priceReader = new PriceReader(sessionToken, accountName, false, ReadReason.TRADING, 0);
                }
            }

            List<String> marketIDs;
            try {
                marketIDs = services.readBetOfferExternalKeys(betOfferIDs);
                if (!marketIDs.isEmpty()) {
                    try {
                        priceReader.readSendPrice(marketIDs, sessionToken);
                        response.setStatus(HttpServletResponse.SC_OK);
                    } catch (APINGException e) {
                        if (e.getErrorCode().equals(ErrorCode.INVALID_SESSION_INFORMATION.name())) {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.NO_SESSION, null, null));
                        } else {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.BETFAIR_ERROR, e.getMessage(), null));
                            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                        }
                    } catch (BetOfferException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        printWriter.print(Utils.toJSONStringErrorMsg(e.getErrorType(), e.getMessage(), null));
                        Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.BETPROVER_ERROR, "No betoffer references returned from Betprover", null));
                    Log.logMessage(CLASSNAME, METHOD, "No betoffer references returned from Betprover", LogLevelEnum.ERROR, false);
                }
            } catch (DBConnectionException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.BETPROVER_ERROR, e.getMessage(), null));
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.NO_SESSION, null, null));
        }
    }
}
