package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;
/**
 * Created by Adrian on 21/06/2017.
 */
public class Analytical {

    public static void main(String[] symbol, double[][] stockPrices){
        long    portfolioPi[]     = {100,200};
        double  confidenceX     = 0.99;
        int     timeHorizonN    = 1;
        NormalDistribution distribution = new NormalDistribution(0,1);
        double riskPercentile = - distribution.inverseCumulativeProbability(1-confidenceX);
        double singleSTDperday[] = new double[symbol.length];

        System.out.println("=========================================================================");
        System.out.println("Analytical.java");
        System.out.println("=========================================================================");

        for (int i = 0; i < symbol.length; i++) {
            System.out.println("\t" + symbol[i]);
            StockParam thisStock = new StockParam(stockPrices[i]);
            //Get Volatilities
            double dailyEqualWeight = thisStock.getEqualWeightVolatility();
            double yearlyEqualWeight = thisStock.getEqualWeightVolatility() * Math.sqrt(252);
            double dailyEWMA = thisStock.getEWMAVolatility();
            double yearlyEWMA = thisStock.getEWMAVolatility() * Math.sqrt(252);
           /*
            //Print Volatilities
            System.out.println("\n\t$" + portfolioPi[i] + " in " + sym);
            System.out.println("\t\tDaily Equal Weighted Volatility is: " + dailyEqualWeight);
            System.out.println("\t\tYearly Equal Weighted Volatility is: " + yearlyEqualWeight);
            System.out.println("\t\tDaily EWMA Volatility is: " + dailyEWMA);
            System.out.println("\t\tYearly EWMA Volatility is: " + yearlyEWMA);
            */
            //Print Single Stock VaR
            singleSTDperday[i] = portfolioPi[i] * dailyEWMA * Math.sqrt(timeHorizonN);
            System.out.println("\t\tStandard deviation of daily changes: " + singleSTDperday[i]);
            double VaR = singleSTDperday[i] * riskPercentile;
            System.out.println("\t\tVaR for " + symbol[i] + " over " + timeHorizonN + " day: " + VaR);
        }
        double stdX = singleSTDperday[0];
        double stdY = singleSTDperday[1];
        double covXY = new StockParam(stockPrices[0],stockPrices[1]).getCovariance();
        double rhoXY = covXY/(stdX*stdY);
        double stdXY = Math.sqrt(Math.pow(stdX,2) + Math.pow(stdY,2) + 2*rhoXY*stdX*stdY);
        System.out.println("\n\t\tValue at Risk for the whole portfolio over " + timeHorizonN + " day: " + stdXY * riskPercentile);
    }

}
