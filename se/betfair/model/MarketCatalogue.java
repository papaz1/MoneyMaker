package se.betfair.model;

import java.util.Date;
import java.util.List;

public class MarketCatalogue {

    private Date utcEncounter;
    private String marketId;
    private String marketName;
    private Date marketStartTime;
    private MarketDescription description;
    private List<RunnerCatalog> runners = null;
    private EventType eventType;
    private Competition competition;
    private Event event;
    private MarketBook marketBook;

    public MarketCatalogue() {
        utcEncounter = new Date();
    }

    public Date getUtcEncounter() {
        return utcEncounter;
    }

    public MarketBook getMarketBook() {
        return marketBook;
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
    }

    public Date getMarketStartTime() {
        return marketStartTime;
    }

    public void setMarketStartTime(Date marketStartTime) {
        this.marketStartTime = marketStartTime;
    }

    public String getMarketId() {
        return marketId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public MarketDescription getDescription() {
        return description;
    }

    public void setDescription(MarketDescription description) {
        this.description = description;
    }

    public List<RunnerCatalog> getRunners() {
        return runners;
    }

    public void setRunners(List<RunnerCatalog> runners) {
        this.runners = runners;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Competition getCompetition() {
        return competition;
    }

    public void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String toString() {
        return getMarketName();
    }

}
