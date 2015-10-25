package se.betfair.model;

/*
 * Hold data for a runner from the historic markets data file
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author          Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-maj-19      Baran Sölen     Initial version
 */
public class BMHistoricRunner {

    private String selectionId;
    private String selection;
    private String odds;
    private String numberBets;
    private String volumeMatched;
    private String latestTaken;
    private String firstTaken;
    private String isWin;
    private String inPlay;

    public void setSelectionId(String selectionId) {
        this.selectionId = selectionId;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public void setOdds(String odds) {
        this.odds = odds;
    }

    public void setNumberBets(String numberBets) {
        this.numberBets = numberBets;
    }

    public void setVolumeMatched(String volumeMatched) {
        this.volumeMatched = volumeMatched;
    }

    public void setLatestTaken(String latestTaken) {
        this.latestTaken = latestTaken;
    }

    public void setFirstTaken(String firstTaken) {
        this.firstTaken = firstTaken;
    }

    public void setWinFlag(String isWin) {
        this.isWin = isWin;
    }

    public void setInPlay(String inPlay) {
        this.inPlay = inPlay;
    }

    public String getSelectionId() {
        return selectionId;
    }

    public String getSelection() {
        return selection;
    }

    public String getOdds() {
        return odds;
    }

    public String getNumberBets() {
        return numberBets;
    }

    public String getVolumeMatched() {
        return volumeMatched;
    }

    public String getLatestTaken() {
        return latestTaken;
    }

    public String getFirstTaken() {
        return firstTaken;
    }

    public String isWin() {
        return isWin;
    }

    public String getInPlay() {
        return inPlay;
    }
}
