package VaR;

import java.util.Arrays;

/**
 * Created by Adrian on 21/06/2017.
 * http://financetrain.com/calculating-var-using-historical-simulation/
 */
public class Historic {

    public static double main(String[] stockSymbol, double[][] stockPrices, int[] stockDelta, optionsData[] options, int[] optionDelta, int timeHorizonN, double confidenceX){
        System.out.println("=========================================================================");
        System.out.println("Historic.java");
        System.out.println("=========================================================================");

        int numSym = stockSymbol.length;

        double[] currentStockPrices = new double[numSym];
        double[][] strikePrices = new double[numSym][];
        double[][] currentPutPrices = new double[numSym][];
        long[] daystoMaturity = new long[numSym];
        double currentValue = 0;
        for (int i = 0; i < numSym; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            strikePrices[i] = options[i].getStrikePrices();
            daystoMaturity[i] = options[i].getDaystoMaturity();
            currentPutPrices[i] = options[i].getPutPrices();
            int numPuts = currentPutPrices[i].length;
            currentValue += stockDelta[i] * currentStockPrices[i] + optionDelta[i] * currentPutPrices[i][numPuts-1];
        }
/*
        for(int i = 0; i < numSym; i++){
            System.out.println(strikePrices[i].length);
            System.out.println(currentPutPrices[i].length);
            for(int j = 0; j < currentPutPrices[i].length; j++)
                System.out.println(currentPutPrices[i][j]);
        }
*/

        /**
         * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
         */
        double[][] priceChanges = new StockParam(stockPrices).getPercentageChanges();
        /** CALCULATE ALL OF TOMORROW'S POSSIBLE STOCK PRICES */
        int numTuple = priceChanges[0].length;
        double[][] tomorrowStockPrices = new double[numSym][numTuple];
        for(int i = 0; i < numSym; i++)
            for(int  j = 0; j < numTuple; j++) {
                tomorrowStockPrices[i][j] = priceChanges[i][j] * currentStockPrices[i];
                //System.out.println(tomorrowStockPrices[i][j]);
            }

        /** PRICE OPTIONS */
        double[][] tomorrowPutPrices = new double[numSym][numTuple];
        for(int i = 0; i < numSym; i++)
            for(int  j = 0; j < numTuple; j++) {
                tomorrowPutPrices[i][j] = options[i].getBlackScholesPut(tomorrowStockPrices[i][j]);
                //System.out.println(tomorrowPutPrices[i][j]);
            }
        /**
         * REVALUE PORTFOLIO FROM ALL POSSIBLE PERCENTAGE CHANGES
         */
        double[] deltaP = new double[numTuple];
        for(int i = 0; i < numTuple;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += (tomorrowStockPrices[j][i] * stockDelta[j]) + (tomorrowPutPrices[j][i] * optionDelta[j]);
            deltaP[i] = sum;
        }
        /** GET VaR FROM xTH DELTAP */
        Arrays.sort(deltaP);
        double index = (1-confidenceX)*deltaP.length;
        double VaR = (currentValue - deltaP[(int) index]) * Math.sqrt(timeHorizonN);
        System.out.println("\n\t\tValue at Risk: " + VaR);
        return VaR;
    }
}
