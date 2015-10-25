package se.moneymaker.container;

import se.moneymaker.model.Account;

public class LoginContainer {

    private Account account;
    private String password;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
