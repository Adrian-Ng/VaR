package VaR;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MonteCarlo {

    private static Random epsilon = new Random();
    public static double[] stepsRandomWalk(double[][] choleskyDecomposition) {
        int numSym = choleskyDecomposition.length;
        // Generate a vector of random variables, sampling from random Gaussian of mean 0 and sd 1
        double[] dz = new double[numSym];
        for(int i = 0; i < numSym; i++)
            dz[i] = epsilon.nextGaussian();
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
        //RETURN LAST RESULT ON THE GRID. THIS IS THE TERMINAL PERCENTAGE CHANGE
        for(int i = 0; i < numSym; i++)
            terminalStockPrice[i] = grid[i][N-1];
        return terminalStockPrice;
    }
    public static double main(String[] stockSymbol, double[][] stockPrices, int[] stockDelta, optionsData[] options, int[] optionDelta, int timeHorizonN, double confidenceX, int printFlag, String relativePath)throws IOException {
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
        /** CALCULATE PERCENTAGE CHANGE IN STOCK PRICE*/
        double[][] priceChanges = new methods(stockPrices).getPercentageChanges();
        /** CALCULATE THE COVARIANCE MATRIX FROM THE STOCK MARKET VARIABLES*/
        double[][] covarianceMatrix = new methods(priceChanges).getCovarianceMatrix();
        System.out.println("\n\t\tCovariance Matrix of historical price changes:");
        for(int i = 0; i < numSym; i++)
            System.out.println("\t\t" + Arrays.toString(covarianceMatrix[i]));
        /** CALCULATE THE CHOLESKY DECOMPOSITION FROM THE STOCK MARKET VARIABLES*/
        double[][] choleskyDecomposition = new methods(priceChanges).getCholeskyDecomposition();
        System.out.println("\n\t\tCholesky Decomposition of historical price changes:");
        for(int i = 0; i < numSym; i++)
            System.out.println("\t\t" + Arrays.toString(choleskyDecomposition[i]));
        /** SIMULATE TOMORROW'S STOCK PRICE VIA MONTE CARLO METHOD*/
        double[][] tomorrowStockPrices = new double[numSym][paths];
        for (int i = 0; i < paths; i++) {
            double[] tuplePercentageChanges = simuluatePath(N, currentStockPrices, dt, choleskyDecomposition);
            for(int j =0; j < numSym; j++)
                tomorrowStockPrices[j][i] = tuplePercentageChanges[j];
        }
        /** PRICE OPTIONS */
        double[][] tomorrowPutPrices = new double[numSym][paths];
        for(int i = 0; i < numSym; i++)
            for(int  j = 0; j < paths; j++)
                tomorrowPutPrices[i][j] = options[i].getBlackScholesPut(tomorrowStockPrices[i][j]);
        /** REVALUE PORTFOLIO FROM ALL POSSIBLE PERCENTAGE CHANGES*/
        double[] deltaP = new double[paths];
        for(int i = 0; i < paths;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += (tomorrowStockPrices[j][i] * stockDelta[j]) + (tomorrowPutPrices[j][i] * optionDelta[j]);
            deltaP[i] = sum;
        }
        /** GET VaR*/
        Arrays.sort(deltaP);
        double index = (1-confidenceX)*deltaP.length;
        double VaR = currentValue - deltaP[(int) index];
        System.out.println("\n\t\tValue at Risk: " + VaR);
        /** PRINT DATA TO CSV*/
        if (printFlag == 1) {
            new methods(tomorrowStockPrices).printMatrixToCSV(stockSymbol, "MonteCarlo stockPrices", relativePath);
            new methods(tomorrowPutPrices).printMatrixToCSV(stockSymbol, "MonteCarlo putPrices", relativePath);
            new methods(deltaP).printVectorToCSV("Portfolio Value", "MonteCarlo Portfolio Value", relativePath);
        }
        return VaR;
    }
}

