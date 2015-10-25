package se.moneymaker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.moneymaker.enums.PoolType;

public class Match {

    private List<BetOffer> betOffers = new ArrayList<>();
    private String externalKey;
    private Date eventDate;
    private String homeTeam;
    private String awayTeam;
    private long Pk;
    private int homeScore = -1; //-1 means no score has been set which is better than 0 which is a valid score
    private int awayScore = -1;
    private int homeRedCards;
    private int awayRedCards;
    private Date utcEncounter;
    private Date matchTime;
    private String source;
    private MatchReference reference;
    private PoolType poolType;

    public PoolType getPoolType() {
        return poolType;
    }

    public void setPoolType(PoolType poolType) {
        this.poolType = poolType;
    }

    public MatchReference getReference() {
        return reference;
    }

    public void setReference(MatchReference reference) {
        this.reference = reference;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getMatchTime() {
        return matchTime;
    }

    public void setMatchTime(Date currentMatchTime) {
        this.matchTime = currentMatchTime;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }

    public long getPk() {
        return Pk;
    }

    public void setPk(long Pk) {
        this.Pk = Pk;
    }

    public void setHome(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public void setAway(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public void setBetOffer(List<BetOffer> betOffers) {
        this.betOffers = betOffers;
    }

    public void addBetOffer(BetOffer betOffer) {
        betOffers.add(betOffer);
    }

    public Date getEventDate() {
        return eventDate;
    }

    public List<BetOffer> getBetOffers() {
        return betOffers;
    }

    public String getHome() {
        return homeTeam;
    }

    public String getAway() {
        return awayTeam;
    }

    public int getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(int homeScore) {
        this.homeScore = homeScore;
    }

    public int getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(int awayScore) {
        this.awayScore = awayScore;
    }

    public void setAwayRedCards(int homeRedCards) {
        this.homeRedCards = homeRedCards;
    }

    public void setHomeRedCards(int awayRedCards) {
        this.awayRedCards = awayRedCards;
    }

    public Date getUTCEncounter() {
        return utcEncounter;
    }

    public void setUTCEncounter(Date utcEncounter) {
        this.utcEncounter = utcEncounter;
    }
}
