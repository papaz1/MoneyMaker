package se.moneymaker.model;

import java.util.Date;

public class AccountStatement {

    private Date utcStatement;
    private String statementLocal;
    private String bookmaker;
    private String accountName;
    private String currency;

    public AccountStatement(String bookmaker, String accountName, String currency) {
        this.bookmaker = bookmaker;
        this.accountName = accountName;
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getBookmaker() {
        return bookmaker;
    }

    public String getStatementLocal() {
        return statementLocal;
    }

    public void setStatementLocal(String statementLocal) {
        this.statementLocal = statementLocal;
    }

    public Date getUTCStatement() {
        return utcStatement;
    }

    public void setUTCStatement(Date utcStatement) {
        this.utcStatement = utcStatement;
    }
}
