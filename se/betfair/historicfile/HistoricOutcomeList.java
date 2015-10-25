package se.betfair.historicfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.Price;

public class HistoricOutcomeList {

    private long id;
    private String name;
    private List<Outcome> outcomes;

    public HistoricOutcomeList() {
        outcomes = new ArrayList<>();
    }

    public HistoricOutcomeList(long id) {
        outcomes = new ArrayList<>();
        this.id = id;
    }

    public HistoricWeightedPrice parseHistoricWeightedPrice() {
        Iterator<Outcome> iteratorOutcomes = outcomes.iterator();
        Outcome outcome;
        List<Price> prices = new ArrayList<>();
        while (iteratorOutcomes.hasNext()) {
            outcome = iteratorOutcomes.next();

            //There is only one price per outcome
            List<Price> pricesTemp = outcome.getPrices();
            Iterator<Price> iteratorPrices = pricesTemp.iterator();
            Price price;
            while (iteratorPrices.hasNext()) {
                price = iteratorPrices.next();
                prices.add(price);
            }
        }
        return new HistoricWeightedPrice(prices);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Outcome> getOutcomes() {
        return outcomes;
    }

    public void add(Outcome outcome) {
        outcomes.add(outcome);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HistoricOutcomeList) {
            HistoricOutcomeList historicOutcomeList = (HistoricOutcomeList) obj;
            return this.id == historicOutcomeList.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
