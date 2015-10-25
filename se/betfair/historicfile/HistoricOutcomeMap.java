package se.betfair.historicfile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import se.moneymaker.model.Outcome;

public class HistoricOutcomeMap {

    private TreeMap<DateInPlay, List<HistoricOutcomeList>> timeline;
    private int size; //Each time a new inner List<Outcome> is created the size will be incremented which is number of rows of the paybackMatrix

    public HistoricOutcomeMap() {
        timeline = new TreeMap<>();
    }

    public void put(DateInPlay dip, Outcome outcome) {

        List<HistoricOutcomeList> historicOutcomesList = timeline.get(dip);

        if (historicOutcomesList == null) {
            List<HistoricOutcomeList> newHistoricOutcomesList = new ArrayList<>();
            HistoricOutcomeList newHistoricOutcomeList = new HistoricOutcomeList(outcome.getExternalKey());
            newHistoricOutcomeList.add(outcome);
            newHistoricOutcomesList.add(newHistoricOutcomeList);
            timeline.put(dip, newHistoricOutcomesList);
            size++;
        } else {

            //If the historic aggregated outcome already exists with regards to outcome id then this is another of the same outcome, add it to the
            //aggregated outcome.
            //Else this is a new outcome and create a new aggregated outcome.
            int lastIndex = historicOutcomesList.lastIndexOf(new HistoricOutcomeList(outcome.getExternalKey()));
            if (lastIndex != -1) {
                HistoricOutcomeList existingHistoricOutcomeList = historicOutcomesList.get(lastIndex);
                existingHistoricOutcomeList.add(outcome);
            } else {
                HistoricOutcomeList newHistoricOutomeList = new HistoricOutcomeList();
                newHistoricOutomeList.setId(outcome.getExternalKey());
                newHistoricOutomeList.setName(outcome.getName());
                newHistoricOutomeList.add(outcome);
                historicOutcomesList.add(newHistoricOutomeList);
            }
        }
    }

    public int size() {
        return size;
    }

    public Iterator<Entry<DateInPlay, List<HistoricOutcomeList>>> iterator() {
        return timeline.entrySet().iterator();
    }
}
