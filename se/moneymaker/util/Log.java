package se.moneymaker.util;

import se.moneymaker.enums.LogLevelEnum;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import se.moneymaker.enums.DBErrorType;
//import net.kencochrane.raven.Client;
//import net.kencochrane.raven.SentryDsn;

public class Log {

    private static LogOutputEnum OUTPUT_MODE;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    private static String rawDsn = "async+https://391c3d2f69f5451c91d0ac0c108d1a8e:2f8a6977c9284da2b085c793f05dc623@sentry.mathsolutions.se/4";
    private static HashMap<String, Integer> msgTurnOff;
    private static List<LogLevelEnum> severities;

    public Log() {
        msgTurnOff = new HashMap<>();
        msgTurnOff.put(DBErrorType.MATCH_NOT_FOUND.toString(), 0);
        Log.OUTPUT_MODE = LogOutputEnum.EXTERNAL_LOGGER;
    }

    public void setOutput(LogOutputEnum output) {
        Log.OUTPUT_MODE = output;
    }

    public void setSeverity(List<LogLevelEnum> severities) {
        Log.severities = severities;
    }

    public static void logMessage(String parentClass, String parentMethod,
            String msg, LogLevelEnum severity, boolean isDebugMessageOnly) {
        StringBuilder sb = new StringBuilder();
        if (severities == null || severities.isEmpty() || severities.contains(severity)) {
            if (!msgTurnOff.containsKey(msg)) {
                if (OUTPUT_MODE == LogOutputEnum.CONSOLE) {
                    String time = sdf.format(Calendar.getInstance().getTime());

                    //Create the final message
                    sb.append(time);
                    sb.append(" ");
                    sb.append(severity);
                    sb.append(":");
                    sb.append(parentClass);
                    sb.append(": ");
                    sb.append(parentMethod);
                    sb.append(": ");
                    sb.append(msg);

                    System.out.println(sb.toString());
                } else if (OUTPUT_MODE == LogOutputEnum.EXTERNAL_LOGGER) {
                    sb.append(parentClass);
                    sb.append(": ");
                    sb.append(parentMethod);
                    sb.append(": ");
                    sb.append(msg);
                    //logSentry(sb.toString(), MUGeneralUtil.getCurrentTimeUTC().getTime(), severity);
                }
            }
        }
    }

//    private static void logSentry(String msg, long timestamp, MULogLevelEnum level) {
    //      SentryDsn dsn = SentryDsn.build(rawDsn);
    //    Client client = new Client(dsn);
    //  client.captureMessage(msg, timestamp, "", level.value(), "");
    //}
}
