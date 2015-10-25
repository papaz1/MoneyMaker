package se.moneymaker.model;

import se.moneymaker.enums.OutcomeTypeEnum;

public class OutcomeItem {

    private final OutcomeTypeEnum type;
    private String[] parameters;

    public OutcomeItem(OutcomeTypeEnum type) {
        this.type = type;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    public String[] getParameters() {
        return parameters;
    }

    public OutcomeTypeEnum getType() {
        return type;
    }
}
