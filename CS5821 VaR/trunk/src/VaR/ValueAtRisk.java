package VaR;

import java.io.IOException;

/**
 * Created by Adrian on 15/07/2017.
 */
public class ValueAtRisk {

    public static void main(String args[])throws IOException {

        System.out.println("=========================================================================");
        System.out.println("ValueAtRisk.java");
        System.out.println("=========================================================================");
        /**
         * 1st Argument: Stock Symbols
         * 2nd Argument: Stock Deltas
         * 3rd Argument: Number of Days of Data
         * 4th Argument: Time Horizon
         * 5th Argument: Confidence Level
         */
        //SPLIT PIPE DELIMITED LIST OF STOCK SYMBOLS
        String[] symbols = args[0].split("\\|");
        int numSym = symbols.length;
        //SPLIT PIPE DELIMITED LIST OF STOCK DELTA
        String[] strStockDelta = args[1].split("\\|");
        int[] stockDelta = new int[strStockDelta.length];
        for(int i = 0; i< strStockDelta.length;i++)
            stockDelta[i] = Integer.parseInt(strStockDelta[i]);
        // NUMBER OF DAYS IN THE PAST TO LOOK AT
        int intDays = Integer.parseInt(args[2]);
        // TIME HORIZON
        int timeHorizonN = Integer.parseInt(args[3]);
        // CONFIDENCE INTERVAL
        double confidenceX = Double.parseDouble(args[4]);

        //double[][] stockPrices = getStocks.main(symbols, intDays);
        getOptions.main(symbols);
/*
        double[] analyticalVaR = Analytical.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double linearVaR = Linear.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double montecarloVaR = MonteCarlo.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double historicVaR = Historic.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        //BackTest.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
*/
    }

}

