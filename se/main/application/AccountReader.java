package se.main.application;

import com.betfair.aping.enums.BetStatus;
import com.betfair.aping.enums.MarketGroupBy;
import com.betfair.aping.exceptions.APINGException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import se.betfair.api.BetfairServices;
import se.betfair.factory.FactoryAccount;
import se.betfair.factory.FactoryBet;
import se.betfair.model.AccountDetailsResponse;
import se.betfair.model.AccountFundsResponse;
import se.betfair.model.ClearedOrderSummary;
import se.betfair.model.ClearedOrderSummaryReport;
import se.betfair.model.ItemDescription;
import se.betfair.model.TimeRange;
import se.moneymaker.dict.Config;
import se.moneymaker.db.DBAccount;
import se.moneymaker.db.DBBet;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.model.AccountStatement;
import se.moneymaker.model.Bet;
import se.moneymaker.util.Log;
import se.moneymaker.util.Utils;

public class AccountReader extends Application implements Runnable {

    private final String CLASSNAME = AccountReader.class.getName();
    private final static long HEARTBEAT = 7200000; //2 hours
    public final static String MERGED_CANCELLED = "MoneyMakeerMergedCancelled";
    private DBBet connBet;
    private DBAccount connAccount;
    private BetfairServices services;
    private FactoryAccount factoryAccount;
    private FactoryBet factoryBet;
    private final SimpleDateFormat df;
    private Date to;
    private Date from;
    private boolean first;
    private final int SLEEP_TIME = 1800000; //30 minutes
    private HashMap<String, String> updatedBetsAndCommission;
    private List<ClearedOrderSummary> existingOrders;
    private List<ClearedOrderSummary> nonExistingOrders;
    private List<BetStatus> betStatuses;
    private String currency;

    public AccountReader(String accountName, String sessionToken, Date from) {
        final String METHOD = "AccountReader";
        initApplication(HEARTBEAT, CLASSNAME);
        this.from = from;
        betStatuses = new ArrayList<>(3);
        betStatuses.add(BetStatus.VOIDED);
        betStatuses.add(BetStatus.LAPSED);
        betStatuses.add(BetStatus.CANCELLED);
        betStatuses.add(BetStatus.SETTLED);
        Config config = Config.getInstance();
        config.get(accountName);

        services = new BetfairServices(sessionToken, accountName);
        try {
            AccountDetailsResponse accountDetails = services.getAccountDetails();
            currency = accountDetails.getCurrencyCode();
        } catch (APINGException e) {
            Log.logMessage(CLASSNAME, METHOD, "Could not get account details from Betfair", LogLevelEnum.CRITICAL, true);
            System.exit(0);
        }

        factoryBet = new FactoryBet(accountName, currency);
        factoryAccount = new FactoryAccount(Source.BETFAIR.getName(), accountName, currency);
        connBet = new DBBet(Source.BETFAIR.getName(), accountName);
        connAccount = new DBAccount();
        df = new SimpleDateFormat("yyyyMMdd");
        first = true;
        updatedBetsAndCommission = new HashMap<>();
        existingOrders = new ArrayList<>();
        nonExistingOrders = new ArrayList<>();
    }

    @Override
    public void run() {
        final String METHOD = "run";
        while (true) {
            try {
                Date today;
                if (first) {
                    today = from;
                    to = Utils.getTomorrow(df);
                } else {
                    today = new Date(Utils.parseStringToLongDate(df.format(new Date()), df));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(today);
                    cal.add(Calendar.HOUR, -3);
                    today = cal.getTime();
                }
                TimeRange range = new TimeRange();
                range.setFrom(today);

                //Check if there are lapsed and settled bets that need to be merged
                ClearedOrderSummaryReport orderSummaryReport = services.listClearedOrders(null, BetStatus.LAPSED, range, null);
                List<ClearedOrderSummary> lapsedOrders = new ArrayList<>();
                lapsedOrders.addAll(orderSummaryReport.getClearedOrders());
                lapsedOrders = keepNew(true, lapsedOrders);

                orderSummaryReport = services.listClearedOrders(null, BetStatus.SETTLED, range, null);
                List<ClearedOrderSummary> settledOrders = new ArrayList<>();
                settledOrders.addAll(orderSummaryReport.getClearedOrders());
                settledOrders = keepNew(true, settledOrders);

                orderSummaryReport = services.listClearedOrders(null, BetStatus.CANCELLED, range, null);
                List<ClearedOrderSummary> cancelledOrders = new ArrayList<>();
                cancelledOrders.addAll(orderSummaryReport.getClearedOrders());
                cancelledOrders = keepNew(true, cancelledOrders);

                iAmAlive();
                //If not empty then merge might be needed
                List<ClearedOrderSummary> laspedOrdersTmp = new ArrayList<>();
                if (!lapsedOrders.isEmpty() && !settledOrders.isEmpty()) {
                    for (ClearedOrderSummary lapsedOrder : lapsedOrders) {
                        boolean found = false;
                        for (ClearedOrderSummary settledOrder : settledOrders) {
                            if (lapsedOrder.getBetId().equals(settledOrder.getBetId())) {
                                settledOrder.setSizeCancelled(lapsedOrder.getSizeCancelled());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            laspedOrdersTmp.add(lapsedOrder);
                        }
                    }
                    lapsedOrders = laspedOrdersTmp;
                }

                List<ClearedOrderSummary> cancelledOrdersTmp = new ArrayList<>();
                if (!cancelledOrders.isEmpty() && !settledOrders.isEmpty()) {
                    for (ClearedOrderSummary cancelledOrder : cancelledOrders) {
                        boolean found = false;
                        for (ClearedOrderSummary settledOrder : settledOrders) {
                            if (cancelledOrder.getBetId().equals(settledOrder.getBetId())) {
                                settledOrder.setSizeCancelled(cancelledOrder.getSizeCancelled());

                                /**
                                 * This is a bit of an ugly solution. The
                                 * itemdescription object is used to indicate
                                 * that this is a merged cancelled bet so that
                                 * it is possible to distinguish between merged
                                 * cancelled bet and merged lapsed bet since
                                 * there is no sizeLapsed field but only
                                 * sizeCnacelled.
                                 */
                                ItemDescription desc = new ItemDescription();
                                desc.setMarketDesc(MERGED_CANCELLED);
                                settledOrder.setItemDescription(desc);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            cancelledOrdersTmp.add(cancelledOrder);
                        }
                    }
                    cancelledOrders = cancelledOrdersTmp;
                }

                for (BetStatus status : betStatuses) {
                    List<ClearedOrderSummary> clearedOrders = new ArrayList<>();
                    if (status.equals(BetStatus.VOIDED)) {
                        orderSummaryReport = services.listClearedOrders(null, status, range, null);
                        clearedOrders.addAll(orderSummaryReport.getClearedOrders());
                        clearedOrders = keepNew(true, clearedOrders);//Only send bets that have not previously been successfully updated
                    } else if (status.equals(BetStatus.LAPSED)) {
                        clearedOrders = lapsedOrders;
                    } else if (status.equals(BetStatus.CANCELLED)) {
                        clearedOrders = cancelledOrders;
                    } else {
                        clearedOrders = settledOrders;
                    }

                    iAmAlive();
                    //Commission data   
                    if (status.equals(BetStatus.SETTLED) && !clearedOrders.isEmpty()) {
                        orderSummaryReport = services.listClearedOrders(null, BetStatus.SETTLED, range, MarketGroupBy.MARKET);
                        if (orderSummaryReport != null && !orderSummaryReport.getClearedOrders().isEmpty()) {
                            List<ClearedOrderSummary> commissions = keepNew(false, orderSummaryReport.getClearedOrders());
                            if (!commissions.isEmpty()) {
                                clearedOrders = updateBetsWithCommission(clearedOrders, commissions);
                                addToUpdated(false, commissions);
                            }
                        }
                    }

                    if (!clearedOrders.isEmpty()) {
                        List<String> errorBets;
                        List<Bet> clearedBetsInDB = factoryBet.updateClearedBets(clearedOrders, status, false);

                        //Some of the bets might not exist in DB
                        splitIntoExistingAndNonExistingOrders(clearedOrders, clearedBetsInDB);

                        if (!clearedOrders.isEmpty() || !clearedBetsInDB.isEmpty()) {
                            if (!clearedBetsInDB.isEmpty()) {

                                //Send everything to DB
                                Log.logMessage(CLASSNAME, METHOD, "Number of bets being updated for " + status.getStatus() + ": " + clearedBetsInDB.size(), LogLevelEnum.INFO, false);
                                errorBets = connBet.updateBets(clearedBetsInDB);
                                addToUpdated(true, existingOrders);

                                if (!errorBets.isEmpty()) {
                                    removeErrorBetsFromUpdated(errorBets);
                                }
                            }

                            //Some of the bets might not been found in the DB, these needs to be created in the DB
                            if (clearedOrders.size() != clearedBetsInDB.size()) {
                                List<Bet> clearedBetsNotInDB = null;
                                try {
                                    clearedBetsNotInDB = factoryBet.updateClearedBets(nonExistingOrders, status, true);
                                    Log.logMessage(CLASSNAME, METHOD, "Number of bets being created and updated: " + clearedBetsNotInDB.size(), LogLevelEnum.INFO, false);
                                } catch (DBConnectionException e) {
                                    //Will never be thrown since these won't be looked up in the DB
                                }

                                errorBets = connBet.upsertBets(clearedBetsNotInDB);
                                addToUpdated(true, nonExistingOrders);

                                if (!errorBets.isEmpty()) {
                                    removeErrorBetsFromUpdated(errorBets);
                                }
                            }
                            existingOrders.clear();
                            nonExistingOrders.clear();
                        }
                    }
                }

                //Funds in the account
                AccountFundsResponse accountFunds = services.getAccountFunds();
                AccountStatement accountStatement = factoryAccount.createAccountStatement(accountFunds.getAvailableToBetBalance());
                connAccount.insertAccountStatementItems(accountStatement);
                iAmAlive();
                try {
                    Log.logMessage(CLASSNAME, METHOD, "Bets updated, thread going to sleep", LogLevelEnum.INFO, true);
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                }

                if (!first) {
                    if (Utils.isNewDay(df, to)) {
                        to = Utils.getTomorrow(df);
                        clearUpdatedData();
                    }
                } else {
                    first = false;
                }
            } catch (ParseException | APINGException | DBConnectionException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
            }
        }
    }

    private void clearUpdatedData() {
        updatedBetsAndCommission.clear();
    }

    private List<ClearedOrderSummary> keepNew(boolean isBet, List<ClearedOrderSummary> clearedOrders) {
        List<ClearedOrderSummary> newOrders = new ArrayList<>();
        for (ClearedOrderSummary orderSummary : clearedOrders) {
            if (isBet) {
                if (!updatedBetsAndCommission.containsKey(orderSummary.getBetId())) {
                    newOrders.add(orderSummary);
                }
            } else {
                if (!updatedBetsAndCommission.containsKey(orderSummary.getMarketId())) {
                    newOrders.add(orderSummary);
                }
            }
        }
        return newOrders;
    }

    private void removeErrorBetsFromUpdated(List<String> errorBets) {
        for (String betID : errorBets) {
            updatedBetsAndCommission.remove(betID);
        }
    }

    private void addToUpdated(boolean isBet, List<ClearedOrderSummary> clearedOrders) {
        for (ClearedOrderSummary orderSummary : clearedOrders) {
            if (isBet) {
                if (orderSummary.getProfit() <= 0) {
                    updatedBetsAndCommission.put(orderSummary.getBetId(), "");
                }
            }
            //For the time being send the commission multiple times just to be safe
            //else {
            //    if (orderSummary.getCommission() > 0) {
            //        updatedBetsAndCommission.put(orderSummary.getMarketId(), "");
            //}
            //}
        }
    }

    private void splitIntoExistingAndNonExistingOrders(List<ClearedOrderSummary> clearedOrders, List<Bet> clearedBets) {
        for (ClearedOrderSummary order : clearedOrders) {
            boolean found = false;
            for (Bet bet : clearedBets) {
                if (order.getBetId().equals(bet.getExternalKey())) {
                    found = true;
                    existingOrders.add(order);
                }
            }
            if (!found) {
                nonExistingOrders.add(order);
            }
        }
    }

    private List<ClearedOrderSummary> updateBetsWithCommission(List<ClearedOrderSummary> clearedOrders, List<ClearedOrderSummary> commissions) {
        for (ClearedOrderSummary commission : commissions) {
            if (commission.getCommission() > 0) {
                ClearedOrderSummary betWithCommission = null;
                double biggestProfit = 0;

                for (ClearedOrderSummary bet : clearedOrders) {
                    if (bet.getMarketId().equals(commission.getMarketId())
                            && bet.getProfit() > biggestProfit) {
                        biggestProfit = bet.getProfit();
                        betWithCommission = bet;
                    }
                }

                if (betWithCommission != null) {
                    betWithCommission.setCommission(commission.getCommission());
                }
            }
        }
        return clearedOrders;
    }
}
