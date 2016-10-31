package se.moneymaker.serviceprovider;

import com.betfair.aping.enums.OrderType;
import com.betfair.aping.enums.PersistenceType;
import com.betfair.aping.enums.Side;
import com.betfair.aping.exceptions.APINGException;
import se.betfair.util.JsonConverter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import se.betfair.api.BetfairServices;
import se.betfair.enums.ErrorCode;
import se.betfair.factory.FactoryBet;
import se.betfair.model.LimitOrder;
import se.betfair.model.PlaceExecutionReport;
import se.betfair.model.PlaceInstruction;
import se.betfair.model.PlaceInstructionReport;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.jsonfactory.JSONFactoryBet;
import se.moneymaker.model.BetInstruction;
import se.moneymaker.container.PlaceBetContainer;
import se.moneymaker.db.DBServices;
import se.moneymaker.exception.BetException;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.exception.NoDataException;
import se.moneymaker.model.Bet;
import se.moneymaker.util.Log;
import se.moneymaker.util.Utils;

public class PlaceBet extends HttpServlet {

    private static final String CLASSNAME = PlaceBet.class.getName();
    private BetfairServices bfServices;
    private final DBServices services;
    private FactoryBet factoryBet;
    private String accountName;

    public PlaceBet(DBServices services) {
        this.services = services;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final String METHOD = "doPost";
        double r = Math.random();
        Log.logMessage(CLASSNAME, METHOD, r + ": Bet received", LogLevelEnum.CRITICAL, true);
        response.setCharacterEncoding(ServiceProviderConstants.CHARACTER_ENCODING);
        response.setContentType(ServiceProviderConstants.CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();

        try {
            String sessionToken = request.getHeader(ServiceProviderConstants.SESSION_TOKEN);
            if (sessionToken != null) {
                StringBuilder sb = ServiceProviderUtility.readRequest(request.getReader());

                //Check if the request is a JSON array, ie multiple bet instructions
                List<PlaceBetContainer> placeBetContainers;
                PlaceBetContainer placeBetContainer;
                if (sb.substring(0, 1).equalsIgnoreCase("[")) {
                    JSONArray tmpRequest = (JSONArray) JSONValue.parse(sb.toString());
                    Iterator<JSONObject> iterator = tmpRequest.iterator();
                    placeBetContainers = new ArrayList<>(tmpRequest.size());
                    while (iterator.hasNext()) {
                        String obj = iterator.next().toString();
                        placeBetContainer = JsonConverter.convertFromJson(obj, PlaceBetContainer.class);
                        accountName = placeBetContainer.getAccount().getAccountName();
                        placeBetContainers.add(placeBetContainer);
                    }
                } else {
                    placeBetContainers = new ArrayList<>(1);
                    placeBetContainer = JsonConverter.convertFromJson(sb.toString(), PlaceBetContainer.class);
                    accountName = placeBetContainer.getAccount().getAccountName();
                    placeBetContainers.add(placeBetContainer);
                }

                synchronized (this) {
                    if (bfServices == null) {
                        bfServices = new BetfairServices(accountName);
                    }
                }
                Log.logMessage(CLASSNAME, METHOD, r + ": Reading external keys", LogLevelEnum.CRITICAL, true);
                placeBetContainers = services.readPopulateExternalKeys(placeBetContainers);
                Log.logMessage(CLASSNAME, METHOD, r + ": Done", LogLevelEnum.CRITICAL, true);
                List<BetInstruction> betInstructions = FactoryBet.createBetInstructions(placeBetContainers);

                //Sorting done so that we can put all bets on same betoffer in same request
                Collections.sort(betInstructions, new Comparator<BetInstruction>() {
                    @Override
                    public int compare(BetInstruction inst1, BetInstruction inst2) {
                        return inst1.getBetOfferExternalKey().compareToIgnoreCase(inst2.getBetOfferExternalKey());
                    }
                });

                List<PlaceInstruction> placeInstructions = new ArrayList<>();
                String marketId = null;
                List<PlaceInstructionReport> placeInstructionReports = new ArrayList<>();
                for (BetInstruction betInstruction : betInstructions) {

                    if (marketId == null) {
                        marketId = betInstruction.getBetOfferExternalKey();
                    }
                    PlaceInstruction placeInstruction = new PlaceInstruction();
                    placeInstruction.setSelectionId(betInstruction.getOutcomeExternalKey());
                    placeInstruction.setOrderType(OrderType.LIMIT);
                    LimitOrder limitOrder = new LimitOrder();
                    limitOrder.setPersistenceType(PersistenceType.LAPSE);
                    limitOrder.setPrice(betInstruction.getPrice());
                    limitOrder.setSize(betInstruction.getSize());
                    placeInstruction.setLimitOrder(limitOrder);
                    if (betInstruction.isIsBack()) {
                        placeInstruction.setSide(Side.BACK);
                    } else {
                        placeInstruction.setSide(Side.LAY);
                    }
                    Log.logMessage(CLASSNAME, METHOD, r + ": Placing bet on marketId: " + betInstruction.getBetOfferExternalKey() + " selectionId: " + betInstruction.getOutcomeExternalKey()
                            + " isBack: " + betInstruction.isIsBack()
                            + " price: " + betInstruction.getPrice()
                            + " size: " + betInstruction.getSize()
                            + " sessionToken: " + sessionToken, LogLevelEnum.INFO, false);

                    //As long as the market ids are same the bet can be done in one request
                    if (marketId.equalsIgnoreCase(betInstruction.getBetOfferExternalKey())) {
                        placeInstructions.add(placeInstruction);
                    } else {
                        if (!placeInstructions.isEmpty()) {
                            try {
                                PlaceExecutionReport placeExecutionReport = placeOrder(marketId, placeInstructions, sessionToken);
                                List<PlaceInstructionReport> tmpPlaceInstructionReports = placeExecutionReport.getInstructionReports();
                                placeInstructionReports.addAll(tmpPlaceInstructionReports);
                            } catch (APINGException e) {
                                if (e.getErrorCode().equals(ErrorCode.INVALID_SESSION_INFORMATION.name())) {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.NO_SESSION, null, null));
                                } else {
                                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                    printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.BETFAIR_ERROR, e.getMessage(), null));
                                    Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                                }
                            }
                            placeInstructions.clear();
                            marketId = betInstruction.getBetOfferExternalKey();
                            placeInstructions.add(placeInstruction);
                        }
                    }
                }

                if (!placeInstructions.isEmpty()) {
                    try {
                        PlaceExecutionReport placeExecutionReport = placeOrder(marketId, placeInstructions, sessionToken);
                        List<PlaceInstructionReport> tmpPlaceInstructionReports = placeExecutionReport.getInstructionReports();
                        placeInstructionReports.addAll(tmpPlaceInstructionReports);
                    } catch (APINGException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.BETFAIR_ERROR, e.getMessage(), null));
                        Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                    }
                    placeInstructions.clear();
                }

                JSONArray responsesJSON = new JSONArray();
                if (!placeInstructionReports.isEmpty()) {
                    responsesJSON = prepareResponse(placeInstructionReports, betInstructions);
                }
                if (!responsesJSON.isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    printWriter.print(responsesJSON.toString());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.NO_SESSION, null, null));
            }
        } catch (BetException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            printWriter.print(e.toJSONString());
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        } catch (DBConnectionException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.BETPROVER_ERROR, e.getMessage(), null));
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        } catch (NoDataException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            printWriter.print(e.toJSONString());
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        } catch (BetOfferException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            printWriter.print(Utils.toJSONStringErrorMsg(e.getErrorType(), e.getMessage(), null));
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        }
    }

    private PlaceExecutionReport placeOrder(String marketId, List<PlaceInstruction> placeInstructions, String sessionToken) throws APINGException, BetException, BetOfferException {
        return bfServices.placeOrders(marketId, placeInstructions, sessionToken);
    }

    private JSONArray prepareResponse(List<PlaceInstructionReport> placeInstructionReports, List<BetInstruction> betInstructions) throws BetException {
        final String METHOD = "prepareResponse";
        if (factoryBet == null) {
            factoryBet = new FactoryBet();
        }

        JSONArray responsesJSON = new JSONArray();
        JSONObject placeBetJSON;
        List<Bet> bets = factoryBet.updatePlacedBets(placeInstructionReports, betInstructions);
        for (Bet bet : bets) {
            placeBetJSON = JSONFactoryBet.createBet(bet, false);
            Log.logMessage(CLASSNAME, METHOD, placeBetJSON.toString(), LogLevelEnum.INFO, true);
            responsesJSON.add(placeBetJSON);
        }
        return responsesJSON;
    }
}
