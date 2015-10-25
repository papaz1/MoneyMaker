package se.betfair.factory;

import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

public class FactoryUtil {

    public static String[] parseTeams(List<String> hierarchyTextPath) throws ParseException {
        if (hierarchyTextPath != null) {
            String hierarchyTextNode;
            Iterator<String> iterator = hierarchyTextPath.iterator();
            while (iterator.hasNext()) {
                hierarchyTextNode = iterator.next();

                //The last element contains the team names
                if (!iterator.hasNext()) {
                    boolean at = false;
                    if (hierarchyTextNode == null) {
                        throw new ParseException("Error parsing teams. Teams are null. hierarchyTextPath: " + hierarchyTextPath.toString(), 0);
                    }
                    int endIndex = hierarchyTextNode.indexOf(" v ");
                    if (endIndex == -1) {
                        endIndex = hierarchyTextNode.indexOf(" V ");
                        if (endIndex == -1) {
                            endIndex = hierarchyTextNode.indexOf(" @ ");
                            if (endIndex == -1) {
                                if (hierarchyTextNode.contains(" vs ")) {
                                    hierarchyTextNode = hierarchyTextNode.replace(" vs ", " v ");
                                    endIndex = hierarchyTextNode.indexOf(" v ");
                                }
                            } else {
                                at = true;
                            }
                        }
                    }

                    if (endIndex == -1) {
                        throw new ParseException("Error parsing teams. hierarchyTextPath: " + hierarchyTextPath.toString(), endIndex);
                    }

                    int beginIndex = 0;

                    String[] teams = new String[2];

                    teams[0] = hierarchyTextNode.substring(beginIndex, endIndex);
                    beginIndex = endIndex + 3;
                    endIndex = hierarchyTextNode.length();
                    teams[1] = hierarchyTextNode.substring(beginIndex, endIndex);

                    if (at) {
                        String tempTeam = teams[0];
                        teams[0] = teams[1];
                        teams[1] = tempTeam;
                    }
                    return teams;
                }
            }
        }
        return null;
    }
}
