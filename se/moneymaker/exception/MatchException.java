package se.moneymaker.exception;

public class MatchException extends Exception {

    public MatchException() {
        super("Match exception");
    }

    public MatchException(String message) {
        super(message);
    }
}
