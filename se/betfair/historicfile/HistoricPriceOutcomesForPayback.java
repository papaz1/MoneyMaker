package se.betfair.historicfile;

import java.util.ArrayList;
import java.util.List;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.Price;

public class HistoricPriceOutcomesForPayback {

    private List<Outcome> outcomes;
    private Price price;

    public HistoricPriceOutcomesForPayback() {
        outcomes = new ArrayList<>();
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public List<Outcome> getOutcomes() {
        return outcomes;
    }

    public void addOutcome(Outcome outcome) {
        this.outcomes.add(outcome);
    }

    public void addAllOutcomes(List<Outcome> outcomes) {
        this.outcomes.addAll(outcomes);
    }

    public int size() {
        return outcomes.size();
    }
}
