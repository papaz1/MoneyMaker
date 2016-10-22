package se.betfair.dict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.betfair.config.BCOutcomeNameEnum;
import se.betfair.model.MarketCatalogue;
import se.betfair.model.RunnerCatalog;
import se.moneymaker.model.Match;

public class MatchDict {

    private static final Map<String, Match> matches = new HashMap<>();

    /**
     * Only MATCH_ODDS should be sent in to this method since its purpose is to
     * get the team name and the corresponding id.
     *
     */
    public static void putAll(List<MarketCatalogue> catalogues) {
        for (MarketCatalogue catalgue : catalogues) {
            List<RunnerCatalog> runnerCatalogs = catalgue.getRunners();
            Match match = new Match();
            boolean home = true;

            /**
             * The reason for having a loop although there will always be only 3
             * outcomes in a match is that we don't want to trust Betfair always
             * having the outcomes in the same position in the array. Also even
             * if Betfair end up mixing the positions of the home and away teams
             * in the array it doesn't matter because at this stage we don't
             * care about who is home or who is away. The only thing we care
             * about is that which teams are in the match and what are their
             * ids.
             */
            for (RunnerCatalog runnerCatalog : runnerCatalogs) {
                if (!runnerCatalog.getRunnerName().equalsIgnoreCase(BCOutcomeNameEnum.DRAW.getOriginalName())) {
                    if (home) {
                        match.setHome(runnerCatalog.getRunnerName());
                        match.setHomeExternalKey(Long.toString(runnerCatalog.getSelectionId()));
                        home = false;
                    } else {
                        match.setAway(runnerCatalog.getRunnerName());
                        match.setAwayExternalKey(Long.toString(runnerCatalog.getSelectionId()));
                    }
                }
            }
            matches.put(catalgue.getEvent().getId(), match);
        }
    }

    public static void clear() {
        matches.clear();
    }

    public static Match get(String eventId) {
        return matches.get(eventId);
    }
}
