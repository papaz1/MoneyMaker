package se.betfair.factory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import se.betfair.model.MarketBook;
import se.betfair.model.MarketCatalogue;
import se.betfair.model.PriceSize;
import se.betfair.model.Runner;
import se.betfair.model.RunnerCatalog;
import se.moneymaker.db.DBServices;
import se.moneymaker.dict.Config;
import se.moneymaker.dict.BetOfferDict;
import se.moneymaker.enums.BetOfferTypeEnum;
import se.moneymaker.enums.ConfigEnum;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Operation;
import se.moneymaker.enums.PoolType;
import se.moneymaker.exception.BetofferExcludedException;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.OutcomeException;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Match;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.Price;
import se.moneymaker.enums.PriceEnum;
import se.moneymaker.enums.ReadReason;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.MatchException;
import se.moneymaker.model.MatchReference;
import se.moneymaker.model.MatchReferenceInfo;
import se.moneymaker.util.Log;
import se.moneymaker.util.Utils;

public class FactorySportsModel {

    private final static String CLASSNAME = FactorySportsModel.class.getName();
    private final Config config;
    private ReadReason readReason;
    private double minuteWeight;
    private String currency;

    public ReadReason getReadReason() {
        return readReason;
    }

    public void setReadReason(ReadReason readReason) {
        this.readReason = readReason;
    }

    public double getMinuteWeight() {
        return minuteWeight;
    }

    public void setMinuteWeight(double minuteWeight) {
        this.minuteWeight = minuteWeight;
    }

    public FactorySportsModel() {
        final String METHOD = "FactorySportsModel";
        config = Config.getInstance();
        DBServices services = new DBServices(true);
        try {
            currency = services.readCurrency(Source.BETFAIR.getName());
        } catch (DBConnectionException e) {
            Log.logMessage(CLASSNAME, METHOD, "Error reading currency code: " + e.getErrorType(), LogLevelEnum.ERROR, true);
        }
    }

    public List<Match> createMatches(Operation operation, List<MarketCatalogue> marketCatalogues) {
        final String METHOD = "createMatches";
        List<Match> matches = new ArrayList<>();
        Match match;
        BetOffer betOffer;
        List<Outcome> outcomes;
        List<Price> prices;
        int numberOfExcludedBetOffers = 0;
        for (MarketCatalogue marketCatalogue : marketCatalogues) {
            try {
                if (operation.equals(Operation.INSERT)) {
                    match = createMatch(marketCatalogue);
                    match.setSource(Source.BETFAIR.getName());
                    betOffer = createBetOffer(marketCatalogue, match.getHome(), match.getAway());
                    outcomes = createOutcomes(marketCatalogue, match.getHome(), match.getAway(), betOffer.getItem().getType());
                } else {
                    match = new Match();
                    betOffer = BetOfferDict.getBetOffer(marketCatalogue.getMarketId());
                    match.setEventDate(betOffer.getEventDate());//Needed for clearing prices on early markets

                    //This is just for safety so that we are sure that we never keep old prices that already have been sent in
                    outcomes = betOffer.getOutcomes();
                    for (Outcome outcome : outcomes) {
                        outcome.clearPrices();
                    }
                }

                if (outcomes != null) {
                    if (!outcomes.isEmpty()) {
                        if (outcomes.size() != betOffer.getMaxNumberOfOutcomes()) {
                            throw new BetofferExcludedException("Betoffer: " + betOffer.getName() + " external key: " + betOffer.getExternalKey() + " exxluded due to all outcomes don't exist");
                        }
                        prices = createPrices(marketCatalogue.getMarketBook());
                        if (!prices.isEmpty()) {
                            setPriceOnOutcome(outcomes, prices); //Only outcomes with at least back or lay will be kept
                            setPaybackAndProbForPrices(outcomes);
                        }
                    }
                    betOffer.setOutcomes(outcomes);

                    if (betOffer.getMaxNumberOfOutcomes() == betOffer.getOutcomes().size()) {
                        betOffer.setPayback(Utils.calculateBetOfferPayback(betOffer.getOutcomes()));
                    }
                    match.addBetOffer(betOffer);
                    matches.add(match);
                }
            } catch (BetOfferException | OutcomeException | ParseException e) {
                numberOfExcludedBetOffers++;
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.WARNING, false);
            } catch (BetofferExcludedException | MatchException e) {
                numberOfExcludedBetOffers++;
            }
        }
        if (numberOfExcludedBetOffers > 0) {
            Log.logMessage(CLASSNAME, METHOD, "Number of betoffers excluded due to filters for case " + operation + ": " + numberOfExcludedBetOffers, LogLevelEnum.INFO, false);
        }
        return matches;
    }

    private Match createMatch(MarketCatalogue marketCatalogue) throws ParseException, BetofferExcludedException, MatchException {
        Match match = new Match();
        match.setSource(Source.BETFAIR.getName());
        match.setExternalKey(marketCatalogue.getEvent().getId());
        match.setEventDate((marketCatalogue.getMarketStartTime()));

        List<String> tmpTeams = new ArrayList<>(1);
        tmpTeams.add(marketCatalogue.getEvent().getName());
        String[] teams = FactoryUtil.parseTeams(tmpTeams);
        match.setHome(teams[0].trim());
        match.setAway(teams[1].trim());

        if (match.getHome().toUpperCase().contains("U1")
                || match.getHome().toUpperCase().contains("U2")
                || match.getHome().toUpperCase().contains("(Y)")) {
            match.setPoolType(PoolType.YOUTH);
        } else if (match.getHome().toUpperCase().contains("(W)")) {
            match.setPoolType(PoolType.FEMALE);
        }

        /*
         MatchReference matchReference = new MatchReference();
         matchReference.setHome(new MatchReferenceInfo(Source.BETFAIR.getName(), match.getHome(), match.getHome()));
         matchReference.setAway(new MatchReferenceInfo(Source.BETFAIR.getName(), match.getAway(), match.getAway()));
         if (marketCatalogue.getCompetition() == null) {
         throw new BetofferExcludedException("Competition is null");
         }
         matchReference.setPool(new MatchReferenceInfo(Source.BETFAIR.getName(), marketCatalogue.getCompetition().getName(), marketCatalogue.getCompetition().getName()));
         matchReference.setEvent(new MatchReferenceInfo(Source.BETFAIR.getName(), match.getExternalKey(), marketCatalogue.getEvent().getName()));
         match.setReference(matchReference);
         */
        return match;
    }

    private BetOffer createBetOffer(MarketCatalogue marketCatalogue, String home, String away) throws BetOfferException, BetofferExcludedException {
        final int CORRECT_SCORE_OLD_NUMBER_OF_OUTCOMES = 17;

        BetOffer betOffer = new BetOffer();

        //Event date is needed on betoffer level because of clearPricesOnEarlyMarkets
        betOffer.setEventDate(marketCatalogue.getMarketStartTime());
        betOffer.setSource(Source.BETFAIR.getName());
        betOffer.setVolumeMatched(marketCatalogue.getMarketBook().getTotalMatched());
        double volumeMatched = Double.parseDouble(config.get(ConfigEnum.BF_TOTAL_AMOUNT_MATCHED));
        if (betOffer.getVolumeMatched() < volumeMatched) {
            throw new BetofferExcludedException("Betoffer " + marketCatalogue.getMarketId() + " excluded due to volume matched < " + volumeMatched);
        }
        betOffer.setExternalKey(marketCatalogue.getMarketId());
        betOffer.setName(marketCatalogue.getMarketName());
        betOffer.setItem(FactoryBetOfferOutcomeItem.parseBetOfferItem(marketCatalogue.getMarketName(), home, away));
        betOffer.setMaxNumberOfOutcomes(marketCatalogue.getMarketBook().getNumberOfRunners());

        //If it's a correct score with only 17 outcomes it's the old Betfaor api correct score. Change the betoffer type to indicate this.
        if (betOffer.getItem().getType().equals(BetOfferTypeEnum.CORRECT_SCORE2) && betOffer.getMaxNumberOfOutcomes() == CORRECT_SCORE_OLD_NUMBER_OF_OUTCOMES) {
            betOffer.getItem().setType(BetOfferTypeEnum.CORRECT_SCORE);
        }

        betOffer.setUtcEncounter(marketCatalogue.getUtcEncounter());
        betOffer.setInPlay(marketCatalogue.getMarketBook().getInplay());
        return betOffer;
    }

    private List<Outcome> createOutcomes(MarketCatalogue marketCatalogue, String home, String away, BetOfferTypeEnum betOfferType) throws OutcomeException {
        List<Outcome> outcomes = new ArrayList<>();
        List<RunnerCatalog> runners = marketCatalogue.getRunners();
        for (RunnerCatalog runnerCatalog : runners) {
            Outcome outcome = createOutcome(runnerCatalog, home, away, betOfferType);
            outcomes.add(outcome);
        }
        outcomes = FactoryBetOfferOutcomeItem.cleanOutcomeNames(betOfferType, outcomes);
        return outcomes;
    }

    private Outcome createOutcome(RunnerCatalog runnerCatalog, String home, String away, BetOfferTypeEnum betOfferType) throws OutcomeException {
        Outcome outcome = new Outcome();
        outcome.setSource(Source.BETFAIR.getName());
        outcome.setID(runnerCatalog.getSelectionId());
        outcome.setName(runnerCatalog.getRunnerName());
        outcome.setItem(FactoryBetOfferOutcomeItem.parseOutcomeItem(betOfferType, home, away, runnerCatalog.getRunnerName()));
        return outcome;
    }

    private List<Price> createPrices(MarketBook marketBook) {
        List<Price> prices = new ArrayList<>();
        boolean isLive = marketBook.getInplay();
        List<Runner> runners = marketBook.getRunners();
        PriceSize priceSize;
        Price price;
        double totalAmountMatchedForSelection;

        for (Runner runner : runners) {
            priceSize = getBestPrice(true, runner.getEx().getAvailableToBack());

            //If there is no money on the price then priceSize will be null
            if (priceSize != null) {
                totalAmountMatchedForSelection = getTotalAmountMatchedForSelection(priceSize, runner.getEx().getTradedVolume());
                price = createPrice(runner.getSelectionId(), priceSize.getPrice(), totalAmountMatchedForSelection, priceSize.getSize(), PriceEnum.BACK, marketBook.getUtcEncounter(), isLive);
                prices.add(price);
            }

            priceSize = getBestPrice(false, runner.getEx().getAvailableToLay());
            if (priceSize != null) {
                totalAmountMatchedForSelection = getTotalAmountMatchedForSelection(priceSize, runner.getEx().getTradedVolume());
                price = createPrice(runner.getSelectionId(), priceSize.getPrice(), totalAmountMatchedForSelection, priceSize.getSize(), PriceEnum.LAY, marketBook.getUtcEncounter(), isLive);
                prices.add(price);
            }
        }
        return prices;
    }

    private double getTotalAmountMatchedForSelection(PriceSize price, List<PriceSize> tradedVolumes) {
        for (PriceSize priceSize : tradedVolumes) {
            if (price.getPrice() == priceSize.getPrice().doubleValue()) {
                return priceSize.getSize();
            }
        }
        return 0;
    }

    private Price createPrice(long selectionId, double price, double totalAmountMatched, double amountAvailable, PriceEnum priceType,
            Date utcEncounter, boolean isLive) {
        Price priceObj = new Price();
        priceObj.setCurrency(currency);
        priceObj.setReadReason(readReason);
        priceObj.setMinuteWeight(minuteWeight);
        priceObj.setSource(Source.BETFAIR.getName());
        priceObj.setId(selectionId);
        priceObj.setType(priceType);
        priceObj.setPrice(price);
        priceObj.setAmountAvailable(amountAvailable);
        priceObj.setUtcEncounter(utcEncounter);
        priceObj.setVolumeMatched(totalAmountMatched);
        priceObj.setInPlay(isLive);
        return priceObj;
    }

    private PriceSize getBestPrice(boolean isBack, List<PriceSize> prices) {
        PriceSize bestPrice = null;
        for (PriceSize price : prices) {
            if (bestPrice != null) {
                if (isBack) {
                    if (price.getPrice() > bestPrice.getPrice()) {
                        bestPrice = price;
                    }
                } else {
                    if (price.getPrice() < bestPrice.getPrice()) {
                        bestPrice = price;
                    }
                }
            } else {
                bestPrice = price;
            }
        }
        return bestPrice;
    }

    //If prices doesn't exist for an outcome, the outcome will not be added to the final list
    private void setPriceOnOutcome(List<Outcome> outcomes, List<Price> prices) {

        /**
         * When doing an update one of the prices on an outcome might be
         * missing. Therefore it is needed to create a new outcomes list so that
         * only existing outcomes and prices are returned
         */
        List<Outcome> outcomesResult = new ArrayList<>();

        for (Outcome outcome : outcomes) {
            Price back = getPrice(PriceEnum.BACK, outcome.getExternalKey(), prices);
            if (back != null) {
                outcome.addPrice(back);
            }
            Price lay = getPrice(PriceEnum.LAY, outcome.getExternalKey(), prices);
            if (lay != null) {
                outcome.addPrice(lay);
            }

            if (back != null || lay != null) {
                outcomesResult.add(outcome);
            }
        }
        outcomes.clear();
        outcomes.addAll(outcomesResult);
    }

    private Price getPrice(PriceEnum priceType, long selectionId, List<Price> prices) {
        for (Price price : prices) {
            if (price.getId() == selectionId && price.getType().equals(priceType)) {
                return price;
            }
        }
        return null;
    }

    private void setPaybackAndProbForPrices(List<Outcome> outcomes) {
        Iterator<Outcome> iteratorOutcome = outcomes.iterator();
        Outcome outcome;
        while (iteratorOutcome.hasNext()) {

            outcome = iteratorOutcome.next();
            List<Price> prices = outcome.getPrices();

            //Payback can only be calculated if there is a back and a lay
            //price, else payback is 0. Here we assume there are only two prices,
            //the best back price and best lay price.
            if (prices.size() == 2) {
                Iterator<Price> iteratorPrice = prices.iterator();
                Price priceOne = iteratorPrice.next();
                Price priceTwo = iteratorPrice.next();

                if (priceOne.getType().equals(PriceEnum.BACK)) {
                    populatePaybackAndProb(priceOne, priceTwo);
                } else {
                    populatePaybackAndProb(priceTwo, priceOne);
                }
            }
        }
    }

    private void populatePaybackAndProb(Price backObject, Price layObject) {
        double pbBack;
        double pbLay;
        double back = backObject.getPrice();
        double lay = layObject.getPrice();
        double probBack;
        double probLay;
        double prob;

        if (back > 1 && lay > 1) {
            probBack = 1 / back;
            probLay = 1 / lay;
            prob = (probBack + probLay) / 2;
            pbBack = prob * back;
            pbLay = prob * lay;

            backObject.setPayback(pbBack);
            backObject.setProbability(pbBack / backObject.getPrice());
            layObject.setPayback(pbLay);
            layObject.setProbability(pbLay / layObject.getPrice());
        }
    }
}
