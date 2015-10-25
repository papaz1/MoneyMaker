package se.moneymaker.exception;

public class OutcomeException extends TemplateException {

    public OutcomeException(String msg) {
        super(msg);
    }

    public OutcomeException(String msg, ErrorType errorType) {
        super(msg, errorType);
    }
}
