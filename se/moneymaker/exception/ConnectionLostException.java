package se.moneymaker.exception;

public class ConnectionLostException extends TemplateException {

    public ConnectionLostException(String msg) {
        super(msg);
    }

    public ConnectionLostException(String msg, ErrorType errorType) {
        super(msg, errorType);
    }
}
