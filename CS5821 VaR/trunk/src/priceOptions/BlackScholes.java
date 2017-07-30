package priceOptions;

/**
 * Created by Adrian on 26/07/2017.
 */
public class BlackScholes {
    public static double CNDF(double x)
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

    public static void main(String[] args) {
        // initialise input Strike price
        int X = Integer.parseInt(args[0]);
        //initialize ints
        double T = 0.5;                                     // half a year
        // initialize doubles
        double S0 = 80.0;                                   // initial asset price
        double sigma = 0.48;                                // volatility
        double r = 0.07;                                    // interest rate

        //calculate d1
        double d1 = (Math.log(S0/X)+ (r +(Math.pow(sigma,2)/2))*T)/(sigma*Math.sqrt(T));
        //calculate d2
        double d2 = d1 - (sigma*Math.sqrt(T));

        //calculate call
        double call = (S0*CNDF(d1))-(X*Math.exp(-r*T)*CNDF(d2));
        //calculate put
        double put = X*Math.exp(-r*T)*CNDF(-d2)-S0*CNDF(-d1);

        System.out.println("\nEuropean Call Option with X = " + X + ": " + call);
        System.out.println("European Put Option with X = " + X + ": " + put);

    }
}
