package se.betfair.dict;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.betfair.exception.BEXElementNotFoundException;
import se.betfair.model.BMNumOutcomesWinners;
import se.betfair.util.BetfairUtility;

public class DictOutcomesWinners {

    private static DictOutcomesWinners instance;
    private Map<String, BMNumOutcomesWinners> dictionary;
    private List<String> db;
    private static final int NUMBER_OF_DB_ENTRIES = 2;

    private DictOutcomesWinners() {
        dictionary = new HashMap<>(NUMBER_OF_DB_ENTRIES);
        db = new ArrayList<>(NUMBER_OF_DB_ENTRIES);

        //First the betoffer type, then number of outcomes, last number of winners
        db.add("OU-H,2,1");
        db.add("1X2,3,1");
        db.add("CS-O,17,1");

        StringReader dictionaryLine;
        String betOfferType;
        int numberOfOutcomes;
        int numberOfWinners;
        for (String strLine : db) {
            dictionaryLine = new StringReader(strLine);
            betOfferType = BetfairUtility.readString(dictionaryLine, ',');
            numberOfOutcomes = BetfairUtility.readInt(dictionaryLine, ',');
            numberOfWinners = BetfairUtility.readInt(dictionaryLine, ',');
            dictionary.put(betOfferType, new BMNumOutcomesWinners(numberOfOutcomes, numberOfWinners));
        }
    }

    public static DictOutcomesWinners getInstance() {
        if (instance == null) {
            instance = new DictOutcomesWinners();
        }
        return instance;
    }

    public int getNumberOfOutcomes(String betOfferType) throws BEXElementNotFoundException {
        BMNumOutcomesWinners num = dictionary.get(betOfferType.toUpperCase());
        if (num == null) {
            throw new BEXElementNotFoundException("Outcomes and winners not found for betoffer type: " + betOfferType);
        } else {
            return num.getNumberOfOutcomes();
        }
    }

    public int getNumberOfWinners(String betOfferType) {
        BMNumOutcomesWinners num = dictionary.get(betOfferType.toUpperCase());
        return num.getNumberOfWinners();
    }
}
