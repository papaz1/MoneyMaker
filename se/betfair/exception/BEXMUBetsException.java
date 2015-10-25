package se.betfair.exception;

/*
 *
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          Feb 1, 2012  Baran Sölen    Initial version
 */
public class BEXMUBetsException extends Exception {

    public BEXMUBetsException() {
        super("Error getting matched and unmatched bets");
    }

    public BEXMUBetsException(String message) {
        super(message);
    }
}
