package se.moneymaker.exception;

public class AccountException extends Exception{
    public AccountException() {
        super("Login-logout failed.");
    }

    public AccountException(String message) {
        super(message);
    }
}
