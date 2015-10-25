package se.betfair.exception;

/*
 * 
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author          Comments
 *------------------------------------------------------------------------------
 * 1.0          2012-jan-04  Baran Sölen     Initial version
 */
public class BEXMarketExcludedException extends Exception {

    public BEXMarketExcludedException() {
        super("Market excluded");
    }

    public BEXMarketExcludedException(String message) {
        super(message);
    }
}
