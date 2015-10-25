package se.betfair.factory;

import java.util.List;
import se.betfair.config.BCMarketEnum;
import se.betfair.config.BCOutcomeNameEnum;
import se.moneymaker.enums.BetOfferTypeEnum;
import se.moneymaker.exception.BetOfferException;
import se.moneymaker.exception.OutcomeException;
import se.moneymaker.model.BetOfferItem;
import se.moneymaker.model.Outcome;
import se.moneymaker.model.OutcomeItem;
import se.moneymaker.enums.OutcomeTypeEnum;
import se.moneymaker.util.Log;
import se.moneymaker.enums.LogLevelEnum;

/*
 * Each new market needs to be identified as a market type. Also this class
 * contains the identification, that is the type, of the outcome. Identifying
 * the outcome means to give it a predefined name.
 */
public class FactoryBetOfferOutcomeItem {

    private static final String CLASSNAME = FactoryBetOfferOutcomeItem.class.getName();

    public static BetOfferItem parseBetOfferItem(String marketName, String home, String away) throws BetOfferException {
        BetOfferItem betOfferItem = null;
        int fromIndex;
        int toIndex;
        String parameter;
        marketName = marketName.toUpperCase();

        //First use use contains so that both "HOME over/under 2.5 goals" and
        //over/under 2.5 goals are found. Betoffers that can't be identifed will always cause
        //an exception to be thrown. 
        if (marketName.contains(BCMarketEnum.OVER_UNDER.value())) {
            if (marketName.startsWith(BCMarketEnum.OVER_UNDER.value())) {
                fromIndex = marketName.indexOf(BCMarketEnum.OVER_UNDER.value()) + BCMarketEnum.OVER_UNDER.value().length() + 1;
                toIndex = marketName.indexOf("GOALS") - 1;

                //The parameter is the number of goals
                parameter = marketName.substring(fromIndex, toIndex);
                //Money back if the game ends with a draw which is the
                //+- value is an integer
                try {
                    Integer.valueOf(parameter);
                    betOfferItem = new BetOfferItem(BetOfferTypeEnum.OU_W);
                } catch (NumberFormatException e) {
                    try {

                        //Not an integer, check if this is a k.25 or k.75 then it is a
                        //possiblePush true
                        int indexDot = parameter.indexOf('.');
                        if (indexDot != -1) {
                            String quarterString = parameter.substring(indexDot, parameter.length());
                            double decimalValue = Double.parseDouble(quarterString);
                            if (decimalValue == 0.25 || decimalValue == 0.75) {
                                betOfferItem = new BetOfferItem(BetOfferTypeEnum.OU_Q);
                                betOfferItem.setParameter(parameter);
                            } else if (decimalValue == 0.5) {
                                betOfferItem = new BetOfferItem(BetOfferTypeEnum.OU_H);
                                betOfferItem.setParameter(parameter);
                            } else {
                                throw new NumberFormatException(" Unexpected decimal value on over/under market: " + decimalValue);
                            }
                        }
                    } catch (StringIndexOutOfBoundsException e2) {
                    }
                }

            }
        } else if (marketName.equalsIgnoreCase(BCMarketEnum.MATCH_ODDS.value())) {
            betOfferItem = new BetOfferItem(BetOfferTypeEnum.MATCH_ODDS);
        } else if (marketName.equalsIgnoreCase(BCMarketEnum.CORRECT_SCORE.value())) {
            betOfferItem = new BetOfferItem(BetOfferTypeEnum.CORRECT_SCORE2);
        } else {
            throw new BetOfferException("Invalid market: " + marketName + ". Following teams are in the market, home: " + home + " away: " + away);
        }
        return betOfferItem;
    }

    public static OutcomeItem parseOutcomeItem(BetOfferTypeEnum betOfferType, String home, String away, String runnerName) throws OutcomeException {
        final String METHOD = "parseOutcomeItem";
        final String CS_OTHER = "ANY UNQUOTED";
        final String CS_OTHER_HOME = "ANY OTHER HOME WIN";
        final String CS_OTHER_AWAY = "ANY OTHER AWAY WIN";
        final String CS_OTHER_DRAW = "ANY OTHER DRAW";

        OutcomeItem outcomeItem = null;
        String runnerNameUpperCase = runnerName.toUpperCase().trim();

        if (betOfferType.equals(BetOfferTypeEnum.MATCH_ODDS)) {
            if (runnerNameUpperCase.equalsIgnoreCase(home.toUpperCase().trim())) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.HOME);
            } else if (runnerNameUpperCase.equalsIgnoreCase(away.toUpperCase().trim())) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.AWAY);
            } else if (runnerNameUpperCase.equalsIgnoreCase(BCOutcomeNameEnum.DRAW.getOriginalName())) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.DRAW);
            }
        }

        if (betOfferType.equals(BetOfferTypeEnum.OU_H)
                || betOfferType.equals(BetOfferTypeEnum.OU_W)
                || betOfferType.equals(BetOfferTypeEnum.OU_Q)) {
            if (runnerNameUpperCase.startsWith(BCOutcomeNameEnum.OVER.getOriginalName())) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.OVER);
            } else if (runnerNameUpperCase.startsWith(BCOutcomeNameEnum.UNDER.getOriginalName())) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.UNDER);
            }
        }

        if (betOfferType.equals(BetOfferTypeEnum.CORRECT_SCORE2)) {
            if (runnerNameUpperCase.equalsIgnoreCase(CS_OTHER_HOME)) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.CORRECT_SCORE_OTHER_GROUP);
                String[] parameters = new String[2];
                parameters[0] = "4";
                parameters[1] = "0";
                outcomeItem.setParameters(parameters);
            } else if (runnerNameUpperCase.equalsIgnoreCase(CS_OTHER_AWAY)) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.CORRECT_SCORE_OTHER_GROUP);
                String[] parameters = new String[2];
                parameters[0] = "0";
                parameters[1] = "4";
                outcomeItem.setParameters(parameters);
            } else if (runnerNameUpperCase.equalsIgnoreCase(CS_OTHER_DRAW)) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.CORRECT_SCORE_OTHER_GROUP);
                String[] parameters = new String[2];
                parameters[0] = "4";
                parameters[1] = "4";
                outcomeItem.setParameters(parameters);
            } else if (runnerNameUpperCase.equalsIgnoreCase(CS_OTHER)) {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.CORRECT_SCORE_OTHER);//TODO: THIS SHOULD BE MOVEED TO ANOTHER BETOFFER TYPE FOR CORRECT SCORE, THE OLD ONE THAT IS IN THE HISTOCIC FILE
            } else {
                outcomeItem = new OutcomeItem(OutcomeTypeEnum.CORRECT_SCORE);
                int indexOfDash = runnerNameUpperCase.indexOf('-');
                String p1 = runnerNameUpperCase.substring(0, indexOfDash - 1);
                String p2 = runnerNameUpperCase.substring(indexOfDash + 2, runnerNameUpperCase.length());
                String[] parameters = new String[2];
                parameters[0] = p1;
                parameters[1] = p2;
                outcomeItem.setParameters(parameters);
            }
        }

        if (outcomeItem == null) {
            outcomeItem = new OutcomeItem(OutcomeTypeEnum.MISSING_OUTCOME_TYPE);
            Log.logMessage(CLASSNAME, METHOD, "Could not parse outcome type. Home: " + home + " Away: " + away + " Runner name: " + runnerName, LogLevelEnum.WARNING, false);
        }

        return outcomeItem;
    }

    public static List<Outcome> cleanOutcomeNames(BetOfferTypeEnum betOfferType, List<Outcome> outcomes) throws OutcomeException {

        //In order to improve the number of identified outcome types we will identify
        //certain patterns to make it easier to now which is HOME and AWAY.
        //For example if there are three outcomes, one of them is identified as THE DRAW
        //and the other one either HOME or AWAY then we automatically know the 3rd.
        if (betOfferType.equals(BetOfferTypeEnum.MATCH_ODDS)) {
            boolean homeFound = false;
            boolean drawFound = false;
            boolean awayFound = false;

            for (Outcome outcome : outcomes) {
                if (outcome.getItem().getType().getName().equalsIgnoreCase(OutcomeTypeEnum.HOME.getName())) {
                    homeFound = true;
                    outcome.setIdentified(homeFound);
                } else if (outcome.getItem().getType().getName().equalsIgnoreCase(OutcomeTypeEnum.DRAW.getName())) {
                    drawFound = true;
                    outcome.setIdentified(drawFound);
                } else if (outcome.getItem().getType().getName().equalsIgnoreCase(OutcomeTypeEnum.AWAY.getName())) {
                    awayFound = true;
                    outcome.setIdentified(awayFound);
                }
            }

            for (Outcome outcome : outcomes) {

                //If anyone was not identified, find out which and set it to either HOME or AWAY.
                //If for some reason the draw couldn't be found then there is an issue with the data overall
                if (!homeFound && drawFound && awayFound) {
                    if (!outcome.isIdentified()) {
                        outcome.setItem(new OutcomeItem(OutcomeTypeEnum.HOME));
                        outcome.setIdentified(true);
                    }
                } else if (homeFound && drawFound && !awayFound) {
                    if (!outcome.isIdentified()) {
                        outcome.setItem(new OutcomeItem(OutcomeTypeEnum.AWAY));
                        outcome.setIdentified(true);
                    }
                }
            }
        }

        //All outcomes should be identified else throw exception
        for (Outcome identifiedOutcome : outcomes) {
            if (identifiedOutcome.getItem().getType().equals(OutcomeTypeEnum.MISSING_OUTCOME_TYPE)) {
                throw new OutcomeException("Outcome types for the market could not be identified");
            }
        }
        return outcomes;
    }
}
