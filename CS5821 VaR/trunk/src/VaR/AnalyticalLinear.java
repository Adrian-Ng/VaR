package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.Arrays;

/**
 * Created by Adrian on 14/07/2017.
 * Referencing Paul Wilmott Chapter 22 and Hull Chapter 21
 */
public class AnalyticalLinear {

public static double[] main(String[] stockSymbol, double[][] stockPrices, int[] stockDelta, optionsData[] options, int[] optionDelta, int timeHorizonN, double confidenceX){
    System.out.println("=========================================================================");
    System.out.println("AnalyticalLinear.java");
    System.out.println("=========================================================================");
    NormalDistribution distribution = new NormalDistribution(0,1);
    double riskPercentile = - distribution.inverseCumulativeProbability(1-confidenceX);
    int numSym = stockSymbol.length;
    String[] nameVolatilityMeasures = {"Standard Deviation", "EWMA", "GARCH(1,1)"};
    double VaR[] = new double[nameVolatilityMeasures.length];
    double[] currentStockPrices = new double[numSym];
    double[][] strikePrices = new double[numSym][];
    double[][] currentPutPrices = new double[numSym][];
    long[] daystoMaturity = new long[numSym];
    double currentValue = 0;
    for (int i = 0; i < numSym; i++) {
        currentStockPrices[i] = stockPrices[i][0];
        strikePrices[i] = options[i].getStrikePrices();
        daystoMaturity[i] = options[i].getDaystoMaturity();
        currentPutPrices[i] = options[i].getPutPrices();
        int numPuts = currentPutPrices[i].length;
        currentValue += stockDelta[i] * currentStockPrices[i] + optionDelta[i] * currentPutPrices[i][numPuts-1];
    }
    /**
     * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
     */
    double[][] priceChanges = new StockParam(stockPrices).getPercentageChanges();
    /**
     * CALCULATE COVARIANCE MATRIX
     */
    System.out.println("\n\t\tCovariance Matrix: ");
    double[][] covarianceMatrix = new StockParam(priceChanges).getCovarianceMatrix();
    for(int i = 0; i < numSym; i++)
        System.out.println("\t\t" + Arrays.toString(covarianceMatrix[i]));
    /**
     * CALCULATE CORRELATION MATRIX
     */
    double[][] correlationMatrix = new double[numSym][numSym];
    System.out.println("\n\t\tCorrelation Matrix:");
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
     * CALCULATE A VECTOR OF VOLATILITIES FOR EACH VOLATILITY MEASURE
     */
    double[] volatilityStDev = new double[numSym];
    double[] volatilityEWMA = new double[numSym];
    double[] volatilityGARCH11 = new double[numSym];
    for(int i = 0; i < numSym; i++) {
        volatilityStDev[i] = new StockParam(priceChanges[i]).getStandardDeviation();
        volatilityEWMA[i] = new StockParam(priceChanges[i],priceChanges[i]).getEWMAVolatility();
        double[] params = new StockParam(stockPrices[i]).getGARCHParams();
        volatilityGARCH11[i] = Math.sqrt(params[0]/(1-params[1]-params[2]));
    }
    double sumStDev = 0;
    double sumEWMA = 0;
    double sumGARCH = 0;
    for(int i = 0; i < numSym; i++)
        for(int j = 0; j < numSym; j++) {
            sumStDev += stockDelta[i] * stockDelta[j] * currentStockPrices[i] * currentStockPrices[j] * correlationMatrix[i][j] * volatilityStDev[i] * volatilityStDev[j];
            sumEWMA += stockDelta[i] * stockDelta[j] * currentStockPrices[i] * currentStockPrices[j] * correlationMatrix[i][j] * volatilityEWMA[i] * volatilityEWMA[j];
            sumGARCH += stockDelta[i] * stockDelta[j] * currentStockPrices[i] * currentStockPrices[j] * correlationMatrix[i][j] * volatilityGARCH11[i] * volatilityGARCH11[j];
        }
    VaR[0] = Math.sqrt(timeHorizonN) * riskPercentile * Math.sqrt(sumStDev);
    VaR[1] = Math.sqrt(timeHorizonN) * riskPercentile * Math.sqrt(sumEWMA);
    VaR[2] = Math.sqrt(timeHorizonN) * riskPercentile * Math.sqrt(sumGARCH);
    /** PRINT VAR*/
    System.out.println("\n\t\tValue At Risk:");
    System.out.println("\t\t\tStandard Deviation: " + VaR[0]);
    System.out.println("\t\t\tEWMA:\t\t\t\t"        + VaR[1]);
    System.out.println("\t\t\tGARCH(1,1):\t\t\t"    + VaR[2]);

    return VaR;
    }
}
