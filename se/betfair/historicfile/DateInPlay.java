package se.betfair.historicfile;

import java.util.Date;

public class DateInPlay implements Comparable {

    private Date latestTaken;
    private boolean inPlay;

    public DateInPlay(Date latestTaken, boolean inPlay) {
        this.latestTaken = latestTaken;
        this.inPlay = inPlay;
    }

    public boolean isInPlay() {
        return inPlay;
    }

    public void setInPlay(boolean inPlay) {
        this.inPlay = inPlay;
    }

    public Date getLatestTaken() {
        return latestTaken;
    }

    public void setLatestTaken(Date latestTaken) {
        this.latestTaken = latestTaken;
    }

    public long getTime() {
        return latestTaken.getTime();
    }

    @Override
    public int compareTo(Object o) {
        DateInPlay dip = (DateInPlay) o;
        Date d1 = this.getLatestTaken();
        Date d2 = dip.getLatestTaken();
        return d1.compareTo(d2);
    }
}
