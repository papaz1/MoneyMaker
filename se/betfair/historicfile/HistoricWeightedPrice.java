package se.betfair.historicfile;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import se.moneymaker.model.Price;

public final class HistoricWeightedPrice {

    private List<Price> prices;
    private double weightedPrice;
    private Date latestTakenDate;

    public HistoricWeightedPrice(List<Price> prices) {
        this.prices = prices;
        this.latestTakenDate = ((Price) prices.get(0)).getLatestTakenDate();
        this.weightedPrice = calculateWeightedPrice(prices);
    }

    public Date getLatestTakenDate() {
        return latestTakenDate;
    }

    private double calculateWeightedPrice(List<Price> prices) {

        //If there is only one price for this timestamp then don't divide the price
        //with the volume matched. Instead just return it.
        if (prices.size() == 1) {
            return ((Price) prices.get(0)).getPrice();
        } else {

            Iterator<Price> iterator = prices.iterator();
            Price price;
            double weightedPriceTemp = 0;
            double volumeMatched = 0;
            double priceSum = 0;

            while (iterator.hasNext()) {
                price = iterator.next();
                priceSum = priceSum + (price.getVolumeMatched() * price.getPrice());
                volumeMatched = volumeMatched + price.getVolumeMatched();
            }

            if (volumeMatched != 0) {
                weightedPriceTemp = priceSum / volumeMatched;
            }
            BigDecimal bd = new BigDecimal(weightedPriceTemp);
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);

            return bd.doubleValue();
        }
    }

    public List<Price> getPrices() {
        return prices;
    }

    public double getWeightedPrice() {
        return weightedPrice;
    }

    public void setWeightedPrice(double weightedPrice) {
        this.weightedPrice = weightedPrice;
    }
}
