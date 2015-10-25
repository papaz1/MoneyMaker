package se.moneymaker.enums;

/*
 * Enum for price
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author          Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-apr-16  Baran Sölen     Initial version
 */
public enum PriceEnum {

    BACK(1),
    LAY(2),
    MATCHED(3);
    private final int type;

    PriceEnum(int type) {
        this.type = type;
    }

    public int value() {
        return type;
    }
}
