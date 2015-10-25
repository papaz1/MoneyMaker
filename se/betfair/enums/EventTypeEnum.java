package se.betfair.enums;

/*
 * Enum for event type
 *
 *------------------------------------------------------------------------------
 * Change History
 *------------------------------------------------------------------------------
 * Version      Date         Author          Comments
 *------------------------------------------------------------------------------
 * 1.0          2011-apr-08  Baran Sölen     Initial version
 */
public enum EventTypeEnum {

    ALL(-1, "All"),
    SOCCER(1, "Soccer"),
    TENNIS(2, "Tennis"),
    GOLF(3, "Golf"),
    CRICKET(4, "Cricket"),
    RUGBY_UNION(5, "Rugby Union"),
    BOXING(6, "Boxing"),
    HORSE_RACING(7, "Horse Racing"),
    MOTOR_SPORT(8, "Motor Sport"),
    SPECIAL_BETS(10, "Special Bets"),
    CYCLING(11, "Cycling"),
    ROWING(12, "Rowing"),
    HORSE_RACING_TODAYS_CARD(13, "Horse Racing Todays Card"),
    SOCCER_FIXTURES(14, "Soccer Fixtures"),
    GREYHOUND_TODAYS_CARD(15, "Greyhound Todays Card"),
    RUGBY_LEAGUE(1477, "Rugby League"),
    DARTS(3503, "Darts"),
    ATHLETICS(3988, "Athletics"),
    GREYHOUND_RACING(4339, "Greyhound Racing"),
    FINANCIAL_BETS(6231, "Financial Bets"),
    SNOOKER(6422, "Snooker"),
    AMERICAN_FOOTBALL(6423, "American Football"),
    BASEBALL(7511, "Baseball"),
    BASKETBALL(7522, "Basketball"),
    HOCKEY(7523, "Hockey"),
    ICE_HOCKEY(7524, "Ice Hockey"),
    SUMO_WRESTLING(7525, "Sumo Wrestling"),
    AUSTRALIAN_RULES(61420, "Australian Rules"),
    GAELIC_FOOTBALL(66598, "Gaelic Football"),
    HURLING(66599, "Hurling"),
    POOL(72382, "Pool"),
    CHESS(136332, "Chess"),
    TROTTING(256284, "Trotting"),
    COMMONWEALTH_GAMES(300000, "Commonwealth Games"),
    POKER(315220, "Poker"),
    WINTER_SPORTS(451485, "Winter Sports"),
    HANDBALL(468328, "Handball"),
    BADMINTON(627555, "Badminton"),
    INTERNATIONAL_RULES(678378, "International Rules"),
    BRIDGE(982477, "Bridge"),
    VOLLEYBALL(998917, "Volleyball"),
    BOWLS(998919, "Bowls"),
    FLOORBALL(998920, "Floorball"),
    NETBALL(606611, "Netball"),
    YACHTING(998916, "Yachting"),
    SWIMMING(620576, "Swimming"),
    EXCHANGE_POKER(1444073, "Exchange Poker"),
    BACKGAMMON(1938544, "Backgammon"),
    GAA_SPORTS(2030972, "Gaa Sports"),
    GAELIC_GAMES(2152880, "Gaelic Games"),
    INTERNATIONAL_MARKETS(2264869, "International Markets"),
    POLITICS(2378961, "Politics");
    private final int id;
    private final String name;

    EventTypeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
