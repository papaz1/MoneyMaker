package se.moneymaker.enums;

public enum ConfigEnum {

    MM_BETPROVER("MM_BETPROVER"),
    BF_TOTAL_AMOUNT_MATCHED("BF_TOTALAMOUNTMATCHED"),
    BF_NUMBER_OF_PROCESSES("BF_NUMBEROFPROCESSES"),
    BF_NUMBER_OF_PROCESSES_MATCHINFO("BF_NUMBEROFPROCESSESMATCHINFO"),
    BF_TIMEBEFORE_MATCH("BF_TIMEBEFORE_MATCH"),
    MM_MEMCACHED("MM_MEMCACHED"),
    MM_PRI_USER("MM_PRI_USER"),
    MM_PRI_DOMAIN("MM_PRI_DOMAIN"),
    MM_PRI_API_KEY("MM_PRI_API_KEY"),
    MM_PUB_API_KEY("MM_PUB_API_KEY"),
    BF_USER_DJ78351("BF_USER_DJ78351"),
    BF_PASS_DJ78351("BF_PASS_DJ78351"),
    PS_USER("PS_USER"),
    PS_PASS("PS_PASS"),
    PS_SCORE_TIME_MINUTES("PS_SCORE_TIME_MINUTES");

    private final String value;

    ConfigEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
