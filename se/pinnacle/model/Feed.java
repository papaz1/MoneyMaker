package se.pinnacle.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.moneymaker.model.Match;
import se.pinnacle.enums.RspStatusEnum;

public class Feed {

    private RspStatusEnum rspStatus;
    private long fdTime;
    private List<Match> matches = new ArrayList<>();
    private Date utcEncounter;

    public Map<String, Match> getMatchesWithScores() {
        Map<String, Match> matchesWithScore = new HashMap<>();
        for (Match match : matches) {
            if (match.getHomeScore() >= 0 && match.getAwayScore() >= 0) {
                matchesWithScore.put(match.getExternalKey(), match);
            }
        }
        return matchesWithScore;
    }

    public void addMatch(Match match) {
        matches.add(match);
    }

    public RspStatusEnum getRspStatus() {
        return rspStatus;
    }

    public void setRspStatus(RspStatusEnum rspStatus) {
        this.rspStatus = rspStatus;
    }

    public long getFdTime() {
        return fdTime;
    }

    public void setFdTime(long fdTime) {
        this.fdTime = fdTime;
    }

    public Date getUTCEncounter() {
        return utcEncounter;
    }

    public void setUTCEncounter(Date utcEncounter) {
        this.utcEncounter = utcEncounter;
    }

}
