package se.moneymaker.enums;

/*
 * 
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author          Comments
 *------------------------------------------------------------------------------
 * 1.0          2012-jul-05      Baran Sölen     Initial version
 */
public enum OutcomeResultEnum {

    WIN("WIN"),
    LOSS("LOSS");
    final String value;

    OutcomeResultEnum(String value) {
        this.value = value;

    }

    public String value() {
        return value;
    }
}
