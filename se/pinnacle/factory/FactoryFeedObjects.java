package se.pinnacle.factory;

import java.util.Date;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import se.moneymaker.enums.PoolType;
import se.moneymaker.model.Match;
import se.moneymaker.util.Utils;
import se.pinnacle.api.XMLTags;
import se.pinnacle.model.Feed;

public class FactoryFeedObjects extends DefaultHandler {

    private Feed feed;
    private Match match;
    private String value;
    private String fdTime;
    private String id;
    private String startDateTime;
    private String team;
    private String score;
    private String redCards;
    private String description;
    private String cutoffDateTime;
    private boolean bFdTime;
    private boolean bEvent;
    private boolean bId;
    private boolean bStartDateTime;
    private boolean bHomeTeam;
    private boolean bAwayTeam;
    private boolean bName;
    private boolean bScore;
    private boolean bRedCards;
    private boolean bDescription;
    private boolean bCutoffDatetime;
    private boolean isFirstHalf;
    private Date utcEncounter;

    public FactoryFeedObjects(Date utcEncounter) {
        feed = new Feed();
        this.utcEncounter = utcEncounter;
        feed.setUTCEncounter(utcEncounter);
    }

    @Override
    public void startElement(String uri, String localName,
            String startName, Attributes attributes)
            throws SAXException {
        switch (startName) {
            case XMLTags.FD_TIME:
                bFdTime = true;
                break;
            case XMLTags.EVENT:
                bEvent = true;
                match = new Match();
                match.setUTCEncounter(utcEncounter);
                break;
            case XMLTags.ID:
                bId = true;
                break;
            case XMLTags.START_DATE_TIME:
                bStartDateTime = true;
                break;
            case XMLTags.HOME_TEAM:
                bHomeTeam = true;
                bAwayTeam = false;
                break;
            case XMLTags.AWAY_TEAM:
                bAwayTeam = true;
                bHomeTeam = false;
                break;
            case XMLTags.NAME:
                bName = true;
                break;
            case XMLTags.SCORE:
                bScore = true;
                break;
            case XMLTags.RED_CARDS:
                bRedCards = true;
                break;
            /*case XMLTags.DESCRIPTION:
             bDescription = true;
             break;
             case XMLTags.CUTOFF_DATETIME:
             bCutoffDatetime = true;
             break;*/
        }
    }

    @Override
    public void endElement(String uri, String localName,
            String endName)
            throws SAXException {
        if (endName.equals(XMLTags.FD_TIME)) {
            feed.setFdTime(Long.parseLong(fdTime));
        } else if (endName.equals(XMLTags.EVENT)) {
            if (includeMatch(match)) {
                if (match.getHome().contains("(W)")
                        || match.getHome().contains("(WF")) {
                    match.setPoolType(PoolType.FEMALE);
                } else if (match.getHome().contains("U1")
                        || match.getHome().contains("U2")) {
                    match.setPoolType(PoolType.YOUTH);
                }
                feed.addMatch(match);
            }
            bEvent = false;
        } else if (bEvent & endName.equals(XMLTags.ID)) {
            match.setExternalKey(id);
        } else if (endName.equals(XMLTags.START_DATE_TIME)) {
            startDateTime = parseDate(startDateTime);
            match.setEventDate(Utils.stringToDate(startDateTime));
        } else if (bHomeTeam & endName.equals(XMLTags.HOME_TEAM)) {
            match.setHomeTeam(team);
        } else if (bAwayTeam & endName.equals(XMLTags.AWAY_TEAM)) {
            match.setAwayTeam(team);
        } else if (bHomeTeam & endName.equals(XMLTags.SCORE)) {
            match.setHomeScore(Integer.parseInt(score));
        } else if (bAwayTeam & endName.equals(XMLTags.SCORE)) {
            match.setAwayScore(Integer.parseInt(score));
        } else if (bHomeTeam & endName.equals(XMLTags.RED_CARDS)) {
            match.setHomeRedCards(Integer.parseInt(redCards));
        } else if (bAwayTeam & endName.equals(XMLTags.RED_CARDS)) {
            match.setAwayRedCards(Integer.parseInt(redCards));
        }
        /*else if (endName.equals(XMLTags.DESCRIPTION)) {
         if (description.equals("1st Half")) {
         isFirstHalf = true;
         }
         } else if (endName.equals(XMLTags.CUTOFF_DATETIME)) {
         if (isFirstHalf) {
         cutoffDateTime = parseDate(cutoffDateTime);
         match.setMatchTime(Utility.stringToDate(startDateTime));
         isFirstHalf = false;
         }
         }*/
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        value = new String(ch, start, length);
        if (bFdTime) {
            fdTime = value;
            bFdTime = false;
        } else if (bId) {
            id = value;
            bId = false;
        } else if (bStartDateTime) {
            startDateTime = value;
            bStartDateTime = false;
        } else if (bName) {
            team = value;
            bName = false;
        } else if (bScore) {
            score = value;
            bScore = false;
        } else if (bRedCards) {
            redCards = value;
            bRedCards = false;
        }
        /*else if (bDescription) {
         description = value;
         bDescription = false;
         }
         else if (bCutoffDatetime) {
         cutoffDateTime = value;
         bCutoffDatetime = false;
         }*/
    }

    public Feed getFeed() {
        return feed;
    }

    private String parseDate(String date) {
        date = date.substring(0, date.lastIndexOf('Z'));
        date = date.replace('T', ' ');
        return date;
    }

    private boolean includeMatch(Match match) {
        String home = match.getHome().toUpperCase();
        return !(home.contains("(CORNERS)"));
    }
}
