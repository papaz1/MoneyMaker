package se.moneymaker.exception;

public class DBConnectionException extends Exception {

    private final String DEFAULT_ERRORTYPE = "DEFAULT_ERROR_TYPE"; //If DB doesn't provide an error type
    private String errorType;
    private String request;

    public DBConnectionException() {
        super("Failed to insert to DB.");
    }

    public DBConnectionException(String message) {
        super(message);
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorType() {
        if (errorType == null) {
            return DEFAULT_ERRORTYPE;
        }
        return errorType;
    }
}
