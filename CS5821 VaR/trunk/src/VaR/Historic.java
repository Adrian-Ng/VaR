package VaR;

import java.util.Arrays;

/**
 * Created by Adrian on 21/06/2017.
 * http://financetrain.com/calculating-var-using-historical-simulation/
 */
public class Historic {

    public static double main(String[] symbol, double[][] stockPrices, int[] stockDelta, double[][] optionPrices, int[] optionDelta, int timeHorizonN, double confidenceX){
        System.out.println("=========================================================================");
        System.out.println("Historic.java");
        System.out.println("=========================================================================");

        int numSym = symbol.length;
        double[] currentStockPrices = new double[numSym];
        /**
         * WHAT DOES THE PORTFOLIO LOOK LIKE?
         */
        for (int i = 0; i < numSym; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            System.out.println("\t\t" + stockDelta[i] + " stocks in " + symbol[i] + ". Current price is: " + currentStockPrices[i]);
        }
        double currentValue = 0;
        for (int i = 0; i < numSym; i++) {
            currentValue += stockDelta[i] * currentStockPrices[i];
        }
        System.out.println("\t\tCurrent Value of Portfolio: " + currentValue);

        /**
         * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
         */
        double[][] priceChanges = new StockParam(stockPrices).getPercentageChanges();

        /**
         * REVALUE PORTFOLIO FROM ALL POSSIBLE PERCENTAGE CHANGES
          */
        double[] deltaP = new double[priceChanges[0].length];

        for(int i = 0; i < priceChanges[0].length;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += priceChanges[j][i] * currentStockPrices[j] * stockDelta[j];
            deltaP[i] = sum;
        }
        /**
         * GET VaR
         */
        Arrays.sort(deltaP);
/*
        for(int i = 0; i < deltaP.length; i++)
            System.out.println(deltaP[i]);
*/
        double index = (1-confidenceX)*deltaP.length;
        double VaR = (currentValue - deltaP[(int) index]) * Math.sqrt(timeHorizonN);
        System.out.println("\n\t\tValue at Risk: " + VaR);
        return VaR;
    }
}
