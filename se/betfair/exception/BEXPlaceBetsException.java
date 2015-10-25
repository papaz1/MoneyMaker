package se.betfair.exception;

/*
 *  
 * Thrown if service placing bets fails
 * 
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-feb-28      Baran Sölen    Initial version
 */
public class BEXPlaceBetsException extends Exception {

    public BEXPlaceBetsException() {
        super("Error placing one or more bets.");
    }

    public BEXPlaceBetsException(String message) {
        super(message);
    }
}
