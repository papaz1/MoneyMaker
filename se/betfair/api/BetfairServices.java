package se.betfair.api;

import com.betfair.aping.containers.AccountDetailsResponseContainer;
import com.betfair.aping.containers.AccountFundsResponseContainer;
import com.betfair.aping.containers.AccountStatementReportContainer;
import com.betfair.aping.containers.CancelOrdersContainer;
import com.betfair.aping.containers.ClearedOrderSummaryReportContainer;
import se.betfair.model.ClearedOrderSummaryReport;
import com.betfair.aping.containers.CurrentOrderSummaryReportContainer;
import com.betfair.aping.containers.ListMarketBooksContainer;
import com.betfair.aping.containers.ListMarketCatalogueContainer;
import com.betfair.aping.containers.PlaceOrdersContainer;
import com.betfair.aping.enums.*;
import com.betfair.aping.exceptions.APINGException;
import se.betfair.util.JsonConverter;
import se.betfair.util.JsonrpcRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import se.betfair.model.AccountDetailsResponse;
import se.betfair.model.AccountFundsResponse;
import se.betfair.model.AccountStatementReport;
import se.betfair.model.CancelExecutionReport;
import se.betfair.model.ClearedOrderSummary;
import se.betfair.model.CurrentOrderSummaryReport;
import se.betfair.model.Token;
import se.betfair.model.MarketBook;
import se.betfair.model.MarketCatalogue;
import se.betfair.model.MarketFilter;
import se.betfair.model.PlaceExecutionReport;
import se.betfair.model.PlaceInstruction;
import se.betfair.model.PlaceInstructionReport;
import se.betfair.model.PriceProjection;
import se.betfair.model.TimeRange;
import se.moneymaker.db.DBServices;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.BetException;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.util.Log;

public class BetfairServices {

    private final String URL_RPC_SERVICES_BETTING = "https://api.betfair.com/exchange/betting/json-rpc/v1";
    private final String URL_RPC_SERVICES_ACCOUNT = "https://api.betfair.com/exchange/account/json-rpc/v1";
    private final String CLASSNAME = BetfairServices.class.getName();
    private final String FILTER = "filter";
    private final String LOCALE = "locale";
    private final String MAX_RESULT = "maxResults";
    private final String MARKET_IDS = "marketIds";
    private final String MARKET_ID = "marketId";
    private final String INSTRUCTIONS = "instructions";
    private final String MARKETROJECTION = "marketProjection";
    private final String PRICEPROJECTION = "priceProjection";
    private final String CURRENCYCODE = "currencyCode";
    private final String BET_IDS = "betIds";
    private final String BET_STATUS = "betStatus";
    private final String SETTLED_DATE_RANGE = "settledDateRange";
    private final String FROM_RECORD = "fromRecord";
    private final String ITEM_DATE_RANGE = "itemDateRange";
    private final String INCLUDE_ITEM = "includeItem";
    private final String WALLET = "wallet";
    private final String locale = Locale.getDefault().toString();
    private Connection connectionAccountFunds;
    private Connection connectionAccountStatement;
    private Connection connectionMarketCatalogue;
    private Connection connectionMarketBook;
    private Connection connectionPlaceOrders;
    private Connection connectionCurrentOrders;
    private Connection connectionCancelOrders;
    private Connection connectionKeepAlive;
    private Connection connectionClearedOrders;
    private Connection connectionLogout;
    private Connection connectionAccoundDetails;
    private String sessionToken;
    private String accountName;

    public BetfairServices(String accountName) {
        this.accountName = accountName;
    }

    public BetfairServices(String sessionToken, String accountName) {
        this.sessionToken = sessionToken;
        this.accountName = accountName;
    }

    public AccountFundsResponse getAccountFunds() throws APINGException {
        final String METHOD = "getAccountFunds";
        final int ACCOUNT_FUNDS_DELAY = 1000;
        if (connectionAccountFunds == null) {
            connectionAccountFunds = new Connection(URL_RPC_SERVICES_ACCOUNT, accountName);
        }
        AccountFundsResponse accountFundsResponse = null;
        Map<String, Object> params = new HashMap<>();
        params.put(WALLET, "UK"); //This should be in the account object instead in the future since this is account dependent
        String result = makeRequest(ApiNgOperation.GET_ACCOUNT_FUNDS, params, connectionAccountFunds, ACCOUNT_FUNDS_DELAY);
        AccountFundsResponseContainer container = JsonConverter.convertFromJson(result, AccountFundsResponseContainer.class);
        if (container != null) {
            if (container.getError() != null) {
                throw container.getError().getData().getAPINGException();
            } else {
                accountFundsResponse = container.getResult();
            }
        } else {
            Log.logMessage(CLASSNAME, METHOD, "Container was null. Raw data returned: " + result, LogLevelEnum.WARNING, true);
        }
        return accountFundsResponse;
    }

    //This is not used in current release
    public AccountStatementReport getAccountStatement(TimeRange itemDateRange) throws APINGException {
        final String METHOD = "getAccountStatement";
        final int ACCOUNT_STATEMENT_DELAY = 1000;
        if (connectionAccountStatement == null) {
            connectionAccountStatement = new Connection(URL_RPC_SERVICES_ACCOUNT, accountName);
        }
        AccountStatementReport accountStatementReport = null;
        Map<String, Object> params = new HashMap<>();
        params.put(ITEM_DATE_RANGE, itemDateRange);
        params.put(INCLUDE_ITEM, "ALL");
        String result = makeRequest(ApiNgOperation.GET_ACCOUNT_STATEMENT, params, connectionMarketCatalogue, ACCOUNT_STATEMENT_DELAY);

        AccountStatementReportContainer container = JsonConverter.convertFromJson(result, AccountStatementReportContainer.class);
        if (container != null) {
            if (container.getError() != null) {
                throw container.getError().getData().getAPINGException();
            } else {
                accountStatementReport = container.getResult();
            }
        } else {
            Log.logMessage(CLASSNAME, METHOD, "Container was null. Raw data returned: " + result, LogLevelEnum.WARNING, true);
        }
        return accountStatementReport;
    }

    public List<MarketCatalogue> listMarketCatalogue(MarketFilter filter, Set<String> marketProjection, String pSessionToken) throws APINGException {
        final String METHOD = "readMarketCatalogue";
        final int DATA_REQUEST_LIMIT = 1000;
        final int MARKET_CATALOGUE_DELAY = 0;
        synchronized (this) {
            if (connectionMarketCatalogue == null) {
                connectionMarketCatalogue = new Connection(URL_RPC_SERVICES_BETTING, accountName);
            }
        }
        List<MarketCatalogue> marketCatalogues = null;
        Map<String, Object> params = new HashMap<>();
        params.put(FILTER, filter);
        params.put(MARKETROJECTION, marketProjection);
        params.put(MAX_RESULT, DATA_REQUEST_LIMIT);

        String result;
        if (pSessionToken != null) {
            result = makeRequest(ApiNgOperation.LISTMARKETCATALOGUE, params, connectionMarketCatalogue, MARKET_CATALOGUE_DELAY, pSessionToken);
        } else {
            result = makeRequest(ApiNgOperation.LISTMARKETCATALOGUE, params, connectionMarketCatalogue, MARKET_CATALOGUE_DELAY);
        }

        ListMarketCatalogueContainer container = JsonConverter.convertFromJson(result, ListMarketCatalogueContainer.class);
        if (container != null) {
            if (container.getError() != null) {
                throw container.getError().getData().getAPINGException();
            } else {
                marketCatalogues = container.getResult();
                if (marketCatalogues.size() == DATA_REQUEST_LIMIT) {
                    Set<String> marketTypeCodes = filter.getMarketTypeCodes();
                    Log.logMessage(CLASSNAME, METHOD, "Maximum number of records (" + DATA_REQUEST_LIMIT + ") returned for " + marketTypeCodes.iterator().next(), LogLevelEnum.WARNING, false);
                }
            }
        } else {
            throw new APINGException("Betfair MarketCatalogue service (market information) returned null", "", "");
        }
        return marketCatalogues;
    }

    public List<MarketBook> listMarketBook(List<String> marketIds, PriceProjection priceProjection, String currency, String pSessionToken) throws APINGException {
        final int DATA_REQUEST_LIMIT = 40;
        final int MARKET_BOOK_DELAY = 0;
        synchronized (this) {
            if (connectionMarketBook == null) {
                connectionMarketBook = new Connection(URL_RPC_SERVICES_BETTING, accountName);
            }
        }
        ListMarketBooksContainer container = new ListMarketBooksContainer();
        String result;
        Map<String, Object> params = new HashMap<>();
        params.put(PRICEPROJECTION, priceProjection);
        params.put(CURRENCYCODE, currency);
        if (marketIds.size() > DATA_REQUEST_LIMIT) {
            int counter = 0;
            List<String> marketIdsSubList = new ArrayList<>();

            //It might be necessary to split the marketIds due to data request limits
            for (String id : marketIds) {
                counter++;
                marketIdsSubList.add(id);
                if (marketIdsSubList.size() == DATA_REQUEST_LIMIT || counter == marketIds.size()) {

                    params.put(MARKET_IDS, marketIdsSubList);
                    if (pSessionToken != null) {
                        result = makeRequest(ApiNgOperation.LISTMARKETBOOK, params, connectionMarketBook, MARKET_BOOK_DELAY, pSessionToken);
                    } else {
                        result = makeRequest(ApiNgOperation.LISTMARKETBOOK, params, connectionMarketBook, MARKET_BOOK_DELAY);
                    }
                    ListMarketBooksContainer tmpContainer = JsonConverter.convertFromJson(result, ListMarketBooksContainer.class);
                    if (tmpContainer != null) {
                        if (tmpContainer.getError() != null) {
                            throw tmpContainer.getError().getData().getAPINGException();
                        }
                        container.addResult(tmpContainer.getResult());
                    } else {
                        throw new APINGException("Betfair MarketBook service (prices) returned null", "", "");
                    }
                    marketIdsSubList.clear();
                    params.remove(MARKET_IDS);
                }
            }
        } else {
            params.put(MARKET_IDS, marketIds);
            if (pSessionToken != null) {
                result = makeRequest(ApiNgOperation.LISTMARKETBOOK, params, connectionMarketBook, MARKET_BOOK_DELAY, pSessionToken);
            } else {
                result = makeRequest(ApiNgOperation.LISTMARKETBOOK, params, connectionMarketBook, MARKET_BOOK_DELAY);
            }
            container = JsonConverter.convertFromJson(result, ListMarketBooksContainer.class);
            if (container != null) {
                if (container.getError() != null) {
                    throw container.getError().getData().getAPINGException();
                }
            } else {
                String error = "MarketBook container was null. Raw data returned: " + result;
                throw new APINGException(error, "", "");
            }
        }
        return container.getResult();
    }

    //The data request limit, maximum of 250 betids, is taken care of by the betupdater. If more is sent then Betfair API will raise an exception.
    public CurrentOrderSummaryReport listCurrentOrders(Set<String> betIds) throws APINGException {
        final int CURRENT_ORDERS_DELAY = 1000;
        if (connectionCurrentOrders == null) {
            connectionCurrentOrders = new Connection(URL_RPC_SERVICES_BETTING, accountName);
        }
        Map<String, Object> params = new HashMap<>();
        params.put(LOCALE, locale);
        params.put(BET_IDS, betIds);
        String result = makeRequest(ApiNgOperation.LISTCURRENTORDERS, params, connectionCurrentOrders, CURRENT_ORDERS_DELAY);

        CurrentOrderSummaryReportContainer container = JsonConverter.convertFromJson(result, CurrentOrderSummaryReportContainer.class);

        if (container.getError() != null) {
            throw container.getError().getData().getAPINGException();
        }

        return container.getResult();
    }

    public ClearedOrderSummaryReport listClearedOrders(Set<String> betIds, BetStatus betStatus, TimeRange settledDateRange, MarketGroupBy value) throws APINGException {
        final int CLEARED_ORDERS_DELAY = 1000;
        final int MAX_RECORDS_RETURNED = 1000;
        int fromIndex = 0;
        boolean hasMore = true;
        List<ClearedOrderSummary> orderSummary = new ArrayList<>();
        Set<String> betIdsTmp = new HashSet<>();

        //betIdsTmp.add("57989602104");
        //betIds = betIdsTmp;
        if (connectionClearedOrders == null) {
            connectionClearedOrders = new Connection(URL_RPC_SERVICES_BETTING, accountName);
        }
        Map<String, Object> params = new HashMap<>();
        params.put(BET_STATUS, betStatus);
        if (betIds != null) {
            params.put(BET_IDS, betIds);
        } else {
            params.put(SETTLED_DATE_RANGE, settledDateRange);
            params.put(FROM_RECORD, fromIndex);
        }
        if (value != null) {
            params.put(value.getKey(), value);
        }

        while (hasMore) {
            String result = makeRequest(ApiNgOperation.LISTCLEAREDORDERS, params, connectionClearedOrders, CLEARED_ORDERS_DELAY);
            ClearedOrderSummaryReportContainer container = JsonConverter.convertFromJson(result, ClearedOrderSummaryReportContainer.class);

            if (container.getError() != null) {
                throw container.getError().getData().getAPINGException();
            }

            ClearedOrderSummaryReport orderSummaryReportTmp = container.getResult();
            List<ClearedOrderSummary> orderSummaryTmp = orderSummaryReportTmp.getClearedOrders();
            orderSummary.addAll(orderSummaryTmp);
            hasMore = orderSummaryReportTmp.isMoreAvailable();
            if (hasMore) {
                fromIndex += MAX_RECORDS_RETURNED;
                params.put(FROM_RECORD, fromIndex);
            }
        }
        ClearedOrderSummaryReport orderSummaryReport = new ClearedOrderSummaryReport();
        orderSummaryReport.setClearedOrders(orderSummary);
        return orderSummaryReport;
    }

    public PlaceExecutionReport placeOrders(String marketId, List<PlaceInstruction> instructions, String sessionToken) throws APINGException, BetException, BetOfferException {
        final String METHOD = "placeOrders";
        final int ORDER_DELAY = 0;
        synchronized (this) {
            if (connectionPlaceOrders == null) {
                connectionPlaceOrders = new Connection(URL_RPC_SERVICES_BETTING, accountName);
            }
        }
        Map<String, Object> params = new HashMap<>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);
        params.put(INSTRUCTIONS, instructions);
        String result = makeRequest(ApiNgOperation.PLACEORDERS, params, connectionPlaceOrders, ORDER_DELAY, sessionToken);
        PlaceOrdersContainer container = JsonConverter.convertFromJson(result, PlaceOrdersContainer.class);

        if (container.getError() != null) {
            throw container.getError().getData().getAPINGException();
        }

        PlaceExecutionReport executionReport = container.getResult();
        ExecutionReportErrorCode executionReportErrorCode = executionReport.getErrorCode();
        if (executionReportErrorCode != null) {
            if (executionReportErrorCode.equals(ExecutionReportErrorCode.MARKET_SUSPENDED)) {
                throw new BetOfferException("Market suspended", ErrorType.MARKET_SUSPENDED);
            } else {
                Log.logMessage(CLASSNAME, METHOD, executionReportErrorCode.name() + " JSON: " + result, LogLevelEnum.ERROR, true);
                throw new BetOfferException(executionReportErrorCode.getMessage(), ErrorType.BETFAIR_ERROR);
            }
        }

        if (executionReport.getStatus().equals(ExecutionReportStatus.FAILURE)) {
            List<PlaceInstructionReport> instructionReports = executionReport.getInstructionReports();
            for (PlaceInstructionReport instructionReport : instructionReports) {
                InstructionReportStatus reportStatus = instructionReport.getStatus();
                if (reportStatus.equals(InstructionReportStatus.FAILURE)) {
                    InstructionReportErrorCode errorCode = instructionReport.getErrorCode();
                    if (errorCode.equals(InstructionReportErrorCode.INVALID_BET_SIZE)) {
                        throw new BetException("Bet size is invalid", ErrorType.INVALID_BET_SIZE);
                    } else {
                        Log.logMessage(CLASSNAME, METHOD, result, LogLevelEnum.ERROR, true);
                    }
                }
            }
        }
        return executionReport;
    }

    public CancelExecutionReport cancelOrders(String marketId) throws APINGException, BetException {
        final int CANCEL_ODER_DELAY = 0;
        if (connectionCancelOrders == null) {
            connectionCancelOrders = new Connection(URL_RPC_SERVICES_BETTING, accountName);
        }
        Map<String, Object> params = new HashMap<>();
        params.put(LOCALE, locale);
        params.put(MARKET_ID, marketId);

        String result = makeRequest(ApiNgOperation.CANCELORDERS, params, connectionCancelOrders, CANCEL_ODER_DELAY);

        CancelOrdersContainer container = JsonConverter.convertFromJson(result, CancelOrdersContainer.class);

        if (container.getError() != null) {
            throw container.getError().getData().getAPINGException();
        }

        CancelExecutionReport executcionReport = container.getResult();
        if (!executcionReport.getStatus().equals(ExecutionReportStatus.SUCCESS)) {
            String msg = "Status: " + executcionReport.getStatus() + " Msg: " + executcionReport.getErrorCode().getMessage();
            throw new BetException(msg, ErrorType.BET_CANCELLATION_ERROR);
        }
        return container.getResult();
    }

    public void keepAlive(String sessionToken) throws APINGException {
        sessionAlive(sessionToken);
    }

    public void keepAlive() throws APINGException {
        sessionAlive(null);
    }

    private void sessionAlive(String tmpSession) throws APINGException {
        final int SESSION_ALIVE_DELAY = 1000;
        final String KEEP_ALIVE_TARGET = "https://identitysso.betfair.com/api/keepAlive";
        if (connectionKeepAlive == null) {
            connectionKeepAlive = new Connection(KEEP_ALIVE_TARGET, accountName);
        }
        String sToken;
        if (tmpSession != null) {
            sToken = tmpSession;
        } else {
            sToken = sessionToken;
        }
        String result = connectionKeepAlive.sendPostRequestJson(sToken, SESSION_ALIVE_DELAY);
        Token token = JsonConverter.convertFromJson(result, Token.class);
        if (!token.getStatus().equalsIgnoreCase("success")) {
            throw new APINGException("Status: " + token.getStatus() + " Error: " + token.getError(), "", "");
        }
    }

    public AccountDetailsResponse getAccountDetails() throws APINGException {
        final int ACCOUNT_DETAILS_DELAY = 1000;
        if (connectionAccoundDetails == null) {
            connectionAccoundDetails = new Connection(URL_RPC_SERVICES_ACCOUNT, accountName);
        }
        Map<String, Object> params = new HashMap<>();
        String result = makeRequest(ApiNgOperation.GET_ACCOUNT_DETAILS, params, connectionAccoundDetails, ACCOUNT_DETAILS_DELAY);
        AccountDetailsResponseContainer container = JsonConverter.convertFromJson(result, AccountDetailsResponseContainer.class);

        if (container.getError() != null) {
            throw container.getError().getData().getAPINGException();
        }

        return container.getResult();
    }

    public void logout(String pSessionToken) {
        final String METHOD = "logout";
        final int LOGOUT_DELAY = 0;
        final String LOGOUT_TARGET = "https://identitysso.betfair.com/api/logout";
        synchronized (this) {
            if (connectionLogout == null) {
                connectionLogout = new Connection(LOGOUT_TARGET, accountName);
            }
        }
        String result = connectionLogout.sendPostRequestJson(pSessionToken, LOGOUT_DELAY);
        Token token = JsonConverter.convertFromJson(result, Token.class);
        if (!token.getStatus().equalsIgnoreCase("success")) {
            Log.logMessage(CLASSNAME, METHOD, "Status: " + token.getStatus() + " Error: " + token.getError(), LogLevelEnum.ERROR, true);
        }
    }

    private String makeRequest(ApiNgOperation operation, Map<String, Object> params, Connection requester, int delay, String pSessionToken) {
        String requestString = createRequestString(operation, params);
        return requester.sendPostRequestJsonRpc(pSessionToken, requestString, delay);
    }

    private String makeRequest(ApiNgOperation operation, Map<String, Object> params, Connection requester, int delay) {
        String requestString = createRequestString(operation, params);
        return requester.sendPostRequestJsonRpc(sessionToken, requestString, delay);
    }

    private String createRequestString(ApiNgOperation operation, Map<String, Object> params) {
        String requestString;
        //Handling the JSON-RPC request
        JsonrpcRequest request = new JsonrpcRequest();
        request.setId("1");
        request.setMethod(operation.getOperationGroup() + operation.getOperationName());
        request.setParams(params);
        requestString = JsonConverter.convertToJson(request);

        return requestString;
    }
}
