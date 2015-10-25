package com.betfair.aping.containers;

import se.betfair.model.ClearedOrderSummaryReport;

public class ClearedOrderSummaryReportContainer extends Container {

    private ClearedOrderSummaryReport result;

    public ClearedOrderSummaryReport getResult() {
        return result;
    }

    public void setResult(ClearedOrderSummaryReport result) {
        this.result = result;
    }

}
