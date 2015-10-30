package com.betfair.aping.containers;

import se.betfair.model.AccountDetailsResponse;

public class AccountDetailsResponseContainer extends Container {

    private AccountDetailsResponse result;

    public AccountDetailsResponse getResult() {
        return result;
    }

    public void setResult(AccountDetailsResponse result) {
        this.result = result;
    }
}
