package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.Arrays;

/**
 * Created by Adrian on 14/07/2017.
 */
public class Linear {

public static void main(String[] symbol, double[][] stockPrices, int[] stockDelta, int timeHorizonN, double confidenceX){
    System.out.println("=========================================================================");
    System.out.println("Linear.java");
    System.out.println("=========================================================================");
    NormalDistribution distribution = new NormalDistribution(0,1);
    double riskPercentile = - distribution.inverseCumulativeProbability(1-confidenceX);
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
     * CALCULATE CORRELATION MATRIX
     */
    double[][] correlationMatrix = new double[numSym][numSym];
    System.out.println("\n\t\tMatrix of Correlation Coefficients:");
    for (int i = 0; i < numSym; i++) {
        for (int j = 0; j < numSym; j++) {
            double covXY = new StockParam(priceChanges[i], priceChanges[j]).getCovariance();
            double stDevX = new StockParam(priceChanges[i]).getStandardDeviation();
            double stDevY = new StockParam(priceChanges[j]).getStandardDeviation();
            correlationMatrix[i][j] = covXY / (stDevX * stDevY);
        }
        System.out.println("\t\t" + Arrays.toString(correlationMatrix[i]));
    }

    /**
     * CALCULATE VECTOR OF STANDARD DEVIATIONS
     */
    double[] stDevVector = new double[numSym];
    for(int i = 0; i < numSym; i++)
        stDevVector[i] = new StockParam(priceChanges[i]).getStandardDeviation();
    System.out.println("\n\t\t Standard Deviation: " + Arrays.toString(stDevVector));

    double sum = 0;
    for(int i = 0; i < numSym; i++)
        for(int j = 0; j <= i; j++) {
            sum += stockDelta[i] * stockDelta[j] * stDevVector[i] * stDevVector[j] * correlationMatrix[i][j];
            System.out.println("\n" + stockDelta[i] + "*" + stockDelta[j] + "*" + stDevVector[i]  + "*" +  + stDevVector[j] + correlationMatrix[i][j]);
        }

    double VaR = Math.sqrt(timeHorizonN) * riskPercentile * Math.sqrt(sum);

    System.out.println("\n\t\tValue at Risk: " + VaR);
    }
}