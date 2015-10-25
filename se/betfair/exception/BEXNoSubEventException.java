package se.betfair.exception;
/*
 * This exception should be thrown when the eventid returns no subevents
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 0.1          Nov 12, 2010   Baran    Initial version
 */

public class BEXNoSubEventException extends Exception {

    public BEXNoSubEventException() {
        super("No sub events can be found.");
    }

    public BEXNoSubEventException(String message) {
        super(message);
    }
}
