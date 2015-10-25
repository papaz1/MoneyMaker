package se.betfair.model;

import java.util.Date;
import java.util.Map;

public class StatementItem {

    private String refId;
    private Date itemDate;
    private double amount;
    private double balance;
    private String itemClass;
    private Map<String, String> itemClassData;
    private StatementLegacyData legacyData;

    public StatementLegacyData getLegacyData() {
        return legacyData;
    }

    public void setLegacyData(StatementLegacyData legacyData) {
        this.legacyData = legacyData;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public Date getItemDate() {
        return itemDate;
    }

    public void setItemDate(Date itemDate) {
        this.itemDate = itemDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getItemClass() {
        return itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }

    public Map<String, String> getItemClassData() {
        return itemClassData;
    }

    public void setItemClassData(Map<String, String> itemClassData) {
        this.itemClassData = itemClassData;
    }

}
