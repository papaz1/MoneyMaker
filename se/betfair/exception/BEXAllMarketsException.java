package se.betfair.exception;

/*
 * Thrown if service getting all markets fail
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-feb-24      Baran Sölen    Initial version
 */
public class BEXAllMarketsException extends Exception {

    public BEXAllMarketsException() {
        super("Error fetching all markets.");
    }

    public BEXAllMarketsException(String message) {
        super(message);
    }
}
