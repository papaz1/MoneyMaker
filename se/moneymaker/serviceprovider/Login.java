package se.moneymaker.serviceprovider;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import se.betfair.api.Account;
import se.betfair.util.JsonConverter;
import se.moneymaker.container.LoginContainer;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.util.Log;

public class Login extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Account account = Account.getInstance();
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        LoginContainer login = JsonConverter.convertFromJson(sb.toString(), LoginContainer.class);

        String accountName;
        String password;

        accountName = login.getAccount().getAccountName();
        password = login.getPassword();
        response.setContentType(ServiceProviderConstants.CONTENT_TYPE);

        if (accountName != null
                && !accountName.isEmpty()
                && password != null
                && !password.isEmpty()) {
            try {
                String sessionToken = account.login(accountName, password);
                response.setHeader(ServiceProviderConstants.SESSION_TOKEN, sessionToken);
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (DBConnectionException e) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
