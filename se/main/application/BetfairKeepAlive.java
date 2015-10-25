package se.main.application;

import com.betfair.aping.exceptions.APINGException;
import se.betfair.api.BetfairServices;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.util.Log;

public class BetfairKeepAlive implements Runnable {

    private final String CLASSNAME = BetfairKeepAlive.class.getName();
    private long SLEEP_TIME = 420000; //7 minutes
    private BetfairServices services;
    private volatile boolean run;
    private Notifiable listener;

    public BetfairKeepAlive(String sessionToken, String accountName) {
        services = new BetfairServices(sessionToken, accountName);
        run = true;
    }

    @Override
    public void run() {
        final String METHOD = "run";
        Log.logMessage(CLASSNAME, METHOD, "BetfairKeepAlive session service running...", LogLevelEnum.INFO, false);
        while (run) {
            try {
                services.keepAlive();
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                }
            } catch (APINGException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getErrorDetails(), LogLevelEnum.CRITICAL, true);
                notifyException();
                stop();
            }
        }
    }

    public void setListener(Notifiable listener) {
        this.listener = listener;
    }

    public void notifyException() {
        if (listener != null) {
            listener.exceptionOccured();
        }
    }

    public void stop() {
        final String METHOD = "stop";
        Log.logMessage(CLASSNAME, METHOD, "Stopping BetfairKeepAlive service", LogLevelEnum.INFO, false);
        run = false;
    }
}
