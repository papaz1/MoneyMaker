package se.moneymaker.dict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.moneymaker.model.BetOffer;
import se.moneymaker.model.Match;

public class BetOfferDict {

    private static final Map<String, BetOffer> newBetOffers = new HashMap<>();
    private static final Map<String, BetOffer> insertedBetOffers = new HashMap<>();

    public static void update(Match match) {
        List<BetOffer> betOffers = match.getBetOffers();
        for (BetOffer betOffer : betOffers) {
            newBetOffers.put(betOffer.getExternalKey(), betOffer);
        }
    }

    public static synchronized void save(Match match) {
        List<BetOffer> betOffers = match.getBetOffers();
        for (BetOffer betOffer : betOffers) {
            insertedBetOffers.put(betOffer.getExternalKey(), betOffer);
        }
    }

    public static boolean containsBetOffer(String id) {
        return insertedBetOffers.get(id) != null;
    }

    public static BetOffer getBetOffer(String id) {
        return insertedBetOffers.get(id);
    }

    public static void saveNewDataClearOldData() {
        insertedBetOffers.clear();
        insertedBetOffers.putAll(newBetOffers);
        newBetOffers.clear();
    }

    public static void clearOldEntries() {
        insertedBetOffers.clear();
    }
}
