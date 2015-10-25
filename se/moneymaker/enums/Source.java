package se.moneymaker.enums;

public enum Source {

    BETFAIR("betfair.com"), 
    PINNACLE("pinnacle");
    private final String source;

    private Source(String source) {
        this.source = source;
    }

    public String getName() {
        return source;
    }
}
