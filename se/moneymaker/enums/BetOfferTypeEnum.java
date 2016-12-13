package se.moneymaker.enums;

public enum BetOfferTypeEnum {

    MATCH_ODDS("1X2", "GOALS", "RT"),
    HALF_TIME("1X2", "GOALS", "P1"),
    OU_W("OU-W", "GOALSUM", "RT"),
    OU_Q("OU-Q", "GOALSUM", "RT"),
    OU_H("OU-H", "GOALSUM", "RT"),
    CORRECT_SCORE("CS-O", "GOALS", "RT"),
    CORRECT_SCORE2("CS-G", "GOALS", "RT");

    private final String name;
    private final String occurrenceCounter;
    private final String matchTimeInterval;

    BetOfferTypeEnum(String name,
            String occurrenceCounter,
            String matchTimeInterval) {
        this.name = name;
        this.occurrenceCounter = occurrenceCounter;
        this.matchTimeInterval = matchTimeInterval;
    }

    public String getName() {
        return name;
    }

    public String getOccurrenceCounter() {
        return occurrenceCounter;
    }

    public String getMatchTimeInterval() {
        return matchTimeInterval;
    }
}
