package se.moneymaker.util;

/*
 *
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 Apr 16, 2013 Baran Sölen Initial version
 */
public enum LogOutputEnum {

    CONSOLE(1),
    EXTERNAL_LOGGER(2);
    private final int output;

    private LogOutputEnum(int output) {
        this.output = output;
    }
}
