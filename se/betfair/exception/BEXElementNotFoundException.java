package se.betfair.exception;

/*
 * 
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-03-18   Baran Sölen    Initial version
 */
public class BEXElementNotFoundException extends Exception {

    public BEXElementNotFoundException() {
        super("Element not found.");
    }

    public BEXElementNotFoundException(String message) {
        super(message);
    }
}
