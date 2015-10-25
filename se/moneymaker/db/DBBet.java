package se.moneymaker.db;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import se.moneymaker.enums.ApiConnectionEnum;
import se.moneymaker.enums.ApiServiceName;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.jsonfactory.JSONFactoryBet;
import se.moneymaker.enums.DBBoolean;
import se.moneymaker.exception.NoDataException;
import se.moneymaker.model.Bet;
import se.moneymaker.util.Log;

public class DBBet {

    private static final String CLASSNAME = DBBet.class.getName();
    private final DBServices services;
    private String bookmaker;
    private String accountName;

    public DBBet(String bookmaker, String accountName) {
        this.bookmaker = bookmaker;
        this.accountName = accountName;
        services = new DBServices(false);
    }

    public List<String> updateBets(List<Bet> bets) {
        final String METHOD = "updateInsertBet";
        List<String> errorBets = new ArrayList<>();

        //splitBetsIntoExistingNonExisting(bets, operation);
        //The existing bets will be corrected the other ones created and then corrected
        for (Bet bet : bets) {
            JSONArray betsJSON = JSONFactoryBet.createBets(bet, false);
            try {
                services.sendRequest(ApiServiceName.UPDATE_BET, betsJSON, ApiConnectionEnum.POST);
            } catch (DBConnectionException e) {
                errorBets.add(bet.getExternalKey());
                Log.logMessage(CLASSNAME, METHOD, "Error updating bet. " + e.getMessage(), LogLevelEnum.ERROR, false);
            }
        }
        return errorBets;
    }

    public List<String> upsertBets(List<Bet> bets) {
        final String METHOD = "upsertBets";
        List<String> errorBets = new ArrayList<>();
        for (Bet bet : bets) {
            try {
                long outcomePk = services.readOutcomePk(bookmaker, bet.getBetOfferId(), bet.getOutcomeExternalKey());
                bet.setOutcomePk(outcomePk);
                services.sendRequest(ApiServiceName.WRTIE_BET_COMBINATION, JSONFactoryBet.createBets(bet, true), ApiConnectionEnum.POST);

                //Get the pk from db
                String[] externalKeys = {bet.getExternalKey()};
                List<Bet> newBet = services.readBets(DBBoolean.FALSE, null, externalKeys, bookmaker, accountName, false);
                if (newBet != null) {

                    bet.setPk(newBet.get(0).getPk());

                    //Now try again with updating the bet after it has been inserted
                    services.sendRequest(ApiServiceName.UPDATE_BET, JSONFactoryBet.createBets(bet, false), ApiConnectionEnum.POST);
                } else {
                    Log.logMessage(CLASSNAME, METHOD, "Bet restored but could not be found when trying to find it", LogLevelEnum.ERROR, false);
                }

            } catch (DBConnectionException e) {
                errorBets.add(bet.getExternalKey());
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
            } catch (NoDataException e) {
                errorBets.add(bet.getExternalKey());
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.CRITICAL, false);
            }
        }
        return errorBets;
    }
    /*
     private void splitBetsIntoExistingNonExisting(List<Bet> betList, Operation operation) {
     final String METHOD = "populateMMDbId";

     String[] externalKeys = new String[betList.size()];
     int i = 0;
     for (se.moneymaker.model.Bet bet : betList) {
     externalKeys[i] = bet.getExternalKey();
     i++;
     }

     int numberOfAttempts = 0;
     boolean success = false;
     while (!success && numberOfAttempts < 3) {
     try {
     List<Bet> betsDb = services.readBets(DBBoolean.NONE, null, externalKeys, bookmaker, accountName);
     if (betsDb != null) {
     String mmDbId;
     for (se.moneymaker.model.Bet bet : betList) {
     mmDbId = getPk(bet.getExternalKey(), betsDb);
     if (mmDbId != null) {
     bet.setPk(mmDbId);
     betsExist.add(bet);
     } else {
     betsNotExist.add(bet);
     }
     }
     } else {
     betsNotExist.addAll(betList);
     }
     success = true;
     } catch (DBConnectionException e) {
     numberOfAttempts++;
     Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
     }
     }
     }

     private String getPk(String externalKey, List<Bet> bets) {
     for (Bet bet : bets) {
     if (bet.getExternalKey().equalsIgnoreCase(externalKey)) {
     return bet.getPk();
     }
     }
     return null;
     }
     */
}
