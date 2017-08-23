package VaR;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class ValueAtRisk {




    public static void main(String args[])throws IOException {

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
        int sumOptions = 0;
        int[] optionDelta = new int[strOptionDelta.length];
        for(int i = 0; i< strOptionDelta.length;i++) {
            optionDelta[i] = Integer.parseInt(strOptionDelta[i]);
            sumOptions += optionDelta[i];
        }
        // NUMBER OF YEARS IN THE PAST TO LOOK AT
        int intYears = Integer.parseInt(args[3]);
        // TIME HORIZON
        int timeHorizonN = Integer.parseInt(args[4]);
        // CONFIDENCE INTERVAL
        double confidenceX = Double.parseDouble(args[5]);

        //format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime Date = LocalDateTime.now();
        String dateStr = Date.format(formatter);
        //are there options?
        String strOptions;
        if(sumOptions == 0)
            strOptions = "";
         else
            strOptions = " - Options";

        //make directory for output
        String relativePath = dateStr + " VaR - " + Arrays.toString(Symbol) + strOptions + " - " + intYears + " years - "  + timeHorizonN + " day horizon - "  + confidenceX + " confidence" +  "/";
        new File(relativePath).mkdir();
        FileOutputStream f = new FileOutputStream(relativePath + "output.txt");
        System.setOut(new PrintStream(f));
        System.out.println("=========================================================================");
        System.out.println("ValueAtRisk.java");
        System.out.println("=========================================================================");

        System.out.println("\tTime Horizon: " + timeHorizonN + " day(s)");
        System.out.println("\tConfidence Level: " + confidenceX);

        // Get Stock Data
        double[][] stockPrices = getStocks.main(Symbol, intYears);
        // Get Options Data
        optionsData[] options = getOptions.main(Symbol);
        //calculate yearly volatility for options
        for(int i = 0; i < numSym; i++)
            options[i].setVolatility(new methods(stockPrices[i]).getStandardDeviation()* Math.sqrt(252));
        double currentValue = PortfolioInfo.print(Symbol, stockPrices,stockDelta,options,optionDelta);
        //initialize results;
        Results results = new Results();
        results.setDataYears(intYears);
        results.setTimeHorizon(timeHorizonN);
        results.setConfidenceLevel(confidenceX);
        results.setCurrentValue(currentValue);
        // Get VaR Measures
        //AnalyticalSingleStock.main(Symbol, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double[] varLinear = AnalyticalLinear.main(Symbol, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double varMC = MonteCarlo.main(Symbol, stockPrices,stockDelta,options, optionDelta, timeHorizonN, confidenceX,1, relativePath);
        double varHistorical = Historic.main(Symbol, stockPrices,stockDelta,options, optionDelta, timeHorizonN, confidenceX,1, relativePath);
        //set results
        results.setVarStDev(varLinear[0]);
        results.setVarEWMA(varLinear[1]);
        results.setVarGARCH(varLinear[2]);
        results.setVarHistorical(varHistorical);
        results.setVarMonteCarlo(varMC);
        results.getVarEWMA();
        System.out.println(results.getVarGARCH());
        ArrayList<BackTestData> ArrayListBT = BackTest.main(Symbol,stockDelta, options, optionDelta, timeHorizonN, confidenceX, relativePath);
        f.close();
        results.OutputCSV(ArrayListBT, relativePath);
    }
}

