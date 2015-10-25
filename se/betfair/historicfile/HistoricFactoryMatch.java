package se.betfair.historicfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import se.betfair.dict.DictOutcomesWinners;
import se.betfair.exception.BEXElementNotFoundException;
import se.betfair.exception.BEXNoSession;
import se.betfair.factory.FactoryBetOfferOutcomeItem;
import se.betfair.model.BMHistoricRunner;
import se.betfair.model.Market;
import se.moneymaker.enums.BetOfferTypeEnum;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.OutcomeException;
import se.moneymaker.util.Utils;
import se.moneymaker.enums.Operation;
import se.moneymaker.enums.OutcomeResultEnum;
import se.moneymaker.enums.PriceEnum;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.BetOfferItem;
import se.moneymaker.model.Match;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.Price;

public class HistoricFactoryMatch {

    private static final int SCALE = 4;

    public List<Match> createMatches(List<Market> markets, Operation operation) throws BetOfferException, BEXNoSession {
        List<Match> matches = new ArrayList<>();
        Match match = null;
        BetOffer betOffer;

        //Sorting is done so that the markets can be grouped together as
        //matches. The assumption being made when doing the sorting below is that
        //the markets list is unique with regards to the hierarchy path of the markets. This means that
        //a market that has been played in more than one occasion can't occur more than once else
        //a game, Barcelona vs Real Madrid OVER UNDER 2.5 GOALS, that has been played on two different
        //occasions will be grouped as the same market (since the hierarchy path will occur more than once)
        //in the list) which they aren't.
        Collections.sort(markets, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                Market m1 = (Market) o1;
                Market m2 = (Market) o2;

                String marketId1 = Integer.toString(m1.getId());
                String marketId2 = Integer.toString(m2.getId());

                return marketId1.compareToIgnoreCase(marketId2);
            }
        });

        if (markets != null) {
            Market market1 = null;
            Market market2 = null;
            Iterator<Market> iterator = markets.iterator();
            boolean first = true;

            //Now continue with the rest of the markets
            while (iterator.hasNext()) {

                //The first match will always be created
                if (first) {
                    market1 = iterator.next();

                    //This is the market of Betfair translated into a betoffer in
                    //MoneyMaker model
                    betOffer = createBetOffer(market1, operation);
                    match = createMatch(market1);
                    match.addBetOffer(betOffer);
                    matches.add(match);

                    first = false;
                }

                //The first one has to be created before anything else, because else an exception
                //has been thrown, that's why !first condition is in the if statement
                if (!first && iterator.hasNext()) {
                    market2 = iterator.next();

                    //If market1 = market2 do not create a new match but instead
                    //add the market data from market2 to an existing match
                    if (!first && market1.getHierarchyTextPathNoFix2().equalsIgnoreCase(market2.getHierarchyTextPathNoFix2())) {

                        betOffer = createBetOffer(market2, operation);
                        match.addBetOffer(betOffer);

                    } else {
                        betOffer = createBetOffer(market2, operation);
                        match = createMatch(market2);

                        match.addBetOffer(betOffer);
                        matches.add(match);
                    }
                }
                market1 = market2;
            }
        }
        return matches;
    }

    protected Match createMatch(Market market) {
        Match match = new Match();
        match.setHome(market.getHome());
        match.setAway(market.getAway());
        match.setEventDate(market.getEventDate());

        return match;
    }

    protected BetOffer createBetOffer(Market market, Operation operation) throws BetOfferException {
        BetOffer betOffer = new BetOffer();
        betOffer.setSource(Source.BETFAIR.getName());
        betOffer.setExternalKey(Integer.toString(market.getId()));
        betOffer.setName(market.getName());
        betOffer.setItem(FactoryBetOfferOutcomeItem.parseBetOfferItem(market.getName(), market.getHome(), market.getAway()));

        List<Outcome> outcomes;
        try {
            if (containsYouthOrWomensSoccer(market.getHome())) {
                throw new BetOfferException("Youth or womens soccer: " + market.getHome() + " vs " + market.getAway(), ErrorType.FILTER);
            }
            outcomes = createOutcomesIncludingPrices(market.getId(), betOffer.getItem().getType(), market.getHome(), market.getAway(), operation);
            DictOutcomesWinners dict = DictOutcomesWinners.getInstance();
            BetOfferItem item = betOffer.getItem();
            try {
                int numberOfOutcomes = dict.getNumberOfOutcomes(item.getType().getName());
                if (numberOfOutcomes != outcomes.size()) {
                    throw new BetOfferException("Incomplete number of outcomes", ErrorType.FILTER);
                }
            } catch (BEXElementNotFoundException e) {
            }

            double volumeMatchedBetOffer = calculateVolumeMatched(outcomes);
            betOffer.setVolumeMatched(volumeMatchedBetOffer);
            outcomes = filterPrematchPrices(outcomes);
            if (!outcomesHavePrices(outcomes)) {
                throw new BetOfferException("One or more outcomes are missing prematch prices", ErrorType.FILTER);
            }
            outcomes = filterBiggestVolumeMatched(outcomes);
        } catch (OutcomeException e) {
            throw new BetOfferException(e.getMessage() + ". Market name: " + market.getName());
        }

        betOffer.setMaxNumberOfOutcomes(market.getNumberOfRunners());
        betOffer.setOutcomes(outcomes);
        populatePriceProb(betOffer);

        //betOffer = (new HistoricPaybackCalculator()).populateApproximatePayback(betOffer);
        return betOffer;
    }

    protected List<Outcome> createOutcomesIncludingPrices(int marketId, BetOfferTypeEnum betOfferType, String home, String away, Operation operation) throws OutcomeException {
        List<BMHistoricRunner> historicRunners = HistoricDataReader.getHistoricRunners(marketId);
        List<Outcome> outcomes = new ArrayList<>();
        Iterator<BMHistoricRunner> iterator = historicRunners.iterator();
        BMHistoricRunner historicRunner1;
        BMHistoricRunner historicRunner2;
        Outcome outcome;
        Price price;
        if (iterator.hasNext()) {
            historicRunner1 = iterator.next();
            outcome = createOutcome(betOfferType, home, away, historicRunner1);
            price = createPrice(historicRunner1);
            outcome.addPrice(price);

            if (iterator.hasNext()) {
                while (iterator.hasNext()) {
                    historicRunner2 = iterator.next();

                    //If they are same just add the price else create a new outcome
                    if (historicRunner1.getSelectionId().equalsIgnoreCase(historicRunner2.getSelectionId())) {
                        price = createPrice(historicRunner2);
                        outcome.addPrice(price);
                    } else {
                        outcomes.add(outcome);
                        outcome = createOutcome(betOfferType, home, away, historicRunner2);
                        price = createPrice(historicRunner2);
                        outcome.addPrice(price);
                    }

                    if (!iterator.hasNext()) {
                        outcomes.add(outcome);
                    }

                    historicRunner1 = historicRunner2;
                }
            } else {
                outcomes.add(outcome);
            }
        }
        try {
            FactoryBetOfferOutcomeItem.cleanOutcomeNames(betOfferType, outcomes);
        } catch (OutcomeException e) {
            throw new OutcomeException(e.getMessage() + ". Market id: " + marketId + ". Home: " + home + " Away: " + away);
        }
        return outcomes;
    }

    private static Outcome createOutcome(BetOfferTypeEnum betOfferType, String home, String away, BMHistoricRunner historicRunner) throws OutcomeException {
        Outcome outcome = new Outcome();
        outcome.setSource(Source.BETFAIR.getName());
        outcome.setID(Integer.parseInt(historicRunner.getSelectionId()));
        outcome.setName(historicRunner.getSelection());
        outcome.setItem(FactoryBetOfferOutcomeItem.parseOutcomeItem(betOfferType, home, away, historicRunner.getSelection()));
        boolean win = Utils.stringToBoolean(historicRunner.isWin());
        if (win) {
            outcome.setResult(OutcomeResultEnum.WIN);
        } else {
            outcome.setResult(OutcomeResultEnum.LOSS);
        }
        return outcome;
    }

    private static Price createPrice(BMHistoricRunner historicRunner) {
        Price price = new Price();
        price.setSource(Source.BETFAIR.getName());
        price.setId(Integer.parseInt(historicRunner.getSelectionId()));
        price.setPrice(Double.parseDouble(historicRunner.getOdds()));
        price.setType(PriceEnum.MATCHED);
        price.setVolumeMatched(Double.parseDouble(historicRunner.getVolumeMatched()));
        price.setNumberOfTransactions(Integer.parseInt(historicRunner.getNumberBets()));
        price.setInPlay(Utils.stringToBoolean(historicRunner.getInPlay()));
        price.setFirstTakenDate(Utils.stringToDate(historicRunner.getFirstTaken()));
        price.setLatestTakenDate(Utils.stringToDate(historicRunner.getLatestTaken()));
        return price;
    }

    private List<Outcome> filterPrematchPrices(List<Outcome> outcomes) {
        for (Outcome outcome : outcomes) {
            List<Price> prices = outcome.getPrices();
            List<Price> prematchPrices = new ArrayList<>();
            for (Price price : prices) {
                if (!price.isInPlay()) {
                    prematchPrices.add(price);
                }
            }
            outcome.setPrices(prematchPrices);
        }
        return outcomes;
    }

    private List<Outcome> filterBiggestVolumeMatched(List<Outcome> outcomes) {
        for (Outcome outcome : outcomes) {
            List<Price> prices = outcome.getPrices();
            if (!prices.isEmpty()) {
                Price biggestPrice = new Price();
                for (Price price : prices) {
                    if (price.getVolumeMatched() > biggestPrice.getVolumeMatched()) {
                        biggestPrice = price;
                    } else if (price.getVolumeMatched() == biggestPrice.getVolumeMatched()) {
                        if (price.getPrice() > biggestPrice.getPrice()) {
                            biggestPrice = price;
                        }
                    }
                }
                outcome.clearPrices();
                outcome.addPrice(biggestPrice);
            }
        }
        return outcomes;
    }

    private void populatePriceProb(BetOffer betOffer) {
        List<Outcome> outcomes = betOffer.getOutcomes();
        double totalProb = 0;
        for (Outcome outcome : outcomes) {
            List<Price> prices = outcome.getPrices();
            for (Price price : prices) {
                if (price.getPrice() > 1) {
                    totalProb = totalProb + (1 / price.getPrice());
                }
            }
        }
        if (totalProb > 0) {
            for (Outcome outcome : outcomes) {
                List<Price> prices = outcome.getPrices();
                for (Price price : prices) {
                    if (price.getPrice() > 1) {
                        price.setProbability(Utils.parseDouble(SCALE, (1 / price.getPrice()) / totalProb));
                    }
                }
            }
        }
    }

    private double calculateVolumeMatched(List<Outcome> outcomes) {
        double volumeMatched = 0;
        for (Outcome outcome : outcomes) {
            List<Price> prices = outcome.getPrices();
            if (!prices.isEmpty()) {
                for (Price price : prices) {
                    volumeMatched += price.getVolumeMatched();
                }
            }
        }
        return volumeMatched;
    }

    private boolean outcomesHavePrices(List<Outcome> outcomes) {
        for (Outcome outcome : outcomes) {
            List<Price> prices = outcome.getPrices();
            if (prices.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean containsYouthOrWomensSoccer(String team) {
        String uTeam = team.toUpperCase();
        if (uTeam.contains("(W)")
                || uTeam.contains("U1")
                || uTeam.contains("U2")) {
            return true;
        }
        return false;
    }
}
