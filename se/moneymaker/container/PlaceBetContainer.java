package se.moneymaker.container;

import java.util.List;
import org.json.simple.JSONArray;
import se.moneymaker.jsonfactory.JSONFactoryReadRequests;
import se.moneymaker.model.Account;

public class PlaceBetContainer {

    private Account account;
    private double requestedStakeLocal;
    private List<PlaceBetItem> items;
    private String betOfferExternalKey;
    private String outcomeExternalKey;

    public String getBetOfferExternalKey() {
        return betOfferExternalKey;
    }

    public void setBetOfferExternalKey(String betOfferExternalKey) {
        this.betOfferExternalKey = betOfferExternalKey;
    }

    public String getOutcomeExternalKey() {
        return outcomeExternalKey;
    }

    public void setOutcomeExternalKey(String outcomeExternalKey) {
        this.outcomeExternalKey = outcomeExternalKey;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public double getRequestedStakeLocal() {
        return requestedStakeLocal;
    }

    public void setRequestedStakeLocal(double requestedStakeLocal) {
        this.requestedStakeLocal = requestedStakeLocal;
    }

    public List<PlaceBetItem> getItems() {
        return items;
    }

    public void setItems(List<PlaceBetItem> items) {
        this.items = items;
    }

    public JSONArray toJSONOutcomeReferenceRequest() {
        JSONArray request = new JSONArray();
        long pk = items.get(0).getOutcome().getPk();
        request.add(JSONFactoryReadRequests.createOutcomeReferenceRequest(pk, -1, null, account.getBookmaker().getName()));
        return request;
    }
}
