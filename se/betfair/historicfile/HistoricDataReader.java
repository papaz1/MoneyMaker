package se.betfair.historicfile;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import se.betfair.config.BetfairCondition;
import se.betfair.config.BCMarketEnum;
import se.betfair.enums.EventTypeEnum;
import se.betfair.model.HistoricMarket;
import se.betfair.model.BMHistoricRunner;
import se.betfair.util.BetfairUtility;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

public class HistoricDataReader {

    private static final String CLASSNAME = HistoricDataReader.class.getName();
    private static Map<Integer, HistoricMarket> historicMarkets;
    private final File file;
    private final BetfairCondition condition;
    private char delimiter;

    public HistoricDataReader(File file, BetfairCondition condition) {
        this.file = file;
        this.condition = condition;
        historicMarkets = new LinkedHashMap<>();
    }

    public Map<Integer, HistoricMarket> getHistoricMarkets() {
        final String METHOD = "getHistoricMarkets";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String unparsedString;
            HistoricMarket historicMarket1 = null;
            HistoricMarket historicMarket2;

            //Skip the first line which is the header
            reader.readLine();
            boolean first = true;
            if ((unparsedString = reader.readLine()) != null) {
                boolean found = false;
                while (!found && unparsedString != null) {
                    if (first) {
                        delimiter = parseDeliminiter(unparsedString);
                        first = false;
                    }

                    try {
                        historicMarket1 = createHistoricMarket(unparsedString);
                        if (includeMarket(historicMarket1, condition)) {
                            found = true;
                        } else {
                            unparsedString = reader.readLine();
                        }
                    } catch (ParseException e) {
                        Log.logMessage(CLASSNAME, METHOD, "Record excluded. " + e.getMessage(), LogLevelEnum.ERROR, false);
                        unparsedString = reader.readLine();
                    }
                }
                if (found) {
                    if ((unparsedString = reader.readLine()) != null) {
                        boolean endOfFile = false;
                        while (!endOfFile) {
                            try {
                                historicMarket2 = createHistoricMarket(unparsedString);
                                if (includeMarket(historicMarket2, condition)) {

                                    /**
                                     * If it's the same market add the runners
                                     * from market 2 to market 1. Also add the
                                     * unparsed market strings. One market
                                     * should have all the corresponding market
                                     * strings from the historic file.
                                     */
                                    if (historicMarket1.getMarketId().equalsIgnoreCase(historicMarket2.getMarketId())) {
                                        historicMarket1.addAllHistoricRunners(historicMarket2.getHistoricRunners());
                                        historicMarket1.addAllUnparsedStrings(historicMarket2.getUnparsedStrings());
                                    } else {
                                        historicMarkets.put(Integer.parseInt(historicMarket1.getMarketId()), historicMarket1);
                                        historicMarket1 = historicMarket2;
                                    }
                                }
                            } catch (ParseException e) {
                                Log.logMessage(CLASSNAME, METHOD, "Record excluded. " + e.getMessage(), LogLevelEnum.ERROR, false);
                            }
                            if ((unparsedString = reader.readLine()) == null) {
                                historicMarkets.put(Integer.parseInt(historicMarket1.getMarketId()), historicMarket1);
                                endOfFile = true;
                            }
                        }
                    } else {
                        historicMarkets.put(Integer.parseInt(historicMarket1.getMarketId()), historicMarket1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.CRITICAL, false);
        } catch (IOException e) {
            Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.CRITICAL, false);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return historicMarkets;
    }

    public static List<BMHistoricRunner> getHistoricRunners(int marketId) {
        return historicMarkets.get(marketId).getHistoricRunners();
    }

    private HistoricMarket createHistoricMarket(String unparsedString) throws ParseException {
        String currentColumn = null;
        int columnCounter = 0;
        int offSet = 0;
        StringReader reader = new StringReader(unparsedString);

        HistoricMarket historicMarket = new HistoricMarket();
        BMHistoricRunner historicRunner = new BMHistoricRunner();

        //Historic market
        String sportsId;
        String marketId;
        String fullDescription;
        String scheduledOff;
        String event;
        String dtActualOff;
        String inPlay;
        String tempString;

        //Historic runner
        String selectionId;
        String selection;
        String odds;
        String numberBets;
        String volumeMatched;
        String latestTaken;
        String firstTaken;
        String winFlag;

        historicMarket.addUnparsedString(unparsedString);
        if (unparsedString.contains("\"")) {
            offSet = 1;
        }
        try {
            currentColumn = "sportsId";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            sportsId = tempString.substring(offSet, tempString.length() - offSet);
            historicMarket.setSportsId(sportsId);

            currentColumn = "marketId";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            marketId = tempString.substring(offSet, tempString.length() - offSet);
            historicMarket.setMarketId(marketId);

            columnCounter++;
            BetfairUtility.readString(reader, delimiter); //Jump past settled_date

            currentColumn = "fullDescription";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            fullDescription = tempString.substring(offSet, tempString.length() - offSet);
            historicMarket.setFullDescription(fullDescription);

            currentColumn = "scheduledOff";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            scheduledOff = tempString.substring(offSet, tempString.length() - offSet);
            historicMarket.setScheduledOff(scheduledOff);

            currentColumn = "event";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            if (!tempString.isEmpty()) {
                event = tempString.substring(offSet, tempString.length() - offSet);
                historicMarket.setEvent(event);
            }

            currentColumn = "dtActualOff";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            if (!tempString.isEmpty()) {
                dtActualOff = tempString.substring(offSet, tempString.length() - offSet);
                historicMarket.setDtActualOff(dtActualOff);
            }

            currentColumn = "selectionId";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            selectionId = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setSelectionId(selectionId);

            currentColumn = "selection";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            selection = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setSelection(selection);

            currentColumn = "odds";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            odds = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setOdds(odds);

            currentColumn = "numberBets";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            numberBets = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setNumberBets(numberBets);

            currentColumn = "volumeMatched";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            volumeMatched = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setVolumeMatched(volumeMatched);

            currentColumn = "latestTaken";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            latestTaken = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setLatestTaken(latestTaken);

            currentColumn = "firstTaken";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            firstTaken = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setFirstTaken(firstTaken);

            currentColumn = "winFlag";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            if (!tempString.isEmpty()) {
                winFlag = tempString.substring(offSet, tempString.length() - offSet);
                historicRunner.setWinFlag(winFlag);
            }

            currentColumn = "inPlay";
            columnCounter++;
            tempString = BetfairUtility.readString(reader, delimiter);
            inPlay = tempString.substring(offSet, tempString.length() - offSet);
            historicRunner.setInPlay(inPlay);

            historicMarket.addHistoricRunner(historicRunner);
        } catch (StringIndexOutOfBoundsException e) {
            throw new ParseException("StringIndexOutOfBoundsException parsing market. Failed to parse column: " + currentColumn + ". Column number: " + columnCounter + ". " + "Current record in file: " + unparsedString, 0);
        }
        return historicMarket;
    }

    private char parseDeliminiter(String str) {
        StringTokenizer tokenizerSemicolon = new StringTokenizer(str, ";");
        StringTokenizer tokenizercomma = new StringTokenizer(str, ",");

        int numberOfSemiColons = tokenizerSemicolon.countTokens();
        int numberOfComma = tokenizercomma.countTokens();

        if (numberOfSemiColons >= numberOfComma) {
            return ';';
        } else {
            return ',';
        }
    }

    private static boolean includeMarket(HistoricMarket historicMarket, BetfairCondition condition) {

        //Event must not be empty which can happen sometimes
        if (historicMarket.getEvent() == null) {
            return false;
        }

        if (!condition.getEventType().equals(EventTypeEnum.ALL)
                && condition.getEventType().getId() != Integer.parseInt(historicMarket.getSportsId())) {
            return false;
        }

        List<BCMarketEnum> menuPathFilter = condition.getMenuPathFilterInclude();
        Iterator<BCMarketEnum> iterator = menuPathFilter.iterator();
        BCMarketEnum filter;

        //If any of the filter strings are in the menupath then this market
        //should be included in the result set, therefore set include to true.
        //menuPathFilter being empty means all values.
        if (!menuPathFilter.isEmpty()) {
            boolean found = false;

            while (iterator.hasNext() && !found) {
                filter = iterator.next();
                if (historicMarket.getFullDescription().toUpperCase().contains(filter.value())) {
                    found = true;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
