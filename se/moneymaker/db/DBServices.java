package se.moneymaker.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.moneymaker.container.BetOfferIdsContainer;
import se.moneymaker.container.PlaceBetContainer;
import se.moneymaker.container.PlaceBetItem;
import se.moneymaker.enums.ApiConnectionEnum;
import se.moneymaker.enums.ApiServiceName;
import se.moneymaker.enums.DBBoolean;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.exception.NoDataException;
import se.moneymaker.jsonfactory.JSONFactoryAccount;
import se.moneymaker.jsonfactory.JSONFactoryBet;
import se.moneymaker.jsonfactory.JSONFactoryBetOffer;
import se.moneymaker.jsonfactory.JSONFactoryMatch;
import se.moneymaker.jsonfactory.JSONFactoryOutcome;
import se.moneymaker.jsonfactory.JSONFactoryReadRequests;
import se.moneymaker.jsonfactory.JSONKeyNames;
import se.moneymaker.model.Account;
import se.moneymaker.model.Bet;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Match;
import se.moneymaker.model.Outcome;

public class DBServices {

    private final DBConnection connection;

    public DBServices(boolean isPublicDomain) {
        connection = new DBConnection(isPublicDomain);
    }

    public String readCurrency(String bookmaker) throws DBConnectionException {
        JSONArray currencyCall = new JSONArray();
        currencyCall.add(JSONFactoryAccount.createCurrencyCodeReadCall(bookmaker));
        JSONArray results = connection.sendRequestResponse(ApiServiceName.READ_CURRENCY, currencyCall, ApiConnectionEnum.POST);

        if (results.size() > 1) {
            throw new DBConnectionException("More than one currency returned for: " + bookmaker);
        } else {
            JSONObject result = (JSONObject) results.get(0);
            JSONObject currencyJSON = new JSONObject();
            currencyJSON = (JSONObject) result.get(JSONKeyNames.KEY_CURRENCY);
            String currency = (String) currencyJSON.get(JSONKeyNames.KEY_PK);
            return currency;
        }
    }

    public long readMatchPk(Match match) throws DBConnectionException {
        JSONObject matchJSON = JSONFactoryMatch.createMatch(match);
        JSONArray matchesJSON = new JSONArray();
        matchesJSON.add(matchJSON);
        JSONArray results = connection.sendRequestResponse(ApiServiceName.MATCH, matchesJSON, ApiConnectionEnum.POST);

        if (results.size() > 1) {
            throw new DBConnectionException("More than one match returned for: " + match.getHome() + " vs " + match.getAway());
        } else {
            JSONObject result = (JSONObject) results.get(0);
            long Pk = (Long) result.get(ApiServiceName.MATCH.getPk());
            return Pk;
        }
    }

    public List<Bet> readBets(DBBoolean isUpdated, String[] states, String[] externalKeys, String bookmaker, String accountName, boolean includeUtcValidUntil) throws DBConnectionException {
        JSONArray request = new JSONArray();
        request.add(JSONFactoryBet.createBetQuery(bookmaker, accountName, isUpdated, states, externalKeys, includeUtcValidUntil));
        JSONArray response;
        response = connection.sendRequestResponse(ApiServiceName.READ_BETS, request, ApiConnectionEnum.POST);

        List<Bet> bets = null;
        if (!response.isEmpty()) {
            JSONObject betForUpdateJSON;
            Iterator<JSONObject> iterator = response.iterator();
            bets = new ArrayList<>();
            while (iterator.hasNext()) {
                betForUpdateJSON = iterator.next();
                bets.add(JSONFactoryBet.createBets(betForUpdateJSON, bookmaker, accountName));
            }
        }
        return bets;
    }

    //The returning betoffers will be populated with the Pk
    public BetOffer insertBetOffer(long matchDBIB, BetOffer betOffer) throws DBConnectionException {
        BetOffer betOfferCopy = betOffer;
        JSONArray betOffersJSON = new JSONArray();
        betOffersJSON.add(JSONFactoryBetOffer.createJSONBetOffer(matchDBIB, betOfferCopy));

        JSONArray results = connection.sendRequestResponse(ApiServiceName.WRITE_BETOFFER, betOffersJSON, ApiConnectionEnum.POST);
        if (results.size() > 1) {
            throw new DBConnectionException("More than one betoffer returned for: " + betOfferCopy.getName() + " matchPk: " + matchDBIB);
        } else {
            JSONObject betOfferWithOutcomes = (JSONObject) results.get(0);

            //Get the Pks
            long betOfferPk = (long) betOfferWithOutcomes.get(JSONKeyNames.KEY_BETOFFER);
            betOfferCopy.setPk(betOfferPk);

            //Outcomes in resulting list are ordered in the same way as in the list in the betoffer
            int index = 0;
            JSONArray outcomesJSON = (JSONArray) betOfferWithOutcomes.get(JSONKeyNames.KEY_OUTCOMES);
            Iterator<Long> iterator = outcomesJSON.iterator();
            List<Outcome> outcomes = betOfferCopy.getOutcomes();
            while (iterator.hasNext()) {
                Long outcomePk = iterator.next();
                Outcome outcome = outcomes.get(index);
                outcome.setPk(outcomePk);
                index++;
            }

            JSONArray betOfferReferences = new JSONArray();
            betOfferReferences.add(JSONFactoryBetOffer.createJSONBetOfferReference(betOfferCopy));
            connection.sendRequest(ApiServiceName.WRITE_BETOFFER_REFERENCE, betOfferReferences, ApiConnectionEnum.POST);

            JSONArray outcomeReferences = JSONFactoryOutcome.createOutcomeReferences(outcomes);
            connection.sendRequest(ApiServiceName.WRITE_OUTCOME_REFERENCE, outcomeReferences, ApiConnectionEnum.POST);
        }
        return betOfferCopy;
    }

    public void sendRequest(ApiServiceName service, JSONArray objectList, ApiConnectionEnum operation) throws DBConnectionException {
        connection.sendRequest(service, objectList, operation);
    }

    public JSONArray sendRequestResponse(ApiServiceName service, JSONArray objectList, ApiConnectionEnum operation) throws DBConnectionException {
        return connection.sendRequestResponse(service, objectList, operation);
    }

    public List<String> readBetOfferExternalKeys(BetOfferIdsContainer container) throws DBConnectionException {
        JSONArray result = connection.sendRequestResponse(ApiServiceName.READ_BETOFFER_REFERENCE, container.toJSONRequest(), ApiConnectionEnum.POST);

        List<String> marketIDs = new ArrayList<>();
        Iterator<JSONObject> iterator = result.iterator();
        while (iterator.hasNext()) {
            JSONObject marketIDMap = iterator.next();
            marketIDs.add((String) marketIDMap.get(JSONKeyNames.KEY_EXTERNAL_KEY));
        }
        return marketIDs;
    }

    public List<PlaceBetContainer> readPopulateExternalKeys(List<PlaceBetContainer> placeBetContainers) throws DBConnectionException, NoDataException {
        List<PlaceBetContainer> result = new ArrayList<>(placeBetContainers.size());

        //Each container contains one bet on one outcome
        for (PlaceBetContainer placeBetContainer : placeBetContainers) {
            PlaceBetItem placeBetItem = placeBetContainer.getItems().get(0);
            long outcomePk = placeBetItem.getOutcome().getPk();
            Account account = placeBetContainer.getAccount();
            String source = account.getBookmaker().getName();

            //Everything ready to create the request
            JSONObject outcomeReference = readOutcomeReference(outcomePk, -1, null, source);

            //Get the outcome external key that will be updated in the container
            String externalKey = (String) outcomeReference.get(JSONKeyNames.KEY_EXTERNAL_KEY);
            placeBetContainer.setOutcomeExternalKey(externalKey);

            //Now the betoffer external key needs to be read by using betoffer pk
            JSONObject outcome = (JSONObject) outcomeReference.get(JSONKeyNames.KEY_OUTCOME);
            JSONObject betOffer = (JSONObject) outcome.get(JSONKeyNames.KEY_BETOFFER);
            Long betOfferPk = (Long) betOffer.get(JSONKeyNames.KEY_PK);
            List<Long> betOfferPkList = new ArrayList<>(1);
            betOfferPkList.add(betOfferPk);

            //Get the response
            JSONObject betOfferReference = readBetOfferReference(betOfferPkList, null, source);

            //Get the betoffer external key that will be updated in the container
            externalKey = (String) betOfferReference.get(JSONKeyNames.KEY_EXTERNAL_KEY);
            placeBetContainer.setBetOfferExternalKey(externalKey);

            result.add(placeBetContainer);
        }
        return result;
    }

    private JSONObject readBetOfferReference(List<Long> betOfferPkList, String externalKey, String source) throws DBConnectionException, NoDataException {
        JSONArray request = new JSONArray();
        JSONObject betOfferReferenceJSON;
        betOfferReferenceJSON = JSONFactoryReadRequests.createBetOfferReferenceRequest(betOfferPkList, externalKey, source);
        request.add(betOfferReferenceJSON);
        JSONArray response = connection.sendRequestResponse(ApiServiceName.READ_BETOFFER_REFERENCE, request, ApiConnectionEnum.POST);
        if (response.size() == 0) {
            throw new NoDataException("Betoffer reference not found for external key: " + externalKey, ErrorType.BETPROVER_ERROR);
        }
        return (JSONObject) response.get(0);
    }

    private JSONObject readOutcomeReference(long outcomePk, long betOfferPk, String outcomeExternalKey, String source) throws DBConnectionException {
        JSONObject outcomeReferenceJSON = JSONFactoryReadRequests.createOutcomeReferenceRequest(outcomePk, betOfferPk, outcomeExternalKey, source);
        JSONArray request = new JSONArray();
        request.add(outcomeReferenceJSON);

        //Get the response
        JSONArray response = connection.sendRequestResponse(ApiServiceName.READ_OUTCOME_REFERENCE, request, ApiConnectionEnum.POST);
        return (JSONObject) response.get(0);
    }

    public long readOutcomePk(String bookmaker, String betOfferExternalKey, String outcomeExternalKey) throws DBConnectionException, NoDataException {
        JSONObject betOfferReference = readBetOfferReference(null, betOfferExternalKey, bookmaker);
        JSONObject betOffer = (JSONObject) betOfferReference.get("betOffer");
        long betOfferPk = (Long) betOffer.get(JSONKeyNames.KEY_PK);
        JSONObject outcomeReference = readOutcomeReference(-1, betOfferPk, outcomeExternalKey, bookmaker);
        JSONObject outcome = (JSONObject) outcomeReference.get("outcome");
        return (Long) outcome.get(JSONKeyNames.KEY_PK);
    }
}
