package se.moneymaker.model;

import se.moneymaker.enums.OutcomeResultEnum;
import java.util.ArrayList;
import java.util.List;

public class Outcome {

    private String name;
    private long externalKey;
    private List<Price> prices;
    private OutcomeResultEnum win;
    private double handicap;
    private boolean isIdentified;
    private OutcomeItem item;
    private long pk;
    private String source;

    public Outcome() {
        prices = new ArrayList<>();
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public void setItem(OutcomeItem item) {
        this.item = item;
    }

    public OutcomeItem getItem() {
        return item;
    }

    public void setIdentified(boolean isIdentified) {
        this.isIdentified = isIdentified;
    }

    public boolean isIdentified() {
        return isIdentified;
    }

    public double getHandicap() {
        return handicap;
    }

    public void setHandicap(double handicap) {
        this.handicap = handicap;
    }

    public OutcomeResultEnum getResult() {
        return win;
    }

    public void setResult(OutcomeResultEnum win) {
        this.win = win;
    }

    public void setExternalKey(long externalKey) {
        this.externalKey = externalKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPrice(Price price) {
        prices.add(price);
    }

    public void clearPrices() {
        prices.clear();
    }

    public long getExternalKey() {
        return externalKey;
    }

    public String getName() {
        return name;
    }

    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }
}
