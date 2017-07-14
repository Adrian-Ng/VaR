package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Arrays;

/**
 * Created by Adrian on 21/06/2017.
 * Referencing Paul Wilmott Chapter 22 and Hull Chapter 21
 */
public class Analytical {

    public static void main(String[] symbol, double[][] stockPrices, int[] stockDelta, int timeHorizonN, double confidenceX) {
        System.out.println("=========================================================================");
        System.out.println("Analytical.java");
        System.out.println("=========================================================================");
        NormalDistribution distribution = new NormalDistribution(0, 1);
        double riskPercentile = -distribution.inverseCumulativeProbability(1 - confidenceX);
        int numSym = symbol.length;
        double[] currentStockPrices = new double[numSym];
        /**
         * WHAT DOES THE PORTFOLIO LOOK LIKE?
         */
        for (int i = 0; i < symbol.length; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            System.out.println("\t\t" + stockDelta[i] + " stocks in " + symbol[i] + ". Current price is: " + currentStockPrices[i]);
        }
        double currentValue = 0;
        for (int i = 0; i < symbol.length; i++) {
            currentValue += stockDelta[i] * currentStockPrices[i];
        }
        System.out.println("\t\tCurrent Value of Portfolio: " + currentValue);

        /**
         * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
         */
        double[][] priceChanges = new StockParam(stockPrices).getPercentageChanges();

        /**
         * CALCULATE VECTOR OF STANDARD DEVIATIONS
         */
        double[] stDevVector = new double[numSym];
        for (int i = 0; i < numSym; i++)
            stDevVector[i] = new StockParam(priceChanges[i]).getStandardDeviation();
        System.out.println("\n\t\t Standard Deviation: " + Arrays.toString(stDevVector));

        double VaR[] = new double[numSym];
        for (int i = 0; i < numSym; i++){
            VaR[i] = stDevVector[i] * stockDelta[i] * currentStockPrices[i] * Math.sqrt(timeHorizonN / 252.0) * riskPercentile;
            System.out.println("\n\t\tValue At Risk " + symbol[i] + ": " + VaR[i]);
        }
    }
}
