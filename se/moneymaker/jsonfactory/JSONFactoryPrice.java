package se.moneymaker.jsonfactory;

import java.util.Date;
import org.json.simple.JSONObject;
import se.moneymaker.model.Price;
import se.moneymaker.enums.PriceEnum;
import se.moneymaker.util.Utils;
import se.moneymaker.util.Constants;

public class JSONFactoryPrice {

    private static final String KEY_OUTCOME = "outcome";
    private static final String KEY_ODDS = "odds";
    private static final String KEY_UTC_ENCOUNTER = "utcEncounter";
    private static final String KEY_VOLUME_AVAILABLE_LOCAL = "volumeAvailableLocal";
    private static final String KEY_VOLUME_MATCHED_LOCAL = "volumeMatchedLocal";
    private static final String KEY_IS_LIVE = "isLive";
    public static final String KEY_UTC_FIRST_ENCOUNTER = "utcFirstEncounter";
    public static final String KEY_UTC_LAST_ENCOUNTER = "utcLastEncounter";
    public static final String KEY_NUMBER_OF_TRANSACTIONS = "numTransactions";
    private static final String KEY_IS_BACK = "isBack";
    private static final String KEY_PROBABILITY = "probability";
    private static final String KEY_READ_REASON = "readReason";
    private static final String KEY_MINUTE_WEIGHT = "minuteWeight";
    private static final String KEY_CURRENCY = "currency";
    
    public static JSONObject parseJSONPrice(long Pk, Price price) {
        JSONObject priceJSON = new JSONObject();
        priceJSON.put(JSONKeyNames.KEY_SOURCE, price.getSource());
        JSONObject pkJSON = new JSONObject();
        pkJSON.put(JSONKeyNames.KEY_PK, Pk);
        priceJSON.put(KEY_OUTCOME, pkJSON);

        if (price.getType() == PriceEnum.BACK) {
            priceJSON.put(KEY_IS_BACK, true);
        } else if (price.getType() == PriceEnum.LAY) {
            priceJSON.put(KEY_IS_BACK, false);
        }

        if (price.getAmountAvailable() > 0) {
            priceJSON.put(KEY_VOLUME_AVAILABLE_LOCAL, price.getAmountAvailable());
        }

        if (price.getProbability() > 0) {
            priceJSON.put(KEY_PROBABILITY, price.getProbability());
        }

        priceJSON.put(KEY_VOLUME_MATCHED_LOCAL, price.getVolumeMatched());
        priceJSON.put(KEY_CURRENCY, price.getCurrency());
        if (price.getNumberOfTransactions() > 0) {
            priceJSON.put(KEY_NUMBER_OF_TRANSACTIONS, price.getNumberOfTransactions());
        }

        priceJSON.put(KEY_ODDS, price.getPrice());

        if (price.getReadReason() != null) {
            priceJSON.put(KEY_READ_REASON, price.getReadReason().toString());
        }

        if (price.getMinuteWeight() > 0) {
            priceJSON.put(KEY_MINUTE_WEIGHT, price.getMinuteWeight());
        }

        Date utcEncounter = price.getUtcEncounter();
        if (utcEncounter != null && !utcEncounter.equals(Utils.stringToDate(Constants.DEFAULT_DATE))) {
            priceJSON.put(KEY_UTC_ENCOUNTER, Utils.dateToString(utcEncounter));
        }

        Date firstEncounter = price.getFirstTakenDate();
        if (firstEncounter != null && !firstEncounter.equals(Utils.stringToDate(Constants.DEFAULT_DATE))) {
            priceJSON.put(KEY_UTC_FIRST_ENCOUNTER, Utils.dateToString(firstEncounter));
        }

        Date lastEncounter = price.getLatestTakenDate();
        if (lastEncounter != null && !lastEncounter.equals(Utils.stringToDate(Constants.DEFAULT_DATE))) {
            priceJSON.put(KEY_UTC_LAST_ENCOUNTER, Utils.dateToString(lastEncounter));
        }

        if (price.isInPlay()) {
            priceJSON.put(KEY_IS_LIVE, true);
        } else {
            priceJSON.put(KEY_IS_LIVE, false);
        }
        return priceJSON;
    }
}
