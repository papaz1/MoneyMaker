package se.moneymaker.dict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.moneymaker.model.KeyValuePair;

public class TeamDict {

    private static final Map<String, String> teams = new HashMap<>();

    public static void clear() {
        teams.clear();
    }

    public static void putAll(List<KeyValuePair> keyValuePairs) {
        for (KeyValuePair keyValuePair : keyValuePairs) {
            teams.put(keyValuePair.getKey(), keyValuePair.getValue());
        }
    }

    public static void put(String teamName, String externalKey) {
        teams.put(teamName, externalKey);
    }

    public static String get(String teamName) {
        return (String) teams.get(teamName);
    }

    public static boolean contains(String teamName) {
        return teams.containsKey(teamName);
    }
}
