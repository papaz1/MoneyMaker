package se.betfair.model;

/*
 * 
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author         Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-03-18   Baran Sölen    Initial version
 */
public class BMNumOutcomesWinners {

    private int numberOfOutcomes;
    private int numberOfWinners;

    public BMNumOutcomesWinners(int numberOfOutcomes, int numberOfWinners) {
        this.numberOfOutcomes = numberOfOutcomes;
        this.numberOfWinners = numberOfWinners;
    }

    public int getNumberOfOutcomes() {
        return numberOfOutcomes;
    }

    public int getNumberOfWinners() {
        return numberOfWinners;
    }
}
