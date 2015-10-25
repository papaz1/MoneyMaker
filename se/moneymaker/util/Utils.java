package se.moneymaker.util;

import se.moneymaker.enums.LogLevelEnum;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.datatype.XMLGregorianCalendar;
import org.json.simple.JSONObject;
import se.moneymaker.dict.Config;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.Price;

public class Utils {

    private static final String CLASSNAME = Utils.class.getName();

    public static Date getTomorrow(SimpleDateFormat df) {
        Calendar cal;
        try {
            cal = Calendar.getInstance();
            Date today = new Date(Utils.parseStringToLongDate(df.format(new Date()), df));
            cal.setTime(today);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            return cal.getTime();
        } catch (ParseException e) {
        }
        return null;
    }

    public static boolean isNewDay(SimpleDateFormat df, Date tomorrow) {
        try {
            Date today = new Date(Utils.parseStringToLongDate(df.format(new Date()), df));
            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            Calendar calCompare = Calendar.getInstance();
            calCompare.setTime(tomorrow);
            if (calToday.equals(calCompare)) {
                return true;
            }
        } catch (ParseException e) {
        }
        return false;
    }

    public static boolean isNewDay2(SimpleDateFormat df, Date firstToday) {
        try {
            Date today = new Date(Utils.parseStringToLongDate(df.format(new Date()), df));
            Calendar calToday = Calendar.getInstance();
            calToday.setTime(today);
            Calendar calCompare = Calendar.getInstance();
            calCompare.setTime(firstToday);
            if (calToday.after(calCompare)) {
                return true;
            }
        } catch (ParseException e) {
        }
        return false;
    }

    public static double parseDouble(int scale, double d) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static HashMap<String, String> readDictionaryFile(String filename) {
        final String METHOD = "init";
        HashMap<String, String> dictionary = new HashMap<>();
        File file = new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        File configFile = new File(file.getParent() + System.getProperty("file.separator") + "workdir" + System.getProperty("file.separator") + filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                StringReader lineReader = new StringReader(line);
                String configKey = Utils.readString(lineReader, '=');
                String configValue = Utils.readString(lineReader, ',');
                dictionary.put(configKey.toUpperCase(), configValue);
            }
        } catch (IOException e1) {
            Log.logMessage(CLASSNAME, METHOD, e1.getMessage(), LogLevelEnum.ERROR, false);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e2) {
                Log.logMessage(CLASSNAME, METHOD, e2.getMessage(), LogLevelEnum.ERROR, false);
            }
        }
        return dictionary;
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

    public static Date parseXMLGregorianDate(XMLGregorianCalendar gregCal) {
        return gregCal.toGregorianCalendar().getTime();
    }

    public static String dateToString(Date date) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        //df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    public static String cleanDateFormatFromDB(String date) {
        String result = date;
        if (date.contains("T")) {
            result = date.replaceFirst("T", " ");
        }
        return result;
    }

    public static Date stringToDate(String possibleDate) {
        Date date = null;
        if (possibleDate != null) {
            List<String> dateFormats = new ArrayList<>();

            SimpleDateFormat df;
            dateFormats.add("dd-MM-yy HH:mm");
            dateFormats.add("dd-MM-yy HH:mm:ss");
            dateFormats.add("dd-MM-yyyy HH:mm");
            dateFormats.add("dd-MM-yyyy HH:mm:ss");
            dateFormats.add("dd/MM/yy HH:mm:ss");
            dateFormats.add("yyyy-MM-dd");
            dateFormats.add("yyyy-MM-dd HH:mm");
            dateFormats.add("yyyy-MM-dd HH:mm:ss");
            dateFormats.add("yyyyMMddHHmmss");
            dateFormats.add("yyyy-MM-ddTHH:mm:ssZ");
            dateFormats.add("yyyy-MM-dd HH:mm:ss.SSS"); //Main format of MM database
            /**
             * Special case of yyyy-MM-dd HH:mm:ss.SSS where MM database has the
             * format yyyy-MM-dd HH:mm:ss.SSSSSS which Date can't handle. So cut
             * the last the digits which doesn't matter.
             */
            if (possibleDate.length() == Config.MM_DB_DATE_LENGTH) {
                possibleDate = possibleDate.substring(0, possibleDate.length() - 3);
            }

            Iterator<String> iterator = dateFormats.iterator();
            String dateFormat;
            while (iterator.hasNext()) {
                dateFormat = iterator.next();
                if (dateFormat.length() == possibleDate.length()) {
                    df = new SimpleDateFormat(dateFormat);
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    df.setLenient(false);
                    try {
                        date = df.parse(possibleDate);
                        return date;
                    } catch (ParseException e) {
                    }
                }
            }
        }
        return date;
    }

    public static String trimQuotationMarks(String s) {
        if (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
            return s.substring(1, s.length() - 1);
        } else {
            return s;
        }
    }

    public static boolean stringToBoolean(String possibleBoolean) {
        return possibleBoolean.equalsIgnoreCase("Y")
                || possibleBoolean.equalsIgnoreCase("true")
                || possibleBoolean.equalsIgnoreCase("1")
                || possibleBoolean.equalsIgnoreCase("IP")
                || possibleBoolean.equalsIgnoreCase("Yes");
    }

    public static Date getCurrentTimeUTC() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return cal.getTime();
    }

    public static long parseStringToLongDate(String date, SimpleDateFormat format) throws ParseException {
        Date d = format.parse(date);
        return d.getTime();
    }

    public static void writeToFile(List<String> rows, File file) {

        //If file exists append records to the end else create new file
        try {
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                raf.seek(raf.length());
                for (String row : rows) {
                    raf.write(row.getBytes());
                    raf.write(System.getProperty("line.separator").getBytes());
                }
            }
        } catch (IOException e) {
        }
    }

    public static String parseErrorType(String message) {
        return message.substring(message.indexOf('$') + 1, message.lastIndexOf('$'));
    }

    public static double calculateBetOfferPayback(List<Outcome> outcomes) {
        Iterator<Outcome> iteratorOutcome = outcomes.iterator();
        Outcome outcome;
        double payback = 0;
        double pricesSum = 0;
        while (iteratorOutcome.hasNext()) {
            outcome = iteratorOutcome.next();
            List<Price> prices = outcome.getPrices();
            Iterator<Price> iteratorPrice = prices.iterator();
            Price priceObject;

            double price;
            while (iteratorPrice.hasNext()) {
                priceObject = iteratorPrice.next();
                price = priceObject.getPrice();
                if (price != 0) {
                    pricesSum = pricesSum + (1 / price);
                }
            }
        }
        if (pricesSum != 0) {
            payback = 1 / pricesSum;
        }
        BigDecimal bd = new BigDecimal(payback);
        bd = bd.setScale(Config.PAYBACK_ROUNDING, BigDecimal.ROUND_HALF_UP);

        return bd.doubleValue();
    }

    public static String toJSONStringErrorMsg(ErrorType type, String msg, String uuid) {
        JSONObject error = new JSONObject();
        error.put("errorType", type.name());
        if (msg == null) {
            error.put("msg", type.getMsg());
        } else {
            error.put("msg", msg);
        }
        error.put("uuid", uuid);
        return error.toString();
    }
}
