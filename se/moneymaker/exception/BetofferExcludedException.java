package se.moneymaker.exception;

public class BetofferExcludedException extends Exception {

    public BetofferExcludedException() {
        super("Market excluded due to filter");
    }

    public BetofferExcludedException(String message) {
        super(message);
    }
}
