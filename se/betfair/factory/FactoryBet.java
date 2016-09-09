package se.betfair.factory;

import com.betfair.aping.enums.BetStatus;
import com.betfair.aping.enums.Side;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import se.betfair.model.ClearedOrderSummary;
import se.betfair.model.CurrentOrderSummary;
import se.betfair.model.ItemDescription;
import se.betfair.model.PlaceInstructionReport;
import se.main.application.AccountReader;
import se.moneymaker.container.PlaceBetContainer;
import se.moneymaker.container.PlaceBetItem;
import se.moneymaker.db.DBServices;
import se.moneymaker.enums.BetStateEnum;
import se.moneymaker.enums.DBBoolean;
import se.moneymaker.enums.LogLevelEnum;
import se.moneymaker.enums.Source;
import se.moneymaker.exception.BetException;
import se.moneymaker.exception.DBConnectionException;
import se.moneymaker.exception.ErrorType;
import se.moneymaker.model.Bet;
import se.moneymaker.model.BetInstruction;
import se.moneymaker.util.Log;
import se.moneymaker.util.Utils;

public class FactoryBet {

    private static final String CLASSNAME = FactoryBet.class.getName();
    private static final int SCALE = 2;
    private String accountName;
    private String currency;

    public FactoryBet() {
    }

    public FactoryBet(String accountName, String currency) {
        this.accountName = accountName;
        this.currency = currency;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public List<Bet> updateCurrentBets(List<CurrentOrderSummary> orders, HashMap<String, Bet> bets) {
        final String METHOD = "updateBets";
        List<Bet> currentBets = new ArrayList<>();
        for (CurrentOrderSummary order : orders) {
            Bet bet = bets.get(order.getBetId());
            if (bet != null) {
                currentBets.add(updateCurrentBet(order, bet));
            } else {
                Log.logMessage(CLASSNAME, METHOD, "Bet was unexpectedly not found: " + order.getBetId(), LogLevelEnum.ERROR, false);
            }
        }
        return currentBets;
    }

    public List<Bet> updateClearedBets(List<ClearedOrderSummary> orders, BetStatus status, boolean isRepairMode) throws DBConnectionException {
        List<Bet> clearedBets = new ArrayList<>(orders.size());
        String[] externalKeys = new String[orders.size()];
        DBServices services = new DBServices(false);

        if (!isRepairMode) {

            //Get requested stake local from DB
            int i = 0;
            for (ClearedOrderSummary order : orders) {
                externalKeys[i] = order.getBetId();
                i++;
            }
            List<Bet> bets = services.readBets(DBBoolean.NONE, null, externalKeys, Source.BETFAIR.getName(), accountName, false);

            if (bets != null) {
                for (ClearedOrderSummary order : orders) {
                    Bet bet = getBet(bets, order.getBetId());
                    if (bet != null) {
                        clearedBets.add(updateClearedBet(order, status, bet.getPk()));
                    }
                }
            }
        } else {
            for (ClearedOrderSummary order : orders) {
                clearedBets.add(updateClearedBet(order, status, null));
            }
        }
        return clearedBets;
    }

    public List<Bet> updatePlacedBets(List<PlaceInstructionReport> placeInstructionReports, List<BetInstruction> betInstructions) throws BetException {
        List<Bet> bets = new ArrayList<>(betInstructions.size());
        Iterator<BetInstruction> iteratorBetInstruction = betInstructions.iterator();

        for (PlaceInstructionReport report : placeInstructionReports) {
            BetInstruction betInstruction = iteratorBetInstruction.next();
            if (report.getBetId() != null) {
                BetfairBet betfairBet = new BetfairBet(report.getBetId());
                betfairBet.setRequestedStakelocal(betInstruction.getSize());
                betfairBet.setPlacedDate(report.getPlacedDate());
                betfairBet.setRequestedPrice(betInstruction.getPrice());
                if (report.getAveragePriceMatched() > 0) {
                    betfairBet.setAveragePriceMatched(report.getAveragePriceMatched());
                }
                betfairBet.setSizeMatched(report.getSizeMatched());
                betfairBet.setSizeRemaining(betInstruction.getSize() - report.getSizeMatched());
                if (betInstruction.isIsBack()) {
                    betfairBet.setSide(Side.BACK);
                } else {
                    betfairBet.setSide(Side.LAY);
                }

                Bet bet = updateBet(new Bet(Source.BETFAIR.getName(), betInstruction.getAccount().getAccountName()), betfairBet, false);
                bets.add(bet);
            } else {
                throw new BetException("BetId returned null by Betfair. Bet not accepted by Betfair for unknown reason.", ErrorType.UNKNOWN_ERROR);
            }
        }
        return bets;
    }

    private Bet updateClearedBet(ClearedOrderSummary order, BetStatus status, String pk) {
        boolean noStakeScalerOnMatched = false;

        BetfairBet betfairBet = new BetfairBet(order.getBetId());
        betfairBet.setCurrenyCode(currency);
        betfairBet.setMarketId(order.getMarketId());
        betfairBet.setSelectionId(order.getSelectionId());
        betfairBet.setPlacedDate(order.getPlacedDate());
        betfairBet.setRequestedPrice(order.getPriceRequested());
        betfairBet.setAveragePriceMatched(order.getPriceMatched());
        betfairBet.setRequestedStakelocal(order.getSizeSettled() + order.getSizeCancelled());

        if (status.equals(BetStatus.VOIDED)) {
            betfairBet.setSizeVoided(order.getSizeSettled());
        } else if (status.equals(BetStatus.CANCELLED)) {
            betfairBet.setSizeCancelled(order.getSizeCancelled());
        } else {
            betfairBet.setSizeMatched(Math.abs(order.getProfit()));
            noStakeScalerOnMatched = true;
        }

        //Check if this is a merged cancelled bet and in that case set the sizeCancelled field
        ItemDescription desc = order.getItemDescription();
        if (desc != null && desc.getMarketDesc() != null) {
            if (desc.getMarketDesc().equals(AccountReader.MERGED_CANCELLED)) {
                betfairBet.setSizeCancelled(order.getSizeCancelled());
            }
        }

        betfairBet.setSide(order.getSide());
        betfairBet.setProfit(order.getProfit());
        if (betfairBet.getSizeCancelled() == 0) {
            betfairBet.setSizeLapsed(order.getSizeCancelled());
        }
        betfairBet.setCommission(order.getCommission());
        Bet bet = new Bet(Source.BETFAIR.getName(), accountName);
        bet.setPk(pk);
        return updateBet(bet, betfairBet, noStakeScalerOnMatched);
    }

    private Bet updateCurrentBet(CurrentOrderSummary order, Bet bet) {
        BetfairBet betfairBet = new BetfairBet(order.getBetId());
        betfairBet.setCurrenyCode(currency);
        betfairBet.setMarketId(order.getMarketId());
        betfairBet.setSelectionId(order.getSelectionId());
        betfairBet.setPlacedDate(order.getPlacedDate());
        betfairBet.setRequestedPrice(order.getPriceSize().getPrice());
        betfairBet.setAveragePriceMatched(order.getAveragePriceMatched());
        betfairBet.setRequestedStakelocal(order.getSizeMatched() + order.getSizeCancelled() + order.getSizeLapsed() + order.getSizeVoided());
        betfairBet.setSizeMatched(order.getSizeMatched());
        betfairBet.setSizeRemaining(order.getSizeRemaining());
        betfairBet.setSizeCancelled(order.getSizeCancelled());
        betfairBet.setSizeLapsed(order.getSizeLapsed());
        betfairBet.setSizeVoided(order.getSizeVoided());
        betfairBet.setSide(order.getSide());
        return updateBet(bet, betfairBet, false);
    }

    private Bet updateBet(Bet bet, BetfairBet betfairBet, boolean noStakeScalerOnMatched) {

        //Common fields for all statuses
        bet.setPk(bet.getPk());
        bet.setCurrency(currency);
        bet.setBetOfferId(betfairBet.getMarketId());
        bet.setOutcomeExternalKey(Long.toString(betfairBet.getSelectionId()));
        bet.setExternalKey(betfairBet.getBetId());
        bet.setRequestedOdds(betfairBet.getRequestedPrice());
        bet.setMatchedOdds(betfairBet.getAveragePriceMatched());
        bet.setUTCPlaced(betfairBet.getPlacedDate());
        bet.setIsBack(betfairBet.isBack());
        bet.setCommission(betfairBet.getCommission());
        bet.setBetCommentBefore("Market id: " + betfairBet.getMarketId() + " Selection id: " + betfairBet.getSelectionId());//This will only be used in repair mode

        //Stake scaler is used to calculate the back and lay stakes from our point of view
        double stakeScalerMatched = 0;
        if (bet.getIsBack() == 1) {
            stakeScalerMatched = 1;
        } else if (bet.getIsBack() == 0) {
            if (betfairBet.getAveragePriceMatched() > 0) {
                stakeScalerMatched = betfairBet.getAveragePriceMatched() - 1;
            } else {
                stakeScalerMatched = betfairBet.getRequestedPrice() - 1;
            }
        }

        bet.setRequestedStakeLocal(stakeScalerMatched * betfairBet.getRequestedStakeLocal());

        //Has anything been matched?
        if (betfairBet.getSizeMatched() > 0) {
            bet.setMatchedOdds(betfairBet.getAveragePriceMatched());
            if (!noStakeScalerOnMatched) {
                bet.setMatchedStakeLocal(stakeScalerMatched * betfairBet.getSizeMatched());
            } else {
                bet.setMatchedStakeLocal(betfairBet.getSizeMatched());
            }
        }

        //Is there anything that is still unmatched?
        if (betfairBet.getSizeRemaining() > 0) {
            bet.setUnmatchedStakeLocal(stakeScalerMatched * betfairBet.getSizeRemaining());
        }

        //Has any portion of the bet been canceled?
        if (betfairBet.getSizeCancelled() > 0) {
            bet.setCanceledStakeLocal(stakeScalerMatched * betfairBet.getSizeCancelled());
        }

        //Has any portion of the bet lapsed or is anything left after the bet is closed?
        if (betfairBet.getSizeLapsed() > 0) {
            bet.setRestStakeLocal(bet.getRequestedStakeLocal() - bet.getMatchedStakeLocal());
        }

        //Has the bet been voided?
        if (betfairBet.getSizeVoided() > 0) {
            bet.setCanceledStakeLocal(stakeScalerMatched * betfairBet.getSizeVoided());
        }

        //If there is a profit then there is a paidout amount
        if (betfairBet.getProfit() > 0) {
            bet.setPaidOutLocal(bet.getMatchedStakeLocal() + betfairBet.getProfit());
        }

        //Requested = Matched + Unmatched + Canceled + Rest
        //Update the status on the bet
        if (bet.getUnmatchedStakeLocal() > 0) {
            bet.setState(BetStateEnum.UNMATCHED);
        } else if (betfairBet.getSizeCancelled() > 0
                || betfairBet.getSizeLapsed() > 0
                || betfairBet.getSizeVoided() > 0
                || betfairBet.getProfit() != 0) {//Profit can be both positive and negative
            bet.setState(BetStateEnum.SETTLED);

        } else {
            bet.setState(BetStateEnum.PENDING);
        }

        //Round to two decimals
        bet.setRequestedStakeLocal(Utils.parseDouble(SCALE, bet.getRequestedStakeLocal()));
        bet.setMatchedStakeLocal(Utils.parseDouble(SCALE, bet.getMatchedStakeLocal()));
        bet.setCanceledStakeLocal(Utils.parseDouble(SCALE, bet.getCanceledStakeLocal()));
        bet.setUnmatchedStakeLocal(Utils.parseDouble(SCALE, bet.getUnmatchedStakeLocal()));
        bet.setUnexpectedPlusLocal(Utils.parseDouble(SCALE, bet.getUnexpectedPlusLocal()));
        bet.setUnexpectedMinusLocal(Utils.parseDouble(SCALE, bet.getUnexpectedMinusLocal()));
        bet.setRestStakeLocal(Utils.parseDouble(SCALE, bet.getRestStakeLocal()));
        bet.setPaidOutLocal(Utils.parseDouble(SCALE, bet.getPaidOutLocal()));

        return bet;
    }

    private Bet getBet(List<Bet> bets, String betId) {
        for (Bet bet : bets) {
            if (bet.getExternalKey().equals(betId)) {
                return bet;
            }
        }
        return null;
    }

    public static List<BetInstruction> createBetInstructions(List<PlaceBetContainer> containers) {
        List<BetInstruction> betInstructions = new ArrayList<>(containers.size());
        for (PlaceBetContainer container : containers) {
            BetInstruction betInstruction = new BetInstruction();
            betInstruction.setAccount(container.getAccount());

            //Will only contain one item, multiple items are for combinations
            PlaceBetItem placeBetItem = container.getItems().get(0);
            betInstruction.setBetOfferExternalKey(container.getBetOfferExternalKey());
            betInstruction.setOutcomeExternalKey(Long.parseLong(container.getOutcomeExternalKey()));
            betInstruction.setPrice(placeBetItem.getRequestedOdds());
            if (placeBetItem.isIsBack()) {
                betInstruction.setSize(container.getRequestedStakeLocal());
            } else {
                double size = Utils.parseDouble(SCALE, container.getRequestedStakeLocal() / (placeBetItem.getRequestedOdds() - 1));
                betInstruction.setSize(size);
            }
            betInstruction.setIsBack(placeBetItem.isIsBack());
            betInstructions.add(betInstruction);
        }
        return betInstructions;
    }

    private class BetfairBet {

        private String marketId;
        private String betId;
        private long selectionId;
        private Side side;
        private BetStatus status;
        private Date placedDate;
        private double averagePriceMatched;
        private double sizeMatched;
        private double sizeRemaining;
        private double sizeCanceled;
        private double sizeVoided;
        private double sizeLapsed;
        private double requestedPrice;
        private double profit;
        private double requestedStakelocal;
        private double commission;
        private String currencyCode;

        public BetfairBet(String betId) {
            this.betId = betId;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrenyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public double getRequestedStakeLocal() {
            return requestedStakelocal;
        }

        public void setRequestedStakelocal(double requestedStakelocal) {
            this.requestedStakelocal = requestedStakelocal;
        }

        public double getProfit() {
            return profit;
        }

        public void setProfit(double profit) {
            this.profit = profit;
        }

        public double getRequestedPrice() {
            return requestedPrice;
        }

        public void setRequestedPrice(double requestedPrice) {
            this.requestedPrice = requestedPrice;
        }

        public double getSizeVoided() {
            return sizeVoided;
        }

        public void setSizeVoided(double sizeVoided) {
            this.sizeVoided = sizeVoided;
        }

        public double getSizeLapsed() {
            return sizeLapsed;
        }

        public void setSizeLapsed(double sizeLapsed) {
            this.sizeLapsed = sizeLapsed;
        }

        public double getSizeRemaining() {
            return sizeRemaining;
        }

        public void setSizeRemaining(double sizeRemaining) {
            this.sizeRemaining = sizeRemaining;
        }

        public double getSizeMatched() {
            return sizeMatched;
        }

        public void setSizeMatched(double sizeMatched) {
            this.sizeMatched = sizeMatched;
        }

        public double getAveragePriceMatched() {
            return averagePriceMatched;
        }

        public void setAveragePriceMatched(double averagePriceMatched) {
            this.averagePriceMatched = averagePriceMatched;
        }

        public Date getPlacedDate() {
            return placedDate;
        }

        public void setPlacedDate(Date placedDate) {
            this.placedDate = placedDate;
        }

        public String getMarketId() {
            return marketId;
        }

        public void setMarketId(String marketId) {
            this.marketId = marketId;
        }

        public String getBetId() {
            return betId;
        }

        public long getSelectionId() {
            return selectionId;
        }

        public void setSelectionId(long selectionId) {
            this.selectionId = selectionId;
        }

        public int isBack() {
            if (side.equals(Side.BACK)) {
                return 1;
            } else if (side.equals(Side.LAY)) {
                return 0;
            } else {
                return -1;
            }
        }

        public void setSide(Side side) {
            this.side = side;
        }

        public BetStatus getStatus() {
            return status;
        }

        public void setStatus(BetStatus status) {
            this.status = status;
        }

        public double getSizeCancelled() {
            return sizeCanceled;
        }

        public void setSizeCancelled(double sizeCanceled) {
            this.sizeCanceled = sizeCanceled;
        }

        public double getCommission() {
            return commission;
        }

        private void setCommission(double commission) {
            this.commission = commission;
        }

    }
}
