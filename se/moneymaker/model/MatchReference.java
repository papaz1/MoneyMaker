package se.moneymaker.model;

public class MatchReference {

    private MatchReferenceInfo home;
    private MatchReferenceInfo away;
    private MatchReferenceInfo pool;
    private MatchReferenceInfo event;

    public MatchReferenceInfo getHome() {
        return home;
    }

    public void setHome(MatchReferenceInfo home) {
        this.home = home;
    }

    public MatchReferenceInfo getAway() {
        return away;
    }

    public void setAway(MatchReferenceInfo away) {
        this.away = away;
    }

    public MatchReferenceInfo getPool() {
        return pool;
    }

    public void setPool(MatchReferenceInfo pool) {
        this.pool = pool;
    }

    public MatchReferenceInfo getEvent() {
        return event;
    }

    public void setEvent(MatchReferenceInfo event) {
        this.event = event;
    }

}
