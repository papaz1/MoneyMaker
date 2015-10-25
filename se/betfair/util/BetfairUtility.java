package se.betfair.util;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Date;
import se.moneymaker.util.Utils;

public class BetfairUtility {

    public static long readLong(StringReader reader, char delimiter) {
        String str = readString(reader, delimiter);
        if (str.length() == 0) {
            return 0;
        } else {
            return Long.parseLong(str);
        }
    }

    public static int readInt(StringReader reader, char delimiter) throws NumberFormatException {
        String str = readString(reader, delimiter);
        if (str.length() == 0) {
            return 0;
        } else {
            return Integer.parseInt(str);
        }
    }

    public static String readString(StringReader reader, char delimiter) {
        try {
            StringBuilder sb = new StringBuilder();
            char c;

            while ((c = (char) reader.read()) != (char) -1) {
                if (c == delimiter) {
                    break;
                } else {
                    sb.append(c);
                }
            }

            return sb.toString();
        } catch (IOException e) {
            // Cannot happen as there is no IO here - just a read from a string
            throw new RuntimeException("Unexpected IOException", e);
        }
    }

    public static Date readDate(StringReader reader, char delimiter) throws ParseException {

        String str = readString(reader, delimiter);
        if (str.length() == 0) {
            return null;
        } else {
            return new Date(Long.parseLong(str));
        }
    }

    public static double readDouble(StringReader reader, char delimiter) throws NumberFormatException {
        String str = readString(reader, delimiter);
        if (str.length() == 0) {
            return 0.0d;
        } else {
            return Double.parseDouble(str);
        }
    }

    public static boolean readBoolean(StringReader reader, char delimiter) {
        String bool = readString(reader, delimiter);

        return Utils.stringToBoolean(bool);
    }
}
