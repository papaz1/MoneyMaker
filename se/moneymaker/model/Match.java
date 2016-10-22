package se.moneymaker.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import se.moneymaker.enums.PoolType;

public class Match {

    private List<BetOffer> betOffers = new ArrayList<>();
    private String externalKey;
    private String eventGroupName;
    private Date eventDate;
    private String home;
    private String away;
    private long Pk;
    private int homeScore = -1; //-1 means no score has been set which is better than 0 which is a valid score
    private int awayScore = -1;
    private Date utcEncounter;
    private Date matchTime;
    private String source;
    private MatchReference reference;
    private PoolType poolType;
    private String eventName;
    private String poolName;
    private String poolExternalKey;
    private String homeExternalKey;
    private String awayExternalKey;

    public String getEventGroupName() {
        return eventGroupName;
    }

    public void setEventGroupName(String eventGroupName) {
        this.eventGroupName = eventGroupName;
    }

    public String getHomeExternalKey() {
        return homeExternalKey;
    }

    public void setHomeExternalKey(String homeExternalKey) {
        this.homeExternalKey = homeExternalKey;
    }

    public String getAwayExternalKey() {
        return awayExternalKey;
    }

    public void setAwayExternalKey(String awayExternalKey) {
        this.awayExternalKey = awayExternalKey;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getPoolExternalKey() {
        return poolExternalKey;
    }

    public void setPoolExternalKey(String poolExternalKey) {
        this.poolExternalKey = poolExternalKey;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

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
        this.home = homeTeam;
    }

    public void setAway(String awayTeam) {
        this.away = awayTeam;
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
        return home;
    }

    public String getAway() {
        return away;
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

    public Date getUTCEncounter() {
        return utcEncounter;
    }

    public void setUTCEncounter(Date utcEncounter) {
        this.utcEncounter = utcEncounter;
    }
}
