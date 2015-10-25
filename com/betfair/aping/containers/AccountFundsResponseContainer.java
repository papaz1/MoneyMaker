package com.betfair.aping.containers;

import se.betfair.model.AccountFundsResponse;

public class AccountFundsResponseContainer extends Container {

    private AccountFundsResponse result;

    public AccountFundsResponse getResult() {
        return result;
    }

    public void setResult(AccountFundsResponse result) {
        this.result = result;
    }
}
