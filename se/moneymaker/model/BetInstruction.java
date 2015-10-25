package se.moneymaker.model;

public class BetInstruction {

    private String betOfferExternalKey;
    private long outcomeExternalKey;
    private boolean isBack;
    private double price;
    private double size;
    private Account account;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getBetOfferExternalKey() {
        return betOfferExternalKey;
    }

    public void setBetOfferExternalKey(String betOfferExternalKey) {
        this.betOfferExternalKey = betOfferExternalKey;
    }

    public long getOutcomeExternalKey() {
        return outcomeExternalKey;
    }

    public void setOutcomeExternalKey(long outcomeExternalKey) {
        this.outcomeExternalKey = outcomeExternalKey;
    }

    public boolean isIsBack() {
        return isBack;
    }

    public void setIsBack(boolean isBack) {
        this.isBack = isBack;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
}
