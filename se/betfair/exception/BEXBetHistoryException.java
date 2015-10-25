package se.betfair.exception;

/*
 * 
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author          Comments
 *------------------------------------------------------------------------------
 * 1.0          2012-mar-10  Baran Sölen     Initial version
 */
public class BEXBetHistoryException extends Exception {

    public BEXBetHistoryException() {
        super("Error getting bet history");
    }

    public BEXBetHistoryException(String message) {
        super(message);
    }
}
