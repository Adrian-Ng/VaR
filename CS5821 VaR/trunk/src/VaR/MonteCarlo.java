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

    public static double main(String[] stockSymbol, double[][] stockPrices, int[] stockDelta, optionsData[] options, int[] optionDelta, int timeHorizonN, double confidenceX) {
        System.out.println("=========================================================================");
        System.out.println("MonteCarlo.java");
        System.out.println("=========================================================================");

        int numSym = stockSymbol.length;
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

        //initialize ints
        int N = 24;                                         // 1 day expressed in hours. this is the number of steps.
        int paths = 100000;                                 // number of random walks we will compute
        // initialize doubles
        double T = timeHorizonN;                            // 1 day
        double dt = T/N;                                    // size of the step where each step is 1 hour


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
         * SIMULATE TOMORROW'S STOCK PRICE PERCENTAGE CHANGES VIA MONTE CARLO METHOD
         */
        double[][] allPercentageChanges = new double[numSym][paths];
        for (int i = 0; i < paths; i++) {
            double[] tuplePercentageChanges = simuluatePath(N, currentStockPrices, dt, choleskyDecomposition);
            for(int j =0; j < numSym; j++)
                allPercentageChanges[j][i] = tuplePercentageChanges[j];
        }

        /** SIMULATE ALL OF TOMORROW'S POSSIBLE STOCK PRICES FROM SIMULATED MONTE CARLO DATA*/
        int numTuple = allPercentageChanges[0].length;
        double[][] tomorrowStockPrices = new double[numSym][numTuple];
        for(int i = 0; i < numSym; i++)
            for(int  j = 0; j < numTuple; j++) {
                tomorrowStockPrices[i][j] = allPercentageChanges[i][j] * currentStockPrices[i];
                //System.out.println(tomorrowStockPrices[i][j]);
            }

        /** PRICE OPTIONS */
        double[][] tomorrowPutPrices = new double[numSym][numTuple];
        for(int i = 0; i < numSym; i++)
            for(int  j = 0; j < numTuple; j++) {
                tomorrowPutPrices[i][j] = options[i].getBlackScholesPut(tomorrowStockPrices[i][j]);
                //System.out.println(tomorrowPutPrices[i][j]);
            }

        /**
         * REVALUE PORTFOLIO FROM ALL POSSIBLE PERCENTAGE CHANGES
         */
        double[] deltaP = new double[allPercentageChanges[0].length];
        for(int i = 0; i < allPercentageChanges[0].length;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += (tomorrowStockPrices[j][i] * stockDelta[j]) + (tomorrowPutPrices[j][i] * optionDelta[j]) ;
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

