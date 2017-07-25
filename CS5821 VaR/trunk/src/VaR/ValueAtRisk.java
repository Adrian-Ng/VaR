package VaR;

import org.apache.commons.math3.stat.inference.BinomialTest;

import java.io.IOException;

import static org.apache.commons.math3.stat.inference.AlternativeHypothesis.TWO_SIDED;

/**
 * Created by Adrian on 15/07/2017.
 */
public class ValueAtRisk {
/*
    public static void testBinomial(int[] violations, int numMoments, double confidenceX){
        BinomialDistribution b = new BinomialDistribution(numMoments, confidenceX);
        for(int i = 0; i < violations.length; i++)
            System.out.println(b.cumulativeProbability(violations[i]));
    }
*/
    public static void main(String args[])throws IOException {

        System.out.println("=========================================================================");
        System.out.println("ValueAtRisk.java");
        System.out.println("=========================================================================");
        /**
         * 1st Argument: Stock Symbols
         * 2nd Argument: Stock Deltas
         * 3rd Argument: Number of Years of Data
         * 4th Argument: Time Horizon
         * 5th Argument: Confidence Level
         * TODO 6th Argument: Call Symbols
         * TODO 7th Argument: Call Deltas
         * TODO 8th Argument: Put Symbols
         * TODO 9th Argument: Put Deltas
         */
        //SPLIT PIPE DELIMITED LIST OF STOCK SYMBOLS
        String[] symbols = args[0].split("\\|");
        int numSym = symbols.length;
        //SPLIT PIPE DELIMITED LIST OF STOCK DELTA
        String[] strStockDelta = args[1].split("\\|");
        int[] stockDelta = new int[strStockDelta.length];
        for(int i = 0; i< strStockDelta.length;i++)
            stockDelta[i] = Integer.parseInt(strStockDelta[i]);
        // NUMBER OF YEARS IN THE PAST TO LOOK AT
        int intDays = Integer.parseInt(args[2]);
        // TIME HORIZON
        int timeHorizonN = Integer.parseInt(args[3]);
        // CONFIDENCE INTERVAL
        double confidenceX = Double.parseDouble(args[4]);



        // Get Stock Data
        double[][] stockPrices = getStocks.main(symbols, intDays);
        // Get Options Data
         optionsData[] options = getOptions.main(symbols);
        double[][] strikePrices = new double[numSym][];
        double[][] callPrices = new double[numSym][];
        double[][] putPrices = new double[numSym][];
        long[] daystoMaturity = new long[numSym];
        for (int i = 0; i < numSym; i++) {
            strikePrices[i] = options[i].getStrikePrices();
            callPrices[i] = options[i].getCallPrices();
            putPrices[i] = options[i].getPutPrices();
            daystoMaturity[i] = options[i].getDaystoMaturity();
        }
        // Get VaR Measures
        double[] analyticalVaR = Analytical.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double linearVaR = Linear.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double montecarloVaR = MonteCarlo.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double historicVaR = Historic.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        int[] violations = BackTest.main(symbols,stockDelta, timeHorizonN, confidenceX);

    }

}

