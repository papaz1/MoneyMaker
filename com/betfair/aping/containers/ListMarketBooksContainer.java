package com.betfair.aping.containers;

import java.util.ArrayList;
import java.util.List;
import se.betfair.model.MarketBook;

public class ListMarketBooksContainer extends Container {

    private List<MarketBook> result;

    public ListMarketBooksContainer() {
        result = new ArrayList();
    }

    public List<MarketBook> getResult() {
        return result;
    }

    public void setResult(List<MarketBook> result) {
        this.result = result;
    }

    public void addResult(List<MarketBook> result) {
        this.result.addAll(result);
    }
}
