package se.betfair.api;

import se.betfair.util.JsonResponseHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import se.moneymaker.dict.Config;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.util.Log;

public class Connection {

    private static final String CLASSNAME = Connection.class.getName();
    private final String HTTP_HEADER_X_APPLICATION = "X-Application";
    private final String HTTP_HEADER_X_AUTHENTICATION = "X-Authentication";
    private final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    private final String HTTP_HEADER_ACCEPT = "Accept";
    private final String HTTP_HEADER_ACCEPT_CHARSET = "Accept-Charset";
    private final String APPLICATION_JSON = "application/json";
    private final String CHARSET = "UTF-8";
    private final int TIMEOUT = 45000;
    private String url;
    protected Lock lock;
    protected Delay delay;
    protected Thread timer;
    private String appKey;

    public Connection(String url, String accountName) {
        Config config = Config.getInstance();
        this.appKey = config.get(accountName.toUpperCase());
        this.url = url;
        lock = new ReentrantLock();
    }

    private String sendPostRequest(String sessionToken, String param, ResponseHandler<String> reqHandler) {
        final String METHOD = "sendPostRequest";
        String jsonRequest = param;
        HttpPost post = new HttpPost(url);
        String response = null;
        try {
            post.setHeader(HTTP_HEADER_CONTENT_TYPE, APPLICATION_JSON);
            post.setHeader(HTTP_HEADER_ACCEPT, APPLICATION_JSON);
            post.setHeader(HTTP_HEADER_ACCEPT_CHARSET, CHARSET);
            post.setHeader(HTTP_HEADER_X_APPLICATION, appKey);
            post.setHeader(HTTP_HEADER_X_AUTHENTICATION, sessionToken);
            if (jsonRequest != null) {
                post.setEntity(new StringEntity(jsonRequest, CHARSET));
            }
            HttpClient httpClient = new DefaultHttpClient();

            HttpParams httpParams = httpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, TIMEOUT);
            HttpConnectionParams.setSoTimeout(httpParams, TIMEOUT);

            response = httpClient.execute(post, reqHandler);
        } catch (UnsupportedEncodingException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        } catch (ClientProtocolException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        } catch (IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
        }
        return response;
    }

    public String sendPostRequestJsonRpc(String sessionToken, String param, int delay) {
        delay(delay);
        return sendPostRequest(sessionToken, param, new JsonResponseHandler());
    }

    public String sendPostRequestJson(String sessionToken, int delay) {
        delay(delay);
        return sendPostRequest(sessionToken, null, new JsonResponseHandler());
    }

    protected void delay(int callDelay) {
        if (callDelay > 0) {

            /**
             * Only reason for having the tryLock instead of lock immidiately is
             * because in debug mode we can have comments saying if there is a
             * need for delay or if we can proceed without a delay
             */
            if (!lock.tryLock()) {
                lock.lock();
            }

            try {
                if (delay == null || !delay.isRunning()) {
                    delay = new Delay(callDelay);
                    timer = new Thread(delay, "sleep");
                    timer.start();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    protected class Delay implements Runnable {

        private boolean isRunning = true;
        private int callDelay;

        protected Delay(int callDelay) {
            this.callDelay = callDelay;
        }

        @Override
        public void run() {
            try {
                lock.lock();
                Thread.sleep(callDelay);
                isRunning = false;
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
        }

        protected boolean isRunning() {
            return isRunning;
        }
    }
}
