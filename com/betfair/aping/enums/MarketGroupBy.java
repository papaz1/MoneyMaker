package com.betfair.aping.enums;

public enum MarketGroupBy {

    MARKET("groupBy");

    final String key;

    private MarketGroupBy(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
