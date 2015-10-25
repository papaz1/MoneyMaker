package com.betfair.aping.enums;

public enum ApiNgOperation {

    GET_ACCOUNT_FUNDS("getAccountFunds", "AccountAPING/v1.0/"),
    GET_ACCOUNT_STATEMENT("getAccountStatement", "AccountAPING/v1.0/"),
    LISTCLEAREDORDERS("listClearedOrders", "SportsAPING/v1.0/"),
    LISTCURRENTORDERS("listCurrentOrders", "SportsAPING/v1.0/"),
    LISTEVENTTYPES("listEventTypes", "SportsAPING/v1.0/"),
    LISTCOMPETITIONS("listCompetitions", "SportsAPING/v1.0/"),
    LISTTIMERANGES("listTimeRanges", "SportsAPING/v1.0/"),
    LISTEVENTS("listEvents", "SportsAPING/v1.0/"),
    LISTMARKETTYPES("listMarketTypes", "SportsAPING/v1.0/"),
    LISTCOUNTRIES("listCountries", "SportsAPING/v1.0/"),
    LISTVENUES("listVenues", "SportsAPING/v1.0/"),
    LISTMARKETCATALOGUE("listMarketCatalogue", "SportsAPING/v1.0/"),
    LISTMARKETBOOK("listMarketBook", "SportsAPING/v1.0/"),
    CANCELORDERS("cancelOrders", "SportsAPING/v1.0/"),
    PLACEORDERS("placeOrders", "SportsAPING/v1.0/");

    private final String operationName;
    private final String operationGroup;

    private ApiNgOperation(String operationName, String operationGroup) {
        this.operationName = operationName;
        this.operationGroup = operationGroup;
    }

    public String getOperationName() {
        return operationName;
    }

    public String getOperationGroup() {
        return operationGroup;
    }
}
