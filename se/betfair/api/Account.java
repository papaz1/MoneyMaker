package se.betfair.api;

import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.util.Log;

public class Account {

    private final String CLASSNAME = Account.class.getName();
    private static Account instance;
    private static final String LOGIN_TARGET = "https://identitysso.betfair.com/api/login";
    private static final String LOGOUT_TARGET = "https://identitysso.betfair.com/api/logout";
    private String username;
    private String sessionToken;

    private Account() {
    }

    public static Account getInstance() {
        if (instance == null) {
            instance = new Account();
        }
        return instance;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String login(String username, String password) throws DBConnectionException {
        final String METHOD = "login";
        final String LOGIN_ERROR = "PROBLEM";
        this.username = username;
        String s = "";
        try {
            s = "&username=" + URLEncoder.encode(username, "UTF-8");
            s += "&password=" + URLEncoder.encode(password, "UTF-8");
            s += "&login=" + URLEncoder.encode("true", "UTF-8");
            s += "&redirectMethod=" + URLEncoder.encode("POST", "UTF-8");
            s += "&product=" + URLEncoder.encode("home.betfair.int", "UTF-8");
            s += "&url=" + URLEncoder.encode("https://www.betfair.com/", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        }

        if (!s.equals("")) {
            sessionToken = makeCall(0, LOGIN_TARGET, s);
            if (sessionToken == null) {
                throw new DBConnectionException("Username or password incorrect");
            } else if (sessionToken.equalsIgnoreCase(LOGIN_ERROR)) {
                throw new DBConnectionException("Username or password incorrect");
            }
        }
        return sessionToken;
    }

    private String makeCall(int call, String target, String query) {
        final String METHOD = "makeCall";
        URL url;
        HttpsURLConnection conn;
        DataOutputStream outStream;
        String outString = "PROBLEM";

        try {
            url = new URL(target);
            conn = (HttpsURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            HttpsURLConnection.setFollowRedirects(true);

            outStream = new DataOutputStream(conn.getOutputStream());

            outStream.writeBytes(query);
            outStream.flush();
            outStream.close();
            conn.disconnect();

            int responseCode = conn.getResponseCode();

            if (call == 0) {
                if (responseCode == 200) {
                    String headerName;

                    for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
                        if (headerName.equals("Set-Cookie")) {
                            String cookie = conn.getHeaderField(i);

                            if (cookie.contains("ssoid")) {
                                outString = cookie.substring(cookie.indexOf("=") + 1, cookie.indexOf(";"));
                            }
                        }
                    }
                }
            } else if (responseCode == 200) {
                outString = "" + responseCode;
            }
        } catch (IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        }

        return outString;
    }
}
