package VaR;

import java.io.*;
import java.util.Arrays;
import org.apache.commons.math3.distribution.*;


/**back test needs to calculate 1000 values of VaR at staggered intervals of data.
 * Each measure will consume 252 days of data. That is one year.
 * Each measure will be displaced by one day.
 *
 */
public class BackTest {

    private static int[] testCoverage(double confidenceX, double epsilon, int numMoments){
        //https://www.value-at-risk.net/backtesting-coverage-tests/
        int alpha = numMoments + 1;
        //We reject the VaR measure if the number of violations is not in this interval
        BinomialDistribution distribution = new BinomialDistribution(alpha, 1-confidenceX);
        //maximise a such that Pr(X < a) ≤ ε/2
        double pr = 0.0;
        int a = 0;
        while(pr <= epsilon/2){
            pr = distribution.cumulativeProbability(a);
            a++;
        }
        //minimize b such that Pr(b < X) ≤ ε/2
        pr = 1.0;
        int b = 0;
        while(pr >= epsilon/2){
            pr = 1- distribution.cumulativeProbability(b);
            b++;
        }
        //maximise Pr(X ∉ [a + n, b]) ≤ ε
        double pr1 = 0.0;
        int n1 = 0;
        while(pr1 <= epsilon/2){
            pr1 = distribution.cumulativeProbability(a + n1) + (1- distribution.cumulativeProbability(b));
            n1++;
        }
        //maximise Pr(X ∉ [a + n, b]) ≤ ε
        double pr2 = 0.0;
        int n2 = 0;
        while(pr2 <= epsilon/2){
            pr2 = distribution.cumulativeProbability(a) + (1- distribution.cumulativeProbability(b-n2));
            n2++;
        }
        int[] nonRejectionInterval = new int[2];
        if(pr1 > pr2) {
            nonRejectionInterval[0] = a + n1;
            nonRejectionInterval[1] = b;
        }
        else {
            nonRejectionInterval[0] = a;
            nonRejectionInterval[1] = b-n2;
        }

        return nonRejectionInterval;
    }
    private static int[] testKupiecPF(double confidenceX, double epsilon, int numMoments){
        int[] nonRejectionInterval = new int[2];
        int alpha = numMoments + 1;
        double q = confidenceX;
        ChiSquaredDistribution distribution = new ChiSquaredDistribution(1, 0);
        double quantile = distribution.inverseCumulativeProbability(1-epsilon);
        int i = 0;
        //CALCULATE LOWER INTERVAL
        while(true) {
            double part1 = (alpha + 1 - i) / (q * (alpha + 1));
            part1 = Math.pow(part1, alpha + 1 - i);
            double part2 = i / ((1 - q) * (alpha + 1));
            part2 = Math.pow(part2, i);
            double answer = 2*Math.log(part1*part2);
            if(answer<=quantile) {
                nonRejectionInterval[0] = i;
                break;
            }
            i++;
        }
        //CALCULATE UPPER INTERVAL
        while(true) {
            double part1 = (alpha + 1 - i) / (q * (alpha + 1));
            part1 = Math.pow(part1, alpha + 1 - i);
            double part2 = i / ((1 - q) * (alpha + 1));
            part2 = Math.pow(part2, i);
            double answer = 2*Math.log(part1*part2);
            if(answer >= quantile){
                nonRejectionInterval[1] = i;
                break;
            }
            i++;
        }
        return nonRejectionInterval;
    }
    private static double epsilon = 0.05; //significance level
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
        String[] nameMeasures = {"Analytical StDev", "Analytical EWMA", "Analytical GARCH(1,1)","Historic", "Monte Carlo"};
        int numMeasures = nameMeasures.length;
        int numYears = 5;              //Get Five Years of Data for BackTest
        int numMoments = 1000;          //Number of VaRs to Calculate
        int intervals = 252;            //Number of Working Days in One Year
        double[][] momentsVaR = new double[numMeasures][numMoments];
        // Get Stock Data
        double[][] stockPrices = getStocks.main(symbol, numYears);
        /** GET DAILY CHANGES IN ABSOLUTE PORTFOLIO VALUE*/
        double[][] priceChanges = new methods(stockPrices).getAbsoluteChanges();
        double[] deltaP = new double[priceChanges[0].length];
        for (int i = 0; i < priceChanges[0].length; i++) {
            double sum = 0.0;
            for (int j = 0; j < numSym; j++)
                sum += stockDelta[j] * priceChanges[j][i];
            deltaP[i] = sum;
        }
        /** SET optionDelta TO ZERO*/
        Arrays.fill(optionDelta,0);
        /** RETURN VaR FOR EACH MOMENT*/
        System.out.print("\nReturning VaR for each moment. This will take a while...");
        for (int i = 0; i < numMoments; i++) {
            double[][] stockSubsetInterval = new double[numSym][intervals];
            for (int j = 0; j < numSym; j++)
                for (int k = i; k < intervals + i; k++)
                    stockSubsetInterval[j][k - i] = stockPrices[j][k];
            System.setOut(dummyStream);
            double[] AnalyticalVaR = AnalyticalLinear.main(symbol, stockSubsetInterval,stockDelta,timeHorizonN, confidenceX);
            momentsVaR[0][i] = AnalyticalVaR[0];
            momentsVaR[1][i] = AnalyticalVaR[1];
            momentsVaR[2][i] = AnalyticalVaR[2];
            momentsVaR[3][i] = Historic.main(symbol, stockSubsetInterval, stockDelta, options,  optionDelta, timeHorizonN, confidenceX,0);
            momentsVaR[4][i] = MonteCarlo.main(symbol, stockSubsetInterval, stockDelta, options,  optionDelta, timeHorizonN, confidenceX,0);
            System.setOut(originalStream);
        }
        System.out.println("\n\t" + momentsVaR[0].length + " moments of VaR calculated.");
        /** COUNT NUMBER OF DAYS WHERE LOSSES VIOLATE VaR*/
        int[] violations = new int[numMeasures];
        Arrays.fill(violations,0);
        //i and j loops through vectors numMoments and numMeasures respectively
        for (int i = 0; i < numMoments; i++)
            for (int j = 0; j < numMeasures; j++) {
                double sum = 0.0;
                for(int k = 0; k < timeHorizonN; k++)
                    sum += deltaP[i+k];
                if (-momentsVaR[j][i] >  sum)
                    violations[j]++;
            }
        System.out.println("\n\tViolations:\n\t\t\t" + Arrays.toString(violations));
        /** STANDARD COVERAGE TEST*/
        int[] nonRejectionIntervalStandardCoverage = testCoverage(confidenceX, epsilon,numMoments);
        System.out.println("\n\tNon-Rejection Interval from Standard Coverage Test:\n\t\t\t" + Arrays.toString(nonRejectionIntervalStandardCoverage));
        for(int i = 0; i < violations.length; i++)
            if(violations[i]<= nonRejectionIntervalStandardCoverage[0]|| violations[i]>= nonRejectionIntervalStandardCoverage[1])
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We REJECT this measure.");
            else
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We don't reject this measure.");
        /** KUPIEC'S PF COVERAGE TEST*/
        int[] nonRejectionIntervalKupiecPF = testKupiecPF(confidenceX, epsilon,numMoments);
        System.out.println("\n\tNon-Rejection Interval from Kupiec's Coverage Test:\n\t\t\t" + Arrays.toString(nonRejectionIntervalKupiecPF));
        for(int i = 0; i < violations.length; i++)
            if(violations[i]<= nonRejectionIntervalKupiecPF[0]|| violations[i]>= nonRejectionIntervalKupiecPF[1])
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We REJECT this measure.");
            else
                System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We don't reject this measure.");

        new methods(momentsVaR).printMatrixToCSV(nameMeasures,"Backtest - " + numMoments + " moments");
        return violations;
    }
}
