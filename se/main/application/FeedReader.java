package se.main.application;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.xml.sax.SAXException;
import se.moneymaker.dict.Config;
import se.moneymaker.db.DBSportsModel;
import se.moneymaker.enums.ConfigEnum;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.model.Match;
import se.moneymaker.util.Log;
import se.moneymaker.util.Utils;
import se.pinnacle.api.PinnacleAPIException;
import se.pinnacle.api.Services;
import se.pinnacle.enums.ServicesEnum;
import se.pinnacle.enums.SportsEnum;
import se.pinnacle.factory.FactoryFeed;
import se.pinnacle.model.Feed;

public class FeedReader extends Application implements Runnable {

    private static final String CLASSNAME = FeedReader.class.getName();
    private static final long HEARTBEAT = 900000; //15 minutes
    private static final int MAX_POLLING_SPEED_SECONDS = 5;
    private long sleepTime = 5500;
    private long lastFetched;
    private Services services;
    private DBSportsModel sportsModel;
    private Date tomorrow;
    private SimpleDateFormat df;
    private int scoreTimeMinutes;
    private List<Match> matches;
    private Map<String, Match> matchesErrorOrNotFound;

    public FeedReader(String sessionKey) {
        initApplication(HEARTBEAT, CLASSNAME);
        services = new Services(sessionKey);
        sportsModel = new DBSportsModel(Source.PINNACLE);
        df = new SimpleDateFormat("yyyyMMdd");
        tomorrow = Utils.getTomorrow(df);
        Config config = Config.getInstance();
        scoreTimeMinutes = Integer.parseInt(config.get(ConfigEnum.PS_SCORE_TIME_MINUTES));
        matches = new ArrayList<>();
        matchesErrorOrNotFound = new HashMap<>();
    }

    @Override
    public void run() {
        final String METHOD = "run";
        Log.logMessage(CLASSNAME, METHOD, "FeedReader running...", LogLevelEnum.INFO, false);

        while (true) {
            Date utcEncounterFeed;
            try {
                if (Utils.isNewDay(df, tomorrow)) {
                    sportsModel.reset();
                    matchesErrorOrNotFound.clear();
                    tomorrow = Utils.getTomorrow(df);
                }

                String rawFeed = services.getFeed(ServicesEnum.GET_FEED, SportsEnum.SOCCER, lastFetched);
                utcEncounterFeed = new Date();
                FactoryFeed factory = new FactoryFeed(rawFeed, utcEncounterFeed);
                Feed feed = factory.createFeed();

                iAmAlive();
                lastFetched = feed.getFdTime();

                if (!feed.getMatchesWithScores().isEmpty()) {
                    try {
                        Map<String, Match> matchesFromFeed = feed.getMatchesWithScores();
                        List<Match> matchesToSendAndBuffer = new ArrayList<>();

                        for (Match match : matches) {
                            long diff = utcEncounterFeed.getTime() - match.getEventDate().getTime();
                            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

                            if (minutes <= scoreTimeMinutes) {
                                if (match.getPk() < 1) { //These  have not been found in DB or couldn't be updated, don't send them again
                                    if (!matchesErrorOrNotFound.containsKey(match.getExternalKey())) {
                                        matchesErrorOrNotFound.put(match.getExternalKey(), match);
                                    }
                                } else {
                                    Match feedMatch = matchesFromFeed.get(match.getExternalKey());
                                    if (feedMatch != null) {
                                        match.setHomeScore(feedMatch.getHomeScore());
                                        match.setAwayScore(feedMatch.getAwayScore());
                                        matchesFromFeed.remove(match.getExternalKey());
                                    }
                                    match.setUTCEncounter(utcEncounterFeed);
                                    matchesToSendAndBuffer.add(match);
                                }
                            }
                        }

                        List<Match> matchesToSendFromFeed = new ArrayList();
                        for (Map.Entry<String, Match> entry : matchesFromFeed.entrySet()) {
                            Match match = entry.getValue();
                            long diff = utcEncounterFeed.getTime() - match.getEventDate().getTime();
                            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                            if (minutes <= scoreTimeMinutes && !matchesErrorOrNotFound.containsKey(match.getExternalKey())) {
                                matchesToSendAndBuffer.add(match);
                            } else {
                                if (minutes > scoreTimeMinutes) {
                                    matchesToSendFromFeed.add(match);
                                }
                            }
                        }

                        if (!matchesToSendAndBuffer.isEmpty() || !matchesToSendFromFeed.isEmpty()) {
                            Log.logMessage(CLASSNAME, METHOD, "Matches/Scores to send and buffer: " + matchesToSendAndBuffer.size() + " Send without buffering: " + matchesToSendFromFeed.size(), LogLevelEnum.INFO, true);
                        }

                        if (!matchesToSendAndBuffer.isEmpty()) {

                            //matches arrayList contains all matches that have successfully been updated in DB
                            matches = sportsModel.updateScores(matchesToSendAndBuffer);
                        }

                        //Remaining matches should not be buffered but just be sent from feed
                        if (!matchesToSendFromFeed.isEmpty()) {
                            sportsModel.updateScores(matchesToSendFromFeed);
                        }
                    } catch (DBConnectionException e) {
                        Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
                    }
                }
            } catch (SAXException | PinnacleAPIException | IOException e) {
                utcEncounterFeed = null;
                Log.logMessage(CLASSNAME, METHOD, e.getMessage(), LogLevelEnum.ERROR, false);
            }

            if (utcEncounterFeed == null) {
                sleepTime = 1000 * MAX_POLLING_SPEED_SECONDS;
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                }
            } else {
                Date runEnd = new Date();
                long diff = runEnd.getTime() - utcEncounterFeed.getTime();
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                if (seconds <= MAX_POLLING_SPEED_SECONDS) {
                    try {
                        sleepTime = 1000 * (MAX_POLLING_SPEED_SECONDS - seconds) + 750; //750 ms is the margin from 5 seconds polling rate rule from Pinnacle
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
