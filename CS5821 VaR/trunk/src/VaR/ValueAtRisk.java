package VaR;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;


public class ValueAtRisk {

    private static Parameters setParameters(String args[]){
        /**
         * 1st Argument: Stock Symbols
         * 2nd Argument: Stock Deltas
         * 3rd Argument: Options Deltas
         * 4th Argument: Number of Years of Data
         * 5th Argument: Time Horizon
         * 6th Argument: Confidence Level
         */
        Parameters p = new Parameters();
        p.setSymbol(args[0]);
        p.setStockDelta(args[1]);
        p.setOptionsDelta(args[2]);
        p.setDataYears(args[3]);
        p.setTimeHorizon(args[4]);
        p.setConfidenceLevel(args[5]);
        return p;
    }

    private static Results estimateVaR(Parameters p, double[][] stockPrices, optionsData[] options) throws IOException{
        double currentValue = PortfolioInfo.print(p, stockPrices,options);
        Results r = new Results();
        r.setCurrentValue(currentValue);
        // Get VaR Measures
        //AnalyticalSingleStock.main(Symbol, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double[] varLinear = Analytical.main(p, stockPrices);
        double[] varMC = MonteCarlo.main(p, stockPrices,options, 1);
        double varHistorical = Historic.main(p, stockPrices,options, 1);
        //set results
        r.setVarANEW(varLinear[0]);
        r.setVarANEWMA(varLinear[1]);
        r.setVarANGARCH(varLinear[2]);
        r.setVarHistorical(varHistorical);
        r.setVarMCEW(varMC[0]);
        r.setVarMCEWMA(varMC[1]);
        r.setVarMCGARCH(varMC[2]);
        return r;
    }

    public static void main(String args[])throws IOException {
        System.out.println(Arrays.toString(args));
        Parameters p = setParameters(args);
        String relativePath = p.getOutputPath();
        FileOutputStream f = new FileOutputStream(relativePath + "output.txt");
        PrintStream originalStream = System.out;
        System.setOut(new PrintStream(f));
        System.out.println("=========================================================================");
        System.out.println("ValueAtRisk.java");
        System.out.println("=========================================================================");
        System.out.println("\tTime Horizon: " + p.getTimeHorizon() + " day(s)");
        System.out.println("\tConfidence Level: " + p.getConfidenceLevel());
        // Get Stock Data
        double[][] stockPrices = getStocks.main(p.getSymbol(), p.getDataYears());
        // Get Options Data
        optionsData[] options = getOptions.main(p.getSymbol());
        //calculate yearly volatility for options
        for(int i = 0; i < p.getNumSym(); i++)
            options[i].setVolatility(new Stats(stockPrices[i],stockPrices[i]).getEWVolatility()* Math.sqrt(252));
        //estimate VaR
        Results r = estimateVaR(p, stockPrices,options);
        //do backtest
        ArrayList<BackTestData> ArrayListBT = BackTest.main(p, options);
        //close print stream f
        f.close();
        //Reset output stream to default
        System.setOut(originalStream);
        //print raw data to csv
        r.OutputCSV(p, ArrayListBT);
    }
}

