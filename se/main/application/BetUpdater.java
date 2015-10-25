package se.main.application;

import com.betfair.aping.exceptions.APINGException;
import java.util.*;
import se.betfair.api.BetfairServices;
import se.betfair.factory.FactoryBet;
import se.betfair.model.CurrentOrderSummary;
import se.betfair.model.CurrentOrderSummaryReport;
import se.moneymaker.db.DBBet;
import se.moneymaker.db.DBServices;
import se.moneymaker.enums.DBBoolean;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.BetException;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.model.Bet;
import se.moneymaker.util.Log;

public class BetUpdater extends Application implements Runnable {

    private static final String CLASSNAME = BetUpdater.class.getName();
    private static final long HEARTBEAT = 1800000; //30 minutes
    private static final long TIME_SLEEP = 10000;
    private DBBet connBet;
    private final int MAX_BETIDS = 250;
    private BetfairServices services;
    private FactoryBet factory;
    private DBServices dbServcies;
    private String accountName;

    public BetUpdater(String accountName, String sessionToken) {
        initApplication(HEARTBEAT, CLASSNAME);
        this.accountName = accountName;
        services = new BetfairServices(sessionToken, accountName);
        factory = new FactoryBet(accountName);
        dbServcies = new DBServices(false);
        connBet = new DBBet(Source.BETFAIR.getName(), accountName);
    }

    @Override
    public void run() {
        final String METHOD = "run";
        Log.logMessage(CLASSNAME, METHOD, "BetUpdater running...", LogLevelEnum.INFO, false);
        while (true) {
            try {
                LinkedHashMap<String, Bet> placedBets = readBets();
                try {
                    if (placedBets != null) {
                        Set<String> betIds = filterBetIdsForCancellation(placedBets);

                        if (!betIds.isEmpty()) {
                            CurrentOrderSummaryReport currentOrderSummaryReport = services.listCurrentOrders(betIds);
                            List<CurrentOrderSummary> orders = currentOrderSummaryReport.getCurrentOrders();

                            for (CurrentOrderSummary orderSummary : orders) {

                                //Is there anything to cancel?
                                if (orderSummary.getSizeRemaining() > 0) {
                                    services.cancelOrders(orderSummary.getMarketId());
                                    orderSummary.setSizeCancelled(orderSummary.getSizeRemaining());
                                    orderSummary.setSizeRemaining(0);
                                    iAmAlive();
                                }
                            }
                            List<Bet> currentBets;
                            if (!orders.isEmpty()) {
                                currentBets = factory.updateCurrentBets(orders, placedBets);
                                updateBetInMMDb(currentBets);
                            }

                        }
                    }
                    iAmAlive();
                } catch (BetException ex) {
                    Log.logMessage(CLASSNAME, METHOD, ex.toString(), LogLevelEnum.ERROR, true);
                }
            } catch (APINGException | DBConnectionException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
            }
            try {
                Thread.sleep(TIME_SLEEP);
            } catch (InterruptedException e) {
            }
        }
    }

    private void updateBetInMMDb(List<Bet> bets) {
        connBet.updateBets(bets);
    }

    private LinkedHashMap<String, Bet> readBets() throws DBConnectionException {
        LinkedHashMap<String, Bet> betMap = null;

        //If there are a lot of bets only top 250 will be fetched and the bets with the shortest
        //validity date will be canceled first
        List<Bet> bets = dbServcies.readBets(DBBoolean.FALSE, null, null, Source.BETFAIR.getName(), accountName, true);
        if (bets != null) {
            Collections.sort(bets, new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    Bet b1 = (Bet) o1;
                    Bet b2 = (Bet) o2;

                    //If the dates are null then the dates will be set
                    //to yesterday making these bets canceled immidiately
                    if (b1.getValidUntil() == null) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                        b1.setValidUntil(cal.getTime());
                    }
                    if (b2.getValidUntil() == null) {
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                        b2.setValidUntil(cal.getTime());
                    }
                    if ((b1.getValidUntil()).before(b2.getValidUntil())) {
                        return -1;
                    } else if ((b1.getValidUntil()).after(b2.getValidUntil())) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            if (bets.size() > MAX_BETIDS) {
                bets = bets.subList(0, MAX_BETIDS - 1);
            }
            betMap = new LinkedHashMap<>(bets.size());
            for (Bet bet : bets) {
                betMap.put(bet.getExternalKey(), bet);
            }
        }
        return betMap;
    }

    private Set<String> filterBetIdsForCancellation(LinkedHashMap<String, Bet> bets) {
        final String METHOD = "parseBetIds";
        Set<String> betIds = new HashSet<>();
        Iterator<Bet> iterator = bets.values().iterator();
        Date today = new Date();
        while (iterator.hasNext()) {
            Bet bet = iterator.next();

            //Check if the bet should be canceled. Betids with 0 shouldn't exist but due to some problem in bet intelligence they do, filter them out.
            if (bet.getValidUntil().before(today)
                    && !bet.getExternalKey().equalsIgnoreCase("0")) {
                betIds.add(bet.getExternalKey());
            } else {
                Log.logMessage(CLASSNAME, METHOD, "Bet with betid 0 found", LogLevelEnum.ERROR, false);
            }

        }
        return betIds;
    }
}
