package VaR;

import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Created by Adrian on 22/06/2017.
 */
public class optionsData {
    //INSTANCE VARIABLES
    private double[] callPrices;
    private double[] putPrices;
    private double[] strikePrices;
    private int daystoMaturity;
    private double volatility;
    //GETTERS
    public double[] getCallPrices(){
        return this.callPrices;
    }
    public double[] getPutPrices(){
        return this.putPrices;
    }
    public double[] getStrikePrices(){
        return this.strikePrices;
    }
    public int getDaystoMaturity(){
        return this.daystoMaturity;
    }
    public double getVolatility() { return this.volatility; }
    //SETTERS
    public void setCallPrices(double[] callPrices){
        this.callPrices = callPrices;
    }
    public void setPutPrices(double[] putPrices){
        this.putPrices = putPrices;
    }
    public void setStrikePrices(double[] strikePrices){
        this.strikePrices = strikePrices;
    }
    public void setDaystoMaturity(int daystoMaturity){ this.daystoMaturity = daystoMaturity; }
    public void setVolatility(double volatility) {this.volatility = volatility; }

    private double getBlackScholesOptionPrices(double stockPrice, int flag){
        NormalDistribution distribution = new NormalDistribution(0,1);
        double X = this.strikePrices[strikePrices.length-1];
        double T = (this.daystoMaturity-1)/252;
        double S0 = stockPrice;
        double r = 0.07;// interest rate
        double sigma = volatility;
        //calculate d1
        double d1 = (Math.log(S0/X)+ (r +(Math.pow(sigma,2)/2))*T)/(sigma*Math.sqrt(T));
        //calculate d2
        double d2 = d1 - (sigma*Math.sqrt(T));
        if (flag == 0) {
            //calculate call
            double call = (S0 * distribution.cumulativeProbability(d1)) - (X * Math.exp(-r * T) * distribution.cumulativeProbability(d2));
            return call;
        }
        else {
            //calculate put
            double put = X * Math.exp(-r * T) * distribution.cumulativeProbability(-d2) - S0 * distribution.cumulativeProbability(-d1);
            return put;
        }
    }
    public double getBlackScholesPut(double stockPrice){
        return getBlackScholesOptionPrices(stockPrice, 1);
    }
    public double getBlackScholesCall(double stockPrice){
        return getBlackScholesOptionPrices(stockPrice, 0);
    }
    //CONSTUCTORS
    public optionsData(){}
    public optionsData(optionsData options){
        setCallPrices(options.getCallPrices());
        setPutPrices(options.getPutPrices());
        setStrikePrices(options.getStrikePrices());
        setDaystoMaturity(options.getDaystoMaturity());
        setVolatility(options.getVolatility());
    }
}