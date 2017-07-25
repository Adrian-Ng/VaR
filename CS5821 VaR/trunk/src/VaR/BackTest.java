package VaR;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import org.apache.commons.math3.stat.inference.*;
import org.apache.commons.math3.distribution.*;
import static org.apache.commons.math3.stat.inference.AlternativeHypothesis.TWO_SIDED;

/**back test needs to calculate 1000 values of VaR at staggered intervals of data.
 * Each measure will consume 252 days of data. That is one year.
 * Each measure will be displaced by one day.
 *
 */
public class BackTest {

    public static void calculatePvalueFromViolations(int[] violations){

    }

    public static int[] testCoverage(int[] violations, double confidenceX, int numMoments){
        //https://www.value-at-risk.net/backtesting-coverage-tests/
        int alpha = numMoments + 1;
        double epsilon = 1 - confidenceX;
        double x1, x2;                 //We reject the VaR measure if the number of violations is not in this interval
        int[] nonRejectionInterval = new int[2];
        //We don't yet know what this interval is. So we initialize some starting points.
        nonRejectionInterval[0] = (int) (numMoments * (1-confidenceX)*0.25);
        nonRejectionInterval[1] = (int) (numMoments * (1-confidenceX)*2.0);
        BinomialDistribution distribution = new BinomialDistribution(alpha, epsilon);
        /*BinomialDistribution distribution = new BinomialDistribution(500, 0.05);
        x1 = 12;
        x2 = 38;*/
        //We must set the interval to whatever maximises Pr(X {not in} [x1, x2])
        int a = nonRejectionInterval[0];
        int b = nonRejectionInterval[1];

        double[] incrementCDF = new double[b-a];
        double[] decrementCDF = new double[b-a];
        for(int i = 0; i < incrementCDF.length; i++) {
            incrementCDF[i] = distribution.cumulativeProbability(a + i);
            decrementCDF[i] = 1 - distribution.cumulativeProbability(b - i);        }

        double maximize = 0.0;
        for(int i = 0; incrementCDF[i] <= epsilon/2; i++)
            for(int j = 0; decrementCDF[j] <= epsilon/2; j++){
                double k = incrementCDF[i] + decrementCDF[j];
                if(k<=epsilon){
                    maximize = Math.max(k,maximize);
                    nonRejectionInterval[0] = a + i;
                    nonRejectionInterval[1] = b - j + 1;
                }
            }
        return nonRejectionInterval;
    }

    public static void testKupiecPF(int[] violations, double confidenceX, int numMoments){
        int[] nonRejectionInterval = new int[2];
        nonRejectionInterval[0] = (int) (numMoments * (1-confidenceX)*0.25);
        nonRejectionInterval[1] = (int) (numMoments * (1-confidenceX)*2.0);
        int alpha = numMoments + 1;
        double q = confidenceX;
        double epsilon = 1 - confidenceX;
        ChiSquaredDistribution distribution = new ChiSquaredDistribution(1, 0);
        double quantile = distribution.inverseCumulativeProbability(confidenceX);
        System.out.println(quantile);
        int a = nonRejectionInterval[0];
        int b = nonRejectionInterval[1];




        System.out.println(nonRejectionInterval[0]);


        //return nonRejectionInterval;
    }

    public static double[] testBinomial(int[] violations, int numMoments, double confidenceX){
        double[] pvalue = new double[violations.length];
        for (int i = 0; i < violations.length; i++)
             pvalue[i] = new BinomialTest().binomialTest(numMoments,numMoments-violations[i],confidenceX, TWO_SIDED);
        return pvalue;
    }

    public static boolean[] booleanRejectNull(int[] violations, int numMoments, double confidenceX){
        boolean[] rejectNullHypothesis = new boolean[violations.length];
        for (int i = 0; i < violations.length; i++)
            rejectNullHypothesis[i] = new BinomialTest().binomialTest(numMoments,numMoments-violations[i],confidenceX, TWO_SIDED, 1-confidenceX);
        return rejectNullHypothesis;
    }

    public static int[] main(String[] symbol, int[] stockDelta, int timeHorizonN, double confidenceX) throws IOException {
        System.out.println("=========================================================================");
        System.out.println("BackTest.java");
        System.out.println("=========================================================================");
        //https://stackoverflow.com/questions/8363493/hiding-system-out-print-calls-of-a-class
        PrintStream originalStream = System.out;
        PrintStream dummyStream = new PrintStream(new OutputStream() {
            public void write(int b) {
                //NO-OP
            }
        });
        int numSym = symbol.length;

        String[] nameMeasures = {"Linear","Historic", "Monte Carlo"};
        int numMeasures = nameMeasures.length;            //Linear + Historic +  Monte Carlo= 3
        int numYears = 5;              //Get Five Years of Data for BackTest
        int numMoments = 1000;          //Number of VaRs to Calculate
        int intervals = 252;            //Number of Working Days in One Year
        double[][] momentsVaR = new double[numMeasures][numMoments];
        // Get Stock Data
        double[][] stockPrices = getStocks.main(symbol, numYears);
        /**
         * GET DAILY CHANGES IN ABSOLUTE PORTFOLIO VALUE
         */
        double[][] priceChanges = new StockParam(stockPrices).getAbsoluteChanges();
        double[] deltaP = new double[priceChanges[0].length];
        for (int i = 0; i < priceChanges[0].length; i++) {
            double sum = 0.0;
            for (int j = 0; j < numSym; j++)
                sum += stockDelta[j] * priceChanges[j][i];
            deltaP[i] = sum;
        }
        /**
         * RETURN VaR FOR EACH MOMENT
         */
        /*for (int i = 0; i < numMoments; i++) {
            double[][] stockSubsetInterval = new double[numSym][intervals];
            for (int j = 0; j < numSym; j++)
                for (int k = i; k < intervals + i; k++)
                    stockSubsetInterval[j][k - i] = stockPrices[j][k];
            System.setOut(dummyStream);
            momentsVaR[0][i] = Linear.main(symbol, stockSubsetInterval, stockDelta, timeHorizonN, confidenceX);
            momentsVaR[1][i] = Historic.main(symbol, stockSubsetInterval, stockDelta, timeHorizonN, confidenceX);
            momentsVaR[2][i] = MonteCarlo.main(symbol, stockSubsetInterval, stockDelta, timeHorizonN, confidenceX);
            System.setOut(originalStream);
        }
        System.out.println("\n\t" + momentsVaR[0].length + " values of VaR calculated.");*/
        /**
         * COUNT NUMBER OF DAYS WHERE LOSSES VIOLATE VaR
         */
/*
        int[] violations = {0, 0, 0};
        for (int i = 0; i < numMoments; i++)
            for (int j = 0; j < numMeasures; j++)
                if (-momentsVaR[j][i] > deltaP[i])
                    violations[j]++;*/
        int[] violations = {41, 75, 43};
        System.out.println("\n\t\tViolations:\n\t\t\t" + Arrays.toString(violations));
        /**
         * STANDARD COVERAGE TEST
         */
        int[] nonRejectionInterval = testCoverage(violations,confidenceX,numMoments);
        System.out.println("\n\t\tNon-Rejection Interval from Standard Coverage Test:\n\t\t\t" + Arrays.toString(nonRejectionInterval));
        for(int i = 0; i < violations.length; i++)
            if(violations[i]<= nonRejectionInterval[0]|| violations[i]>= nonRejectionInterval[1])
                System.out.println("\t\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We REJECT this measure.");
            else
                System.out.println("\t\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We don't reject this measure.");

        /**
         * KUPIEC'S PF COVERAGE TEST
         */
        testKupiecPF(violations,confidenceX,numMoments);

        /*** BINOMIAL TEST*/
        System.out.println("\n\t\tBinomial Test");
        double[] pvalue = testBinomial(violations,numMoments,confidenceX);
        boolean[] rejectNull = booleanRejectNull(violations,numMoments,confidenceX);
        System.out.println("\t\t\tP-Values:\t" + Arrays.toString(pvalue));
        System.out.println("\t\t\tReject:\t" + Arrays.toString(rejectNull));
        return violations;
    }
}
