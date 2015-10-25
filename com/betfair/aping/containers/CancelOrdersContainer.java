package com.betfair.aping.containers;

import se.betfair.model.CancelExecutionReport;

public class CancelOrdersContainer extends Container {

    private CancelExecutionReport result;

    public CancelExecutionReport getResult() {
        return result;
    }

    public void setResult(CancelExecutionReport result) {
        this.result = result;
    }
}
