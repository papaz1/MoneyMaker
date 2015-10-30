package se.moneymaker.jsonfactory;

import java.util.Arrays;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.moneymaker.dict.Config;
import se.moneymaker.enums.ApiServiceName;
import se.moneymaker.enums.ConfigEnum;
import se.moneymaker.enums.DBBoolean;
import se.moneymaker.model.Bet;
import se.moneymaker.util.Utils;

public class JSONFactoryBet {

    private static final String KEY_REQUESTED_ODDS = "requestedOdds";
    private static final String KEY_MATCHED_ODDS = "matchedOdds";
    private static final String KEY_MATCHED_STAKE_LOCAL = "matchedStakeLocal";
    private static final String KEY_CANCELED_STAKE_LOCAL = "canceledStakeLocal";
    private static final String KEY_PAIDOUT_LOCAL = "paidOutLocal";
    private static final String KEY_ITEM_PAIDOUT_LOCAL = "itemPaidOutLocal";
    private static final String KEY_UTC_PLACED = "utcPlaced";
    private static final String KEY_STATEMENT_AFTER_SETTLMENT_LOCAL = "statementAfterSettlementLocal";
    private static final String KEY_REST_STAKE_LOCAL = "restStakeLocal";
    private static final String KEY_UNMATCHED_STAKE_LOCAL = "unmatchedStakeLocal";
    public static final String KEY_REQUESTED_STAKE_LOCAL = "requestedStakeLocal";
    private static final String KEY_IS_BACK = "isBack";
    private static final String KEY_OUTCOME_PK = "outcome";
    private static final String KEY_PROVIDER_BET_STATUS = "providerBetStatus";
    private static final String KEY_FAIR_ODDS = "fairOdds";
    private static final String KEY_IS_LIVE = "isLive";
    private static final String KEY_IS_FIXED_ODDS = "isFixedOdds";
    private static final String KEY_STATE = "state";
    private static final String KEY_IS_UPDATED = "isUpdated";
    private static final String KEY_ITEMS = "items";
    private static final String KEY_PUNTER = "punter";
    private static final String KEY_REASON = "reason";
    public static final String KEY_UTC_VALID_UNTIL = "utcValidUntil";
    private static final String KEY_STATE_NAME_IN = "state__name__in";
    private static final String KEY_EXTERNAL_KEY_IN = "externalKey__in";
    private static final String UNEXPECTED_PLUS_LOCAL = "unexpectedPlusLocal";
    private static final String UNEXPECTED_MINUS_LOCAL = "unexpectedMinusLocal";
    private static final String COMMISSION_LOCAL = "commissionLocal";
    private static final String UNMATCHED_STAKE_GT = "unmatchedStakeUSD__gt";
    private static final String UTC_VALID_UNTIL_LTE = "utcValidUntil__lte";

    public static JSONArray createBets(Bet bet, boolean isRepairMode) {
        JSONArray betsJSON = new JSONArray();
        betsJSON.add(JSONFactoryBet.createBet(bet, isRepairMode));
        return betsJSON;
    }

    public static JSONObject createBet(Bet bet, boolean isRepairMode) {
        JSONObject betJSON = new JSONObject();
        betJSON.put(JSONKeyNames.KEY_CURRENCY, bet.getCurrency());
        betJSON.put(UNEXPECTED_PLUS_LOCAL, bet.getUnexpectedPlusLocal());
        betJSON.put(UNEXPECTED_MINUS_LOCAL, bet.getUnexpectedMinusLocal());

        betJSON.put(COMMISSION_LOCAL, bet.getCommission());

        if (bet.getState() != null) {
            betJSON.put(KEY_STATE, bet.getState().value());
        }

        if (bet.getPk() != null) {
            betJSON.put(JSONKeyNames.KEY_PK, bet.getPk());
        } else if (bet.getExternalKey() != null) {
            betJSON.put(JSONKeyNames.KEY_EXTERNAL_KEY, bet.getExternalKey());
            String accountName = bet.getAccountName();
            betJSON.put(JSONKeyNames.KEY_ACCOUNT, JSONFactoryAccount.parseAccount(bet.getBookmaker(), accountName));

        }

        betJSON.put(KEY_MATCHED_STAKE_LOCAL, bet.getMatchedStakeLocal());
        betJSON.put(KEY_CANCELED_STAKE_LOCAL, bet.getCanceledStakeLocal());

        if (bet.getUTCPlaced() != null) {
            betJSON.put(KEY_UTC_PLACED, Utils.dateToString(bet.getUTCPlaced()));
        }

        if (bet.getStatementAfterSettledLocal() != null) {
            betJSON.put(KEY_STATEMENT_AFTER_SETTLMENT_LOCAL, bet.getStatementAfterSettledLocal());
        }

        betJSON.put(KEY_REST_STAKE_LOCAL, bet.getRestStakeLocal());

        betJSON.put(KEY_UNMATCHED_STAKE_LOCAL, bet.getUnmatchedStakeLocal());

        if (bet.getRequestedStakeLocal() > 0) {
            betJSON.put(KEY_REQUESTED_STAKE_LOCAL, bet.getRequestedStakeLocal());
        }

        if (bet.getProviderBetStatus() != null) {
            JSONObject providerStatusJSON = new JSONObject();
            providerStatusJSON.put(JSONKeyNames.KEY_NAME, bet.getProviderBetStatus());
            betJSON.put(KEY_PROVIDER_BET_STATUS, providerStatusJSON);
        }

        JSONArray itemsJSON = new JSONArray();
        JSONObject itemJSON = new JSONObject();
        betJSON.put(KEY_PAIDOUT_LOCAL, bet.getPaidOutLocal());
        itemJSON.put(KEY_ITEM_PAIDOUT_LOCAL, bet.getPaidOutLocal());

        if (bet.getRequestedOdds() != 0) {
            itemJSON.put(KEY_REQUESTED_ODDS, bet.getRequestedOdds());
        }

        if (bet.getOutcomePk() > 0) {
            JSONObject outcomeJSON = new JSONObject();
            outcomeJSON.put(JSONKeyNames.KEY_PK, bet.getOutcomePk());
            itemJSON.put(KEY_OUTCOME_PK, outcomeJSON);
        }

        JSONObject punterJSON;
        JSONObject reasonJSON;
        if (isRepairMode) {
            itemJSON.put(KEY_FAIR_ODDS, bet.getRequestedOdds());
            if (bet.getMatchedOdds() == 0) {
                bet.setMatchedOdds(bet.getRequestedOdds());
            }
            itemJSON.put(KEY_IS_LIVE, false);
            itemJSON.put(KEY_IS_FIXED_ODDS, true);
            reasonJSON = new JSONObject();
            reasonJSON.put(JSONKeyNames.KEY_NAME, "BET_RESTORED");
            itemJSON.put(KEY_REASON, reasonJSON);
            punterJSON = new JSONObject();
            punterJSON.put(JSONKeyNames.KEY_DOMAIN, Config.getInstance().get(ConfigEnum.MM_PRI_DOMAIN));
            punterJSON.put(JSONKeyNames.KEY_NAME, Config.getInstance().get(ConfigEnum.MM_PRI_USER));
            betJSON.put(KEY_PUNTER, punterJSON);
            betJSON.put(KEY_UTC_VALID_UNTIL, Utils.dateToString(new Date()));
        }

        if (bet.getMatchedOdds() > 0) {
            itemJSON.put(KEY_MATCHED_ODDS, bet.getMatchedOdds());
        }
        if (bet.getIsBack() == 1) {
            itemJSON.put(KEY_IS_BACK, true);
        } else if (bet.getIsBack() == 0) {
            itemJSON.put(KEY_IS_BACK, false);
        }

        itemsJSON.add(itemJSON);
        betJSON.put(KEY_ITEMS, itemsJSON);
        return betJSON;
    }

    public static Bet createBets(JSONObject betJSON, String bookmaker, String accountName) {
        Bet bet = new Bet(bookmaker, accountName);
        bet.setExternalKey(((String) betJSON.get(JSONKeyNames.KEY_EXTERNAL_KEY)));
        bet.setPk((String) betJSON.get(ApiServiceName.READ_BETS.getPk()));
        bet.setValidUntil(Utils.stringToDate((String) betJSON.get(JSONFactoryBet.KEY_UTC_VALID_UNTIL)));
        bet.setRequestedStakeLocal((Double) betJSON.get(JSONFactoryBet.KEY_REQUESTED_STAKE_LOCAL));
        bet.setPk((String) betJSON.get(JSONKeyNames.KEY_PK));
        return bet;
    }

    public static JSONObject createBetQuery(String bookmaker, String accountName, DBBoolean isUpdated, String[] states, String[] externalKeys, boolean includeUtcValidUntil) {
        JSONObject betJSON = new JSONObject();
        betJSON.put(JSONKeyNames.KEY_ACCOUNT, JSONFactoryAccount.parseAccount(bookmaker, accountName));

        if (includeUtcValidUntil) {
            betJSON.put(UTC_VALID_UNTIL_LTE, Utils.dateToString(new Date()));
        }

        if (isUpdated.equals(DBBoolean.TRUE)) {
            betJSON.put(KEY_IS_UPDATED, true);
        } else if (isUpdated.equals(DBBoolean.FALSE)) {
            betJSON.put(KEY_IS_UPDATED, false);
        }

        if (states != null) {
            JSONArray statesJSON = new JSONArray();
            statesJSON.addAll(Arrays.asList(states));
            betJSON.put(KEY_STATE_NAME_IN, statesJSON);
        }

        if (externalKeys != null) {
            JSONArray externalKeysJSON = new JSONArray();
            externalKeysJSON.addAll(Arrays.asList(externalKeys));
            betJSON.put(KEY_EXTERNAL_KEY_IN, externalKeysJSON);
        }

        return betJSON;
    }
}
