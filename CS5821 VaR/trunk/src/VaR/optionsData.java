package VaR;

import java.util.Arrays;

/**
 * Created by Adrian on 22/06/2017.
 */
public class optionsData {

    //INSTANCE VARIABLES
    private double[] callPrices;
    private double[] putPrices;
    private double[] strikePrices;
    private long daystoMaturity;
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
    public long getDaystoMaturity(){
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
    public void setDaystoMaturity(long daystoMaturity){ this.daystoMaturity = daystoMaturity; }
    public void setVolatility(double volatility) {this.volatility = volatility; }
    //TREES

    private static double gridOptionPrice[][];
    private final double dt = 1.0/365.0; // size of the step where each step is 1 day
    private final double r = 0.07;// interest rate

    private double[][] simulateStockPrices(double stockPrice, double u, double d, int T) {
        double gridStockPrice[][] = new double[T][T];
        gridStockPrice[0][0] = stockPrice;
        // compute increase by u on the extreme side only
        for (int i = 1; i < T; i++)
            gridStockPrice[0][i] = gridStockPrice[0][i-1] * u;
        // compute all subsequent decrease by d
        for (int j = 1; j < T; j++) {
            for (int i = 1; i < T; i++)
                gridStockPrice[j][i] = gridStockPrice[j - 1][i - 1] * d;
            System.out.println(Arrays.toString(gridStockPrice[j]));
        }
        return gridStockPrice;
    }

    public double getEuropeanCall (double stockPrice) {
        double X = this.strikePrices[strikePrices.length-1];
        int T = (int) this.daystoMaturity;
        double u = Math.exp(volatility*Math.sqrt(dt));           // stock price increase
        double d = Math.exp(-volatility*Math.sqrt(dt));          // stock price decrease
        double p = (Math.exp(r*dt)-d)/(u-d);                // 0.4975
        double[][] gridStockPrice = simulateStockPrices(stockPrice,u,d,T);
        gridOptionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            gridOptionPrice[i][T-1] = Math.max(gridStockPrice[i][T-1]-X,0.0);
        // compute subsequent option prices
        for (int i = T-2;i >= 0; i--)
            for(int j = T-2; j >= 0; j--)
                gridOptionPrice[j][i] = Math.exp(-r*dt)*((p* gridOptionPrice[j][i+1])+(1-p)* gridOptionPrice[j+1][i+1]);
        return(gridOptionPrice[0][0]);
    }

    public double getEuropeanPut (double stockPrice) {
        double X = this.strikePrices[strikePrices.length-1];
        int T = (int) this.daystoMaturity;
        double u = Math.exp(volatility*Math.sqrt(dt));           // stock price increase
        double d = Math.exp(-volatility*Math.sqrt(dt));          // stock price decrease
        double p = (Math.exp(r*dt)-d)/(u-d);                // 0.4975
        double[][] gridStockPrice = simulateStockPrices(stockPrice,u,d,T);
        gridOptionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            gridOptionPrice[i][T-1] = Math.max(X-gridStockPrice[i][T-1],0.0);
        // compute subsequent option prices
        for (int i = T-2;i >= 0; i--)
            for(int j = T-2; j >= 0; j--)
                gridOptionPrice[j][i] = Math.exp(-r*dt)*((p*gridOptionPrice[j][i+1])+(1-p)*gridOptionPrice[j+1][i+1]);
        return(gridOptionPrice[0][0]);
    }

    public double getAmericanCall (double stockPrice) {
        double X = this.strikePrices[strikePrices.length-1];
        int T = (int) this.daystoMaturity;
        double u = Math.exp(volatility*Math.sqrt(dt));           // stock price increase
        double d = Math.exp(-volatility*Math.sqrt(dt));          // stock price decrease
        double p = (Math.exp(r*dt)-d)/(u-d);                // 0.4975
        double[][] gridStockPrice = simulateStockPrices(stockPrice,u,d,T);
        gridOptionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            gridOptionPrice[i][T-1] = Math.max(gridStockPrice[i][T-1]-X,0.0);
        // compute subsequent option prices
        for (int i = T-2;i >= 0; i--)
            for (int j = T - 2; j >= 0; j--)
                gridOptionPrice[j][i] = Math.exp(-r * dt) * ((p * gridOptionPrice[j][i + 1]) + (1 - p) * gridOptionPrice[j + 1][i + 1]);
        return(gridOptionPrice[0][0]);
    }

    public double getAmericanPut (double stockPrice) {
        double X = this.strikePrices[strikePrices.length-1];
        int T = (int) this.daystoMaturity;
        double u = Math.exp(volatility * Math.sqrt(dt));           // stock price increase
        double d = Math.exp(-volatility * Math.sqrt(dt));          // stock price decrease
        double p = (Math.exp(r * dt) - d) / (u - d);                // 0.4975
        double[][] gridStockPrice = simulateStockPrices(stockPrice,u, d,T);
        gridOptionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            gridOptionPrice[i][T - 1] = Math.max(gridStockPrice[i][T - 1] - X, 0.0);
        // compute subsequent option prices
        for (int i = T - 2; i >= 0; i--)
            for (int j = T - 2; j >= 0; j--)
                // consider Early Exercise. What's larger: the calculated option price or the difference between the strike and share price?
                gridOptionPrice[j][i] = Math.max(Math.exp(-r * dt) * ((p * gridOptionPrice[j][i + 1]) + (1 - p) * gridOptionPrice[j + 1][i + 1]), X - gridStockPrice[j][i]);
        return (gridOptionPrice[0][0]);
    }

    private  double CNDF(double x)
    {
        int neg = (x < 0d) ? 1 : 0;
        if ( neg == 1)
            x *= -1d;
        double k = (1d / ( 1d + 0.2316419 * x));
        double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
                k - 0.356563782) * k + 0.319381530) * k;
        y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;
        return (1d - neg) * y + neg * (1d - y);
    }

    public double getBlackScholesPut(double stockPrice){
        double X = this.strikePrices[strikePrices.length-1];
        double T = this.daystoMaturity/365;
        double S0 = stockPrice;
        double sigma = volatility;
        //calculate d1
        double d1 = (Math.log(S0/X)+ (r +(Math.pow(sigma,2)/2))*T)/(sigma*Math.sqrt(T));
        //calculate d2
        double d2 = d1 - (sigma*Math.sqrt(T));

        //calculate call
        //double call = (S0*CNDF(d1))-(X*Math.exp(-r*T)*CNDF(d2));
        //calculate put
        double put = X*Math.exp(-r*T)*CNDF(-d2)-S0*CNDF(-d1);
        return put;
    }
}