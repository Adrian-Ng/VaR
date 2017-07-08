package VaR;

import java.util.ArrayList;

/**
 * Created by Adrian on 21/06/2017.
 */
/**
 * CS5930 Computational Finance Coursework
 * Marking ID: 6485
 * Source code for Monte Carlo simulation
 */

import java.util.HashMap;
import java.util.Random;
public class MonteCarlo {



    public static void printVariables (int X, int N, double S0, double sigma, double r, double dt, double expectationEuroCall, double expectationEuroPut) {
        //for debugging
        // X
        System.out.println("\tX: " + X);
        // T
        System.out.println("\tN: " +N);
        // S0
        System.out.println("\tS0: " + S0);
        // sigma
        System.out.println("\tsigma: " + sigma);
        //r
        System.out.println("\tr: " + r);
        // dt
        System.out.println("\tdt: " + dt);
        //expectationEuroCall,
        System.out.println("\texpectationEuroCall: " + expectationEuroCall);
        // expectationEuroPut
        System.out.println("\texpectationEuroPut: " + expectationEuroPut);
    }
    private static double grid[];

    public static double stepsRandomWalk(double dt) {
        // sample from random Gaussian of mean 0 and sd 1
        Random epsilon = new Random();
        double dz = epsilon.nextGaussian()*Math.sqrt(dt);
        return dz;
    }

    public static double simuluatePath(int N, double S0, double dt, double r, double sigma) {
        // allocate memory to grid
        grid = new double[N];
        grid[0] = S0;
        for (int i = 1; i < N; i++){
            double dz = stepsRandomWalk(dt);
            grid[i] = grid[i-1] + (r*grid[i-1]*dt)+(sigma*grid[i-1]*dz);
            //System.out.println(dz);
        }
        return grid[N-1];
    }

    public static void main(HashMap<String, ArrayList<Double>> mapStocks) {

        int numsym = mapStocks.size();
        //initialize ints
        int N = (int) Math.ceil(365.0/2.0);                 // 6 months expressed in days. this is the number of steps.
        int paths = 100000;                                 // number of random walks we will compute
        // initialize doubles
        //double S0 = 80.0;                                   // initial asset price
        double[] sigma = new double[numsym];                // volatility
        //double r = 0.07;                                    // interest rate
        double T = 0.5;                                     // half a year
        double dt = T/N;                                    // size of the step where each step is 1 day

        int i = 0;
        for (String sym : mapStocks.keySet()) {
            ArrayList<Double> alStocks = mapStocks.get(sym);
            StockParam thisStock = new StockParam(alStocks);
            //Get Volatilities
            double dailyEqualWeight = thisStock.getEqualWeightVolatility();
            double yearlyEqualWeight = thisStock.getEqualWeightVolatility() * Math.sqrt(252);
            double dailyEWMA = thisStock.getEWMAVolatility();
            double yearlyEWMA = thisStock.getEWMAVolatility() * Math.sqrt(252);






/*
            // simulate a number of stock price trajectories
            for (int i = 0; i < paths; i++) {
                double St = simuluatePath(N, S0, dt, r, sigma);

            }*/
        }

        //montecarlo.printVariables(X, N, S0, sigma, r, dt,expectationEuroCall,expectationEuroPut);

    }

}

