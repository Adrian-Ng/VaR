package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Arrays;

/**
 * Created by Adrian on 21/06/2017.
 * Referencing Paul Wilmott Chapter 22 and Hull Chapter 21
 */
public class Analytical {

    public static double[][] main(String[] symbol, double[][] stockPrices, int[] stockDelta, int timeHorizonN, double confidenceX) {
        System.out.println("=========================================================================");
        System.out.println("Analytical.java");
        System.out.println("=========================================================================");
        NormalDistribution distribution = new NormalDistribution(0, 1);
        double riskPercentile = -distribution.inverseCumulativeProbability(1 - confidenceX);
        int numSym = symbol.length;
        String[] nameVolatilityMeasures = {"Standard Deviation", "Equal Weighted", "EWMA"};
        double VaR[][] = new double[nameVolatilityMeasures.length][numSym];
        /** CALCULATE PERCENTAGE CHANGE IN STOCK PRICE*/
        double[][] priceChanges = new StockParam(stockPrices).getPercentageChanges();

        /** LOOP THROUGH EACH STOCK*/
        for (int i = 0; i < numSym; i++) {
            System.out.println("\t" + symbol[i]);
            /**PRINT STOCK VARIABLES*/
            System.out.println("\t\tStock Variables:");
            System.out.println("\t\t\tDelta:\t\t\t\t"       + stockDelta[i]);
            System.out.println("\t\t\tCurrent Price:\t\t"   + stockPrices[i][0]);
            System.out.println("\t\t\tValue:\t\t\t\t"       + stockDelta[i]*stockPrices[i][0]);

            /** CALCULATE VOLATILITIES*/
            double volatilityStDev = new StockParam(priceChanges[i]).getStandardDeviation();
            double volatilityEqualWeighted = new StockParam(stockPrices[i],stockPrices[i]).getEqualWeightVolatility();
            double volatilityEWMA = new StockParam(stockPrices[i],stockPrices[i]).getEWMAVolatility();

            /** PRINT VOLATILITIES*/
            System.out.println("\n\t\tVolatilities:");
            System.out.println("\t\t\tStandard Deviation: " + volatilityStDev);
            System.out.println("\t\t\tEqual Weighted:\t\t"  + volatilityEqualWeighted);
            System.out.println("\t\t\tEWMA:\t\t\t\t"        + volatilityEWMA);

            /** CALCULATE VAR FOR EACH VOLATILITY MEASURE*/
            VaR[0][i] = volatilityStDev * stockDelta[i] * stockPrices[i][0] * Math.sqrt(timeHorizonN) * riskPercentile;
            VaR[1][i] = volatilityEqualWeighted * stockDelta[i] * stockPrices[i][0] * Math.sqrt(timeHorizonN) * riskPercentile;
            VaR[2][i] = volatilityEWMA * stockDelta[i] * stockPrices[i][0] * Math.sqrt(timeHorizonN) * riskPercentile;

            /** PRINT VAR*/
            System.out.println("\n\t\tValue At Risk:");
            System.out.println("\t\t\tStandard Deviation: " + VaR[0][i]);
            System.out.println("\t\t\tEqual Weighted:\t\t"  + VaR[1][i]);
            System.out.println("\t\t\tEWMA:\t\t\t\t"        + VaR[2][i]);
            System.out.println("\t-----------------------------------------------");
        }
        return VaR;
    }
}
