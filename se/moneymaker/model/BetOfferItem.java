package se.moneymaker.model;

import se.moneymaker.enums.BetOfferTypeEnum;

public class BetOfferItem {

    private BetOfferTypeEnum type;
    private String parameter;

    public BetOfferItem(BetOfferTypeEnum type) {
        this.type = type;
    }

    public void setParameter(String value) {
        this.parameter = value;
    }

    public void setType(BetOfferTypeEnum type) {
        this.type = type;
    }

    public BetOfferTypeEnum getType() {
        return type;
    }

    public String getParameter() {
        return parameter;
    }
}
