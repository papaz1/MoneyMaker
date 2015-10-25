package se.moneymaker.exception;

public class BetOfferException extends TemplateException {

    public BetOfferException(String msg) {
        super(msg);
    }

    public BetOfferException(String msg, ErrorType errorType) {
            super(msg, errorType);
    }
}
