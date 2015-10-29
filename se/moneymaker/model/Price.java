package se.moneymaker.model;

import se.moneymaker.enums.PriceEnum;
import java.util.Date;
import se.moneymaker.enums.ReadReason;

public class Price {

    private long id;
    private double price;
    private double amountAvailable;
    private int numberOfTransactions;
    private Date firstTakenDate;
    private Date latestTakenDate;
    private Date utcEncounter;
    private boolean inPlay;
    private double volumeMatched;
    private double payback;
    private double probability;
    private PriceEnum type;
    private String source;
    private ReadReason readReason;
    private double minuteWeight;
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getMinuteWeight() {
        return minuteWeight;
    }

    public void setMinuteWeight(double minuteWeight) {
        this.minuteWeight = minuteWeight;
    }

    public ReadReason getReadReason() {
        return readReason;
    }

    public void setReadReason(ReadReason readReason) {
        this.readReason = readReason;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public PriceEnum getType() {
        return type;
    }

    public void setType(PriceEnum type) {
        this.type = type;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public double getPayback() {
        return payback;
    }

    public void setPayback(double payback) {
        this.payback = payback;
    }

    public double getVolumeMatched() {
        return volumeMatched;
    }

    public void setVolumeMatched(double volumeMatched) {
        this.volumeMatched = volumeMatched;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public void setAmountAvailable(double amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public Date getFirstTakenDate() {
        return firstTakenDate;
    }

    public void setFirstTakenDate(Date firstTakenDate) {
        this.firstTakenDate = firstTakenDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isInPlay() {
        return inPlay;
    }

    public void setInPlay(boolean inPlay) {
        this.inPlay = inPlay;
    }

    public Date getLatestTakenDate() {
        return latestTakenDate;
    }

    public void setLatestTakenDate(Date latestTakenDate) {
        this.latestTakenDate = latestTakenDate;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double odds) {
        this.price = odds;
    }

    public Date getUtcEncounter() {
        return utcEncounter;
    }

    public void setUtcEncounter(Date utcEncounter) {
        this.utcEncounter = utcEncounter;
    }
}
