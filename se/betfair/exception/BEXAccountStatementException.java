package se.betfair.exception;

/*
 * 
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author          Comments
 *------------------------------------------------------------------------------
 * 1.0          2012-feb-12  Baran Sölen     Initial version
 */
public class BEXAccountStatementException extends Exception {

    public BEXAccountStatementException() {
        super("Account statement exception");
    }

    public BEXAccountStatementException(String message) {
        super(message);
    }
}
