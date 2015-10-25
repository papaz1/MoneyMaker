package se.moneymaker.enums;

/*
 *
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 Jun 4, 2012 Baran Sölen Initial version
 */
public enum BetStateEnum {

    UNMATCHED("UNMATCHED"),
    PENDING("PENDING"),
    SETTLED("SETTLED");
    final String state;

    BetStateEnum(String state) {
        this.state = state;
    }

    public String value() {
        return state;
    }
}
