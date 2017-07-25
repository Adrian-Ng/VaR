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
        double[] terminalPercentChange = new double[numSym];

        for(int i = 0; i < numSym; i++)
            grid[i][0] = currentStockPrices[i];
        for(int i = 1; i < N; i++) {
            double[] correlatedRandomVariables = stepsRandomWalk(choleskyDecomposition);
            for (int j = 0; j < numSym; j ++)
                grid[j][i] = (correlatedRandomVariables[j] * grid[j][i-1] * Math.sqrt(dt)) +  grid[j][i-1];
        }
        //RETURN LAST RESULT ON THE GRID. THIS IS THE TERMINAL PERCENTAGE CHANGE
        for(int i = 0; i < numSym; i++)
            terminalPercentChange[i] = grid[i][N-1]/currentStockPrices[i];
        return terminalPercentChange;
    }

    public static double main(String[] symbol, double[][] stockPrices, int[] stockDelta, int timeHorizonN, double confidenceX) {
        System.out.println("=========================================================================");
        System.out.println("MonteCarlo.java");
        System.out.println("=========================================================================");

        //initialize ints
        int N = 24;                                         // 1 day expressed in hours. this is the number of steps.
        int paths = 100000;                                 // number of random walks we will compute
        // initialize doubles
        double T = timeHorizonN;                            // 1 day
        double dt = T/N;                                    // size of the step where each step is 1 hour

        int numSym = symbol.length;
        double[] currentStockPrices = new double[numSym];
        /**
         * WHAT DOES THE PORTFOLIO LOOK LIKE?
         */
        for (int i = 0; i < numSym; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            System.out.println("\t\t" + stockDelta[i] + " stocks in " + symbol[i] + ". Current price is: " + currentStockPrices[i]);
        }
        double currentValue = 0;
        for (int i = 0; i < numSym; i++) {
            currentValue += stockDelta[i] * currentStockPrices[i];
        }
        System.out.println("\t\tCurrent Value of Portfolio: " + currentValue);

        /**
         * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
         */
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
         * SIMULATE A NUMBER OF STOCK PERCENTAGE CHANGES
         */
        double[][] allPercentageChanges = new double[numSym][paths];
        for (int i = 0; i < paths; i++) {
            double[] tuplePercentageChanges = simuluatePath(N, currentStockPrices, dt, choleskyDecomposition);
            //System.out.println(Arrays.toString(correlatedRandomVariables));
            for(int j =0; j < numSym; j++)
                allPercentageChanges[j][i] = tuplePercentageChanges[j];
        }

        /**
         * REVALUE PORTFOLIO FROM ALL POSSIBLE PERCENTAGE CHANGES
         */
        double[] deltaP = new double[allPercentageChanges[0].length];
        for(int i = 0; i < allPercentageChanges[0].length;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += allPercentageChanges[j][i] * stockDelta[j] * currentStockPrices[j];
            deltaP[i] = sum;
        }
        /**
         * GET VaR
         */
        Arrays.sort(deltaP);
        double index = (1-confidenceX)*deltaP.length;
        double VaR = currentValue - deltaP[(int) index];
        System.out.println("\n\t\tValue at Risk: " + VaR);
        return VaR;
    }
}

