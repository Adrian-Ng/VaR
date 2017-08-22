package VaR;

import org.apache.commons.math3.distribution.BinomialDistribution;

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
         * 3rd Argument: Options Deltas
         * 4th Argument: Number of Years of Data
         * 5th Argument: Time Horizon
         * 6th Argument: Confidence Level
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
            options[i].setVolatility(new methods(stockPrices[i]).getStandardDeviation()* Math.sqrt(252));
        PortfolioInfo.print(Symbol, stockPrices,stockDelta,options,optionDelta);
/*
        //binomialtest
        int alpha = 501;
        BinomialDistribution distribution = new BinomialDistribution(alpha, 1-confidenceX);
        for(int i = 0; i < 100; i++)
            System.out.println(i + " " + (1 - distribution.cumulativeProbability(i)));

        int alpha = 501;
        double q = 0.95;
        int i = 0;
        double quantile = 3.841;
        while(true) {
            double part1 = (alpha + 1 - i) / (q * (alpha + 1));
            part1 = Math.pow(part1, alpha + 1 - i);
            double part2 = i / ((1 - q) * (alpha + 1));
            part2 = Math.pow(part2, i);
            System.out.println(2*Math.log(part1*part2));
            double answer = 2*Math.log(part1*part2);
            if(answer<=quantile)
                break;
            i++;
        }
        while(true) {
            double part1 = (alpha + 1 - i) / (q * (alpha + 1));
            part1 = Math.pow(part1, alpha + 1 - i);
            double part2 = i / ((1 - q) * (alpha + 1));
            part2 = Math.pow(part2, i);
            System.out.println(2*Math.log(part1*part2));
            double answer = 2*Math.log(part1*part2);
            if(answer >= quantile)
                break;
            i++;
        }*/
        // Get VaR Measures
        //AnalyticalSingleStock.main(Symbol, stockPrices,stockDelta, timeHorizonN, confidenceX);
        //AnalyticalLinear.main(Symbol, stockPrices,stockDelta, timeHorizonN, confidenceX);
        //MonteCarlo.main(Symbol, stockPrices,stockDelta,options, optionDelta, timeHorizonN, confidenceX,1);
        //Historic.main(Symbol, stockPrices,stockDelta,options, optionDelta, timeHorizonN, confidenceX,1);
        BackTest.main(Symbol,stockDelta, options, optionDelta, timeHorizonN, confidenceX);
    }
}

