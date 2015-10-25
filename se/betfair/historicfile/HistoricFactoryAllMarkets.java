package se.betfair.historicfile;

import java.io.StringReader;
import java.text.ParseException;
import java.util.*;
import se.betfair.config.BetfairCondition;
import se.betfair.config.BCMarketEnum;
import se.betfair.dict.DictOutcomesWinners;
import se.betfair.dict.EventTypeDictionary;
import se.betfair.enums.EventTypeEnum;
import se.betfair.exception.BEXElementNotFoundException;
import se.betfair.factory.FactoryBetOfferOutcomeItem;
import se.betfair.exception.BEXMarketExcludedException;
import se.betfair.factory.FactoryUtil;
import se.betfair.model.*;
import se.betfair.util.BetfairUtility;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.model.BetOfferItem;
import se.moneymaker.util.Utils;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.exception.ErrorType;

public class HistoricFactoryAllMarkets {

    private static final String CLASSNAME = HistoricFactoryAllMarkets.class.getName();
    private static String marketName;
    private static String menuPath;

    public static MarketHolder parseHistoricMarkets(Map<Integer, HistoricMarket> historicMarkets, BetfairCondition condition) {
        final String METHOD = "parseHistoricMarkets";
        StringBuilder erroneousRows = null;
        List<Market> successfullyCreatedMarkets = new ArrayList<>();
        List<HistoricMarket> erroneousMarkets = new ArrayList<>();
        Market market;
        for (Map.Entry<Integer, HistoricMarket> entry : historicMarkets.entrySet()) {
            HistoricMarket historicMarket = entry.getValue();
            try {
                try {
                    market = parseHistoricMarket(entry.getKey(), historicMarket, condition);
                    if (market.include()) {
                        successfullyCreatedMarkets.add(market);
                    }
                } catch (ParseException | StringIndexOutOfBoundsException e) {
                    erroneousMarkets.add(historicMarket);
                    Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                } catch (BetOfferException e) {
                    if (!e.getErrorType().equals(ErrorType.FILTER)) {
                        erroneousMarkets.add(historicMarket);
                        Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                    }
                }
            } catch (BEXElementNotFoundException e) {
                if (erroneousRows == null) {
                    erroneousRows = new StringBuilder();

                    //Header
                    erroneousRows.append("HEADER").append(System.getProperty("line.separator"));
                } else {
                    erroneousRows.append(entry.getValue().getUnparsedStrings()).append(System.getProperty("line.separator"));
                }
            } catch (BEXMarketExcludedException e) {
            }
        }
        if (erroneousRows != null) {
            if (erroneousRows.length() > 1) {
                HistoricFileSystem.writeErrorsToFile(erroneousRows, "ERROR_MISSING_OUTCOMEWINNERS");
            }
        }
        return new MarketHolder(successfullyCreatedMarkets, erroneousMarkets);
    }

    private static Market parseHistoricMarket(Integer key, HistoricMarket historicMarket, BetfairCondition condition) throws BEXMarketExcludedException, ParseException, BetOfferException, StringIndexOutOfBoundsException, BEXElementNotFoundException {
        Market market = new Market();
        market.setId(key);
        market.setEventType(condition.getEventType());
        marketName = historicMarket.getEvent();
        menuPath = historicMarket.getFullDescription();

        //For some markets the market name is part of the menu path. Therefore
        //first find out if the market name is part of the menu path. Then
        //some parsing has to be done to seperate the market name from the menu
        //path.
        if (marketName.isEmpty()) {
            int lastMenuPathSeperator = menuPath.lastIndexOf('/');
            marketName = menuPath.substring(lastMenuPathSeperator + 1, menuPath.length());
            menuPath = menuPath.substring(0, lastMenuPathSeperator);
        }

        market.setMenuPath(menuPath);
        testForIncludeBasedOnString(true, marketName, condition.getMarketNameFilterInclude());
        market.setName(marketName);

        String hierarchyTextPath = menuPath;
        List<String> hierarchyTextPathNoFix = new ArrayList<>();
        StringBuilder hierarchyTextPathNoFix2 = new StringBuilder();
        String hierarchyTextNode;
        StringReader hierarchyTextPathReader = new StringReader(hierarchyTextPath);

        EventTypeEnum eventType = EventTypeDictionary.getEventType(Integer.parseInt(historicMarket.getSportsId()));
        market.setEventType(eventType);

        char delimiter = '/';
        hierarchyTextNode = BetfairUtility.readString(hierarchyTextPathReader, delimiter);

        try {
            parseHierarchy(hierarchyTextPathReader, delimiter, hierarchyTextPathNoFix, hierarchyTextPathNoFix2);

            market.setHierarchyTextPathNoFix(hierarchyTextPathNoFix);
            market.setHierarchyTextPathNoFix2(hierarchyTextPathNoFix2.toString());

            String[] teams = FactoryUtil.parseTeams(hierarchyTextPathNoFix);
            market.setHome(teams[0].trim());
            market.setAway(teams[1].trim());

            BetOfferItem betOfferItem = FactoryBetOfferOutcomeItem.parseBetOfferItem(marketName, market.getHome(), market.getAway());

            market.setEventDate(Utils.stringToDate(historicMarket.getSchduledOff()));
            market.setActualEventDate(Utils.stringToDate(historicMarket.getDtActualOff()));

            DictOutcomesWinners outWinDict = DictOutcomesWinners.getInstance();
            market.setNumberOfRunners(outWinDict.getNumberOfOutcomes(betOfferItem.getType().getName()));
            market.setNumberOfWinners(outWinDict.getNumberOfWinners(betOfferItem.getType().getName()));
            if (includeMarketPostParseBasedOnCondition(market, condition)) {
                market.setIncludeMarket(true);
            } else {
                market.setIncludeMarket(false);
            }
        } catch (BetOfferException e) {
            throw new BetOfferException("Market excluded: " + e.getMessage(), ErrorType.FILTER);
        } catch (ParseException e) {
            market.setIncludeMarket(false);
            throw new ParseException("Market excluded: Team names could not be parsed. Market id: " + market.getId() + ". Market name: " + market.getName() + ". Message: " + e.getMessage(), 0);
        } catch (StringIndexOutOfBoundsException e) {
            market.setIncludeMarket(false);
            throw new StringIndexOutOfBoundsException("Market excluded: " + e.getMessage() + ". Market id: " + market.getId() + ". Market name: " + market.getName());
        }
        return market;
    }

    /**
     * This method tests if the market should be included or not. The test is
     * performed by using the stringToTest which can for example be the
     * marketName and then checking the filterValues. Setting include to true
     * means that the market should be inlcuded based on the filter values.
     * False means it should be excluded. The reason the method is not returning
     * false but instead throws an exception is that this method is being used
     * extensively in this class. Therefore the code will be much cleaner with
     * try/catch for the markets since try/catch only needs to be written once
     * instead of having if/else each and every time a new test for include is
     * being done.
     */
    private static BCMarketEnum testForIncludeBasedOnString(boolean include, String stringToTest, List<BCMarketEnum> filterValues) throws BEXMarketExcludedException {
        Iterator<BCMarketEnum> filterIterator = filterValues.iterator();
        BCMarketEnum filter;
        BCMarketEnum foundFilter = null;

        //If any of the filter strings are in the stringToTest then this market
        //should be included in the result set, therefore set include to true.
        //Empty means all values.
        if (!filterValues.isEmpty()) {
            while (filterIterator.hasNext() && foundFilter == null) {
                filter = filterIterator.next();

                if (filter.isEqual()) {
                    if (stringToTest.toUpperCase().equalsIgnoreCase(filter.value())) {
                        foundFilter = filter;
                    }
                } else {
                    if (stringToTest.toUpperCase().contains(filter.value())) {

                        //if the addition begins with has been added then also check that the market name
                        //begins with the string
                        if (filter.beginsWith()) {
                            if (stringToTest.toUpperCase().startsWith(filter.value())) {
                                foundFilter = filter;
                            }
                        } else {
                            foundFilter = filter;
                        }
                    }
                }
            }
            if (include && foundFilter == null) {
                throw new BEXMarketExcludedException();
            } else if (!include && foundFilter != null) {
                throw new BEXMarketExcludedException();
            }
        }
        return foundFilter;
    }

    private static boolean includeMarketPostParseBasedOnCondition(Market market, BetfairCondition condition) {
        final String METHOD = "includeMarketPostParse";

        Date eventDate = market.getEventDate();
        Date conditionEventDate = condition.getEventDate();

        boolean filterIncludeEventDate;

        //Filter the market on when the event starts checking the condition 
        //objects eventDate with the market eventDate. If this condition is false
        //then fetch every market with eventDateFrom > todays datetime
        if (eventDate != null && conditionEventDate != null) {
            filterIncludeEventDate = eventDate.after(conditionEventDate);
        } else if (eventDate != null) {
            Calendar cal = Calendar.getInstance();

            filterIncludeEventDate = eventDate.after(cal.getTime());
        } else {
            Log.logMessage(CLASSNAME, METHOD, "Market excluded: Event date is null. Market name: " + market.getName()
                    + " Menu path: " + market.getMenuPath(), LogLevelEnum.WARNING, false);

            //This shouldn't happen but unfortunately there are cases when Betfair for some
            //reason have null values for the event date. These markets should be ignored.
            filterIncludeEventDate = false;
        }

        return filterIncludeEventDate;
    }

    private static void parseHierarchy(StringReader hierarchyTextPathReader, char delimiter, List<String> hierarchyTextPathNoFix, StringBuilder hierarchyTextPathNoFix2) throws BEXMarketExcludedException {
        boolean endOfString = false;
        boolean isTeam = false;
        String hierarchyTextNode;
        String teams = null;
        while (!endOfString) {
            hierarchyTextNode = BetfairUtility.readString(hierarchyTextPathReader, delimiter);
            if (!hierarchyTextNode.isEmpty()) {

                //The fixture node is not interesting since statistics is being done on sports->league and not on for example fixtures 25 feb
                if (!hierarchyTextNode.toUpperCase().contains(Market.SOCCER_FIXTURES)
                        && !hierarchyTextNode.toUpperCase().contains(Market.SOCCER_MATCHES)) {

                    /**
                     * The last node text will be empty because the last node is
                     * the actual market. The market id will therefore be saved
                     * but there is no text in the menu path to be saved. The
                     * market name is parsed in the beginning of the long string
                     * that is parsed to get the market data.
                     */
                    if (!hierarchyTextNode.isEmpty()) {

                        /**
                         * We need to identify when there are teams. Some teams
                         * will have the delimiter in the team name which will
                         * cause the parser to divide the team name
                         */
                        if (!isTeam) {
                            if (hierarchyTextNode.contains(" v ")
                                    || hierarchyTextNode.contains(" V ")
                                    || hierarchyTextNode.contains(" vs ")
                                    || hierarchyTextNode.contains(" @ ")) {
                                teams = hierarchyTextNode;
                                isTeam = true;
                            } else {

                                hierarchyTextPathNoFix.add(hierarchyTextNode);

                                //Also build up a string since this string will be used in the sorting
                                hierarchyTextPathNoFix2.append(hierarchyTextNode);
                                hierarchyTextPathNoFix2.append('\\');
                            }
                        } else {
                            teams = teams + "/" + hierarchyTextNode;
                        }
                    }
                }
            } else {
                hierarchyTextPathNoFix.add(teams);

                //Also build up a string since this string will be used in the sorting
                hierarchyTextPathNoFix2.append(teams);
                hierarchyTextPathNoFix2.append('\\');
                endOfString = true;
            }
        }
    }
}
