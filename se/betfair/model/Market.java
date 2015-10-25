package se.betfair.model;

import java.util.Date;
import java.util.List;
import se.betfair.enums.EventTypeEnum;

public class Market {

    //Market filters
    public static final String SOCCER_FIXTURES = "FIXTURES";
    public static final String SOCCER_MATCHES = "MATCHES";
    //Used for message logging
    private int id;
    private String marketName;
    private String betType;
    private String marketStatus;
    private Date eventDate; //The date and time the event starts (in milliseconds since January 1 1970 00:00:00 GMT)
    private Date actualEventDate;
    private String menuPath;
    private List<String> hierarchyTextPath;
    private String hierarchyTextPath2;
    private String betDelay;
    private int exchangeId;
    private String countryISOCode;
    /*
     * The time (in milliseconds since January 1 1970 00:00:00 GMT)
     * since the cached market data was last refreshed from the exchange database.
     * The API caches market information for 5 minutes.
     */
    private Date lastRefresh;
    private int numberOfRunners;
    private int numberOfWinners;
    private double totalAmountMatched;
    private boolean BSPMarket; //If True, indicates that the market supports Betfair Starting Price bets.
    private boolean inPlay; //True for turning in play
    private boolean include; //Used to decide if this is a relevant market for betting
    private List<Integer> hierarchyIdPath;
    private String hierarchyIdPath2;
    private String home;
    private String away;
    private EventTypeEnum eventType;
    private String country;

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public EventTypeEnum getEventType() {
        return eventType;
    }

    public void setEventType(EventTypeEnum eventType) {
        this.eventType = eventType;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String marketName) {
        this.marketName = marketName;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void setAway(String away) {
        this.away = away;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public void setStatus(String marketStatus) {
        this.marketStatus = marketStatus;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public void setActualEventDate(Date actualEventDate) {
        this.actualEventDate = actualEventDate;
    }

    public void setMenuPath(String menuPath) {
        this.menuPath = menuPath;
    }

    public void setBetDelay(String betDelay) {
        this.betDelay = betDelay;
    }

    public void setExchangeId(int exchangeId) {
        this.exchangeId = exchangeId;
    }

    public void setCountryISOCode(String countryISOCode) {
        this.countryISOCode = countryISOCode;
    }

    public void setLastRefresh(Date lastRefresh) {
        this.lastRefresh = lastRefresh;
    }

    public void setNumberOfRunners(int numberOfRunners) {
        this.numberOfRunners = numberOfRunners;
    }

    public void setNumberOfWinners(int numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public void setTotalAmountMatched(double totalAmountMatched) {
        this.totalAmountMatched = totalAmountMatched;
    }

    public void setBSPMarket(boolean BSPMarket) {
        BSPMarket = this.BSPMarket;
    }

    public void setInPlay(boolean inPlay) {
        this.inPlay = inPlay;
    }

    public void setHierarchyTextPathNoFix(List<String> hierarchyTextPath) {
        this.hierarchyTextPath = hierarchyTextPath;
    }

    /**
     * This will be used in the sorting of all markets
     *
     * @param hierarchyTextPath2
     */
    public void setHierarchyTextPathNoFix2(String hierarchyTextPath2) {
        this.hierarchyTextPath2 = hierarchyTextPath2;
    }

    public void setHierarchyIdPathNoFix(List<Integer> hierarchyIdPath) {
        this.hierarchyIdPath = hierarchyIdPath;
    }

    public void setIncludeMarket(boolean include) {
        this.include = include;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return marketName;
    }

    public String getBetType() {
        return betType;
    }

    public String getStatus() {
        return marketStatus;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public Date getActualEventDate() {
        return actualEventDate;
    }

    public String getMenuPath() {
        return menuPath;
    }

    public List<String> getHierarchyTextPathNoFix() {
        return hierarchyTextPath;
    }

    public String getHierarchyTextPathNoFix2() {
        return hierarchyTextPath2;
    }

    public String getBetDelay() {
        return betDelay;
    }

    public int getExchangeId() {
        return exchangeId;
    }

    public String getCountryISOCode() {
        return countryISOCode;
    }

    public Date getLastRefresh() {
        return lastRefresh;
    }

    public int getNumberOfRunners() {
        return numberOfRunners;
    }

    public int getNumberOfWinners() {
        return numberOfWinners;
    }

    public double getTotalAmountMatched() {
        return totalAmountMatched;
    }

    public boolean getBSPMarket() {
        return BSPMarket;
    }

    public boolean isInPlay() {
        return inPlay;
    }

    public List<Integer> getHierarchyIdPathNoFix() {
        return hierarchyIdPath;
    }

    public String getHierarchyIdPathNoFix2() {
        return hierarchyIdPath2;
    }

    public boolean include() {
        return include;
    }

    public String getHome() {
        return home;
    }

    public String getAway() {
        return away;
    }
}
