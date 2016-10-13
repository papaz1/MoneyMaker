package se.betfair.model;

import java.util.List;

public class DeveloperApp {
    private String appName;
    private Long appId;
    private List<DeveloperAppVersion> appVersions; 

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public List<DeveloperAppVersion> getAppVersions() {
        return appVersions;
    }

    public void setAppVersions(List<DeveloperAppVersion> appVersions) {
        this.appVersions = appVersions;
    }
}
