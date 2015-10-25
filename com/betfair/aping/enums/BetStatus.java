package com.betfair.aping.enums;

public enum BetStatus {

    MATCHED("MATCHED"),
    UNMATCHED("UNMATCHED"),
    SETTLED("SETTLED"),
    VOIDED("VOIDED"),
    LAPSED("LAPSED"),
    CANCELLED("CANCELLED");

    private String status;

    private BetStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
