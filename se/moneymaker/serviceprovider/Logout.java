package se.moneymaker.serviceprovider;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.betfair.api.BetfairServices;
import se.betfair.util.JsonConverter;
import se.moneymaker.container.LogoutContainer;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.util.Utils;

public class Logout extends HttpServlet {

    private BetfairServices bfServices;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(ServiceProviderConstants.CHARACTER_ENCODING);
        response.setContentType(ServiceProviderConstants.CONTENT_TYPE);
        PrintWriter printWriter = response.getWriter();

        String sessionToken = request.getHeader(ServiceProviderConstants.SESSION_TOKEN);
        if (sessionToken != null) {
            StringBuilder sb = ServiceProviderUtility.readRequest(request.getReader());
            LogoutContainer logoutContainer = JsonConverter.convertFromJson(sb.toString(), LogoutContainer.class);

            synchronized (this) {
                if (bfServices == null) {
                    bfServices = new BetfairServices(logoutContainer.getAccount().getAccountName());
                }
            }

            bfServices.logout(sessionToken);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            printWriter.print(Utils.toJSONStringErrorMsg(ErrorType.NO_SESSION, null, null));
        }
    }
}
