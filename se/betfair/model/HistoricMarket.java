package se.betfair.model;

import java.util.ArrayList;
import java.util.List;

public class HistoricMarket {

    private List<String> unparsedStrings = new ArrayList<>();
    private String sportsId;
    private String marketId;
    private String fullDescription;
    private String scheduledOff;
    private String event;
    private String dtActualOff;
    private List<BMHistoricRunner> historicRunners = new ArrayList<>();

    public void setSportsId(String sportsId) {
        this.sportsId = sportsId;
    }

    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public void setScheduledOff(String scheduledOff) {
        this.scheduledOff = scheduledOff;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setDtActualOff(String dtActualOff) {
        this.dtActualOff = dtActualOff;
    }

    public void addUnparsedString(String unparsedString) {
        unparsedStrings.add(unparsedString);
    }

    public void addAllUnparsedStrings(List<String> unparsedStrings) {
        this.unparsedStrings.addAll(unparsedStrings);
    }

    public void addHistoricRunner(BMHistoricRunner historicRunner) {
        historicRunners.add(historicRunner);
    }

    public void addAllHistoricRunners(List<BMHistoricRunner> historicRunners) {
        this.historicRunners.addAll(historicRunners);
    }

    public String getSportsId() {
        return sportsId;
    }

    public String getMarketId() {
        return marketId;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public String getSchduledOff() {
        return scheduledOff;
    }

    public String getEvent() {
        return event;
    }

    public String getDtActualOff() {
        return dtActualOff;
    }

    public List<String> getUnparsedStrings() {
        return unparsedStrings;
    }

    public String getUnparsedMarketString() {
        StringBuilder builder = new StringBuilder();
        int counter = 0;
        for (String str : unparsedStrings) {
            counter++;
            if (counter < unparsedStrings.size()) {
                builder.append(str).append(System.getProperty("line.separator"));
            } else {
                builder.append(str);
            }
        }
        return builder.toString();
    }

    public List<BMHistoricRunner> getHistoricRunners() {
        return historicRunners;
    }
}
