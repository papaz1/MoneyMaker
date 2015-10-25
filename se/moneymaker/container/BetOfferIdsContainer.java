package se.moneymaker.container;

import java.util.List;
import org.json.simple.JSONArray;
import se.moneymaker.jsonfactory.JSONFactoryReadRequests;
import se.moneymaker.model.Account;

public class BetOfferIdsContainer {

    private String source;
    private List<Long> betOfferPkList;
    private Account account;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<Long> getbetOfferPkList() {
        return betOfferPkList;
    }

    public void setbetOfferPkList(List<Long> betOfferPkList) {
        this.betOfferPkList = betOfferPkList;
    }

    public JSONArray toJSONRequest() {
        JSONArray request = new JSONArray();
        request.add(JSONFactoryReadRequests.createBetOfferReferenceRequest(betOfferPkList, null, source));
        return request;
    }
}
