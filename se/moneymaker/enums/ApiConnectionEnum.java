package se.moneymaker.enums;

/*
 *
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 Apr 24, 2011 Baran Sölen Initial version
 */
public enum ApiConnectionEnum {

    POST("POST"),
    GET("GET");
    private final String operation;

    private ApiConnectionEnum(String operation) {
        this.operation = operation;
    }

    public String value() {
        return operation;
    }
}
