package se.betfair.exception;

/*
 * Thrown when login or logout fails
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          May 22, 2011      Baran Sölen    Initial version
 */
public class BEXAccountException extends Exception {

    public BEXAccountException() {
        super("Account exception");
    }

    public BEXAccountException(String message) {
        super(message);
    }
}
