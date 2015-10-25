package se.betfair.exception;

/*
 *
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          Jan 23, 2012 Baran Sölen    Initial version
 */
public class BEXGetBetException extends Exception {

    public static final String BET_ID_INVALID = "BET_ID_INVALID";

    public BEXGetBetException() {
        super("Error getting information regarding bet");
    }

    public BEXGetBetException(String message) {
        super(message);
    }
}
