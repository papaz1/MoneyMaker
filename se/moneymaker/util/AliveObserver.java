package se.moneymaker.util;

import se.moneymaker.enums.LogLevelEnum;

public class AliveObserver implements Runnable {

    private static final String CLASSNAME = AliveObserver.class.getName();
    private final long TIME_SLEEP = 300000; //5 minutes
    private final AliveLog log;
    private boolean run;

    public AliveObserver(AliveLog log) {
        final String METHOD = "AliveObserver";
        Log.logMessage(CLASSNAME, METHOD, "Alive observer for " + log.getApplication() + " running...", LogLevelEnum.INFO, false);
        this.log = log;
        run = true;
    }

    @Override
    public void run() {
        while (run) {
            try {
                log.isAlive();
                Thread.sleep(TIME_SLEEP);
            } catch (InterruptedException e) {
            }
        }
    }

    public void stop() {
        run = false;
    }

}
