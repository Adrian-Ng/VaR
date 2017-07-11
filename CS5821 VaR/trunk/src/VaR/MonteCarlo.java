package VaR;


/**
 * Created by Adrian on 21/06/2017.
 * http://financetrain.com/calculating-var-using-monte-carlo-simulation/
 */

import java.util.Arrays;
import java.util.Random;

public class MonteCarlo {
    private static double grid[];

    public static double[][] calculatePriceChanges(double[][] stockPrices){
        int numSym = stockPrices.length;
        int numTuples = stockPrices[0].length;
        double[][] priceDiff = new double[numSym][numTuples - 1];
        for  (int i = 0;i < numSym;i++)
            for (int j = 0; j < numTuples - 1; j++)
                priceDiff[i][j] = stockPrices[i][j] - stockPrices[i][j+1];
        return priceDiff;
    }

    public static double stepsRandomWalk(double dt) {
        // sample from random Gaussian of mean 0 and sd 1
        Random epsilon = new Random();
        double dz = epsilon.nextGaussian()*Math.sqrt(dt);
        return dz;
    }
    public static double simuluatePath(int N, double S0, double dt, double mu, double sigma) {
        // allocate memory to grid
        grid = new double[N];
        grid[0] = S0;
        for (int i = 1; i < N; i++){
            double dz = stepsRandomWalk(dt);
            //grid[i] = grid[i-1] + (mu*grid[i-1]*dt) + (sigma*grid[i-1]*dz);
            grid[i] = (((mu*dt)+(sigma*dz))*grid[i-1]) + grid[i-1];
            //System.out.println(dz);
        }
        //return the final stock price
        return grid[N-1];
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
        int numTuples = stockPrices[0].length;

        double[][] covarianceMatrix = new StockParam(stockPrices).getCovarianceMatrix();
        for(int i = 0; i < numSym; i++)
            for(int j = 0; j < numSym; j++)
                System.out.println(covarianceMatrix[i][j]);

        /*for (int i = 0; i < symbol.length; i++) {
            System.out.println("\t" + symbol[i]);

           // double[][] priceDiff = calculatePriceChanges(stockPrices);
            StockParam thisStock = new StockParam(stockPrices[i]);

            System.out.println("\t\tCurrent Stock Price: " + stockPrices[i][0]);
            System.out.println("\t\tWe have " + portfolioPi[i] + " shares");
            //Get Current Value
            double currentValue = stockPrices[i][0] * portfolioPi[i];
            System.out.println("\t\tCurrent Single Stock Portfolio Value: " + String.format("%.2f",currentValue));
            //Get Mean Price
            double meanStockPrice = thisStock.getMean();
            //System.out.println(meanStockPrice);

            //Get Variance
            double stDevStockPrice = thisStock.getStandardDeviation();
            //System.out.println(stDevStockPrice);
            //Get Volatilities
            double dailyEqualWeight = thisStock.getEqualWeightVolatility();
            double yearlyEqualWeight = thisStock.getEqualWeightVolatility() * Math.sqrt(252);
            double dailyEWMA = thisStock.getEWMAVolatility();
            double yearlyEWMA = thisStock.getEWMAVolatility() * Math.sqrt(252);

            double[] TerminalStocks = new double[paths];
            // simulate a number of stock price trajectories
            for (int j = 0; j < paths; j++) {
                TerminalStocks[j] = simuluatePath(N, stockPrices[i][0], dt, meanStockPrice, stDevStockPrice);
                //System.out.println(TerminalStocks[i]);
            }
            //sort
            Arrays.sort(TerminalStocks);
            double VaR = TerminalStocks[(int) (confidenceX*paths)-1];
            System.out.println("\t\tVaR: " + String.format("%.2f",VaR));
            System.out.println("\t\tVaR: " + VaR);

        }*/
    }

}

