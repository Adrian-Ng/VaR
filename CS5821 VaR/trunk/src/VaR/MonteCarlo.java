package VaR;


/**
 * Created by Adrian on 21/06/2017.
 * http://financetrain.com/calculating-var-using-monte-carlo-simulation/
 */

import java.util.Arrays;
import java.util.Random;

public class MonteCarlo {


    public static double[] stepsRandomWalk(double[][] choleskyDecomposition) {
        int numSym = choleskyDecomposition.length;
        // Generate a vector of random variables, sampling from random Gaussian of mean 0 and sd 1
        double[] dz = new double[numSym];
        for(int i = 0; i < numSym; i++) {
            Random epsilon = new Random();
            dz[i] = epsilon.nextGaussian();
        }
        //multiply the Cholesky Decomposition by the vector of random variables.
        double[] correlatedRandomVariables = new double[numSym];
        for(int i = 0; i < numSym; i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += choleskyDecomposition[i][j] * dz[j] ;
            correlatedRandomVariables[i] = sum;
        }
        return correlatedRandomVariables;
    }
    public static double[] simuluatePath(int N, double[] currentStockPrices, double dt, double[][] choleskyDecomposition) {
        int numSym = choleskyDecomposition.length;
        double[][] grid = new double[numSym][N];
        double[] terminalStockPrice = new double[numSym];

        for(int i = 0; i < numSym; i++)
            grid[i][0] = currentStockPrices[i];
        for(int i = 1; i < N; i++) {
            double[] correlatedRandomVariables = stepsRandomWalk(choleskyDecomposition);
            for (int j = 0; j < numSym; j ++)
                grid[j][i] = (correlatedRandomVariables[j] * grid[j][i-1] * Math.sqrt(dt)) +  grid[j][i-1];
        }

        for(int i = 0; i < numSym; i++)
            terminalStockPrice[i] = grid[i][N-1]/currentStockPrices[i];
        return terminalStockPrice;
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

        int numSym = stockPrices.length;
        double[] currentStockPrices = new double[numSym];
        /**
         * WHAT DOES THE PORTFOLIO LOOK LIKE?
         */
        for (int i = 0; i < symbol.length; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            System.out.println("\t\t" + portfolioPi[i] + " stocks in " + symbol[i] + ". Current price is: " + currentStockPrices[i]);
        }
        double currentValue = 0;
        for (int i = 0; i < symbol.length; i++) {
            currentValue += portfolioPi[i] * currentStockPrices[i];
        }
        System.out.println("\t\tCurrent Value of Portfolio: " + currentValue);

        double[][] priceChanges = new StockParam(stockPrices).getPercentageChanges();

        /**
         * CALCULATE THE COVARIANCE MATRIX FROM THE STOCK MARKET VARIABLES
         */
        double[][] covarianceMatrix = new StockParam(priceChanges).getCovarianceMatrix();
        System.out.println("\n\t\tCovariance Matrix of historical price changes:");
        for(int i = 0; i < numSym; i++)
            System.out.println("\t\t" + Arrays.toString(covarianceMatrix[i]));
        /**
         * CALCULATE THE CHOLESKY DECOMPOSITION FROM THE STOCK MARKET VARIABLES
         */
        double[][] choleskyDecomposition = new StockParam(priceChanges).getCholeskyDecomposition();
        System.out.println("\n\t\tCholesky Decomposition of historical price changes:");
        for(int i = 0; i < numSym; i++)
            System.out.println("\t\t" + Arrays.toString(choleskyDecomposition[i]));

        /**
         * SIMULATE A NUMBER OF CORRELATED RANDOM VARIABLES
         */
        double[][] terminalPrices = new double[numSym][paths];
        for (int i = 0; i < paths; i++) {
            double[] correlatedRandomVariables = simuluatePath(N, currentStockPrices, dt, choleskyDecomposition);
            //System.out.println(Arrays.toString(correlatedRandomVariables));
            for(int j =0; j < numSym; j++)
                terminalPrices[j][i] = correlatedRandomVariables[j];
        }

        double[] deltaP = new double[terminalPrices[0].length];
        for(int i = 0; i < terminalPrices[0].length;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += terminalPrices[j][i] * portfolioPi[j] ;
            deltaP[i] = sum;
        }
        Arrays.sort(deltaP);
        double index = (1-confidenceX)*deltaP.length;
        System.out.println("\t\tValue at Risk: " + deltaP[(int) index]);


    }

}

