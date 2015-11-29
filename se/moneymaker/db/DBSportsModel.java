package se.moneymaker.db;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import se.moneymaker.enums.ApiServiceName;
import se.moneymaker.enums.ApiConnectionEnum;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.moneymaker.enums.DBErrorType;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.jsonfactory.JSONFactoryMatchInfo;
import se.moneymaker.jsonfactory.JSONFactoryMatch;
import se.moneymaker.jsonfactory.JSONFactoryPrice;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Match;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.Price;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Source;
import se.moneymaker.jsonfactory.JSONKeyNames;
import se.moneymaker.util.Utils;

public class DBSportsModel {

    private final String CLASSNAME = DBSportsModel.class.getName();
    private final DBServices services;
    private Set<String> matchesNotFound;
    private File matchesNotFoundInDBFile;
    private Source source;
    public static int tmpCounter;

    //This constructor is only relevant for services
    public DBSportsModel() {
        services = new DBServices(true);
        matchesNotFound = new HashSet<>();
    }

    //This constructor is only relevant for applications
    public DBSportsModel(Source source) {
        services = new DBServices(true);
        matchesNotFound = new HashSet<>();
        this.source = source;
        createMatchesNotFoundFile();
    }

    private void createMatchesNotFoundFile() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Date d = new Date();
        String today = df.format(d);
        File file = new File(Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        matchesNotFoundInDBFile = new File(file.getParent() + System.getProperty("file.separator") + "workdir" + System.getProperty("file.separator") + source + "_" + today + ".txt");
    }

    public void reset() {
        if (matchesNotFound != null) {
            matchesNotFound.clear();
            matchesNotFoundInDBFile.delete();
            createMatchesNotFoundFile();
        }
    }

    public List<Match> updateScores(List<Match> matches) throws DBConnectionException {
        final String METHOD = "updateScores";
        List<Match> result = new ArrayList<>();
        JSONArray currentScores = new JSONArray();
        JSONArray timeReferencesJSON = new JSONArray();
        for (Match match : matches) {
            try {

                //Find the match, build an array with the score objects then send all scores in one call
                if (match.getPk() < 1) {
                    long matchPk = services.readMatchPk(match);
                    match.setPk(matchPk);
                }
                currentScores.add(JSONFactoryMatch.createCurrentScore(match));
                //services.sendRequest(ApiServiceName.CURRENT_SCORE, currentScoresJSON, ApiConnectionEnum.POST);
                //currentScoresJSON.clear();
                //timeReferencesJSON.add(JSONFactoryMatch.createTimeReference(match));
            } catch (DBConnectionException e) {
                if (e.getErrorType().equals(DBErrorType.MATCH_NOT_FOUND.toString())) {
                    if (matchesNotFound != null
                            && !matchesNotFound.contains(match.getExternalKey())
                            && e.getErrorType().equals(DBErrorType.MATCH_NOT_FOUND.toString())) {
                        matchesNotFound.add(match.getExternalKey());
                        StringBuilder sb = new StringBuilder();
                        sb.append("Event date: ").append(match.getEventDate()).append(" Teams: ").append(match.getHome()).append(" vs ").append(match.getAway());
                        List<String> matchesNotFoundList = new ArrayList<>(1);
                        matchesNotFoundList.add(sb.toString());
                        matchesNotFound.add(sb.toString());
                        Utils.writeToFile(matchesNotFoundList, matchesNotFoundInDBFile);
                    }
                }
            }
            result.add(match);
        }
        if (!currentScores.isEmpty()) {
            Log.logMessage(CLASSNAME, METHOD, "Number of live scores being sent: " + currentScores.size() + "/" + matches.size(), LogLevelEnum.INFO, false);
            try {
                services.sendRequest(ApiServiceName.WRITE_CURRENT_SCORE, currentScores, ApiConnectionEnum.POST);
            } catch (DBConnectionException e) {

                //Send the scores objects one by one and the one that can't be inserted should have its pk set to 0 to indicate error
                if (e.getErrorType().equals(DBErrorType.INCONSISTENT_SCORE.toString())) {

                    Iterator<JSONObject> iterator = currentScores.iterator();
                    while (iterator.hasNext()) {
                        JSONArray currentScores2 = new JSONArray();
                        JSONObject score = iterator.next();
                        currentScores2.add(score);
                        try {
                            services.sendRequest(ApiServiceName.WRITE_CURRENT_SCORE, currentScores2, ApiConnectionEnum.POST);
                        } catch (DBConnectionException e2) {
                            if (e2.getErrorType().equals(DBErrorType.INCONSISTENT_SCORE.toString())) {
                                for (Match match : result) {
                                    JSONObject event = (JSONObject) score.get(JSONFactoryMatch.KEY_EVENT);
                                    long pk = (Long) event.get(JSONKeyNames.KEY_PK);
                                    if (match.getPk() == pk) {
                                        match.setPk(0);//Clear the pk
                                    }
                                }
                            } else {
                                throw e2;
                            }
                        }
                    }
                } else {
                    throw e;
                }
            }
            //services.sendRequest(ApiServiceName.EVENT_TIME_REFERENCE, timeReferencesJSON, ApiConnectionEnum.POST);
        } else {
            if (!matches.isEmpty()) {
                Log.logMessage(CLASSNAME, METHOD, "No matches found in Betprover (" + matches.size() + ")", LogLevelEnum.INFO, false);
            }
        }
        return result;
    }

    public Match insertSportsModel(Match match, boolean isHistoric) throws DBConnectionException {
        final String METHOD = "insertSportsModel";
        Match matchCopy = match;

        //If the process has been parallized there could be errors due to this. Attempt to send these
        //numberOfAttempts times.
        int numberOfAttempts = 0;
        boolean success = false;
        if (matchesNotFound.contains(matchCopy.getExternalKey())) {
            DBConnectionException dbException = new DBConnectionException();
            dbException.setErrorType(DBErrorType.MATCH_NOT_FOUND.toString());
            throw dbException;
        }
        while (!success) {
            try {
                if (matchCopy.getPk() < 1) {
                    long pk = services.readMatchPk(matchCopy);
                    matchCopy.setPk(pk);
                }
                success = true;
            } catch (DBConnectionException e) {
                if (matchesNotFound != null
                        && !matchesNotFound.contains(matchCopy.getExternalKey())
                        && e.getErrorType().equals(DBErrorType.MATCH_NOT_FOUND.toString())) {
                    matchesNotFound.add(matchCopy.getExternalKey());
                    StringBuilder sb = new StringBuilder();
                    sb.append("Event date: ").append(matchCopy.getEventDate()).append(" Teams: ").append(matchCopy.getHome()).append(" vs ").append(matchCopy.getAway());
                    List<String> matchesNotFoundList = new ArrayList<>(1);
                    matchesNotFoundList.add(sb.toString());
                    Utils.writeToFile(matchesNotFoundList, matchesNotFoundInDBFile);
                }
                processDBException(e, numberOfAttempts, METHOD);
            }
        }
        matchCopy = insertBetOffersOutcomes(matchCopy, isHistoric);
        return matchCopy;
    }

    public void updateSportsModel(Match match) throws DBConnectionException {
        List<BetOffer> betOffers;
        betOffers = match.getBetOffers();
        for (BetOffer betOffer : betOffers) {
            insertPrices(betOffer.getOutcomes(), false);
        }
    }

    public void matchInfo(String previousmmDbId) throws DBConnectionException {
        final String METHOD = "matchInfo";
        JSONArray matchInfoJSON = new JSONArray();
        matchInfoJSON.add(JSONFactoryMatchInfo.createMatchInfo(source, previousmmDbId));

        int numberOfAttempts = 0;
        boolean success = false;
        while (!success) {
            try {
                services.sendRequest(ApiServiceName.WRITE_MATCH_INFO, matchInfoJSON, ApiConnectionEnum.POST);
                success = true;
            } catch (DBConnectionException e) {
                processDBException(e, numberOfAttempts, METHOD);
            }
        }
    }

    private Match insertBetOffersOutcomes(Match match, boolean isHistoric) throws DBConnectionException {
        final String METHOD = "extractBetOffersAndParseJSON";
        Match matchCopy = match;

        List<BetOffer> betOffers = matchCopy.getBetOffers();
        for (BetOffer betOffer : betOffers) {

            //If the process has been parallized there could be errors due to this. Attempt to send these
            //JSON string numberOfAttempts times.
            int numberOfAttempts = 0;
            boolean success = false;
            while (!success) {
                try {
                    betOffer = services.insertBetOffer(matchCopy.getPk(), betOffer);
                    success = true;
                } catch (DBConnectionException e) {
                    processDBException(e, numberOfAttempts, METHOD);
                }
            }

            insertPrices(betOffer.getOutcomes(), isHistoric);
        }
        return matchCopy;
    }

    private void insertPrices(List<Outcome> outcomes, boolean isHistoric) throws DBConnectionException {
        final String METHOD = "extractPricesAndParseJSON";
        JSONArray pricesJSON = new JSONArray();
        for (Outcome outcome : outcomes) {
            List<Price> prices = outcome.getPrices();
            for (Price price : prices) {
                if (price.getPayback() > 0 || price.getProbability() > 0) {
                    pricesJSON.add(JSONFactoryPrice.parseJSONPrice(outcome.getPk(), price));
                }
            }
        }

        int numberOfAttempts = 0;
        boolean success = false;
        while (!success) {
            try {
                if (!pricesJSON.isEmpty()) {
                    if (isHistoric) {
                        services.sendRequest(ApiServiceName.WRITE_PRICE_HISTORIC, pricesJSON, ApiConnectionEnum.POST);
                    } else {
                        services.sendRequest(ApiServiceName.WRITE_PRICE_OBSERVATION, pricesJSON, ApiConnectionEnum.POST);

                    }
                }
                success = true;
            } catch (DBConnectionException e) {
                processDBException(e, numberOfAttempts, METHOD);
            }
        }
    }

    private void processDBException(DBConnectionException e, int numberOfAttempts, final String METHOD) throws DBConnectionException {
        if ((e.getMessage().contains(DBErrorType.DATABASE_INTEGRITY_ERROR.toString())
                || e.getMessage().contains(DBErrorType.DATABASE_OPERATIONAL_ERROR.toString())) && numberOfAttempts < DBConnection.NUMBER_OF_ATTEMPTS) {
            numberOfAttempts++;
            Log.logMessage(CLASSNAME, METHOD, "Database integrity-operational error. " + numberOfAttempts + " attempt", LogLevelEnum.INFO, true);
        } else if ((e.getMessage().contains(DBErrorType.DATABASE_INTEGRITY_ERROR.toString())
                || e.getMessage().contains(DBErrorType.DATABASE_OPERATIONAL_ERROR.toString())) && numberOfAttempts == DBConnection.NUMBER_OF_ATTEMPTS) {
            throw new DBConnectionException("Database integrity-operational error. " + e.getMessage());
        } else {
            throw e;
        }
    }
}
