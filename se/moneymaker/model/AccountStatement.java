package se.moneymaker.model;

import java.util.Date;

/*
 *
 *
 * ------------------------------------------------------------------------------
 * Change History
 * ------------------------------------------------------------------------------
 * Version Date Author Comments
 * ------------------------------------------------------------------------------
 * 1.0 2012-mar-10 Baran Sölen Initial version
 */
public class AccountStatement {

    private Date utcStatement;
    private String statementLocal;
    private String bookmaker;
    private String accountName;

    public AccountStatement(String bookmaker, String accountName) {
        this.bookmaker = bookmaker;
        this.accountName = accountName;
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
