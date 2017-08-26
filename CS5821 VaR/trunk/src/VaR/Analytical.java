package VaR;
import org.apache.commons.math3.distribution.NormalDistribution;
import java.util.Arrays;
/**
 * Created by Adrian on 14/07/2017.
 * Referencing Paul Wilmott Chapter 22 and Hull Chapter 21
 */
public class Analytical {

public static double[] main(Parameters p, double[][] stockPrices){
    System.out.println("=========================================================================");
    System.out.println("Analytical.java");
    System.out.println("=========================================================================");
    NormalDistribution distribution = new NormalDistribution(0,1);
    double riskPercentile = - distribution.inverseCumulativeProbability(1-p.getConfidenceLevel());
    int numSym = p.getNumSym();
    // get Parameters
    int[] stockDelta = p.getStockDelta();
    //initialize arrays
    String[] nameVolatilityMeasures = {"EW", "EWMA","GARCH(1,1)"};
    double VaR[] = new double[nameVolatilityMeasures.length];
    double[] currentStockPrices = new double[numSym];
    for (int i = 0; i < numSym; i++)
        currentStockPrices[i] = stockPrices[i][0];

    /** CALCULATE PERCENTAGE CHANGE IN STOCK PRICE*/
    double[][] priceChanges = new Stats(stockPrices).getPercentageChanges();

    /** COMPUTE CORRELATION MATRICES*/
    double[][][] correlationMatrix = new double[nameVolatilityMeasures.length][numSym][numSym];
    for(int i = 0; i < nameVolatilityMeasures.length; i++) {
        System.out.println("\n\t\tCorrelation Matrix " + nameVolatilityMeasures[i] + ":");
        correlationMatrix[i] = new Stats(priceChanges).getCorrelationMatrix(i+1);
    }

    /** CALCULATE A VECTOR OF VOLATILITIES FOR EACH VOLATILITY MEASURE*/
    double[][] volatility = new double[nameVolatilityMeasures.length][numSym];
    for(int i = 0; i < nameVolatilityMeasures.length; i++)
        for(int j = 0; j < numSym; j++)
            volatility[i][j] = new Stats(priceChanges[j],priceChanges[j]).getVolatility(i+1);

    /** COMPUTE SUM OF LINEAR COMPONENTS */
    double[] sum = new double[nameVolatilityMeasures.length];
    Arrays.fill(sum,0);
    for(int i = 0; i < numSym; i++)
        for(int j = 0; j < numSym; j++)
            for(int k = 0; k < nameVolatilityMeasures.length; k++)
                    sum[k] += stockDelta[i] * stockDelta[j] * currentStockPrices[i] * currentStockPrices[j] * correlationMatrix[k][i][j] * volatility[k][i] * volatility[k][j];
    /** CALCULATE VaR*/
    for(int i = 0; i < nameVolatilityMeasures.length;i++)
        VaR[i] = Math.sqrt(p.getTimeHorizon()) * riskPercentile * Math.sqrt(sum[i]);
    /** PRINT VAR*/
    System.out.println("\n\t\tValue At Risk:");
    System.out.println("\t\t\tEW\t\t\t\t\t: " + VaR[0]);
    System.out.println("\t\t\tEWMA:\t\t\t\t"        + VaR[1]);
    System.out.println("\t\t\tGARCH(1,1):\t\t\t"    + VaR[2]);
    return VaR;
    }
}
