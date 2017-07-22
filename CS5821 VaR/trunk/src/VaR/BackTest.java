package VaR;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

/**back test needs to calculate 1000 values of VaR at staggered intervals of data.
 * Each measure will consume 252 days of data. That is one year.
 * Each measure will be displaced by one day.
 *
 */
public class BackTest {
/*
    public static double[][] CalculateVaRMoments(){

    }
    */

    public static void main(String[] symbol, int[] stockDelta, int timeHorizonN, double confidenceX) throws IOException {
        System.out.println("=========================================================================");
        System.out.println("BackTest.java");
        System.out.println("=========================================================================");
        int numSym = symbol.length;
        int numMeasures = 3;            //Linear + Historical +  Monte Carlo= 3
        int numYears = 5;              //Get Five Years of Data for BackTest
        int numMoments = 1000;          //Number of VaRs to Calculate
        int intervals = 252;            //Number of Working Days in One Year

        //https://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
        PrintStream originalStream = System.out;
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
                //NO-OP
            }
        });
        double[][] momentsVaR = new double[numMeasures][numMoments];
        // Get Stock Data
        double[][] stockPrices = getStocks.main(symbol, numYears);
        // How Many Rows of Data Did we get?
        int numTuples = stockPrices[0].length;
        // Get Price Changes
        double[][] priceChanges = new StockParam(stockPrices).getAbsoluteChanges();
        double[] portfolioValue = new double[priceChanges[0].length];
        for (int i = 0; i < priceChanges[0].length; i++) {
            double sum = 0.0;
            for (int j = 0; j < numSym; j++)
                sum += stockDelta[j] * priceChanges[j][i];
            portfolioValue[i] = sum;
            //System.out.println(portfolioValue[i]);
        }


        for (int i = 0; i < numMoments; i++) {
            double[][] stockSubsetInterval = new double[numSym][intervals];
            for (int j = 0; j < numSym; j++)
                for (int k = i; k < intervals + i; k++)
                    stockSubsetInterval[j][k - i] = stockPrices[j][k];
            System.setOut(dummyStream);
            momentsVaR[0][i] = Linear.main(symbol, stockSubsetInterval, stockDelta, timeHorizonN, confidenceX);
            momentsVaR[1][i] = Historic.main(symbol, stockSubsetInterval, stockDelta, timeHorizonN, confidenceX);
            momentsVaR[2][i] = MonteCarlo.main(symbol, stockSubsetInterval, stockDelta, timeHorizonN, confidenceX);
            System.setOut(originalStream);
            //System.out.println("\t\t" + momentsVaR[0][i] + " " + momentsVaR[1][i]);
        }
        System.out.println("\n\t" + momentsVaR[0].length + " values of VaR calculated.");

        int[] violations = {0, 0};
        for (int i = 0; i < numMoments; i++)
            for (int j = 0; j < numMeasures; j++)
                if (-momentsVaR[j][i] > portfolioValue[i])
                    violations[j]++;
                    //System.out.println(-momentsVaR[j][i] + " " + portfolioValue[i]);

        System.out.println(Arrays.toString(violations));
    }
}
