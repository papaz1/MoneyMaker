package se.moneymaker.jsonfactory;

import org.json.simple.JSONObject;
import se.moneymaker.enums.Source;

public class JSONFactoryMatchInfo {

    private static final String TAG_EVENT = "event";

    public static JSONObject createMatchInfo(Source source, String mmDbId) {
        JSONObject matchInfo = new JSONObject();
        JSONObject pkJSON = new JSONObject();
        pkJSON.put(JSONKeyNames.KEY_PK, mmDbId);
        matchInfo.put(JSONKeyNames.KEY_SOURCE, source.getName());
        matchInfo.put(TAG_EVENT, pkJSON);

        return matchInfo;
    }
}
