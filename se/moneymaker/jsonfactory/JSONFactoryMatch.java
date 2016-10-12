package se.moneymaker.jsonfactory;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import se.betfair.util.JsonConverter;
import se.moneymaker.enums.OccurenceCounter;
import se.moneymaker.enums.TimeInterval;
import se.moneymaker.model.Match;
import se.moneymaker.util.Utils;

public class JSONFactoryMatch {

    public static final String KEY_EVENT = "event";
    private static final String KEY_HOME = "home";
    private static final String KEY_AWAY = "away";
    private static final String KEY_UTC_MIN = "utcMin";
    private static final String KEY_UTC_MAX = "utcMax";
    private static final String KEY_OCCURENCE_COUNTER = "occurrenceCounter";
    private static final String KEY_TIME_INTERVAL = "timeInterval";
    private static final String KEY_SORE = "score";
    private static final String KEY_PERIOD = "period";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_SECOND = "second";
    private static final String KEY_MATCH_TIME = "matchTime";
    private static final String KEY_UTC_TIME = "utcTime";
    private static final String KEY_REFERENCE = "reference";
    private static final String KEY_POOLTYPE_NAME = "poolType__name";
    private static final String KEY_EVENGROUP_NAME = "eventGroup__name";
    private static final String KEY_POOL = "pool";

    public static JSONObject createMatch(Match match) {

        JSONObject matchJSON = new JSONObject();
        Calendar min = Calendar.getInstance();
        min.setTime(match.getEventDate());
        min.add(Calendar.HOUR_OF_DAY, -4);
        Date utcMin = min.getTime();

        Calendar max = Calendar.getInstance();
        max.setTime(match.getEventDate());
        max.add(Calendar.HOUR_OF_DAY, 4);
        Date utcMax = max.getTime();

        matchJSON.put(JSONKeyNames.KEY_UTC_SCHEDULED, Utils.dateToString(match.getEventDate()));
        matchJSON.put(KEY_UTC_MIN, Utils.dateToString(utcMin));
        matchJSON.put(KEY_UTC_MAX, Utils.dateToString(utcMax));

        if (match.getReference() != null) {
            String matchReference = JsonConverter.convertToJson(match.getReference());
            JSONObject matchReferenceJSON = null;
            try {
                matchReferenceJSON = (JSONObject) new JSONParser().parse(matchReference);
            } catch (ParseException ex) {
            }
            matchJSON.put(KEY_REFERENCE, matchReferenceJSON);
        }

        JSONObject pool;
        if (match.getPoolType() != null) {
            pool = new JSONObject();
            pool.put(KEY_POOLTYPE_NAME, match.getPoolType().name());
            pool.put(KEY_EVENGROUP_NAME, match.getEventGroupName());
            matchJSON.put(KEY_POOL, pool);
        }

        return matchJSON;
    }

    public static JSONObject createCurrentScore(Match match) {
        JSONObject currentScore = new JSONObject();
        JSONObject pkJSON = new JSONObject();
        pkJSON.put(JSONKeyNames.KEY_PK, match.getPk());
        currentScore.put(KEY_EVENT, pkJSON);
        currentScore.put(JSONKeyNames.KEY_UTC_ENCOUNTER, Utils.dateToString(match.getUTCEncounter()));
        JSONObject scoreJSON = new JSONObject();
        scoreJSON.put(KEY_HOME, match.getHomeScore());
        scoreJSON.put(KEY_AWAY, match.getAwayScore());
        scoreJSON.put(KEY_OCCURENCE_COUNTER, OccurenceCounter.GOALS.toString());
        scoreJSON.put(KEY_TIME_INTERVAL, TimeInterval.ALL.toString());
        currentScore.put(KEY_SORE, scoreJSON);
        return currentScore;
    }

    //Currently only time reference for first period exists so timeJSON has hardcoded values
    public static JSONObject createTimeReference(Match match) {
        JSONObject timeReference = new JSONObject();
        JSONObject pkJSON = new JSONObject();
        pkJSON.put(JSONKeyNames.KEY_PK, match.getPk());
        timeReference.put(KEY_EVENT, pkJSON);
        JSONObject timeJSON = new JSONObject();
        timeJSON.put(KEY_PERIOD, 1);
        timeJSON.put(KEY_MINUTE, 0);
        timeJSON.put(KEY_SECOND, 0);
        timeReference.put(KEY_MATCH_TIME, timeJSON);
        timeReference.put(KEY_UTC_TIME, match.getMatchTime());
        return timeReference;
    }
}
