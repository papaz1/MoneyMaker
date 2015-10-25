package se.betfair.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.betfair.enums.EventTypeEnum;

public class BetfairCondition {

    private EventTypeEnum eventType;
    private List<BCMarketEnum> menuPathFilterInclude;
    private List<BCMarketEnum> marketNameFilterInclude;
    private Date eventDate;
    private double totalAmountMatched;
    private boolean prematchOnly;

    public boolean isPrematchOnly() {
        return prematchOnly;
    }

    public void setPrematchOnly(boolean prematchOnly) {
        this.prematchOnly = prematchOnly;
    }

    public BetfairCondition() {
        menuPathFilterInclude = new ArrayList<>();
        marketNameFilterInclude = new ArrayList<>();
    }

    public List<BCMarketEnum> getMarketNameFilterInclude() {
        return marketNameFilterInclude;
    }

    public void addMarketNameFilterInclude(BCMarketEnum condition) {
        marketNameFilterInclude.add(condition);
    }

    public void setTotalAmountMatched(double totalAmount) {
        this.totalAmountMatched = totalAmount;
    }

    public void setEventDateFrom(Date eventDate) {
        this.eventDate = eventDate;
    }

    public void setEventType(EventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public void addMenuPathFilterInclude(BCMarketEnum condition) {
        menuPathFilterInclude.add(condition);
    }

    public List<BCMarketEnum> getMenuPathFilterInclude() {
        return menuPathFilterInclude;
    }

    public EventTypeEnum getEventType() {
        return eventType;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public double getTotalAmountMatched() {
        return totalAmountMatched;
    }
}
