package VaR;


/**
 * Created by Adrian on 21/06/2017.
 * http://financetrain.com/calculating-var-using-monte-carlo-simulation/
 */

import java.util.Arrays;
import java.util.Random;

public class MonteCarlo {

    public static double[][] calculatePriceChanges(double[][] stockPrices){
        int numSym = stockPrices.length;
        int numTuples = stockPrices[0].length;
        double[][] priceDiff = new double[numSym][numTuples - 1];
        for  (int i = 0;i < numSym;i++)
            for (int j = 0; j < numTuples - 1; j++)
                priceDiff[i][j] = stockPrices[i][j] - stockPrices[i][j+1];
        return priceDiff;
    }
    public static double[] stepsRandomWalk(double dt, double[][] choleskyDecomposition) {
        int numSym = choleskyDecomposition.length;
        // Generate a vector of random variables, sampling from random Gaussian of mean 0 and sd 1
        double[] dz = new double[numSym];
        for(int i = 0; i < numSym; i++) {
            Random epsilon = new Random();
            dz[i] = epsilon.nextGaussian() * Math.sqrt(dt);
        }
        //multiply the Cholesky Decomposition by the vector of random variables.
        double[] correlatedRandomVariables = new double[numSym];
        for(int i = 0; i < numSym; i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += choleskyDecomposition[i][j] * dz[j];
            correlatedRandomVariables[i] = sum;
        }
        return dz;
    }
    public static double[] simuluatePath(int N, double currentStockPrices[], double dt, double[] mu, double[] sigma, double[][] choleskyDecomposition) {
        int numSym = mu.length;
        double[] terminalPrices = new double[numSym];
        double[] dz = stepsRandomWalk(dt, choleskyDecomposition);
          for(int i = 0; i < numSym; i++)
              terminalPrices[i] = currentStockPrices[i] * Math.exp((mu[i]-(Math.pow(sigma[i],2)*0.5))*dt+sigma[i]*Math.sqrt(dt)*dz[i]);
        return terminalPrices;
    }
    public static void main(String[] symbol, double[][] stockPrices) {

        long  portfolioPi[]     = {100,200};
        double  confidenceX     = 0.99;
        int     timeHorizonN    = 1;
        //initialize ints
        int N = 24;                                   // 1 day expressed in hours. this is the number of steps.
        int paths = 100000;                                  // number of random walks we will compute
        // initialize doubles
        double T = timeHorizonN;                            // 1 day
        double dt = T/N;                                    // size of the step where each step is 1 hour

        System.out.println("=========================================================================");
        System.out.println("MonteCarlo.java");
        System.out.println("=========================================================================");

        double currentPrice[] = new double[symbol.length];
        int numSym = stockPrices.length;


        /**
         * WHAT DOES THE PORTFOLIO LOOK LIKE?
         */
        for (int i = 0; i < symbol.length; i++) {
            currentPrice[i] = stockPrices[i][0];
            System.out.println("\t\t" + portfolioPi[i] + " stocks in " + symbol[i] + ". Current price is: " + currentPrice[i]);
        }
        double currentValue = 0;
        for (int i = 0; i < symbol.length; i++) {
            currentValue += portfolioPi[i] * currentPrice[i];
        }
        System.out.println("\t\tCurrent Value of Portfolio: " + currentValue);

        double[][] priceChanges = calculatePriceChanges(stockPrices);

        /**
         * CALCULATE THE COVARIANCE MATRIX FROM THE STOCK MARKET VARIABLES
         */
        double[][] covarianceMatrix = new StockParam(priceChanges).getCovarianceMatrix();
        System.out.println("\n\t\tCovariance Matrix:");
        for(int i = 0; i < numSym; i++)
            System.out.println("\t\t" + Arrays.toString(covarianceMatrix[i]));
        /**
         * CALCULATE THE CHOLESKY DECOMPOSITION FROM THE STOCK MARKET VARIABLES
         */
        double[][] choleskyDecomposition = new StockParam(priceChanges).getCholeskyDecomposition();
        System.out.println("\n\t\tCholesky Decomposition:");
        for(int i = 0; i < numSym; i++)
            System.out.println("\t\t" + Arrays.toString(choleskyDecomposition[i]));

        /**
         * RETURN A VECTOR OF MEAN STOCK PRICES
         */
        double[] meanStock = new double[numSym];
        for(int i = 0; i < numSym; i++)
            meanStock[i] = new StockParam(priceChanges[i]).getMean();

        /**
         * RETURN A VECTOR OF VOLATILITY
         */
        double[] volatilities = new double[numSym];
        for(int i = 0; i < numSym; i++)
            volatilities[i] = new StockParam(priceChanges[i]).getEWMAVolatility();

        /**
         * RETURN A VECTOR OF CURRENT STOCK PRICES
         */
        double[] currentStockPrices = new double[numSym];
        for(int i = 0; i < numSym; i++)
            currentStockPrices[i] = stockPrices[i][0];

        /**
         * SIMULATE A NUMBER OF TERMINAL STOCK PRICES
         */
        double[][] terminalPrices = new double[paths][numSym];
        // simulate a number of stock price trajectories
        for (int i = 0; i < paths; i++) {
            terminalPrices[i] = simuluatePath(N, currentStockPrices, dt, meanStock, volatilities, choleskyDecomposition);
            //System.out.println(Arrays.toString(terminalPrices[i]));
        }
        double[] deltaP = new double[terminalPrices[0].length];
        for(int i = 0; i < terminalPrices[0].length;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += terminalPrices[j][i] * portfolioPi[j];
            deltaP[i] = sum;
        }
        Arrays.sort(deltaP);
        double index = (1-confidenceX)*deltaP.length;
        System.out.println("\t\tValue at Risk: " + deltaP[(int) index]);


    }

}

