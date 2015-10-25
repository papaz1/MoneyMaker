package se.betfair.model;

import java.util.List;

public class MarketHolder {

    private List<Market> successfullyCreatedMarkets;
    private List<HistoricMarket> erroneousMarkets;

    public MarketHolder(List<Market> successfullyCreatedMarkets, List<HistoricMarket> erroneousMarket) {
        this.successfullyCreatedMarkets = successfullyCreatedMarkets;
        this.erroneousMarkets = erroneousMarket;
    }

    public List<Market> getSuccessfullyCreatedMarkets() {
        return successfullyCreatedMarkets;
    }

    public List<HistoricMarket> getErroneousMarkets() {
        return erroneousMarkets;
    }
}
