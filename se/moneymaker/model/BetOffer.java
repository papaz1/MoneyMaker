package se.moneymaker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class BetOffer {

    private String externalKey;
    private String name;
    private boolean inPlay;
    private BetOfferItem item;
    private List<Outcome> outcomes = new ArrayList<>();
    private Map insertedOutcomes;
    private double volumeMatched;
    private double payback;
    private boolean possibleNoBet;
    private boolean possiblePush;
    private int maxNumberOfOutcomes;
    private Date utcEncounter;
    long Pk;
    private String source;
    private Date eventDate;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getPk() {
        return Pk;
    }

    public void setPk(long Pk) {
        this.Pk = Pk;
    }

    public int getMaxNumberOfOutcomes() {
        return maxNumberOfOutcomes;
    }

    public void setUtcEncounter(Date utcEncounter) {
        this.utcEncounter = utcEncounter;
    }

    public Date getUtcEncounter() {
        return utcEncounter;
    }

    public void setMaxNumberOfOutcomes(int maxNumberOfOutcomes) {
        this.maxNumberOfOutcomes = maxNumberOfOutcomes;
    }

    public boolean isPossiblePush() {
        return possiblePush;
    }

    public void setPossiblePush(boolean possiblePush) {
        this.possiblePush = possiblePush;
    }

    public boolean isPossibleNoBet() {
        return possibleNoBet;
    }

    public void setPossibleNoBet(boolean possibleNoBet) {
        this.possibleNoBet = possibleNoBet;
    }

    public double getPayback() {
        return payback;
    }

    public void setPayback(double payback) {
        this.payback = payback;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setItem(BetOfferItem item) {
        this.item = item;
    }

    public void setInPlay(boolean inPlay) {
        this.inPlay = inPlay;
    }

    public void setOutcomes(List<Outcome> outcomes) {
        this.outcomes = outcomes;
    }

    public void addOutcome(Outcome outcome) {
        outcomes.add(outcome);
    }

    public void setInsertedOutcomes(Map insertedOutcomes) {
        this.insertedOutcomes = insertedOutcomes;
    }

    public void setVolumeMatched(double volumeMatched) {
        this.volumeMatched = volumeMatched;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public String getName() {
        return name;
    }

    public BetOfferItem getItem() {
        return item;
    }

    public boolean isInPlay() {
        return inPlay;
    }

    public List<Outcome> getOutcomes() {
        return outcomes;
    }

    public Map getInsertedOutcomes() {
        return insertedOutcomes;
    }

    public double getVolumeMatched() {
        return volumeMatched;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }
}
