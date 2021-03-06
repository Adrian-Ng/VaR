package VaR;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class MonteCarlo {
    private static Random epsilon = new Random();
    private static double[] weinerProcess(double[][] choleskyDecomposition) {

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
                sum += choleskyDecomposition[i][j] * dz[j];
            correlatedRandomVariables[i] = sum;
        }
        return correlatedRandomVariables;
    }
    private static double[] randomWalk(int N, double[] currentStockPrices, double dt, double[][] choleskyDecomposition) {
        int numSym = choleskyDecomposition.length;
        double[][] grid = new double[numSym][N];
        double[] terminalStockPrice = new double[numSym];
        for(int i = 0; i < numSym; i++)
            grid[i][0] = currentStockPrices[i];
        for(int i = 1; i < N; i++) {
            double[] correlatedRandomVariables = weinerProcess(choleskyDecomposition);
            for (int j = 0; j < numSym; j ++)
                grid[j][i] = (correlatedRandomVariables[j] * grid[j][i-1] * Math.sqrt(dt)) +  grid[j][i-1];
        }
        //RETURN LAST RESULT ON THE GRID. THIS IS THE TERMINAL PERCENTAGE CHANGE
        for(int i = 0; i < numSym; i++)
            terminalStockPrice[i] = grid[i][N-1];
        return terminalStockPrice;
    }
    public static double[] main(Parameters p, double[][] stockPrices, optionsData[] options, int printFlag)throws IOException {
        System.out.println("=========================================================================");
        System.out.println("MonteCarlo.java");
        System.out.println("=========================================================================");
        int numSym = p.getNumSym();
        //get Parameters
        int[] stockDelta = p.getStockDelta();
        int[] optionDelta = p.getOptionsDelta();
        //initialize arrays
        String[] nameVolatilityMeasures = {"EW", "EWMA","GARCH(1,1)"};
        double[] currentStockPrices = new double[numSym];
        double[][] strikePrices = new double[numSym][];
        double[][] currentPutPrices = new double[numSym][];
        int[] daystoMaturity = new int[numSym];
        double todayPi = 0;
        for (int i = 0; i < numSym; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            strikePrices[i] = options[i].getStrikePrices();
            daystoMaturity[i] = options[i].getDaystoMaturity();
            currentPutPrices[i] = options[i].getPutPrices();
            int numPuts = currentPutPrices[i].length;
            todayPi += stockDelta[i] * currentStockPrices[i] + optionDelta[i] * currentPutPrices[i][numPuts-1];
        }
        //initialize ints
        int N = 24;                                        // 1 day expressed in hours. this is the number of steps.
        int paths = 10000;                                 // number of random walks we will compute
        // initialize doubles

        double dt = 1.0/N;                                    // magnitude of step
        /** CALCULATE PERCENTAGE CHANGE IN STOCK PRICE*/
        double[][] priceChanges = new Stats(stockPrices).getPercentageChanges();

        /** CALCULATE THE CHOLESKY DECOMPOSITION FROM THE STOCK MARKET VARIABLES*/
        double[][][] choleskyDecomposition = new double[nameVolatilityMeasures.length][numSym][numSym];
        for(int i = 0; i < choleskyDecomposition.length; i++) {
            System.out.println("\n\t\tCholesky Decomposition:" + nameVolatilityMeasures[i] + ":");
            choleskyDecomposition[i] = new Stats(priceChanges).getCholeskyDecomposition(i + 1);
            for(int j = 0; j < numSym; j++)
                System.out.println("\t\t" + Arrays.toString(choleskyDecomposition[i][j]));
        }
        /** SIMULATE TOMORROW'S STOCK PRICE VIA MONTE CARLO METHOD*/
        double[][][] tomorrowStockPrices = new double[nameVolatilityMeasures.length][numSym][paths];
        for(int i = 0; i < tomorrowStockPrices.length; i++)
            for (int j = 0; j < paths; j++) {
                double[] tuplePercentageChanges = randomWalk(N, currentStockPrices, dt, choleskyDecomposition[i]);
                for(int k =0; k < numSym; k++)
                    tomorrowStockPrices[i][k][j] = tuplePercentageChanges[k];
            }
        /** PRICE OPTIONS */
        double[][][] tomorrowPutPrices = new double[nameVolatilityMeasures.length][numSym][paths];
        for(int i = 0; i < tomorrowPutPrices.length; i++)
            for(int j = 0; j < numSym; j++)
                for(int  k = 0; k < paths; k++)
                    tomorrowPutPrices[i][j][k] = options[j].getBlackScholesPut(tomorrowStockPrices[i][j][k]);
        /** REVALUE PORTFOLIO FROM ALL POSSIBLE PERCENTAGE CHANGES*/
        double[][] tomorrowPi = new double[nameVolatilityMeasures.length][paths];
        for(int i = 0; i < tomorrowPi.length; i++)
            for(int j = 0; j < paths;j++) {
                double sum = 0;
                for (int k = 0; k < numSym; k++)
                    sum += (tomorrowStockPrices[i][k][j] * stockDelta[k]) + (tomorrowPutPrices[i][k][j] * optionDelta[k]);
                tomorrowPi[i][j] = sum;
            }
        /** GET VaR*/
        double[] VaR = new double[nameVolatilityMeasures.length];
        for(int i = 0; i < tomorrowPi.length; i++) {
            Arrays.sort(tomorrowPi[i]);
            double index = (1 - p.getConfidenceLevel()) * tomorrowPi[i].length;
            VaR[i] = (todayPi - tomorrowPi[i][(int) index]) * Math.sqrt(p.getTimeHorizon());
        }
        System.out.println("\n\t\tValue At Risk:");
        System.out.println("\t\t\tEW:\t\t\t\t\t"        + VaR[0]);
        System.out.println("\t\t\tEWMA:\t\t\t\t"        + VaR[1]);
        System.out.println("\t\t\tGARCH(1,1):\t\t\t"    + VaR[2]);
        /** PRINT DATA TO CSV*/
        if (printFlag == 1) {
            for(int i = 0; i < nameVolatilityMeasures.length; i++) {
                new Stats(tomorrowStockPrices[i]).printMatrixToCSV(p.getSymbol(), "MonteCarlo stockPrices " + nameVolatilityMeasures[i], p.getOutputPath());
                new Stats(tomorrowPutPrices[i]).printMatrixToCSV(p.getSymbol(), "MonteCarlo putPrices " + nameVolatilityMeasures[i], p.getOutputPath());
                new Stats(tomorrowPi[i]).printVectorToCSV("Portfolio Value", "MonteCarlo Portfolio Value " + nameVolatilityMeasures[i], p.getOutputPath());
            }
        }
        return VaR;
    }
}

