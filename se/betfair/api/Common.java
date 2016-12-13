package se.betfair.api;

import com.betfair.aping.enums.PriceData;
import java.util.HashSet;
import java.util.Set;
import se.betfair.model.PriceProjection;

public class Common {

    public static final String EVENT_TYPE_SOCCER = "1";

    public static final String MARKET_TYPE_MATCH_ODDS = "MATCH_ODDS";
    public static final String MARKET_TYPE_OVER_UNDER_05 = "OVER_UNDER_05";
    public static final String MARKET_TYPE_OVER_UNDER_15 = "OVER_UNDER_15";
    public static final String MARKET_TYPE_OVER_UNDER_25 = "OVER_UNDER_25";
    public static final String MARKET_TYPE_OVER_UNDER_35 = "OVER_UNDER_35";
    public static final String MARKET_TYPE_OVER_UNDER_45 = "OVER_UNDER_45";
    public static final String MARKET_TYPE_OVER_UNDER_55 = "OVER_UNDER_55";
    public static final String MARKET_TYPE_OVER_UNDER_65 = "OVER_UNDER_65";
    public static final String MARKET_TYPE_OVER_UNDER_75 = "OVER_UNDER_75";
    public static final String MARKET_TYPE_OVER_UNDER_85 = "OVER_UNDER_85";
    public static final String MARKET_TYPE_CORRECT_SCORE = "CORRECT_SCORE";
    public static final String MARKET_TYPE_HALF_TIME = "HALF_TIME";
    public static final String MARKET_PROJECTION_MARKET_DESCRIPTION = "MARKET_DESCRIPTION";
    public static final String MARKET_PROJECTION_EVENT = "EVENT";
    public static final String MARKET_PROJECTION_EVENT_TYPE = "EVENT_TYPE";
    public static final String MARKET_PROJECTION_COMPETITION = "COMPETITION";
    public static final String MARKET_PROJECTION_MARKET_START_TIME = "MARKET_START_TIME";
    public static final String MARKET_BETTING_TYPE_ODDS = "ODDS";
    public static final String MARKET_SORT_FIRST_TO_START = "FIRST_TO_START";
    public static final String MARKET_SORT_LAST_TO_START = "LAST_TO_START";
    public static final String MARKET_PROJECTION_RUNNER_DESCRIPTION = "RUNNER_DESCRIPTION";

    public static PriceProjection getPricePojectionSettings() {
        PriceProjection priceProjection = new PriceProjection();
        priceProjection.setVirtualise(true);
        Set<PriceData> priceData = new HashSet<>();
        priceData.add(PriceData.EX_BEST_OFFERS);
        //priceData.add(PriceData.EX_TRADED); Cost heavy operation that currently is not used in Betprover
        priceProjection.setPriceData(priceData);
        return priceProjection;
    }
}
