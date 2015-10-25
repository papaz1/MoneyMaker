package se.moneymaker.enums;

/*
 * Enum for log
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 2011-03-18 Baran Sölen Initial version
 */
public enum LogLevelEnum {

    CRITICAL(50, "CRITICAL"),
    ERROR(40, "ERROR"),
    WARNING(30, "WARNING"),
    INFO(20, "INFORMATION");
    private final int severity;
    private final String severityName;

    private LogLevelEnum(int severity, String severityName) {
        this.severity = severity;
        this.severityName = severityName;
    }

    public int value() {
        return severity;
    }

    public String severityName() {
        return severityName;
    }
}
