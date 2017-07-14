package VaR;

import java.util.Arrays;

/**
 * Created by Adrian on 21/06/2017.
 * http://financetrain.com/calculating-var-using-historical-simulation/
 */
public class Historic {

    public static void main(String[] symbol, double[][] stockPrices, int[] stockDelta, int timeHorizonN, double confidenceX){
        System.out.println("=========================================================================");
        System.out.println("Historic.java");
        System.out.println("=========================================================================");

        int numSym = symbol.length;
        double[] currentStockPrices = new double[numSym];
        /**
         * WHAT DOES THE PORTFOLIO LOOK LIKE?
         */
        for (int i = 0; i < symbol.length; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            System.out.println("\t\t" + stockDelta[i] + " stocks in " + symbol[i] + ". Current price is: " + currentStockPrices[i]);
        }
        double currentValue = 0;
        for (int i = 0; i < symbol.length; i++) {
            currentValue += stockDelta[i] * currentStockPrices[i];
        }
        System.out.println("\t\tCurrent Value of Portfolio: " + currentValue);

        /**
         * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
         */
        double[][] priceChanges = new StockParam(stockPrices).getPercentageChanges();

        /**
         * CALCULATE CHANGE IN VALUE OF PORTFOLIO ON EVERY DAY
          */
        double[] deltaP = new double[stockPrices[0].length-1];

        for(int i = 0; i < priceChanges[0].length;i++) {
            double sum = 0;
            for (int j = 0; j < symbol.length; j++)
                sum += priceChanges[j][i] * stockDelta[j];
            deltaP[i] = sum;
        }
        Arrays.sort(deltaP);
        double index = (1-confidenceX)*deltaP.length;
        System.out.println("\n\t\tValue at Risk: " + deltaP[(int) index]);
    }
}
