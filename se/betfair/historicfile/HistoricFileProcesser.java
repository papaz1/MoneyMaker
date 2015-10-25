package se.betfair.historicfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import se.betfair.config.BetfairCondition;
import se.betfair.config.BCMarketEnum;
import se.betfair.exception.BEXNoSession;
import se.betfair.model.HistoricMarket;
import se.betfair.model.Market;
import se.betfair.model.MarketHolder;
import se.moneymaker.dict.Config;
import se.moneymaker.enums.ConfigEnum;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Match;
import se.moneymaker.db.DBSportsModel;
import se.moneymaker.util.Utils;
import se.moneymaker.util.Constants;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;
import se.betfair.enums.EventTypeEnum;
import se.moneymaker.db.DBServices;
import se.moneymaker.enums.DBErrorType;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.ErrorType;

public class HistoricFileProcesser {

    private static final String CLASSNAME = HistoricFileProcesser.class.getName();
    private final File historicFile;
    private Map<Integer, HistoricMarket> historicMarkets;
    private final StringBuilder erroneousRecords;
    private int marketIndex;
    private int matchInfoIndex;
    private List<Market> markets;
    private List<String> matchInfos;
    private final HistoricMarketModeEnum mode;
    private final File matchinfoFile;
    private final File matchinfoProcessed;
    private static final String MATCH_INFO_PROCESSED_FILENAME = "PROCESSED_MATCHINFOS.mm";
    private HashMap<String, String> matchInfosToProcess;
    private DBServices services;
    private Map<Integer, Match> matchMap;
    private Config config;
    private int numberOfProcesses;
    private DBSportsModel matchInfoCalls;
    private int matchInfoCounter;
    private String latestAddedMatchInfo;
    private int processCounter;

    public HistoricFileProcesser(File historicFile, File matchinfoFile, HistoricMarketModeEnum mode) {
        this.mode = mode;
        this.historicFile = historicFile;
        this.matchinfoFile = matchinfoFile;
        matchinfoProcessed = new File(matchinfoFile.getParent() + System.getProperty("file.separator") + MATCH_INFO_PROCESSED_FILENAME);
        erroneousRecords = new StringBuilder();
        erroneousRecords.append("HEADER").append(System.getProperty("line.separator"));
        services = new DBServices(true);
        config = Config.getInstance();
    }

    public void processHistoricData() {
        final String METHOD = "processHistoricData";
        matchInfos = new ArrayList<>();
        if (mode.equals(HistoricMarketModeEnum.MATCHINFO)) {
            matchInfoIndex = 0;
            int numberOfMatchInfoProcesses = Integer.parseInt(config.get(ConfigEnum.BF_NUMBER_OF_PROCESSES_MATCHINFO));
            if (numberOfMatchInfoProcesses <= 0) {
                numberOfMatchInfoProcesses = 1;
            }
            readMatchInfoFile();
            Log.logMessage(CLASSNAME, METHOD, "Number of expand prices calls to make: " + matchInfos.size() + ". Starting matchInfo process", LogLevelEnum.INFO, false);
            updateMatchInfo(numberOfMatchInfoProcesses);
            Log.logMessage(CLASSNAME, METHOD, "Processing match infos completed", LogLevelEnum.INFO, false);
        } else if (mode.equals(HistoricMarketModeEnum.PRICE)) {
            marketIndex = 0;

            numberOfProcesses = Integer.parseInt(config.get(ConfigEnum.BF_NUMBER_OF_PROCESSES));

            if (numberOfProcesses <= 0) {
                numberOfProcesses = 1;
            }

            int numberOfErroneousMarkets = 0;

            List<Match> erroneousRows = new ArrayList<>();
            BetfairCondition condition = new BetfairCondition();
            condition.setEventType(EventTypeEnum.SOCCER);
            condition.addMarketNameFilterInclude(BCMarketEnum.MATCH_ODDS);
            condition.addMarketNameFilterInclude(BCMarketEnum.OVER_UNDER);
            //condition.addMarketNameFilterInclude(BCMarketEnum.CORRECT_SCORE);

            condition.addMenuPathFilterInclude(BCMarketEnum.FIXTURES);
            condition.addMenuPathFilterInclude(BCMarketEnum.MATCHES);
            condition.setTotalAmountMatched(0);
            condition.setEventDateFrom(Utils.stringToDate(Constants.DEFAULT_DATE));

            HistoricDataReader historicData;
            historicData = new HistoricDataReader(historicFile, condition);

            historicMarkets = historicData.getHistoricMarkets();
            MarketHolder marketHolder = HistoricFactoryAllMarkets.parseHistoricMarkets(historicMarkets, condition);
            markets = marketHolder.getSuccessfullyCreatedMarkets();
            List<HistoricMarket> erroneousHistoricMarkets = marketHolder.getErroneousMarkets();
            for (HistoricMarket historicMarket : erroneousHistoricMarkets) {
                addErroneousRecord(historicMarket.getUnparsedMarketString());
                numberOfErroneousMarkets++;
            }

            Log.logMessage(CLASSNAME, METHOD, "Markets found in file: " + historicMarkets.size() + " Markets left after filter: " + markets.size() + ". Markets not included due to error when parsing: " + numberOfErroneousMarkets, LogLevelEnum.INFO, false);
            Log.logMessage(CLASSNAME, METHOD, "Processing file: " + historicFile.getName(), LogLevelEnum.INFO, false);

            //One market might exist in two different exchanges with different market ids. Keep the one with the biggest volume matched on Betoffer.
            HistoricFactoryMatch factory = new HistoricFactoryMatch();
            matchMap = new HashMap<>();
            LinkedHashMap<Integer, Market> marketMap = new LinkedHashMap<>();
            for (Market market : markets) {
                List<Market> marketsTmp = new ArrayList<>(1);
                marketsTmp.add(market);
                try {
                    List<Match> matches = factory.createMatches(marketsTmp, null);
                    Match match = matches.get(0);
                    List<BetOffer> betOffers = match.getBetOffers();
                    BetOffer betOffer = betOffers.get(0);

                    String hashString = match.getHome() + match.getAway() + match.getEventDate() + betOffer.getName();
                    int hashCode = hashString.toUpperCase().hashCode();

                    Match existingMatch = matchMap.get(hashCode);

                    //If the betoffer already existed then it was a betoffer on two exchanges. Keep the one with biggest volume matched.
                    if (existingMatch == null) {
                        matchMap.put(hashCode, match);
                        marketMap.put(hashCode, market);
                    } else {
                        double volumeMatched = betOffer.getVolumeMatched();
                        List<BetOffer> existingBetOffers = existingMatch.getBetOffers();
                        BetOffer existingBetOffer = existingBetOffers.get(0);
                        double existingVolumeMatched = existingBetOffer.getVolumeMatched();
                        if (volumeMatched > existingVolumeMatched) {
                            matchMap.remove(hashCode);
                            marketMap.remove(hashCode);
                            matchMap.put(hashCode, match);
                            marketMap.put(hashCode, market);
                        }
                    }
                } catch (BetOfferException e) {
                    if (e != null && !e.getErrorType().equals(ErrorType.FILTER)) {
                        addErroneousMarket(market);
                        numberOfErroneousMarkets++;
                        Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                    }
                } catch (BEXNoSession ex) {
                }
            }

            //marketMap contains the final list of markets that should be used by the this method further on
            markets.clear();
            markets.addAll(marketMap.values());

            String previousMatchPk = null;
            int numberOfInsertedMarkets = 0;
            processCounter = 0;
            MatchExpandedModel previousMatch = null;
            matchInfosToProcess = new HashMap();
            boolean marketsExist = true;
            HashMap<Integer, Long> pkMap = new HashMap<>();
            Set<Integer> notFound = new HashSet<>();
            while (marketsExist) {

                //Get markets corresponding to the number of processes that is going to be created for parsing
                List<Market> marketsSubList = getMarketsSubList(numberOfProcesses);
                if (marketsSubList != null) {
                    Thread[] parserThreads = new Thread[marketsSubList.size()];
                    ParserProcess[] parserProcesses = new ParserProcess[marketsSubList.size()];
                    int index = 0;

                    //For each of the markets a thread will be created responsible for the parsing
                    for (Market market : marketsSubList) {
                        parserProcesses[index] = new ParserProcess(market);
                        parserThreads[index] = new Thread(parserProcesses[index]);
                        parserThreads[index].start();
                        index++;
                    }

                    for (Thread parserThread : parserThreads) {
                        try {
                            parserThread.join();
                        } catch (InterruptedException e) {
                            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                        }
                    }

                    List<MatchMarketMap> matchMarketMapList = new ArrayList<>();
                    for (ParserProcess parserProcess : parserProcesses) {
                        processCounter++;
                        Match match = parserProcess.getMatch();
                        if (match != null) {
                            long pk;
                            String hashString = match.getHome() + match.getAway() + match.getEventDate();
                            int hashCode = hashString.toUpperCase().hashCode();
                            try {
                                if (!notFound.contains(hashCode)) {
                                    Long existingPk = pkMap.get(hashCode);
                                    if (existingPk != null) {
                                        match.setPk(existingPk);
                                    } else {
                                        pk = services.readMatchPk(match);
                                        pkMap.put(hashCode, pk);
                                    }
                                    matchMarketMapList.add(new MatchMarketMap(parserProcess.getMatch(), parserProcess.getMarket()));
                                }
                            } catch (DBConnectionException e) {
                                if (e.getErrorType().equals(DBErrorType.MATCH_NOT_FOUND.toString())) {
                                    if (!notFound.contains(hashCode)) {
                                        notFound.add(hashCode);
                                    }
                                }
                            }
                        }
                    }

                    index = 0;
                    Thread[] matchesToSendThreads = new Thread[matchMarketMapList.size()];
                    MMSendProcess[] sendProcesses = new MMSendProcess[matchMarketMapList.size()];

                    //For each of the matches a thread will be created responsible for sending the match to Betprover
                    for (MatchMarketMap matchMarketMap : matchMarketMapList) {
                        sendProcesses[index] = new MMSendProcess(matchMarketMap);
                        matchesToSendThreads[index] = new Thread(sendProcesses[index]);
                        matchesToSendThreads[index].start();
                        index++;
                    }

                    for (Thread matchesToSendThread : matchesToSendThreads) {
                        try {
                            matchesToSendThread.join();
                        } catch (InterruptedException e) {
                            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                        }
                    }

                    //Now that every thread is done collect the result and proceeed
                    List<MatchMarketMap> result = new ArrayList<>();
                    for (MMSendProcess sendProcess : sendProcesses) {
                        if (sendProcess.geResultMatchMarketMap() != null) {
                            result.add(sendProcess.geResultMatchMarketMap());
                            numberOfInsertedMarkets++;
                        } else {
                            numberOfErroneousMarkets++;
                            erroneousRows.add(sendProcess.geOriginaltMatchMarketMap().getMatch());
                        }
                    }

                    int resultCounter = 0;
                    for (MatchMarketMap matchMarketMap : result) {
                        resultCounter++;
                        Match insertedMatch = matchMarketMap.getMatch();
                        Market market = matchMarketMap.getMarket();

                        //For the historic matches after all markets for a match been sent
                        //a call will be made marking the end of match which will "expand prices"
                        if (previousMatch != null) {
                            if (!market.getHierarchyTextPathNoFix2().equalsIgnoreCase(previousMatch.getHierarchy())
                                    && !previousMatch.isExpanded()) {

                                //The current market belongs to a new match so end the 
                                //previous match by expanding prices
                                previousMatch.setExpanded();
                                addMatchInfo(previousMatchPk);
                            }
                        }

                        previousMatchPk = Long.toString(insertedMatch.getPk());

                        //Only one element exists or this is the last element
                        if (result.size() == 1 || result.size() == resultCounter) {
                            addMatchInfo(previousMatchPk);
                        } else {

                            //Only create new matches if this is the first run or the market belongs to a new match                        
                            if (previousMatch == null) {
                                previousMatch = new MatchExpandedModel(market.getHierarchyTextPathNoFix2());
                            } else if (!previousMatch.getHierarchy().equalsIgnoreCase(market.getHierarchyTextPathNoFix2())) {
                                previousMatch = new MatchExpandedModel(market.getHierarchyTextPathNoFix2());
                            }
                        }
                    }

                    //Only one market exists or this is the last market in the file
                    if (markets.size() == 1 || markets.size() == processCounter) {
                        addMatchInfo(previousMatchPk);
                    }
                } else {
                    marketsExist = false;
                }
            }
            Log.logMessage(CLASSNAME, METHOD, "Done processing. Markets found in file: " + historicMarkets.size() + ". Markets left after filter: " + markets.size() + ". Inserted markets: " + numberOfInsertedMarkets + ". Matches found in DB: " + pkMap.size() + ". Matchinfo: " + matchInfosToProcess.size() + ". Erroneous markets: " + numberOfErroneousMarkets + ".", LogLevelEnum.INFO, false);
            addErroneousMatches(erroneousRows);
            writeErroneousRecordsToFile();
            writeMatchinfoToFile();
        }
    }

    private void writeMatchinfoToFile() {
        matchInfos.addAll(matchInfosToProcess.keySet());
        Utils.writeToFile(matchInfos, matchinfoFile);
    }

    private void readMatchInfoFile() {
        final String METHOD = "readMatchInfoFile";
        Log.logMessage(CLASSNAME, METHOD, "Reading file", LogLevelEnum.INFO, false);
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(matchinfoFile));
            String line;
            HashMap m = new HashMap();
            try {
                BufferedReader reader2 = new BufferedReader(new FileReader(matchinfoProcessed));
                while ((line = reader2.readLine()) != null) {
                    m.put(line, line);
                }
            } catch (IOException e2) {
                Log.logMessage(CLASSNAME, METHOD, "This message can be ignored if the program is being run for the first time: " + e2.getMessage(), LogLevelEnum.WARNING, false);
            }

            while ((line = reader.readLine()) != null) {
                if (!m.containsKey((line))) {
                    matchInfos.add(line);
                }
            }
            Log.logMessage(CLASSNAME, METHOD, "Randomizing records", LogLevelEnum.INFO, false);
            Collections.shuffle(matchInfos); //To avoid collisions and locks when making calls to Betprover
        } catch (IOException e1) {
            Log.logMessage(CLASSNAME, METHOD, e1.getMessage(), LogLevelEnum.INFO, false);
        }
    }

    private void addErroneousMatches(List<Match> matches) {
        for (Match match : matches) {
            List<BetOffer> betOffers = match.getBetOffers();
            for (BetOffer betOffer : betOffers) {
                Integer marketId = Integer.parseInt(betOffer.getExternalKey());
                HistoricMarket historicMarket = historicMarkets.get(marketId);
                if (historicMarket != null) {
                    addErroneousRecord(historicMarket.getUnparsedMarketString());
                }
            }
        }
    }

    private void addErroneousMarket(Market market) {
        HistoricMarket historicMarket = historicMarkets.get(market.getId());
        addErroneousRecord(historicMarket.getUnparsedMarketString());
    }

    private void addErroneousRecord(String record) {
        erroneousRecords.append(record).append(System.getProperty("line.separator"));
    }

    private void writeErroneousRecordsToFile() {
        final int DEFAULT_LENGTH = 8;
        if (erroneousRecords.length() > DEFAULT_LENGTH) {
            HistoricFileSystem.writeErrorsToFile(erroneousRecords, "INSERT_ERROR_" + historicFile.getName());
        }
    }

    private void addMatchInfo(String previousmmDbId) {
        final String METHOD = "addMatchInfo";
        if (previousmmDbId != null) {
            if (!matchInfosToProcess.containsKey(previousmmDbId)) {
                if (numberOfProcesses == 1) {
                    if (markets.size() == processCounter) {
                        try {
                            matchInfoCounter++;
                            Log.logMessage(CLASSNAME, METHOD, "Processing match info number: " + matchInfoCounter + " Pk: " + latestAddedMatchInfo, LogLevelEnum.INFO, true);
                            matchInfoCalls.matchInfo(previousmmDbId);
                        } catch (DBConnectionException e) {
                            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, true);
                        }
                    } else if (latestAddedMatchInfo != null && !latestAddedMatchInfo.equals(previousmmDbId)) {
                        if (matchInfoCalls == null) {
                            matchInfoCalls = new DBSportsModel(Source.BETFAIR);
                        }
                        try {
                            matchInfoCounter++;
                            Log.logMessage(CLASSNAME, METHOD, "Processing match info number: " + matchInfoCounter + " Pk: " + latestAddedMatchInfo, LogLevelEnum.INFO, true);
                            matchInfoCalls.matchInfo(latestAddedMatchInfo);
                        } catch (DBConnectionException e) {
                            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, true);
                        }
                    }
                }
                latestAddedMatchInfo = previousmmDbId;
                matchInfosToProcess.put(previousmmDbId, "1"); //The value "1" does not matter, it will not be used
            }
        }
    }

    public void updateMatchInfo(int numberOfMatchInfoProcesses) {
        final String METHOD = "matchInfo";
        int totalNumberOfMatchInfos = matchInfos.size();
        int currentNumberOfMatchInfos = 0;
        boolean idExists = true;
        while (idExists) {
            List<String> matchInfoSubList = getMatchInfoSubList(numberOfMatchInfoProcesses);
            if (matchInfoSubList != null) {
                Thread[] connThreads = new Thread[matchInfoSubList.size()];
                MatchInfoProcess[] matchInfoProcesses = new MatchInfoProcess[matchInfoSubList.size()];
                int index = 0;
                currentNumberOfMatchInfos = currentNumberOfMatchInfos + matchInfoSubList.size();
                Log.logMessage(CLASSNAME, METHOD, "Processing " + matchInfoSubList.size() + " matchInfos, total: " + currentNumberOfMatchInfos + "/" + totalNumberOfMatchInfos, LogLevelEnum.INFO, false);
                for (String mmDbId : matchInfoSubList) {
                    matchInfoProcesses[index] = new MatchInfoProcess(mmDbId);
                    connThreads[index] = new Thread(matchInfoProcesses[index]);
                    connThreads[index].start();
                    index++;
                }

                for (Thread connThread : connThreads) {
                    try {
                        connThread.join();
                    } catch (InterruptedException e) {
                        Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                    }
                }
                //TODO FELHANTERING FÖR DE SOM INTE GÅR IN FINNS INTE. ALLT ANSES VARA KLART
                Utils.writeToFile(matchInfoSubList, matchinfoProcessed);
            } else {
                idExists = false;
            }
        }
    }

    private List<String> getMatchInfoSubList(int numberOfMatchInfoProcesses) {
        List<String> matchInfoSubList = null;
        if (matchInfoIndex < matchInfos.size()) {
            if (matchInfoIndex + numberOfMatchInfoProcesses >= matchInfos.size()) {
                matchInfoSubList = matchInfos.subList(matchInfoIndex, matchInfos.size());
                matchInfoIndex = matchInfos.size();
            } else {
                matchInfoSubList = matchInfos.subList(matchInfoIndex, matchInfoIndex + numberOfMatchInfoProcesses);
                matchInfoIndex += numberOfMatchInfoProcesses;
            }
        }
        return matchInfoSubList;
    }

    private List<Market> getMarketsSubList(int numberOfProcesses) {
        List<Market> marketsSubList = null;
        if (marketIndex < markets.size()) {
            if (marketIndex + numberOfProcesses >= markets.size()) {
                marketsSubList = markets.subList(marketIndex, markets.size());
                marketIndex = markets.size();
            } else {
                marketsSubList = markets.subList(marketIndex, marketIndex + numberOfProcesses);
                marketIndex += numberOfProcesses;
            }
        }
        return marketsSubList;
    }

    private class MatchExpandedModel {

        private final String hierarchy;
        private boolean isExpanded;

        public MatchExpandedModel(String hierarchy) {
            this.hierarchy = hierarchy;
        }

        public String getHierarchy() {
            return hierarchy;
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        private void setExpanded() {
            isExpanded = true;
        }
    }

    private class MatchMarketMap {

        private final Match match;
        private final Market market;

        public MatchMarketMap(Match match, Market market) {
            this.match = match;
            this.market = market;
        }

        public Match getMatch() {
            return match;
        }

        public Market getMarket() {
            return market;
        }
    }

    private class MMSendProcess implements Runnable {

        private final DBSportsModel connSportsModel;
        private final MatchMarketMap matchMarketMap;
        private MatchMarketMap matchMarketMapResult;
        private Match match;
        private DBConnectionException e;

        public MMSendProcess(MatchMarketMap matchMarketMap) {
            connSportsModel = new DBSportsModel();
            this.matchMarketMap = matchMarketMap;
            match = matchMarketMap.getMatch();
        }

        @Override
        public void run() {
            final String METHOD = "run";
            try {
                match = connSportsModel.insertSportsModel(match, true);
                matchMarketMapResult = new MatchMarketMap(match, matchMarketMap.getMarket());
            } catch (DBConnectionException e1) {
                this.e = e1;
                if (!e1.getErrorType().equalsIgnoreCase(DBErrorType.MATCH_NOT_FOUND.toString())) {
                    Log.logMessage(CLASSNAME, METHOD, e1.getMessage(), LogLevelEnum.ERROR, false);
                }
            }
        }

        public MatchMarketMap geOriginaltMatchMarketMap() {
            return matchMarketMap;
        }

        public MatchMarketMap geResultMatchMarketMap() {
            return matchMarketMapResult;
        }

        public DBConnectionException getException() {
            return e;
        }
    }

    private class MatchInfoProcess implements Runnable {

        private final DBSportsModel connSportsModel;
        private final String mmDbId;

        public MatchInfoProcess(String mmDbId) {
            connSportsModel = new DBSportsModel(Source.BETFAIR);
            this.mmDbId = mmDbId;
        }

        @Override
        public void run() {
            final String METHOD = "MatchInfoProcess-run";
            try {
                connSportsModel.matchInfo(mmDbId);
            } catch (DBConnectionException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.WARNING, false);
            }

        }
    }

    private class ParserProcess implements Runnable {

        private final Market market;
        private Match match;

        public ParserProcess(Market market) {
            this.market = market;
        }

        @Override
        public void run() {
            String hashString = market.getHome() + market.getAway() + market.getEventDate() + market.getName();
            int hashCode = hashString.toUpperCase().hashCode();
            match = matchMap.get(hashCode);
        }

        public Market getMarket() {
            return market;
        }

        public Match getMatch() {
            return match;
        }
    }
}
