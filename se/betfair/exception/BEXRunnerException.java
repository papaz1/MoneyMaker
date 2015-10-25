package se.betfair.exception;

/*
 * Thrown if service getting runners fails
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-feb-24      Baran Sölen    Initial version
 */
public class BEXRunnerException extends Exception {

    public BEXRunnerException() {
        super("Error fetching runner.");
    }

    public BEXRunnerException(String message) {
        super(message);
    }
}
