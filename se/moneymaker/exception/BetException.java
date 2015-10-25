package se.moneymaker.exception;

public class BetException extends TemplateException {

    public BetException(String msg) {
        super(msg);
    }

    public BetException(String msg, ErrorType errorType) {
        super(msg, errorType);
    }
}
