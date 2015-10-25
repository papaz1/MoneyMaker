package se.pinnacle.enums;

public enum ServicesEnum {

    GET_CURRRENCIES("https://api.pinnaclesports.com/v1/currencies"),
    GET_FEED("https://api.pinnaclesports.com/v1/feed"),
    GET_LEAGUES("https://api.pinnaclesports.com/v1/leagues"),
    GET_SPORTS("https://api.pinnaclesports.com/v1/sports");

    private String url;

    private ServicesEnum(String url) {
        this.url = url;
    }

    public String getURL() {
        return url;
    }
}
