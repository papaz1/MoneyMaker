package se.betfair.model;

public class DeveloperAppVersion {
    private String owner;
    private Long versionId;
    private String version;
    private String applicationKey;
    private boolean delayData;
    private boolean subscriptionRequired;
    private boolean ownerManaged;
    private boolean active;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public boolean isDelayData() {
        return delayData;
    }

    public void setDelayData(boolean delayData) {
        this.delayData = delayData;
    }

    public boolean isSubscriptionRequired() {
        return subscriptionRequired;
    }

    public void setSubscriptionRequired(boolean subscriptionRequired) {
        this.subscriptionRequired = subscriptionRequired;
    }

    public boolean isOwnerManaged() {
        return ownerManaged;
    }

    public void setOwnerManaged(boolean ownerManaged) {
        this.ownerManaged = ownerManaged;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
