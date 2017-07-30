package priceOptions;

/**
 * Created by Adrian on 26/07/2017.
 */
import java.util.Arrays;

public class Trees {
    //declare class arrays
    private static double stockPrice[][];
    private static double optionPrice[][];

    public static void stockPrices(double S0, double u, double d, int T) {
        stockPrice[0][0] = S0;
        // compute increase by u on the extreme side only
        for (int i = 1; i < T; i++)
            stockPrice[0][i] = stockPrice[0][i-1] * u;
        // compute all subsequent decrease by d
        for (int j = 1; j < T; j++){
            for(int i = 1; i< T; i++)
                stockPrice[j][i] = stockPrice[j-1][i-1] * d;
        }
    }

    public static double europeanCall (int X, int T,double dt, double r, double p) {
        optionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            optionPrice[i][T-1] = Math.max(stockPrice[i][T-1]-X,0.0);
        // compute subsequent option prices
        for (int i = T-2;i >= 0; i--) {
            for(int j = T-2; j >= 0; j--) {
                optionPrice[j][i] = Math.exp(-r*dt)*((p*optionPrice[j][i+1])+(1-p)*optionPrice[j+1][i+1]);
            }
        }
        return(optionPrice[0][0]);
    }

    public static double europeanPut (int X, int T,double dt, double r, double p) {
        optionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            optionPrice[i][T-1] = Math.max(X-stockPrice[i][T-1],0.0);
        // compute subsequent option prices
        for (int i = T-2;i >= 0; i--) {
            for(int j = T-2; j >= 0; j--) {
                optionPrice[j][i] = Math.exp(-r*dt)*((p*optionPrice[j][i+1])+(1-p)*optionPrice[j+1][i+1]);
            }
        }
        return(optionPrice[0][0]);
    }

    public static double americanCall (int X, int T,double dt, double r, double p) {
        optionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            optionPrice[i][T-1] = Math.max(stockPrice[i][T-1]-X,0.0);
        // compute subsequent option prices
        for (int i = T-2;i >= 0; i--) {
            for (int j = T - 2; j >= 0; j--) {
                optionPrice[j][i] = Math.exp(-r * dt) * ((p * optionPrice[j][i + 1]) + (1 - p) * optionPrice[j + 1][i + 1]);
            }
        }
        return(optionPrice[0][0]);
    }

    public static double americanPut (int X, int T,double dt, double r, double p) {
        optionPrice = new double[T][T];
        //compute option prices at maturity
        for (int i = 0; i < T; i++)
            optionPrice[i][T-1] = Math.max(stockPrice[i][T-1]-X,0.0);
        // compute subsequent option prices
        for (int i = T-2;i >= 0; i--) {
            for(int j = T-2; j >= 0; j--) {
                // consider Early Exercise. What's larger: the calculated option price or the difference between the strike and share price?
                optionPrice[j][i] = Math.max(Math.exp(-r*dt)*((p*optionPrice[j][i+1])+(1-p)*optionPrice[j+1][i+1]),X-stockPrice[j][i]);
            }
        }
        return(optionPrice[0][0]);
    }

    public static void main(String[] args) {

        // initialise input Strike price
        int X = Integer.parseInt(args[0]);
        //initialize ints
        int T = (int) Math.ceil(365.0/2.0);                 // 6 months expressed in days. this is the number of steps.
        // initialize doubles
        double S0 = 80.0;                                   // initial asset price
        double sigma = 0.48;                                // volatility
        double r = 0.07;                                    // interest rate
        double dt = 1.0/365.0;                              // size of the step where each step is 1 day


        double u = Math.exp(sigma*Math.sqrt(dt));           // stock price increase
        double d = Math.exp(-sigma*Math.sqrt(dt));          // stock price decrease
        double p = (Math.exp(r*dt)-d)/(u-d);                // 0.4975
        // allocate memory to stockPrice
        stockPrice = new double[T][T];
        // compute stockPrice
        stockPrices(S0, u, d , T);
        // compute option price for European Call
        double euroCall = europeanCall(X, T, dt, r, p);

        // compute option price for European Put
        double euroPut = europeanPut(X, T, dt, r, p);

        // compute option price for American Call
        double amerCall = americanCall(X, T, dt, r, p);

        // compute option price for American Put
        double amerPut = americanPut(X, T, dt, r, p);

        //trees.printVariables(X, T, S0, sigma, u, d, p, r, dt);

        //print results
        System.out.println("European Call where X = " + X + ": \n\t" + euroCall);
        System.out.println("European Put where X = " + X + ": \n\t" + euroPut);
        System.out.println("American Call where X = " + X + ": \n\t" + amerCall);
        System.out.println("American Put where X = " + X + ": \n\t" + amerPut);
    }
}
