package se.betfair.exception;

/*
 *  Thrown if service getting active event types fails
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-feb-28  Baran Sölen    Initial version
 */
public class BEXEventException extends Exception {

    public BEXEventException() {
        super("Error fetching event types or events.");
    }

    public BEXEventException(String message) {
        super(message);
    }
}
