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

    public static int[] testCoverage(double confidenceX, int numMoments){
        //https://www.value-at-risk.net/backtesting-coverage-tests/
        int alpha = numMoments + 1;
        double epsilon = 1 - confidenceX;
        int[] nonRejectionInterval = new int[2];
        //We reject the VaR measure if the number of violations is not in this interval
        //We don't yet know what this interval is. So we initialize some starting points.
        nonRejectionInterval[0] = (int) (numMoments * epsilon*0.25);
        nonRejectionInterval[1] = (int) (numMoments * epsilon*2.0);
        BinomialDistribution distribution = new BinomialDistribution(alpha, epsilon);
        int a = nonRejectionInterval[0];
        int b = nonRejectionInterval[1];

        double[] incrementCDF = new double[b-a];
        double[] decrementCDF = new double[b-a];
        for(int i = 0; i < incrementCDF.length; i++) {
            incrementCDF[i] = distribution.cumulativeProbability(a + i);
            decrementCDF[i] = 1 - distribution.cumulativeProbability(b - i);
        }
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

    public static int[] testKupiecPF(double confidenceX, int numMoments){
        int[] nonRejectionInterval = new int[2];
        nonRejectionInterval[0] = (int) (numMoments * (1-confidenceX)*0.5);
        nonRejectionInterval[1] = (int) (numMoments * (1-confidenceX)*1.5);
        int alpha = numMoments + 1;
        double q = confidenceX;
        ChiSquaredDistribution distribution = new ChiSquaredDistribution(1, 0);
        double quantile = distribution.inverseCumulativeProbability(confidenceX);
        int a = nonRejectionInterval[0];
        int b = nonRejectionInterval[1];
        /**CALCULATE LOWER INTERVAL*/
        double minimize = Double.POSITIVE_INFINITY;
        for(int i = 0;i < a;i++){
            double part1 = Math.pow((alpha-(i+a))/(q*alpha),alpha-(i+a));
            double part2 = Math.pow((i+a)/((1-q)*alpha),(i+a));
            double diff  = Math.abs(quantile - 2*Math.log(part1*part2));
            if(diff<minimize) {
                minimize = Math.min(diff,minimize);
                nonRejectionInterval[0] = i + a;
            }
        }
        /**CALCULATE UPPER INTERVAL*/
        minimize = Double.POSITIVE_INFINITY;
        for(int i = 0;i < a;i++){
            double part1 = Math.pow((alpha-(b-i))/(q*alpha),alpha-(b-i));
            double part2 = Math.pow((b-i)/((1-q)*alpha),(b-i));
            double diff  = Math.abs(quantile - 2*Math.log(part1*part2));
            if(diff<minimize) {
                minimize = Math.min(diff,minimize);
                nonRejectionInterval[1] = b - i + 1;//ADD 1 TO ROUND UP
            }
        }
        //System.out.println(Arrays.toString(nonRejectionInterval));
        return nonRejectionInterval;
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

    public static int[] main(String[] symbol, int[] stockDelta, optionsData[] options, int[] optionDelta, int timeHorizonN, double confidenceX) throws IOException {
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
        for (int i = 0; i < numMoments; i++) {
            double[][] stockSubsetInterval = new double[numSym][intervals];
            for (int j = 0; j < numSym; j++)
                for (int k = i; k < intervals + i; k++)
                    stockSubsetInterval[j][k - i] = stockPrices[j][k];
            System.setOut(dummyStream);
            momentsVaR[0][i] = Linear.main(symbol, stockSubsetInterval, stockDelta, timeHorizonN, confidenceX);
            momentsVaR[1][i] = Historic.main(symbol, stockSubsetInterval, stockDelta, options,  optionDelta, timeHorizonN, confidenceX);
            momentsVaR[2][i] = MonteCarlo.main(symbol, stockSubsetInterval, stockDelta, optionDelta, timeHorizonN, confidenceX);
            System.setOut(originalStream);
        }
        System.out.println("\n\t" + momentsVaR[0].length + " values of VaR calculated.");
        /**
         * COUNT NUMBER OF DAYS WHERE LOSSES VIOLATE VaR
         */
        int[] violations = {0, 0, 0};
        //i and j loops through vectors numMoments and numMeasures respectively
        for (int i = 0; i < numMoments; i++)
            for (int j = 0; j < numMeasures; j++) {
                double sum = 0.0;
                for(int k = 0; k < timeHorizonN; k++)
                    sum += deltaP[i+k];
                if (-momentsVaR[j][i] >  sum)
                    violations[j]++;
            }
        //int[] violations = {41, 55, 43};
        System.out.println("\n\tViolations:\n\t\t\t" + Arrays.toString(violations));
        /**
         * STANDARD COVERAGE TEST
         */
        int[] nonRejectionIntervalStandardCoverage = testCoverage(confidenceX,numMoments);
        System.out.println("\n\tNon-Rejection Interval from Standard Coverage Test:\n\t\t\t" + Arrays.toString(nonRejectionIntervalStandardCoverage));
        for(int i = 0; i < violations.length; i++)
            if(violations[i]<= nonRejectionIntervalStandardCoverage[0]|| violations[i]>= nonRejectionIntervalStandardCoverage[1])
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We REJECT this measure.");
            else
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We don't reject this measure.");

        /**
         * KUPIEC'S PF COVERAGE TEST
         */
        int[] nonRejectionIntervalKupiecPF = testKupiecPF(confidenceX,numMoments);
        System.out.println("\n\tNon-Rejection Interval from Kupiec's Coverage Test:\n\t\t\t" + Arrays.toString(nonRejectionIntervalKupiecPF));
        for(int i = 0; i < violations.length; i++)
            if(violations[i]<= nonRejectionIntervalKupiecPF[0]|| violations[i]>= nonRejectionIntervalKupiecPF[1])
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We REJECT this measure.");
            else
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We don't reject this measure.");

        /*** BINOMIAL TEST*/
        System.out.println("\n\tBinomial Test");
        double[] pvalue = testBinomial(violations,numMoments,confidenceX);
        boolean[] rejectNull = booleanRejectNull(violations,numMoments,confidenceX);
        System.out.println("\t\tP-Values:\t" + Arrays.toString(pvalue));
        System.out.println("\t\tReject:\t" + Arrays.toString(rejectNull));
        return violations;
    }
}
