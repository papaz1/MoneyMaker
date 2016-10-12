package se.betfair.historicfile;

import java.awt.Point;
import java.util.*;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.Price;
import se.moneymaker.util.Utils;

public class HistoricPaybackCalculator {

    private static final long PAYBACK_DIFF_PRE_INPLAY = 86400000; //24 hours
    private static final long PAYBACK_DIFF_INPLAY = 60000;
    private Object[][] paybackMatrix;

    public BetOffer populateApproximatePayback(BetOffer betOffer) {
        int numberOfColumns = betOffer.getMaxNumberOfOutcomes();
        HistoricOutcomeMap timeline = parseTimeline(betOffer);

        paybackMatrix = parsePaybackMatrix(timeline, numberOfColumns);

        //If all the outcomes don't exist then there is no point in
        //approximating the prices on the timeline because the payback for each
        //date will be 0
        for (int i = 1; i < paybackMatrix[0].length; i++) {
            if (paybackMatrix[0][i] == null) {
                return betOffer;
            }
        }

        paybackMatrix = fillApproximatePrices(paybackMatrix);
        BetOffer betOfferWithPayback = fillPayback(betOffer, calculatePayback(paybackMatrix));
        //BUDisplay.printPaybackMatrix(paybackMatrix);

        return betOfferWithPayback;
    }

    private BetOffer fillPayback(BetOffer betOffer, Map<Date, List<Price>> datePrices) {
        Iterator<Outcome> iteratorOutcome = (betOffer.getOutcomes()).iterator();
        Outcome outcome;
        while (iteratorOutcome.hasNext()) {
            outcome = iteratorOutcome.next();
            Iterator<Price> iteratorPrice = (outcome.getPrices()).iterator();
            Price priceToFillWithPayback;

            while (iteratorPrice.hasNext()) {
                priceToFillWithPayback = iteratorPrice.next();
                List<Price> paybackPrices = datePrices.get(priceToFillWithPayback.getLatestTakenDate());

                if (paybackPrices != null) {
                    Iterator<Price> iteratorPaybackPrice = paybackPrices.iterator();
                    Price paybackPrice;

                    while (iteratorPaybackPrice.hasNext()) {
                        paybackPrice = iteratorPaybackPrice.next();

                        if (priceToFillWithPayback.getPrice() == paybackPrice.getPrice()
                                && priceToFillWithPayback.getId() == paybackPrice.getId()) {
                            priceToFillWithPayback.setPayback(paybackPrice.getPayback());
                        }
                    }
                }
            }
        }
        return betOffer;
    }

    private Map<Date, List<Price>> calculatePayback(Object[][] paybackMatrix) {
        Map<Date, List<Price>> datePrices = new HashMap<>();
        double payback;
        for (int i = 1; i < paybackMatrix.length; i++) {
            List<HistoricPriceOutcomesForPayback> outcomesList = new ArrayList<>();
            //List<Price> prices = new ArrayList<>();

            //Loop through outcomes, each column contains an outcome except column 0 which contains the date
            for (int j = 1; j < paybackMatrix[i].length; j++) {
                boolean abort = false;
                if (paybackMatrix[i][j] != null) {
                    HistoricWeightedPrice weightedPrice = (HistoricWeightedPrice) paybackMatrix[i][j];
                    List<Price> weightedPrices = weightedPrice.getPrices();
                    Iterator<Price> iterator = weightedPrices.iterator();

                    //If there are multiple prices for this outcome, then for each of the prices
                    //a payback will be calculated together with the weighted prices of the other outcomes
                    while (iterator.hasNext() && !abort) {
                        HistoricPriceOutcomesForPayback outcomes = new HistoricPriceOutcomesForPayback();
                        Price price = iterator.next();
                        //prices.add(price); //This is the price we are at in the loop
                        Outcome outcome = new Outcome();
                        outcome.addPrice(price);
                        outcomes.addOutcome(outcome);
                        outcomes.setPrice(price);
                        List<Outcome> otherOutcomes = getOtherOutcomes(paybackMatrix, i, j);
                        if (otherOutcomes != null) {
                            outcomes.addAllOutcomes(otherOutcomes);
                            outcomesList.add(outcomes);
                        } else {
                            abort = true;
                        }
                    }
                } else {
                    abort = true;
                }
                if (abort) {
                    //prices.clear();
                    outcomesList.clear();
                    break;
                }
            }
            if (!outcomesList.isEmpty()) {
                Iterator<HistoricPriceOutcomesForPayback> iteratorOutcomes = outcomesList.iterator();
                HistoricPriceOutcomesForPayback outcomesForPaybackCalculation;

                while (iteratorOutcomes.hasNext()) {
                    outcomesForPaybackCalculation = iteratorOutcomes.next();
                    payback = Utils.calculateBetOfferPayback(outcomesForPaybackCalculation.getOutcomes());
                    Date latestTaken = ((DateInPlay) paybackMatrix[i][0]).getLatestTaken();
                    List<Price> pricesInDatePrices = datePrices.get(latestTaken);
                    Price possiblePriceToAdd = outcomesForPaybackCalculation.getPrice();

                    if (pricesInDatePrices != null) {

                        //Only add the payback to this price if this price has the same
                        //date as the date in BMDateInPlay object. Else this price is copied
                        //and doesn't actually belong to this row in the payback matrix.
                        if (latestTaken.equals(possiblePriceToAdd.getLatestTakenDate())) {
                            possiblePriceToAdd.setPayback(payback);
                            pricesInDatePrices.add(possiblePriceToAdd);
                        }
                    } else {

                        //Only add the payback to this price if this price has the same
                        //date as the date in BMDateInPlay object. Else this price is copied
                        //and doesn't actually belong to this row in the payback matrix.
                        if (latestTaken.equals(possiblePriceToAdd.getLatestTakenDate())) {
                            possiblePriceToAdd.setPayback(payback);
                            List<Price> newPricesInDatePrices = new ArrayList<>();
                            newPricesInDatePrices.add(possiblePriceToAdd);
                            datePrices.put(latestTaken, newPricesInDatePrices);
                        }
                    }
                }
            }
        }
        return datePrices;
    }

    /**
     * Gets the other outcomes including their weighted price in the payback
     * matrix
     *
     * @param paybackMatrix
     * @param i Row index
     * @param excludeColumnIndex
     * @return
     */
    private List<Outcome> getOtherOutcomes(Object[][] paybackMatrix, int i, int excludeColumnIndex) {
        List<Outcome> outcomes = new ArrayList<>();

        for (int j = 1; j < paybackMatrix[i].length; j++) {
            if (j != excludeColumnIndex) {
                if (paybackMatrix[i][j] != null) {
                    Price price = new Price();
                    price.setPrice(((HistoricWeightedPrice) paybackMatrix[i][j]).getWeightedPrice());
                    Outcome outcome = new Outcome();
                    outcome.addPrice(price);
                    outcomes.add(outcome);
                } else {
                    return null;
                }
            }
        }
        return outcomes;
    }

    /**
     * The Object[][] matrix consists of the objects BMDateInPlay and
     * BMHistoricWeightedPrice
     *
     * @param paybackMatrix
     * @return
     */
    private Object[][] fillApproximatePrices(Object[][] paybackMatrix) {
        long diffLater;
        long diffEarlier;
        boolean currentDateIsInPlay;
        for (int i = paybackMatrix.length - 1; i > 0; i--) {
            for (int j = 1; j < paybackMatrix[i].length; j++) {

                //If the current price is null then look for a price close in time
                //that can be filled in this position
                if (paybackMatrix[i][j] == null) {
                    currentDateIsInPlay = isInPlay(new Point(i, 0));

                    //The two closest prices in the payback matrix will be compared
                    //to the current one. But before deciding on which one to copy
                    //we need to see if they are eligible for copy according to rules.
                    //If they aren't eligible for copy then we set the time difference to -1
                    //indicating that this value shouldn't be considered in the choice of
                    //price.
                    Point laterPosition = getClosestPrice(new Point(i, j), 1);
                    if (laterPosition != null) {
                        diffLater = getEligibleTimeDifference(currentDateIsInPlay, laterPosition, new Point(i, j));
                    } else {
                        diffLater = -1;
                    }

                    Point earlierPosition = getClosestPrice(new Point(i, j), -1);
                    if (earlierPosition != null) {
                        diffEarlier = getEligibleTimeDifference(currentDateIsInPlay, earlierPosition, new Point(i, j));
                    } else {
                        diffEarlier = -1;
                    }

                    //Copy the closest price
                    if (diffLater != -1 && diffEarlier != -1) {
                        if (diffLater <= diffEarlier) {
                            paybackMatrix[i][j] = paybackMatrix[laterPosition.x][laterPosition.y];
                        } else if (diffEarlier < diffLater) {
                            paybackMatrix[i][j] = paybackMatrix[earlierPosition.x][earlierPosition.y];
                        }
                    } else if (diffLater != -1) {
                        paybackMatrix[i][j] = paybackMatrix[laterPosition.x][laterPosition.y];
                    } else if (diffEarlier != -1) {
                        paybackMatrix[i][j] = paybackMatrix[earlierPosition.x][earlierPosition.y];
                    } else {
                        //Both are -1 and therefore there is nothing to copy
                    }
                }
            }
        }
        return paybackMatrix;
    }

    /**
     * Get the closest position where there is a price
     *
     * @param currentPosition
     * @param direction
     * @return closest position where there is a price else null
     */
    private Point getClosestPrice(Point currentPosition, int direction) {
        if (direction == 1) {
            for (int i = currentPosition.x + 1; i < paybackMatrix.length; i++) {
                if (paybackMatrix[i][currentPosition.y] != null) {
                    return new Point(i, currentPosition.y);
                }
            }
        } else {
            for (int i = currentPosition.x - 1; i > 0; i--) {
                if (paybackMatrix[i][currentPosition.y] != null) {
                    return new Point(i, currentPosition.y);
                }
            }
        }
        return null;
    }

    //Check if Point one is eligible ofor copy
    //Before choosing the closest date, check if the dates are eligable, that is
    //close enough to be considered for copying. If the price isn't eligable for copying
    //then it will be set to -1. There are two rules:
    //1. You can only copy prices within the same period, that is inPlay prices with inPlay price
    //and pre inPlay prices with pre inPlay price.
    //2. And then there is the time constraint in BetfairConfig for inPlay and pre inPlay prices.
    private long getEligibleTimeDifference(boolean currentDateIsInPlay, Point one, Point minusTwo) {
        long diff;

        //Rule 1
        //The inPlay flag is fetched from the BMDateInPlay object in column 0
        if (currentDateIsInPlay != isInPlay(new Point(one.x, 0))) {
            return -1;
        } else {

            //Rule 2
            //Always later date - earlier date so that we don't get negative results
            if (one.x >= minusTwo.x) {
                diff = getTimeDifference(new Point(one.x, one.y), new Point(minusTwo.x, minusTwo.y));
            } else {
                diff = getTimeDifference(new Point(minusTwo.x, minusTwo.y), new Point(one.x, one.y));
            }

            if (currentDateIsInPlay) {
                if (diff > PAYBACK_DIFF_INPLAY) {
                    return -1;
                } else {
                    return diff;
                }
            } else {
                if (diff > PAYBACK_DIFF_PRE_INPLAY) {
                    return -1;
                } else {
                    return diff;
                }
            }
        }
    }

    private boolean isInPlay(Point pos) {
        return ((DateInPlay) paybackMatrix[pos.x][pos.y]).isInPlay();
    }

    private long getTimeDifference(Point one, Point minusTwo) {
        long dateOne;
        long dateTwo;

        //If we are looking at a position in the matrix where there isn't a price then
        //the time difference will be compared to that rows global price which is in column 0
        if (paybackMatrix[one.x][one.y] == null) {
            dateOne = ((DateInPlay) paybackMatrix[one.x][0]).getTime();
        } else {
            dateOne = ((HistoricWeightedPrice) paybackMatrix[one.x][one.y]).getLatestTakenDate().getTime();
        }

        if (paybackMatrix[minusTwo.x][minusTwo.y] == null) {
            dateTwo = ((DateInPlay) paybackMatrix[minusTwo.x][0]).getTime();
        } else {
            dateTwo = ((HistoricWeightedPrice) paybackMatrix[minusTwo.x][minusTwo.y]).getLatestTakenDate().getTime();
        }

        return dateOne - dateTwo;
    }

    /**
     * Create a matrix where column 0 has the dates for the prices and first row
     * contains the outcome ids. Then places the prices in matrix according to
     * matrix[date][outcome].
     */
    private Object[][] parsePaybackMatrix(HistoricOutcomeMap timeline, int numberOfColumns) {
        Object[][] tempPaybackMatrix = new Object[timeline.size() + 1][numberOfColumns + 1];
        int rowPrice = 1;
        int columnPrice;
        int rowDate = 1;
        int columnOutcome = 1;

        Iterator timelineIterator = timeline.iterator();
        Map<Long, Integer> outcomeColumns = new HashMap<>();
        while (timelineIterator.hasNext()) {

            Map.Entry entry = (Map.Entry) timelineIterator.next();

            //This is the date for which the outcomes belong to
            tempPaybackMatrix[rowDate][0] = entry.getKey();
            rowDate++;

            List<HistoricOutcomeList> aggregatedHistoricOutcomes = (List<HistoricOutcomeList>) entry.getValue();
            Iterator<HistoricOutcomeList> iteratorOutcomes = aggregatedHistoricOutcomes.iterator();
            HistoricOutcomeList outcomeList;
            while (iteratorOutcomes.hasNext()) {
                outcomeList = iteratorOutcomes.next();

                //Check if this outcome already exists. Only add outcome
                //in a new column if the outcome doesn't already exists.
                if (!outcomeColumns.containsKey(outcomeList.getId())) {
                    outcomeColumns.put(outcomeList.getId(), columnOutcome);
                    tempPaybackMatrix[0][columnOutcome] = outcomeList.getId();
                    columnPrice = columnOutcome;
                    columnOutcome++;
                } else {

                    //This is the index for the column that the aggregated price will be
                    //inserted in
                    columnPrice = outcomeColumns.get(outcomeList.getId());
                }
                tempPaybackMatrix[rowPrice][columnPrice] = outcomeList.parseHistoricWeightedPrice();
            }
            rowPrice++;
        }
        return tempPaybackMatrix;
    }

    /**
     * Make a timeline grouping the outcomes/prices according to the date price
     * were matched. This will be used to build the payback matrix that first
     * will be built and then will be filled according to time rules pre-inplay
     * and inplay.
     *
     * @param betOffer
     * @return
     */
    public HistoricOutcomeMap parseTimeline(BetOffer betOffer) {
        HistoricOutcomeMap timeline = new HistoricOutcomeMap();
        List<Outcome> outcomes = betOffer.getOutcomes();
        Iterator<Outcome> iteratorOutcomes = outcomes.iterator();
        Outcome outcome;
        Outcome newOutcome;

        while (iteratorOutcomes.hasNext()) {
            outcome = iteratorOutcomes.next();
            List<Price> prices = outcome.getPrices();
            Iterator<Price> iteratorPrices = prices.iterator();
            Price price;
            Date latestTaken;

            while (iteratorPrices.hasNext()) {
                price = iteratorPrices.next();
                latestTaken = price.getLatestTakenDate();
                DateInPlay dip = new DateInPlay(latestTaken, price.isInPlay());

                //Each timestamp must be unique, if the timestamp already
                //exists in the timeline then add the outcome with the price
                //on the existing timestamp.
                //Betfair can have two different prices on same outcome
                //on the same timestamp. If this is the case then create
                //another entry in the timeline like if this was a different
                //timestamp. For each row in the timestamp each outcome
                //can only be represented once.
                newOutcome = new Outcome();
                newOutcome.setExternalKey(outcome.getExternalKey());
                newOutcome.setItem(outcome.getItem());
                newOutcome.addPrice(price);
                timeline.put(dip, newOutcome);
            }
        }
        return timeline;
    }
}
