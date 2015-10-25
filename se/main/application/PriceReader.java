package se.main.application;

import com.betfair.aping.exceptions.APINGException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import se.betfair.api.BetfairServices;
import se.betfair.api.Common;
import se.betfair.enums.MarketStatus;
import se.betfair.factory.FactorySportsModel;
import se.betfair.model.MarketBook;
import se.betfair.model.MarketCatalogue;
import se.betfair.model.MarketFilter;
import se.betfair.model.PriceProjection;
import se.betfair.model.TimeRange;
import se.moneymaker.dict.Config;
import se.moneymaker.db.DBSportsModel;
import se.moneymaker.dict.BetOfferDict;
import se.moneymaker.enums.ConfigEnum;
import se.moneymaker.enums.DBErrorType;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Operation;
import se.moneymaker.enums.ReadReason;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Match;
import se.moneymaker.model.Outcome;
import se.moneymaker.util.Log;
import se.moneymaker.util.Utils;

public class PriceReader extends Application implements Runnable {

    private final static String CLASSNAME = PriceReader.class.getName();
    private static final long HEARTBEAT = 1800000; //30 minutes
    private final long TIME_SLEEP = 360000;
    private MarketFilter marketFilter;
    private BetfairServices services;
    private Set<String> marketProjection;
    private PriceProjection priceProjection;
    private DBSportsModel sportsModel;
    private Calendar from;
    private Calendar to;
    private TimeRange timeRange;
    private Set<String> marketTypeCodeMatchOdds;
    private Set<String> marketTypeCodeOverUnder1;
    private Set<String> marketTypeCodeOverUnder2;
    private Set<String> marketTypeCodeOverUnder3;
    private Set<String> marketTypeCodeOverUnder4;
    private Set<String> marketTypeCodeOverUnder5;
    private Set<String> marketTypeCodeOverUnder6;

    private Set<String> marketTypeCodeCorrectScore;
    private List<Set<String>> marketTypeCodes;
    private String sessionToken;
    private boolean runAsApplication;
    private long timeBeforeMatchMinutes;
    private Date today;
    private Date tomorrow;
    private SimpleDateFormat df;
    private FactorySportsModel factory;
    private String accountName;
    private HashMap<String, MarketCatalogue> marketCatalgouesMap;

    public PriceReader(String sessionToken, String accountName, boolean runAsApplication, ReadReason readReason, double minuteWeight) {
        this.accountName = accountName;
        this.runAsApplication = runAsApplication;
        this.sessionToken = sessionToken;
        initCommonAttributes();
        initMarketProjection();
        factory = new FactorySportsModel();
        factory.setReadReason(readReason);
        factory.setMinuteWeight(minuteWeight);
        df = new SimpleDateFormat("yyyyMMdd");
        try {
            today = new Date(Utils.parseStringToLongDate(df.format(new Date()), df));
        } catch (ParseException ex) {
        }
        if (runAsApplication) {
            marketFilter = createtMarketFilter();
            tomorrow = Utils.getTomorrow(df);
            initApplication(HEARTBEAT, CLASSNAME);
            Config config = Config.getInstance();
            String tmpTimeBeforeMatchMinutes = config.get(ConfigEnum.BF_TIMEBEFORE_MATCH);
            timeBeforeMatchMinutes = Long.parseLong(tmpTimeBeforeMatchMinutes);
            marketTypeCodes = new ArrayList<>();
            marketTypeCodeMatchOdds = new HashSet<>();
            marketTypeCodeMatchOdds.add(Common.MARKET_TYPE_MATCH_ODDS);
            marketTypeCodes.add(marketTypeCodeMatchOdds);
            marketTypeCodeOverUnder1 = new HashSet<>();
            marketTypeCodeOverUnder2 = new HashSet<>();
            marketTypeCodeOverUnder3 = new HashSet<>();
            marketTypeCodeOverUnder4 = new HashSet<>();
            marketTypeCodeOverUnder5 = new HashSet<>();
            marketTypeCodeOverUnder6 = new HashSet<>();

            marketTypeCodeOverUnder1.add(Common.MARKET_TYPE_OVER_UNDER_05);
            marketTypeCodeOverUnder2.add(Common.MARKET_TYPE_OVER_UNDER_15);
            marketTypeCodeOverUnder3.add(Common.MARKET_TYPE_OVER_UNDER_25);
            marketTypeCodeOverUnder4.add(Common.MARKET_TYPE_OVER_UNDER_35);
            marketTypeCodeOverUnder5.add(Common.MARKET_TYPE_OVER_UNDER_45);
            marketTypeCodeOverUnder6.add(Common.MARKET_TYPE_OVER_UNDER_55);
            //marketTypeCodeOverUnder4.add(Common.MARKET_TYPE_OVER_UNDER_65);
            //marketTypeCodeOverUnder5.add(Common.MARKET_TYPE_OVER_UNDER_75);
            //marketTypeCodeOverUnder6.add(Common.MARKET_TYPE_OVER_UNDER_85);
            marketTypeCodes.add(marketTypeCodeOverUnder1);
            marketTypeCodes.add(marketTypeCodeOverUnder2);
            marketTypeCodes.add(marketTypeCodeOverUnder3);
            marketTypeCodes.add(marketTypeCodeOverUnder4);
            marketTypeCodes.add(marketTypeCodeOverUnder5);
            marketTypeCodes.add(marketTypeCodeOverUnder6);
            marketTypeCodeCorrectScore = new HashSet<>();
            marketTypeCodeCorrectScore.add(Common.MARKET_TYPE_CORRECT_SCORE);
            //marketTypeCodes.add(marketTypeCodeCorrectScore);
        } else {
            marketCatalgouesMap = new HashMap<>();
        }
    }

    @Override
    public void run() {
        final String METHOD = "run";
        Log.logMessage(CLASSNAME, METHOD, "PriceReader running...", LogLevelEnum.INFO, false);
        while (runAsApplication) {
            try {
                if (Utils.isNewDay(df, tomorrow)) {
                    sportsModel.reset();
                    tomorrow = Utils.getTomorrow(df);
                }
                List<MarketCatalogue> marketCatalogues = null;
                updateTimeRange();

                for (Set<String> marketTypeCode : marketTypeCodes) {
                    marketFilter.setMarketTypeCodes(marketTypeCode);

                    List<MarketCatalogue> tmpMarketCatalogues = services.listMarketCatalogue(marketFilter, marketProjection, null);
                    if (tmpMarketCatalogues != null) {
                        if (marketCatalogues == null) {
                            marketCatalogues = new ArrayList<>();
                        }
                        marketCatalogues.addAll(tmpMarketCatalogues);
                    }
                }
                iAmAlive();
                try {
                    processMarketCatalogues(marketCatalogues, null);
                } catch (DBConnectionException | BetOfferException e) {
                }
                try {
                    Log.logMessage(CLASSNAME, METHOD, "Thread going to sleep for: " + TimeUnit.MILLISECONDS.toMinutes(TIME_SLEEP) + " minutes", LogLevelEnum.INFO, false);
                    Thread.sleep(TIME_SLEEP);
                } catch (InterruptedException e) {
                }
            } catch (APINGException e) {
                Log.logMessage(CLASSNAME, METHOD, e.toString(), LogLevelEnum.ERROR, false);
            }
        }
    }

    public void stop() {
        if (runAsApplication) {
            stopApp();
            runAsApplication = false;
        }
    }

    //Each betting process will handle its own marketid, ie same marketid will never be in two parallell processes
    public void readSendPrice(List<String> marketIds, String pSessionToken) throws APINGException, DBConnectionException, BetOfferException {
        final String METHOD = "readSendPrice";
        synchronized (this) {
            if (Utils.isNewDay2(df, today)) {
                BetOfferDict.clearOldEntries();
                marketCatalgouesMap.clear();
                try {
                    today = new Date(Utils.parseStringToLongDate(df.format(new Date()), df));
                } catch (ParseException e) {
                }
            }
        }

        Set<String> marketIdsNew = new HashSet<>();
        List<MarketCatalogue> existingMarketCatalogues = new ArrayList<>();
        for (String marketId : marketIds) {
            MarketCatalogue tmpCatalogue = marketCatalgouesMap.get(marketId);
            if (tmpCatalogue == null) {
                marketIdsNew.add(marketId);
            } else {
                existingMarketCatalogues.add(tmpCatalogue);
            }
        }

        if (!marketIdsNew.isEmpty() && !existingMarketCatalogues.isEmpty()) {

            //In order to support multiple markets in one request both new and existing must be assumed to exist and added to same list when processing market catalogues
            Log.logMessage(CLASSNAME, METHOD, "Bet intelligence has sent mix of existing and non existing markets in same call. Not possible in current solution.", LogLevelEnum.CRITICAL, runAsApplication);
        }

        //These are all new markets, existing ones will never read market catalogue
        List<MarketCatalogue> marketCatalogues;
        if (!marketIdsNew.isEmpty()) {
            MarketFilter tmpMarketFilter = createtMarketFilter();
            tmpMarketFilter.setMarketIds(marketIdsNew);
            marketCatalogues = services.listMarketCatalogue(tmpMarketFilter, marketProjection, pSessionToken);
            if (marketCatalogues == null) {
                marketCatalogues = new ArrayList<>();
            }
            marketCatalogues.addAll(existingMarketCatalogues);
            for (MarketCatalogue tmpCatalgoue : marketCatalogues) {
                marketCatalgouesMap.put(tmpCatalgoue.getMarketId(), tmpCatalgoue);
            }
        } else {
            marketCatalogues = new ArrayList<>();
            marketCatalogues.addAll(existingMarketCatalogues);
        }
        processMarketCatalogues(marketCatalogues, pSessionToken);
    }

    private void processMarketCatalogues(List<MarketCatalogue> marketCatalogues, String pSessionToken) throws APINGException, DBConnectionException, BetOfferException {
        final String METHOD = "processMarketCatalogues";
        DBConnectionException ex = null;
        List<MarketCatalogue> marketCataloguesInsert = new ArrayList<>();
        List<MarketCatalogue> marketCataloguesUpdate = new ArrayList<>();

        if (marketCatalogues != null && !marketCatalogues.isEmpty()) {
            Log.logMessage(CLASSNAME, METHOD, "Number of markets found: " + marketCatalogues.size(), LogLevelEnum.INFO, false);
            List<String> marketIds = getMarketIds(marketCatalogues);

            List<MarketBook> marketBooks;
            if (pSessionToken != null) {
                marketBooks = services.listMarketBook(marketIds, priceProjection, pSessionToken);
                if (containsSuspendedMarkets(marketBooks)) {
                    throw new BetOfferException("Market suspended", ErrorType.MARKET_SUSPENDED);
                }
            } else {
                marketBooks = services.listMarketBook(marketIds, priceProjection, sessionToken);
                int before = marketBooks.size();
                marketBooks = keepOpenMarkets(marketBooks);
                int after = marketBooks.size();
                int notOpen = before - after;
                Log.logMessage(CLASSNAME, METHOD, "Number of betoffers excluded due to market not being OPEN: " + notOpen, LogLevelEnum.INFO, runAsApplication);
            }

            if (marketBooks != null) {
                marketCatalogues = join(marketCatalogues, marketBooks);

                for (MarketCatalogue catalogue : marketCatalogues) {
                    if (BetOfferDict.containsBetOffer((catalogue.getMarketId()))) {
                        marketCataloguesUpdate.add(catalogue);
                    } else {
                        marketCataloguesInsert.add(catalogue);
                    }
                }

                List<Match> matchesForInsert = new ArrayList<>();
                if (!marketCataloguesInsert.isEmpty()) {
                    matchesForInsert = factory.createMatches(Operation.INSERT, marketCataloguesInsert);
                    if (runAsApplication) {
                        matchesForInsert = clearPricesOnEarlyMarkets(matchesForInsert);
                    }
                }

                List<Match> matchesForUpdate = new ArrayList<>();
                if (!marketCataloguesUpdate.isEmpty()) {
                    matchesForUpdate = factory.createMatches(Operation.UPDATE, marketCataloguesUpdate);
                    if (runAsApplication) {
                        matchesForUpdate = clearPricesOnEarlyMarkets(matchesForUpdate);
                    }
                }
                int totalNumberOfBetoffers = matchesForInsert.size() + matchesForUpdate.size();
                Log.logMessage(CLASSNAME, METHOD, "Number of betoffers being sent: " + totalNumberOfBetoffers + " (Existing: " + matchesForUpdate.size() + "/" + totalNumberOfBetoffers + ")", LogLevelEnum.INFO, false);

                try {
                    sendMatches(matchesForInsert, Operation.INSERT);
                } catch (DBConnectionException e) {
                    ex = e;
                }
                try {
                    sendMatches(matchesForUpdate, Operation.UPDATE);
                } catch (DBConnectionException e) {
                    ex = e;
                }
                if (runAsApplication) {
                    BetOfferDict.saveNewDataClearOldData();
                }
                if (ex != null) {
                    throw ex;
                }
            } else {
                Log.logMessage(CLASSNAME, METHOD, "No prices returned", LogLevelEnum.WARNING, false);
            }
        } else {
            Log.logMessage(CLASSNAME, METHOD, "No markets returned", LogLevelEnum.WARNING, false);
        }
    }

    private List<String> getMarketIds(List<MarketCatalogue> marketCatalogues) {
        List<String> ids = new ArrayList<>(marketCatalogues.size());
        for (MarketCatalogue marketCatalogue : marketCatalogues) {
            ids.add(marketCatalogue.getMarketId());
        }
        return ids;
    }

    private void initCommonAttributes() {
        services = new BetfairServices(sessionToken, accountName);
        if (runAsApplication) {
            sportsModel = new DBSportsModel(Source.BETFAIR);
        } else {
            sportsModel = new DBSportsModel();
        }
        priceProjection = Common.getPricePojectionSettings();
    }

    private MarketFilter createtMarketFilter() {
        MarketFilter tmpMarketFilter = new MarketFilter();
        Set<String> eventTypeId = new HashSet<>();
        eventTypeId.add(Common.EVENT_TYPE_SOCCER);
        Set<String> marketBettingTypes = new HashSet<>();
        marketBettingTypes.add(Common.MARKET_BETTING_TYPE_ODDS);
        tmpMarketFilter.setMarketBettingTypes(marketBettingTypes);
        tmpMarketFilter.setEventTypeIds(eventTypeId);
        return tmpMarketFilter;
    }

    private void initMarketProjection() {
        marketProjection = new HashSet<>();
        marketProjection.add(Common.MARKET_PROJECTION_EVENT);
        marketProjection.add(Common.MARKET_PROJECTION_COMPETITION);
        //marketProjection.add(ApiCommon.MARKET_PROJECTION_MARKET_DESCRIPTION);
        marketProjection.add(Common.MARKET_PROJECTION_RUNNER_DESCRIPTION);
        marketProjection.add(Common.MARKET_PROJECTION_EVENT_TYPE);
        marketProjection.add(Common.MARKET_PROJECTION_MARKET_START_TIME);

    }

    private void updateTimeRange() {
        timeRange = new TimeRange();
        from = Calendar.getInstance();//yesterdays date in case any games are played over midnight
        from.add(Calendar.DATE, -1);
        timeRange.setFrom(from.getTime());
        to = Calendar.getInstance();
        to.add(Calendar.DATE, 1);
        timeRange.setTo(to.getTime());
        marketFilter.setMarketStartTime(timeRange);
    }

    private List<MarketCatalogue> join(List<MarketCatalogue> marketCatalogues, List<MarketBook> marketBooks) {
        List<MarketCatalogue> tmpMarketCatalogues = new ArrayList<>();
        HashMap<String, MarketBook> marketBooksMap = new HashMap<>(marketBooks.size());
        for (MarketBook marketBook : marketBooks) {
            marketBooksMap.put(marketBook.getMarketId(), marketBook);
        }

        for (MarketCatalogue marketCatalogue : marketCatalogues) {
            MarketBook bookTmp = marketBooksMap.get(marketCatalogue.getMarketId());
            if (bookTmp != null) {
                marketCatalogue.setMarketBook(bookTmp);
                tmpMarketCatalogues.add(marketCatalogue);
            }
        }
        return tmpMarketCatalogues;
    }

    private void sendMatches(List<Match> matches, Operation operation) throws DBConnectionException {
        final String METHOD = "sendMatches";
        boolean error = false;
        int numberOfMatchesNotFound = 0;

        if (!matches.isEmpty()) {
            Map<String, Long> pkMap = null;
            if (operation.equals(Operation.INSERT)) {
                pkMap = new HashMap<>();
            }

            //Send the matches in one by one since we don't want to abort if one fails
            for (Match match : matches) {
                try {
                    if (operation.equals(Operation.INSERT)) {
                        Long pk = pkMap.get(match.getExternalKey());
                        if (pk != null) {
                            match.setPk(pk);
                        }
                        match = sportsModel.insertSportsModel(match, false);
                        if (match.getPk() >= 1) {//This should always be the case after inserting a match
                            pkMap.put(match.getExternalKey(), match.getPk());
                        }
                    } else if (operation.equals(Operation.UPDATE)) {
                        sportsModel.updateSportsModel(match);
                    }
                    if (runAsApplication) {
                        BetOfferDict.update(match);
                    } else {
                        BetOfferDict.save(match);
                    }
                } catch (DBConnectionException e) {
                    if (e.getErrorType().equals(DBErrorType.MATCH_NOT_FOUND.toString())) {
                        numberOfMatchesNotFound++;
                    } else {
                        error = true;
                        Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                    }
                }
            }
            if (numberOfMatchesNotFound > 0) {
                Log.logMessage(CLASSNAME, METHOD, "Number of matches not found for case " + operation + ": " + numberOfMatchesNotFound + "/" + matches.size(), LogLevelEnum.INFO, false);
            }
            if (error) {
                throw new DBConnectionException("Error sending data to DB");
            }
        }
    }

    private List<Match> clearPricesOnEarlyMarkets(List<Match> matches) {
        List<Match> result = new ArrayList<>();
        Date currentTime = new Date();
        for (Match match : matches) {
            Date marketStartTime = match.getEventDate();
            long diff = marketStartTime.getTime() - currentTime.getTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            if (minutes > timeBeforeMatchMinutes) {
                List<BetOffer> betOffers = match.getBetOffers();
                for (BetOffer betOffer : betOffers) {
                    List<Outcome> outcomes = betOffer.getOutcomes();
                    for (Outcome outcome : outcomes) {
                        outcome.clearPrices();
                    }
                }
            }
            result.add(match);
        }
        return result;
    }

    private boolean containsSuspendedMarkets(List<MarketBook> marketBooks) {
        final String METHOD = "containsSuspendedMarkets";
        for (MarketBook marketBook : marketBooks) {
            String status = marketBook.getStatus();
            if (!status.equals(MarketStatus.OPEN.name())) {
                if (!status.equals(MarketStatus.SUSPENDED.name())) {
                    Log.logMessage(CLASSNAME, METHOD, "Unexpected market status: " + status, LogLevelEnum.WARNING, true);
                }
                return true;
            }
        }
        return false;
    }

    private List<MarketBook> keepOpenMarkets(List<MarketBook> marketBooks) {
        List<MarketBook> tmpMarketBooks = new ArrayList<>();
        for (MarketBook marketBook : marketBooks) {
            String status = marketBook.getStatus();
            if (status.equals(MarketStatus.OPEN.name())) {
                tmpMarketBooks.add(marketBook);
            }
        }
        return tmpMarketBooks;
    }
}
