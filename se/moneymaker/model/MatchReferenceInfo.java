package se.moneymaker.model;

public class MatchReferenceInfo {

    private String source;
    private String externalKey;
    private String humanText;

    public MatchReferenceInfo(String source, String externalKey, String humanText) {
        this.source = source;
        this.externalKey = externalKey;
        this.humanText = humanText;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalKey) {
        this.externalKey = externalKey;
    }

    public String getHumanText() {
        return humanText;
    }

    public void setHumanText(String humanText) {
        this.humanText = humanText;
    }
}
