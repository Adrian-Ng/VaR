package VaR;

import java.io.*;
import java.util.ArrayList;
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

    private static double loglikelihood(double alpha, int i, double q){
        double part1 = (alpha + 1 - i) / (q * (alpha + 1));
        part1 = Math.pow(part1, alpha + 1 - i);
        double part2 = i / ((1 - q) * (alpha + 1));
        part2 = Math.pow(part2, i);
        double result = 2*Math.log(part1*part2);
        return result;
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
            if(loglikelihood(alpha, i, q)<=quantile) {
                break;
            }
            i++;
        }
        double dist1 = Math.abs(loglikelihood(alpha, i ,q)-quantile);
        double dist2 = Math.abs(loglikelihood(alpha, i-1,q)-quantile);
        if(dist1 > dist2)
            nonRejectionInterval[0] = i;
        else nonRejectionInterval[0] = i-1;

        //CALCULATE UPPER INTERVAL
        while(true) {
            if(loglikelihood(alpha, i, q) >= quantile){
                break;
            }
            i++;
        }
        dist1 = Math.abs(loglikelihood(alpha, i ,q)-quantile);
        dist2 = Math.abs(loglikelihood(alpha, i-1,q)-quantile);
        if(dist1 > dist2)
            nonRejectionInterval[1] = i;
        else nonRejectionInterval[1] = i-1;
        return nonRejectionInterval;
    }

    private static ArrayList<BackTestData> doCoverageTests(double confidenceX, int numMoments, int[] violations, String[] nameMeasures) {
        ArrayList<BackTestData> backTestDatas = new ArrayList<BackTestData>();
        double[] epsilon = {0.05, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        /** STANDARD COVERAGE TEST*/
        for(int e = 0; e < epsilon.length; e++) {
            int[] nonRejectionIntervalStandardCoverage = testCoverage(confidenceX, epsilon[e], numMoments);
            System.out.println("\n\tNon-Rejection Interval from Standard Coverage Test at Significance Level " + epsilon[e] + ":\n\t\t\t" + Arrays.toString(nonRejectionIntervalStandardCoverage));
            for (int i = 0; i < violations.length; i++) {
                BackTestData standardBT = new BackTestData();
                Boolean reject;
                if (violations[i] <= nonRejectionIntervalStandardCoverage[0] || violations[i] >= nonRejectionIntervalStandardCoverage[1]) {
                    System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We REJECT this measure.");
                    reject = true;
                } else {
                    System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We don't reject this measure.");
                    reject = false;
                }
                standardBT.setMeasure(nameMeasures[i]);
                standardBT.setEpsilon(epsilon[e]);
                standardBT.setCoverage("Standard");
                standardBT.setLower(nonRejectionIntervalStandardCoverage[0]);
                standardBT.setUpper(nonRejectionIntervalStandardCoverage[1]);
                standardBT.setViolations(violations[i]);
                standardBT.setReject(reject);
                backTestDatas.add(standardBT);
            }
            /** KUPIEC'S PF COVERAGE TEST*/
            int[] nonRejectionIntervalKupiecPF = testKupiecPF(confidenceX, epsilon[e], numMoments);
            System.out.println("\n\tNon-Rejection Interval from Kupiec's Coverage Test at Significance Level " + epsilon[e] + ":\n\t\t\t" + Arrays.toString(nonRejectionIntervalKupiecPF));
            for (int i = 0; i < violations.length; i++) {
                BackTestData kupiecBT = new BackTestData();
                Boolean reject;
                if (violations[i] <= nonRejectionIntervalKupiecPF[0] || violations[i] >= nonRejectionIntervalKupiecPF[1]) {
                    System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We REJECT this measure.");
                    reject = true;
                } else {
                    System.out.println("\t\t" + nameMeasures[i] + " has " + violations[i] + " violations. We don't reject this measure.");
                    reject = false;
                }
                kupiecBT.setMeasure(nameMeasures[i]);
                kupiecBT.setEpsilon(epsilon[e]);
                kupiecBT.setCoverage("Kupiec");
                kupiecBT.setLower(nonRejectionIntervalStandardCoverage[0]);
                kupiecBT.setUpper(nonRejectionIntervalStandardCoverage[1]);
                kupiecBT.setViolations(violations[i]);
                kupiecBT.setReject(reject);
                backTestDatas.add(kupiecBT);
            }
        }
        return backTestDatas;
    }
    public static ArrayList<BackTestData> main(Parameters p, optionsData[] options) throws IOException {
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
        int numSym = p.getNumSym();
        //get Parameters
        int[] stockDelta = p.getStockDelta();
        int[] optionDelta = p.getOptionsDelta();
        String[] nameMeasures = {"StDev", "EWMA", "GARCH","Historical", "Monte Carlo"};
        int numMeasures = nameMeasures.length;
        int numYears = 5;              //Get Five Years of Data for BackTest
        int numMoments = 1000;          //Number of VaRs to Calculate
        int intervals = 252;            //Number of Working Days in One Year
        double[][] momentsVaR = new double[numMeasures][numMoments];
        // Get Stock Data
        double[][] stockPrices = getStocks.main(p.getSymbol(), numYears);
        /** GET DAILY CHANGES IN ABSOLUTE PORTFOLIO VALUE*/
        double[][] priceChanges = new Stats(stockPrices).getAbsoluteChanges();
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
            double[] AnalyticalVaR = Analytical.main(p, stockSubsetInterval);
            momentsVaR[0][i] = AnalyticalVaR[0];
            momentsVaR[1][i] = AnalyticalVaR[1];
            momentsVaR[2][i] = AnalyticalVaR[2];
            //momentsVaR[3][i] = AnalyticalVaR[2];
            //momentsVaR[4][i] = AnalyticalVaR[2];
            momentsVaR[3][i] = Historic.main( p,stockSubsetInterval, options,0);
            momentsVaR[4][i] = MonteCarlo.main(p,stockSubsetInterval, options,0);
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
                for(int k = 0; k < p.getTimeHorizon(); k++)
                    sum += deltaP[i+k];
                if (-momentsVaR[j][i] >  sum)
                    violations[j]++;
            }
        System.out.println("\n\tViolations:\n\t\t\t" + Arrays.toString(violations));

        ArrayList<BackTestData> ArrayListBT =  doCoverageTests(p.getConfidenceLevel(), numMoments, violations, nameMeasures);
        new Stats(momentsVaR).printMatrixToCSV(nameMeasures,"Backtest - " + numMoments + " moments", p.getOutputPath());
        return ArrayListBT;
    }
}
