package se.moneymaker.db;

import se.moneymaker.enums.ApiServiceName;
import se.moneymaker.enums.ApiConnectionEnum;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import se.moneymaker.dict.Config;
import se.moneymaker.enums.ConfigEnum;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

public class DBConnection {

    private final String CLASSNAME = DBConnection.class.getName();
    public final static int NUMBER_OF_ATTEMPTS = 4;
    private final WebResource resource;
    private static final String TYPE = "application/json";
    private final ClientConfig config;
    private final Client client;
    private static final String MSG_NULL = "Response from Betprover is null";
    private static final String MSG_CONNECTION = "Connection to Betprover could not be established";
    private static final String AUTHORIZATION = "AUTHORIZATION";
    private final String apiKey;

    public DBConnection(boolean isPublicDomain) {
        Config mmConfig = Config.getInstance();
        if (isPublicDomain) {
            apiKey = mmConfig.get(ConfigEnum.MM_PUB_API_KEY);
        } else {
            apiKey = mmConfig.get(ConfigEnum.MM_PRI_API_KEY);
        }

        //Setup the REST client
        config = new DefaultClientConfig();
        client = Client.create(config);
        resource = client.resource(mmConfig.get(ConfigEnum.MM_BETPROVER));
    }

    public void sendRequest(ApiServiceName service, JSONArray objectList, ApiConnectionEnum operation) throws DBConnectionException {
        final String METHOD = "sendRequest";

        //Send matches to MoneyMaker database
        ClientResponse response = null;
        int numberOfAttempts = 0;
        boolean success = false;
        while (!success && numberOfAttempts < NUMBER_OF_ATTEMPTS) {
            try {
                if (operation.equals(ApiConnectionEnum.POST)) {
                    response = resource.path(service.getServiceName()).header(AUTHORIZATION, apiKey).accept(TYPE).type(TYPE).post(ClientResponse.class, objectList.toString());
                } else if (operation.equals(ApiConnectionEnum.GET)) {
                    response = resource.path(service.getServiceName()).accept(TYPE).type(TYPE).get(ClientResponse.class);
                }
                success = true;
            } catch (ClientHandlerException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage() + ". " + numberOfAttempts + " attempt", LogLevelEnum.ERROR, false);
                numberOfAttempts++;
            }
        }

        if (success && response != null) {
            Status responseStatus = response.getClientResponseStatus();
            if (!responseStatus.equals(Status.OK)) {
                JSONObject errorJSON = (JSONObject) JSONValue.parse(response.getEntity(String.class));
                DBConnectionException exception = createDBException(errorJSON, objectList.toString());
                throw exception;
            }
        } else {
            if (!success) {
                throw new DBConnectionException(MSG_CONNECTION);
            } else {
                throw new DBConnectionException(MSG_NULL);
            }
        }
    }

    public JSONArray sendRequestResponse(ApiServiceName service, JSONArray objectList, ApiConnectionEnum operation) throws DBConnectionException {
        final String METHOD = "sendRequestResponse";
        JSONArray insertedList = new JSONArray();
        ClientResponse response = null;
        int numberOfAttempts = 0;
        boolean success = false;
        while (!success && numberOfAttempts < NUMBER_OF_ATTEMPTS) {
            try {
                if (operation.equals(ApiConnectionEnum.POST)) {
                    String request;
                    
                    if (service.getServiceName().startsWith("read")) {
                        JSONObject object = (JSONObject) objectList.get(0);
                        request = object.toString();
                    } else {
                        request = objectList.toString();
                    }
                    response = resource.path(service.getServiceName()).header(AUTHORIZATION, apiKey).accept(TYPE).type(TYPE).post(ClientResponse.class, request);
                } else if (operation.equals(ApiConnectionEnum.GET)) {
                    response = resource.path(service.getServiceName()).accept(TYPE).type(TYPE).get(ClientResponse.class);
                }
                success = true;
            } catch (ClientHandlerException e) {
                Log.logMessage(CLASSNAME, METHOD, e.getMessage() + ". " + numberOfAttempts + " attempt", LogLevelEnum.ERROR, false);
                numberOfAttempts++;
            }
        }

        if (success && response != null) {
            Status responseStatus = response.getClientResponseStatus();
            if (!responseStatus.equals(Status.OK)) {
                JSONObject errorJSON = (JSONObject) JSONValue.parse(response.getEntity(String.class));
                DBConnectionException exception = createDBException(errorJSON, objectList.toString());
                throw exception;
            } else {
                insertedList = (JSONArray) JSONValue.parse(response.getEntity(String.class));
            }
        } else {
            if (!success) {
                throw new DBConnectionException(MSG_CONNECTION);
            } else {
                throw new DBConnectionException(MSG_NULL);
            }
        }
        return insertedList;
    }

    private DBConnectionException createDBException(JSONObject errorJSON, String jsonRequest) {
        String errorType = null;
        String uuid;
        String error;

        try {
            errorType = getErrorType(errorJSON);
            uuid = ((String) errorJSON.get("uuid"));
            StringBuilder sb = new StringBuilder();
            sb.append("uuid: ").append(uuid).append(". errorType: $").append(errorType).append("$").append(" JSON request: ").append(jsonRequest);
            error = sb.toString();
        } catch (NullPointerException e) {
            if (errorJSON != null) {
                error = "NullPointerException parsing error. JSON object: " + errorJSON.toString();
            } else {
                error = "Error message returned from DB was null";
            }
        }
        DBConnectionException exception = new DBConnectionException(error);
        exception.setRequest(jsonRequest);
        exception.setErrorType(errorType);

        return exception;
    }

    private String getErrorType(JSONObject errorJSON) {
        return ((String) errorJSON.get("errorType"));
    }
}
