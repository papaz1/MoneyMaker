package se.moneymaker.model;

import se.moneymaker.enums.BetStateEnum;
import java.util.Date;

public class Bet {

    private BetStateEnum state;
    private double canceledStakeLocal;
    private String Pk;
    private String externalKey;
    private Date validUntil;
    private double matchedStakeLocal;
    private double requestedOdds;
    private double matchedOdds;
    private String bookmaker;
    private String accountName;
    private double paidOutLocal;
    private String statementAfterSettledLocal;
    private double unmatchedStakeLocal;
    private double restStakeLocal;
    private double requestedStakeLocal;
    private String betOfferName;
    private String outcomeExternalKey;
    private int isBack = -1;
    private String betOfferId;
    private Date utcPlaced;
    private long outcomePk;
    private String betCommentBefore;
    private String providerBetStatus;
    private double commission;
    private double unexpectedPlusLocal;
    private double unexpectedMinusLocal;
    private String currency;

    public double getUnexpectedPlusLocal() {
        return unexpectedPlusLocal;
    }

    public void setUnexpectedPlusLocal(double unexpectedPlusLocal) {
        this.unexpectedPlusLocal = unexpectedPlusLocal;
    }

    public double getUnexpectedMinusLocal() {
        return unexpectedMinusLocal;
    }

    public void setUnexpectedMinusLocal(double unexpectedMinusLocal) {
        this.unexpectedMinusLocal = unexpectedMinusLocal;
    }

    public Bet(String bookmaker, String accountName) {
        this.bookmaker = bookmaker;
        this.accountName = accountName;
    }

    public double getCommission() {
        return commission;
    }

    public void setCommission(double commission) {
        this.commission = commission;
    }

    public double getUnmatchedStakeLocal() {
        return unmatchedStakeLocal;
    }

    public void setUnmatchedStakeLocal(double unmatchedStakeLocal) {
        this.unmatchedStakeLocal = unmatchedStakeLocal;
    }

    public Date getUtcPlaced() {
        return utcPlaced;
    }

    public void setUtcPlaced(Date utcPlaced) {
        this.utcPlaced = utcPlaced;
    }

    public BetStateEnum getState() {
        return state;
    }

    public void setState(BetStateEnum state) {
        this.state = state;
    }

    public String getProviderBetStatus() {
        return providerBetStatus;
    }

    public void setProviderBetStatus(String providerBetStatus) {
        this.providerBetStatus = providerBetStatus;
    }

    public String getBetCommentBefore() {
        return betCommentBefore;
    }

    public void setBetCommentBefore(String betCommentBefore) {
        this.betCommentBefore = betCommentBefore;
    }

    public long getOutcomePk() {
        return outcomePk;
    }

    public void setOutcomePk(long outcomePk) {
        this.outcomePk = outcomePk;
    }

    public Date getUTCPlaced() {
        return utcPlaced;
    }

    public void setUTCPlaced(Date utcPlaced) {
        this.utcPlaced = utcPlaced;
    }

    public String getBetOfferId() {
        return betOfferId;
    }

    public void setBetOfferId(String betOfferId) {
        this.betOfferId = betOfferId;
    }

    public int getIsBack() {
        return isBack;
    }

    public void setIsBack(int isBack) {
        this.isBack = isBack;
    }

    public String getOutcomeExternalKey() {
        return outcomeExternalKey;
    }

    public void setOutcomeExternalKey(String outcomeExternalKey) {
        this.outcomeExternalKey = outcomeExternalKey;
    }

    public String getBetOfferName() {
        return betOfferName;
    }

    public void setBetOfferName(String betOfferName) {
        this.betOfferName = betOfferName;
    }

    public double getRequestedStakeLocal() {
        return requestedStakeLocal;
    }

    public void setRequestedStakeLocal(double requestedStakeLocal) {
        this.requestedStakeLocal = requestedStakeLocal;
    }

    public double getRestStakeLocal() {
        return restStakeLocal;
    }

    public void setRestStakeLocal(double restStakeLocal) {
        this.restStakeLocal = restStakeLocal;
    }

    public String getStatementAfterSettledLocal() {
        return statementAfterSettledLocal;
    }

    public void setStatementAfterBetLocal(String statementAfterSettledLocal) {
        this.statementAfterSettledLocal = statementAfterSettledLocal;
    }

    public double getPaidOutLocal() {
        return paidOutLocal;
    }

    public void setPaidOutLocal(double paidOutLocal) {
        this.paidOutLocal = paidOutLocal;
    }

    public double getCanceledStakeLocal() {
        return canceledStakeLocal;
    }

    public void setCanceledStakeLocal(double canceledStakeLocal) {
        this.canceledStakeLocal = canceledStakeLocal;
    }

    public String getPk() {
        return Pk;
    }

    public void setPk(String pk) {
        this.Pk = pk;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getBookmaker() {
        return bookmaker;
    }

    public double getMatchedOdds() {
        return matchedOdds;
    }

    public void setMatchedOdds(double matchedOdds) {
        this.matchedOdds = matchedOdds;
    }

    public double getRequestedOdds() {
        return requestedOdds;
    }

    public void setRequestedOdds(double requestedOdds) {
        this.requestedOdds = requestedOdds;
    }

    public double getMatchedStakeLocal() {
        return matchedStakeLocal;
    }

    public void setMatchedStakeLocal(double matchedStakeLocal) {
        this.matchedStakeLocal = matchedStakeLocal;
    }

    public String getExternalKey() {
        return externalKey;
    }

    public void setExternalKey(String externalID) {
        this.externalKey = externalID;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
