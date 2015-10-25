package se.moneymaker.enums;

public enum OutcomeTypeEnum {

    HOME("1"),
    AWAY("2"),
    DRAW("X"),
    OVER("O"),
    UNDER("U"),
    CORRECT_SCORE("CS_PAR"),
    CORRECT_SCORE_OTHER("OTHER"),
    CORRECT_SCORE_OTHER_GROUP("CS_PG"),
    MISSING_OUTCOME_TYPE("MISSING_OUTCOME_TYPE");
    final String name;

    OutcomeTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
