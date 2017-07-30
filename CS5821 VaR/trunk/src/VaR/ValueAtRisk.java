package VaR;

import java.io.IOException;
import java.util.Arrays;

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
        //SPLIT PIPE DELIMITED LIST OF STOCK SYMBOLS
        String[] symbolsStock = args[0].split("\\|");
        int numSymStock = symbolsStock.length;
        //SPLIT PIPE DELIMITED LIST OF STOCK DELTA
        String[] strStockDelta = args[1].split("\\|");
        int[] stockDelta = new int[strStockDelta.length];
        for(int i = 0; i< strStockDelta.length;i++)
            stockDelta[i] = Integer.parseInt(strStockDelta[i]);
        //SPLIT PIPE DELIMITED LIST OF OPTIONS SYMBOLS
        String[] symbolsOptions = args[2].split("\\|");
        int numSymOptions = symbolsOptions.length;
        //SPLIT PIPE DELIMITED LIST OF OPTIONS DELTA
        String[] strOptionsDelta = args[3].split("\\|");
        int[] optionsDelta = new int[strOptionsDelta.length];
        for(int i = 0; i< strOptionsDelta.length;i++)
            optionsDelta[i] = Integer.parseInt(strOptionsDelta[i]);
        // NUMBER OF YEARS IN THE PAST TO LOOK AT
        int intDays = Integer.parseInt(args[4]);
        // TIME HORIZON
        int timeHorizonN = Integer.parseInt(args[5]);
        // CONFIDENCE INTERVAL
        double confidenceX = Double.parseDouble(args[6]);
        // Get Stock Data
        double[][] stockPrices = getStocks.main(symbolsStock, intDays);



        //System.out.println("\t\tVolatilities" + Arrays.toString(optionsVolatility));
        // Get Options Data
        optionsData[] options = getOptions.main(symbolsStock);

        //calculate yearly volatility for options
        //double[] optionsVolatility = new double[numSymStock];
        for(int i = 0; i < numSymStock; i++)
            options[i].setVolatility(new StockParam(stockPrices[i]).getEWMAVolatility()* Math.sqrt(252));
        double[][] strikePrices = new double[numSymStock][];
        //double[][] callPrices = new double[numSymStock][];
        //double[][] putPrices = new double[numSymStock][];
        long[] daystoMaturity = new long[numSymStock];
        for (int i = 0; i < numSymStock; i++) {
            strikePrices[i] = options[i].getStrikePrices();
            //callPrices[i] = options[i].getCallPrices();
            //putPrices[i] = options[i].getPutPrices();
            daystoMaturity[i] = options[i].getDaystoMaturity();
        }
        //double[][] priceEuropeanCall = new double[numSymStock][stockPrices[0].length];
        double[][] priceEuropeanPut = new double[numSymStock][stockPrices[0].length];
        //double[][] priceAmericanCall = new double[numSymStock][stockPrices[0].length];
        //double[][] priceAmericanPut = new double[numSymStock][stockPrices[0].length];
        //CALCULATE OPTIONS PRICES
        for(int i = 0; i < numSymStock; i++)
            for(int j = 0; j < stockPrices[i].length;j++){
            //priceEuropeanCall[i][j] = options[i].getEuropeanCall(stockPrices[i][j]);
            priceEuropeanPut[i][j] = options[i].getEuropeanPut(stockPrices[i][j]);
            //priceAmericanCall[i][j] = options[i].getAmericanCall(stockPrices[i][j]);
            //priceAmericanPut[i][j] = options[i].getAmericanPut(stockPrices[i][j]);
        }
        // Get VaR Measures
        double[] analyticalVaR = Analytical.main(symbolsStock, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double linearVaR = Linear.main(symbolsStock, stockPrices,stockDelta, timeHorizonN, confidenceX);
        double montecarloVaR = MonteCarlo.main(symbolsStock, stockPrices,stockDelta, priceEuropeanPut, optionsDelta, timeHorizonN, confidenceX);
        double historicVaR = Historic.main(symbolsStock, stockPrices,stockDelta, priceEuropeanPut, optionsDelta, timeHorizonN, confidenceX);
        //int[] violations = BackTest.main(symbolsStock,stockDelta, priceEuropeanPut, optionsDelta, timeHorizonN, confidenceX);

    }

}

