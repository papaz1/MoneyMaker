package se.betfair.config;

public enum BCOutcomeNameEnum {

    DRAW("THE DRAW"),
    OVER("OVER"),
    UNDER("UNDER");
    private final String originalName;

    BCOutcomeNameEnum(String originalName) {
        this.originalName = originalName;
    }

    public String getOriginalName() {
        return originalName;
    }
}