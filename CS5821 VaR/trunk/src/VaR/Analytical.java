package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Adrian on 21/06/2017.
 */
public class Analytical {

    public static double covariance(HashMap<String, ArrayList<Double>> mapStocks){
        //Get Keys
        Set setSym = mapStocks.keySet();
        //Convert Set To Array
        String[] strSym = (String[]) setSym.toArray(new String[setSym.size()]);
        //Initialize ArrayList
        ArrayList<Double> stockX = new ArrayList<Double>(mapStocks.get(strSym[0]));
        ArrayList<Double> stockY = new ArrayList<Double>(mapStocks.get(strSym[1]));
        //Get Mean
        double meanX = new StockParam(mapStocks.get(strSym[0])).getMean();
        double meanY = new StockParam(mapStocks.get(strSym[1])).getMean();
        //Calculation
        double sum = 0;
        for (int i = 0; i < stockX.size(); i++)
            sum +=(stockX.get(i) - meanX)*(stockY.get(i) - meanY);
        return sum/stockX.size();
    }

    public static void main(HashMap<String, ArrayList<Double>> mapStocks){
        double  portfolioPi[]     = {10000000,5000000};
        double  confidenceX     = 0.99;
        int     timeHorizonN    = 1;
        NormalDistribution distribution = new NormalDistribution(0,1);
        double riskPercentile = - distribution.inverseCumulativeProbability(1-confidenceX);
        double singleSTDperday[] = new double[mapStocks.size()];
        int i = 0;
        for (String sym : mapStocks.keySet()) {
            ArrayList<Double> alStocks = mapStocks.get(sym);
            StockParam thisStock = new StockParam(alStocks);
            //Get Volatilities
            double dailyEqualWeight = thisStock.getEqualWeightVolatility();
            double yearlyEqualWeight = thisStock.getEqualWeightVolatility() * Math.sqrt(252);
            double dailyEWMA = thisStock.getEWMAVolatility();
            double yearlyEWMA = thisStock.getEWMAVolatility() * Math.sqrt(252);
            //Print Volatilities
            System.out.println("\n\t$" + portfolioPi[i] + " in " + sym);
            System.out.println("\t\tDaily Equal Weighted Volatility is: " + dailyEqualWeight);
            System.out.println("\t\tYearly Equal Weighted Volatility is: " + yearlyEqualWeight);
            System.out.println("\t\tDaily EWMA Volatility is: " + dailyEWMA);
            System.out.println("\t\tYearly EWMA Volatility is: " + yearlyEWMA);
            //Print Single Stock VaR
            singleSTDperday[i] = portfolioPi[i] * dailyEWMA * Math.sqrt(timeHorizonN);
            System.out.println("\n\t\t\tStandard deviation of daily changes: " + singleSTDperday[i]);
            double VaR = singleSTDperday[i] * riskPercentile;
            System.out.println("\n\t\t\tVaR for " + sym + " over " + timeHorizonN + " day: " + VaR);
            i++;
        }

        double stdX = singleSTDperday[0];
        double stdY = singleSTDperday[1];

        double covXY = covariance(mapStocks);

        double rhoXY = covXY/(stdX*stdY);

        double stdXY = Math.sqrt(Math.pow(stdX,2) + Math.pow(stdY,2) + 2*rhoXY*stdX*stdY);

        System.out.println("\nValue at Risk for the whole portfolio over " + timeHorizonN + " day: " + stdXY * riskPercentile);

    }

}
