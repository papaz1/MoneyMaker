package se.betfair.exception;

/*
 * Thrown if service getting the market prices compressed fails
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-feb-24      Baran Sölen    Initial version
 */
public class BEXMarketPricesCompressedException extends Exception {

    public BEXMarketPricesCompressedException() {
        super("Error fetching the market prices.");
    }

    public BEXMarketPricesCompressedException(String message) {
        super(message);
    }
}
