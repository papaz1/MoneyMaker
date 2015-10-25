package se.betfair.exception;

/*
 *
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          Jan 18, 2012 Baran Sölen    Initial version
 */
public class BEXCancelBetsException extends Exception {

    public BEXCancelBetsException() {
        super("Error cancelling one or more bets.");
    }

    public BEXCancelBetsException(String message) {
        super(message);
    }
}
