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
         * 3rd Argument: Options Symbols
         * 4th Argument: Options Deltas
         * 5th Argument: Number of Years of Data
         * 6th Argument: Time Horizon
         * 7th Argument: Confidence Level
         */
        //SPLIT PIPE DELIMITED LIST OF SYMBOLS
        String[] Symbol = args[0].split("\\|");
        int numSym = Symbol.length;
        //SPLIT PIPE DELIMITED LIST OF STOCK DELTA
        String[] strStockDelta = args[1].split("\\|");
        int[] stockDelta = new int[strStockDelta.length];
        for(int i = 0; i< strStockDelta.length;i++)
            stockDelta[i] = Integer.parseInt(strStockDelta[i]);
        //SPLIT PIPE DELIMITED LIST OF OPTIONS DELTA
        String[] strOptionDelta = args[2].split("\\|");
        int[] optionDelta = new int[strOptionDelta.length];
        for(int i = 0; i< strOptionDelta.length;i++)
            optionDelta[i] = Integer.parseInt(strOptionDelta[i]);
        // NUMBER OF YEARS IN THE PAST TO LOOK AT
        int intDays = Integer.parseInt(args[3]);
        // TIME HORIZON
        int timeHorizonN = Integer.parseInt(args[4]);
        // CONFIDENCE INTERVAL
        double confidenceX = Double.parseDouble(args[5]);

        System.out.println("\tTime Horizon: " + timeHorizonN + " day(s)");
        System.out.println("\tConfidence Level: " + confidenceX);

        // Get Stock Data
        double[][] stockPrices = getStocks.main(Symbol, intDays);
        // Get Options Data
        optionsData[] options = getOptions.main(Symbol);
        //calculate yearly volatility for options
        for(int i = 0; i < numSym; i++)
            options[i].setVolatility(new StockParam(stockPrices[i]).getStandardDeviation()* Math.sqrt(252));

        PortfolioInfo.print(Symbol, stockPrices,stockDelta,options,optionDelta);

        // Get VaR Measures
        //[][] analyticalVaR = AnalyticalSingleStock.main(Symbol, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double[] linearVaR = AnalyticalLinear.main(Symbol, stockPrices,stockDelta,options, optionDelta, timeHorizonN, confidenceX);
        double montecarloVaR = MonteCarlo.main(Symbol, stockPrices,stockDelta,options, optionDelta, timeHorizonN, confidenceX);
        double historicVaR = Historic.main(Symbol, stockPrices,stockDelta,options, optionDelta, timeHorizonN, confidenceX);
        int[] violations = BackTest.main(Symbol,stockDelta, options, optionDelta, timeHorizonN, confidenceX);
    }
}

