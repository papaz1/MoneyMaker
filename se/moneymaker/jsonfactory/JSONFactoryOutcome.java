package se.moneymaker.jsonfactory;

import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Outcome;
import se.moneymaker.enums.OutcomeResultEnum;

public class JSONFactoryOutcome {

    private static final String KEY_OUTCOME_TEMPLATE = "outcomeTemplate";
    private static final String KEY_COMPLEX_PARAMETER = "complexParameter";
    private static final String KEY_OUTCOME_TYPE = "outcomeType";
    private static final String KEY_WIN_FLAG = "winFlag";
    private static final String KEY_OUTCOME = "outcome";

    public static JSONArray parseJSONOutcome(BetOffer betOffer) {
        JSONArray outcomesJSON = new JSONArray();
        List<Outcome> outcomes = betOffer.getOutcomes();
        Iterator<Outcome> iteratorOutcomes = outcomes.iterator();
        Outcome outcome;
        while (iteratorOutcomes.hasNext()) {
            outcome = iteratorOutcomes.next();
            outcomesJSON.add(parseJSONOutcome(outcome));
        }

        return outcomesJSON;
    }

    public static JSONArray createOutcomeReferences(List<Outcome> outcomes) {
        JSONArray outcomeReferences = new JSONArray();
        for (Outcome outcome : outcomes) {
            outcomeReferences.add(createOutcomeReference(outcome));
        }
        return outcomeReferences;
    }

    private static JSONObject createOutcomeReference(Outcome outcome) {
        JSONObject outcomeReference = new JSONObject();
        JSONObject outcomeJSON = new JSONObject();
        outcomeJSON.put(JSONKeyNames.KEY_PK, outcome.getPk());
        outcomeReference.put(KEY_OUTCOME, outcomeJSON);
        outcomeReference.put(JSONKeyNames.KEY_SOURCE, outcome.getSource());
        outcomeReference.put(JSONKeyNames.KEY_EXTERNAL_KEY, outcome.getExternalKey());
        outcomeReference.put(JSONKeyNames.KEY_HUMAN_TEXT, outcome.getName());
        return outcomeReference;
    }

    public static JSONObject parseOutcomeQuery(String bookmaker, String betOfferId, String outcomeId) {
        final String BETOFFER_EXTERNAL_KEY = "betOffer__externalKey";
        final String OUTCOME_EXTERNAL_KEY = "outcome__externalKey";
        final String BOOKMAKER = "betOffer__event__eventDimension__source__name";
        JSONObject outcomeJSON = new JSONObject();
        outcomeJSON.put(OUTCOME_EXTERNAL_KEY, outcomeId);
        outcomeJSON.put(BETOFFER_EXTERNAL_KEY, betOfferId);
        outcomeJSON.put(BOOKMAKER, bookmaker);

        return outcomeJSON;
    }

    private static JSONObject parseJSONOutcome(Outcome outcome) {

        //Top level
        JSONObject outcomeJSON = new JSONObject();
        if (outcome.getResult() != null) {
            if (outcome.getResult().equals(OutcomeResultEnum.WIN)) {
                outcomeJSON.put(KEY_WIN_FLAG, true);
            } else if (outcome.getResult().equals(OutcomeResultEnum.LOSS)) {
                outcomeJSON.put(KEY_WIN_FLAG, false);
            }
        }

        //Level1
        JSONObject outcomeTemplateJSON = new JSONObject();

        //Level2
        outcomeTemplateJSON.put(KEY_OUTCOME_TYPE, outcome.getItem().getType().getName());
        if (outcome.getItem().getParameters() != null) {
            JSONArray complexParameters = new JSONArray();
            String[] parameters = outcome.getItem().getParameters();

            //If the parameter is a number it should be an int and not a string
            for (String parameter : parameters) {
                try {
                    int param = Integer.parseInt(parameter);
                    complexParameters.add(param);
                } catch (NumberFormatException e) {
                    complexParameters.add(parameter);
                }
            }
            outcomeTemplateJSON.put(KEY_COMPLEX_PARAMETER, complexParameters);
        }

        outcomeJSON.put(KEY_OUTCOME_TEMPLATE, outcomeTemplateJSON);
        return outcomeJSON;
    }

}
