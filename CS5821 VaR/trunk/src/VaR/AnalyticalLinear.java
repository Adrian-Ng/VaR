package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.Arrays;

/**
 * Created by Adrian on 14/07/2017.
 * Referencing Paul Wilmott Chapter 22 and Hull Chapter 21
 */
public class AnalyticalLinear {

public static double[] main(String[] stockSymbol, double[][] stockPrices, int[] stockDelta,int timeHorizonN, double confidenceX){
    System.out.println("=========================================================================");
    System.out.println("AnalyticalLinear.java");
    System.out.println("=========================================================================");
    NormalDistribution distribution = new NormalDistribution(0,1);
    double riskPercentile = - distribution.inverseCumulativeProbability(1-confidenceX);
    int numSym = stockSymbol.length;
    String[] nameVolatilityMeasures = {"Standard Deviation", "EWMA","GARCH(1,1)"};
    double VaR[] = new double[nameVolatilityMeasures.length];
    double[] currentStockPrices = new double[numSym];
    for (int i = 0; i < numSym; i++)
        currentStockPrices[i] = stockPrices[i][0];
    /**
     * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
     */
    double[][] priceChanges = new methods(stockPrices).getPercentageChanges();
    /**
     * CALCULATE COVARIANCE MATRIX
     */
    System.out.println("\n\t\tCovariance Matrix: ");
    double[][] covarianceMatrix = new methods(priceChanges).getCovarianceMatrix();
    for(int i = 0; i < numSym; i++)
        System.out.println("\t\t" + Arrays.toString(covarianceMatrix[i]));
    /**
     * CALCULATE CORRELATION MATRIX EQUAL WEIGHTINGS
     */
    double[][][] correlationMatrix = new double[nameVolatilityMeasures.length][numSym][numSym];
    System.out.println("\n\t\tCorrelation Matrix Standard Deviation:");
    for (int i = 0; i < numSym; i++) {
        for (int j = 0; j < numSym; j++) {
            double covXY = new methods(priceChanges[i], priceChanges[j]).getCovariance();
            double stDevX = new methods(priceChanges[i]).getStandardDeviation();
            double stDevY = new methods(priceChanges[j]).getStandardDeviation();
            correlationMatrix[0][i][j] = covXY / (stDevX * stDevY);
        }
        System.out.println("\t\t" + Arrays.toString(correlationMatrix[0][i]));
    }

    /**
     * CALCULATE CORRELATION MATRIX FOR EWMA
     */
    System.out.println("\n\t\tCorrelation Matrix EWMA:");
    for (int i = 0; i < numSym; i++) {
        for (int j = 0; j < numSym; j++) {
            double covXY = new methods(priceChanges[i], priceChanges[j]).getEWMAVariance();
            double stDevX = new methods(priceChanges[i],priceChanges[i]).getEWMAVolatility();
            double stDevY = new methods(priceChanges[j],priceChanges[j]).getEWMAVolatility();
            correlationMatrix[1][i][j] = covXY / (stDevX * stDevY);
        }
        System.out.println("\t\t" + Arrays.toString(correlationMatrix[1][i]));
    }

    /**
     * CALCULATE CORRELATION MATRIX FOR GARCH(1,1)
     */
    System.out.println("\n\t\tCorrelation Matrix GARCH(1,1):");
    for (int i = 0; i < numSym; i++) {
        for (int j = 0; j < numSym; j++) {
            double covXY = new methods(priceChanges[i], priceChanges[j]).getGARCH11Variance();
            double stDevX = new methods(priceChanges[i],priceChanges[i]).getGARCH11Volatility();
            double stDevY = new methods(priceChanges[j],priceChanges[j]).getGARCH11Volatility();
            correlationMatrix[2][i][j] = covXY / (stDevX * stDevY);
        }
        System.out.println("\t\t" + Arrays.toString(correlationMatrix[2][i]));
    }
    /**
     * CALCULATE A VECTOR OF VOLATILITIES FOR EACH VOLATILITY MEASURE
     */
    double[] volatilityStDev = new double[numSym];
    double[] volatilityEWMA = new double[numSym];
    double[] volatilityGARCH11 = new double[numSym];
    for(int i = 0; i < numSym; i++) {
        volatilityStDev[i] = new methods(priceChanges[i]).getStandardDeviation();
        volatilityEWMA[i] = new methods(priceChanges[i],priceChanges[i]).getEWMAVolatility();
        volatilityGARCH11[i] = new methods(priceChanges[i],priceChanges[i]).getGARCH11Volatility();
    }


    double sumStDev = 0;
    double sumEWMA = 0;
    double sumGARCH = 0;
    for(int i = 0; i < numSym; i++)
        for(int j = 0; j < numSym; j++) {
            sumStDev += stockDelta[i] * stockDelta[j] * currentStockPrices[i] * currentStockPrices[j] * correlationMatrix[0][i][j] * volatilityStDev[i] * volatilityStDev[j];
            sumEWMA += stockDelta[i] * stockDelta[j] * currentStockPrices[i] * currentStockPrices[j] * correlationMatrix[1][i][j] * volatilityEWMA[i] * volatilityEWMA[j];
            sumGARCH += stockDelta[i] * stockDelta[j] * currentStockPrices[i] * currentStockPrices[j] * correlationMatrix[2][i][j] * volatilityGARCH11[i] * volatilityGARCH11[j];
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
