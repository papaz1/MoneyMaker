package se.main.application;

import se.moneymaker.util.AliveLog;
import se.moneymaker.util.AliveObserver;

public abstract class Application {

    private AliveLog log;
    private AliveObserver observer;

    protected void initApplication(long heartbeat, String application) {
        log = new AliveLog(heartbeat, application);
        observer = new AliveObserver(log);
        Thread target = new Thread(observer);
        target.start();
    }

    protected void iAmAlive() {
        log.iAmAlive();
    }

    protected void stopApp() {
        observer.stop();
    }
}
