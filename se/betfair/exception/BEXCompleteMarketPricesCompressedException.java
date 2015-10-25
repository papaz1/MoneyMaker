package se.betfair.exception;

/*
 * Thrown if service getting the complete market prices compressed fails
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-feb-24      Baran Sölen    Initial version
 */
public class BEXCompleteMarketPricesCompressedException extends Exception {

    public BEXCompleteMarketPricesCompressedException() {
        super("Error fetching the complete market prices.");
    }

    public BEXCompleteMarketPricesCompressedException(String message) {
        super(message);
    }
}
