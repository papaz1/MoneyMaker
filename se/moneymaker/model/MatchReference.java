package se.moneymaker.model;

public class MatchReference {

    private MatchReferenceInfo homeTeamRef;
    private MatchReferenceInfo awayTeamRef;
    private MatchReferenceInfo poolRef;
    private MatchReferenceInfo eventRef;

    public MatchReferenceInfo getHome() {
        return homeTeamRef;
    }

    public void setHome(MatchReferenceInfo home) {
        this.homeTeamRef = home;
    }

    public MatchReferenceInfo getAway() {
        return awayTeamRef;
    }

    public void setAway(MatchReferenceInfo away) {
        this.awayTeamRef = away;
    }

    public MatchReferenceInfo getPool() {
        return poolRef;
    }

    public void setPool(MatchReferenceInfo pool) {
        this.poolRef = pool;
    }

    public MatchReferenceInfo getEvent() {
        return eventRef;
    }

    public void setEvent(MatchReferenceInfo event) {
        this.eventRef = event;
    }

}
