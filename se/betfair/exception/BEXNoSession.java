package se.betfair.exception;

/*
 *
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 May 3, 2013 Baran Sölen Initial version
 */
public class BEXNoSession extends Exception {

    public BEXNoSession() {
        super("No Betfair session");
    }

    public BEXNoSession(String message) {
        super(message);
    }
}
