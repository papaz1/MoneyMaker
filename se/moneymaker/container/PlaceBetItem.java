package se.moneymaker.container;

import java.util.Date;
import se.moneymaker.model.Outcome;
import se.moneymaker.util.Utils;

public class PlaceBetItem {

    private Outcome outcome;
    private double requestedOdds;
    private boolean isBack;

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public double getRequestedOdds() {
        return requestedOdds;
    }

    public void setRequestedOdds(double requestedOdds) {
        this.requestedOdds = requestedOdds;
    }

    public boolean isIsBack() {
        return isBack;
    }

    public void setIsBack(boolean isBack) {
        this.isBack = isBack;
    }
}
