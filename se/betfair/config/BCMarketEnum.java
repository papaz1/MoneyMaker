package se.betfair.config;

/*
 * Enum for market
 * There are two constants here only used for the menupath filter and not
 * for filtering markets. FIXTURES and MATCHES are only for menupath filtering.
 * The rest is for filtering markets.
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 2011-apr-16 Baran Sölen Initial version
 */
public enum BCMarketEnum {

    FIRST_GOALSCORER("FIRST GOALSCORER", false, false, true),
    TO_SCORE("TO SCORE", true, false, true),
    WINCAST("WINCAST", false, false, true),
    SCORECAST("SCORECAST", false, false, true),
    OVER_UNDER("OVER/UNDER", false, true, false),
    MATCH_ODDS("MATCH ODDS", true, false, false),
    HALF_TIME("HALF TIME", true, false, false),
    CORRECT_SCORE("CORRECT SCORE", true, false, false),
    //The two below are not for market filtering
    FIXTURES("FIXTURES", false, false, true),
    MATCHES("MATCHES", false, false, true);
    private final String value;
    private final boolean isEqual; //If this is true then .equals will be checked instead of .contains
    private final boolean beginsWith;
    private final boolean couldContainTeamNameInMarketName;

    BCMarketEnum(String value, boolean isEqual, boolean beginsWith, boolean couldContainTeamNameInMarketName) {
        this.value = value;
        this.isEqual = isEqual;
        this.beginsWith = beginsWith;
        this.couldContainTeamNameInMarketName = couldContainTeamNameInMarketName;
    }

    public String value() {
        return value;
    }

    public boolean isEqual() {
        return isEqual;
    }

    public boolean beginsWith() {
        return beginsWith;
    }

    public boolean couldContainTeamNameInMarketName() {
        return couldContainTeamNameInMarketName;
    }
}
