package se.moneymaker.exception;

public class NoDataException extends TemplateException {

    public NoDataException(String msg) {
        super(msg);
    }

    public NoDataException(String msg, ErrorType errorType) {
        super(msg, errorType);
    }
}
