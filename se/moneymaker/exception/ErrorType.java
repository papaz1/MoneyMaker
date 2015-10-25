package se.moneymaker.exception;

public enum ErrorType {

    DEFAULT("errorType not set"),
    NO_SESSION("Session no longer valid"),
    UNKNOWN_ERROR("Unknown error"),
    BETPROVER_ERROR(""),
    BETFAIR_ERROR(""),
    FILTER("Object/value removed due to filter"),
    INVALID_BET_SIZE("Invalid bet size"),
    MARKET_SUSPENDED(""),
    BET_CANCELLATION_ERROR("");
    private final String msg;

    private ErrorType(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

}
