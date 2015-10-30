package se.moneymaker.model;

import java.util.Date;

public class AccountTransaction {

    private String id;
    private String bookmaker;
    private String accountName;
    private Date utcTransaction;
    private String depositedLocal = "0";
    private String withdrawnLocal = "0";
    private double internalFeeLocal;
    private String externalFeeLocal = "0";
    private String comment;

    public AccountTransaction(String bookmaker, String accountName) {
        this.bookmaker = bookmaker;
        this.accountName = accountName;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setExternalFeeLocal(String externalFeeLocal) {
        this.externalFeeLocal = externalFeeLocal;
    }

    public String getExternalFeeLocal() {
        return externalFeeLocal;
    }

    public Date getUtcTransaction() {
        return utcTransaction;
    }

    public void setUtcTransaction(Date utcTransaction) {
        this.utcTransaction = utcTransaction;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getBookmaker() {
        return bookmaker;
    }

    public String getDepositedLocal() {
        return depositedLocal;
    }

    public void setDepositedLocal(String depositedLocal) {
        this.depositedLocal = depositedLocal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getInternalFeeLocal() {
        return internalFeeLocal;
    }

    public void setInternalFeeLocal(double internalFeeLocal) {
        this.internalFeeLocal = internalFeeLocal;
    }

    public Date getUTCTransaction() {
        return utcTransaction;
    }

    public void setUTCTransaction(Date utcTransaction) {
        this.utcTransaction = utcTransaction;
    }

    public String getWithdrawnLocal() {
        return withdrawnLocal;
    }

    public void setWithdrawnLocal(String withdrawnLocal) {
        this.withdrawnLocal = withdrawnLocal;
    }
}
