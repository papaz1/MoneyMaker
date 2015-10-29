package se.moneymaker.enums;

import se.moneymaker.jsonfactory.JSONKeyNames;

public enum ApiServiceName {

    AUTH("auth/check", ""),
    RESULT("match/info", ""),
    MATCH("special/match/search", JSONKeyNames.KEY_PK),
    WRITE_BETOFFER("write/price/betoffer", JSONKeyNames.KEY_PK),
    READ_BETOFFER_REFERENCE("read/price/betofferreference/0", JSONKeyNames.KEY_PK),
    READ_OUTCOME_REFERENCE("read/price/outcomereference/1", JSONKeyNames.KEY_PK),
    OUTCOME("outcome", JSONKeyNames.KEY_PK),
    WRITE_PRICE_OBSERVATION("write/price/observation", JSONKeyNames.KEY_PK),
    WRITE_PRICE_HISTORIC("write/price/grouped", JSONKeyNames.KEY_PK),
    READ_BETS("read/bet/combination/0", JSONKeyNames.KEY_PK),
    UPDATE_BET("update/bet/combination", ""),
    BET_OUTCOMELESS_PLACE("bet/outcomeless/place", ""),
    WRTIE_BET_COMBINATION("write/bet/combination", ""),
    WRITE_BET_ACCOUNT_STATEMENT("write/account/statement", ""),
    WRITE_BET_ACCOUNT_TRANSACTION("write/account/transaction", ""),
    WRITE_MATCH_INFO("write/price/matchinfo", ""),
    READ_CURRENCY("read/config/datasource/0", ""),
    READ_OUTCOME("read/price/outcome/0", JSONKeyNames.KEY_PK),
    WRITE_BETOFFER_REFERENCE("write/price/betofferreference", ""),
    WRITE_OUTCOME_REFERENCE("write/price/outcomereference", ""),
    WRITE_CURRENT_SCORE("write/gss/currentmatchscore", ""),
    WRITE_EVENT_TIME_REFERENCE("write/schedule/EventTimeReference", "");
    private final String serviceName;
    private final String Pk;

    private ApiServiceName(String serviceName, String Pk) {
        this.serviceName = serviceName;
        this.Pk = Pk;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getPk() {
        return Pk;
    }
}
