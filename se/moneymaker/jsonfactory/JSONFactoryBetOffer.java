package se.moneymaker.jsonfactory;

import org.json.simple.JSONObject;
import se.moneymaker.model.BetOffer;

public class JSONFactoryBetOffer {

    private static final String KEY_EVENT = "event";
    private static final String KEY_BETOFFER_TEMPLATE = "betOfferTemplate";
    private static final String KEY_OCCURENCE_COUNTER = "occurrenceCounter";
    private static final String KEY_BETOFFER_TYPE = "betOfferType";
    private static final String KEY_PARAMETER = "parameter";
    private static final String KEY_TIME_MATCH_INTERVAL = "matchTimeInterval";

    public static JSONObject createJSONBetOffer(long Pk, BetOffer betOffer) {

        //Top level
        JSONObject betOfferInclOutcomeJSON = new JSONObject();

        //Level1
        JSONObject betOfferJSON = new JSONObject();

        JSONObject betEventJSON = new JSONObject();
        betEventJSON.put(JSONKeyNames.KEY_PK, Pk);
        betOfferJSON.put(KEY_EVENT, betEventJSON);

        //Level2
        JSONObject betOfferTemplateJSON = new JSONObject();

        betOfferTemplateJSON.put(KEY_BETOFFER_TYPE, betOffer.getItem().getType().getName());
        betOfferTemplateJSON.put(KEY_OCCURENCE_COUNTER, betOffer.getItem().getType().getOccurrenceCounter());
        betOfferTemplateJSON.put(KEY_TIME_MATCH_INTERVAL, betOffer.getItem().getType().getMatchTimeInterval());

        if (betOffer.getItem().getParameter() != null) {
            try {
                double parameter = Double.parseDouble(betOffer.getItem().getParameter());
                betOfferTemplateJSON.put(KEY_PARAMETER, parameter);
            } catch (NumberFormatException e) {
                betOfferTemplateJSON.put(KEY_PARAMETER, betOffer.getItem().getParameter());
            }
        }

        betOfferJSON.put(KEY_BETOFFER_TEMPLATE, betOfferTemplateJSON);
        betOfferInclOutcomeJSON.put(JSONKeyNames.KEY_BETOFFER, betOfferJSON);
        betOfferInclOutcomeJSON.put(JSONKeyNames.KEY_OUTCOMES, JSONFactoryOutcome.parseJSONOutcome(betOffer));

        return betOfferInclOutcomeJSON;
    }

    public static JSONObject createJSONBetOfferReference(BetOffer betOffer) {
        JSONObject betOfferReference = new JSONObject();
        JSONObject betOfferJSON = new JSONObject();
        betOfferJSON.put(JSONKeyNames.KEY_PK, betOffer.getPk());
        betOfferReference.put(JSONKeyNames.KEY_BETOFFER, betOfferJSON);
        betOfferReference.put(JSONKeyNames.KEY_SOURCE, betOffer.getSource());
        betOfferReference.put(JSONKeyNames.KEY_EXTERNAL_KEY, betOffer.getExternalKey());
        betOfferReference.put(JSONKeyNames.KEY_HUMAN_TEXT, betOffer.getName());
        return betOfferReference;
    }
}
