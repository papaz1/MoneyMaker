package com.betfair.aping.containers;

import se.betfair.model.CurrentOrderSummaryReport;

public class CurrentOrderSummaryReportContainer extends Container {

    private CurrentOrderSummaryReport result;

    public CurrentOrderSummaryReport getResult() {
        return result;
    }

    public void setResult(CurrentOrderSummaryReport result) {
        this.result = result;
    }
}
