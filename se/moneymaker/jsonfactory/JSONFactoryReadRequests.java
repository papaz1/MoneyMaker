package se.moneymaker.jsonfactory;

import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONFactoryReadRequests {

    public static JSONObject createBetOfferReferenceRequest(List<Long> betOfferPkList, String betOfferExternalKey, String source) {
        JSONObject request = new JSONObject();
        if (betOfferPkList != null) {
            JSONArray PkJSON = new JSONArray();
            for (Long ID : betOfferPkList) {
                PkJSON.add(ID);
            }
            request.put("betOffer__in", PkJSON);
        } else if (betOfferExternalKey != null) {
            request.put(JSONKeyNames.KEY_EXTERNAL_KEY, betOfferExternalKey);
        }

        request.put("source__name", source);
        return request;
    }

    public static JSONObject createOutcomeReferenceRequest(long outcomePk, long betOfferPk, String outcomeExternalKey, String source) {
        JSONObject request = new JSONObject();

        if (outcomePk > 0) {
            JSONArray PkJSON = new JSONArray();
            PkJSON.add(outcomePk);
            request.put("outcome__in", PkJSON);
        } else if (outcomeExternalKey != null && betOfferPk > 0) {
            request.put(JSONKeyNames.KEY_EXTERNAL_KEY, outcomeExternalKey);
            request.put("outcome__betOffer__pk", betOfferPk);
        }

        request.put("source__name", source);
        return request;
    }
}
