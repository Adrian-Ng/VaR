package VaR;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Adrian on 21/06/2017.
 * http://financetrain.com/calculating-var-using-historical-simulation/
 */
public class Historic {

    public static double main(Parameters p, double[][] stockPrices, optionsData[] options, int printFlag)throws IOException{
        System.out.println("=========================================================================");
        System.out.println("Historic.java");
        System.out.println("=========================================================================");
        int numSym = p.getNumSym();
        //get Parameters
        int[] stockDelta = p.getStockDelta();
        int[] optionDelta = p.getOptionsDelta();
        //initialize arrays
        double[] currentStockPrices = new double[numSym];
        double[][] strikePrices = new double[numSym][];
        double[][] currentPutPrices = new double[numSym][];
        int[] daystoMaturity = new int[numSym];
        double currentValue = 0;
        //get Options data
        for (int i = 0; i < numSym; i++) {
            currentStockPrices[i] = stockPrices[i][0];
            strikePrices[i] = options[i].getStrikePrices();
            daystoMaturity[i] = options[i].getDaystoMaturity();
            currentPutPrices[i] = options[i].getPutPrices();
            int numPuts = currentPutPrices[i].length;
            currentValue += stockDelta[i] * currentStockPrices[i] + optionDelta[i] * currentPutPrices[i][numPuts-1];
        }
        /** CALCULATE PERCENTAGE CHANGE IN STOCK PRICE*/
        double[][] priceChanges = new Stats(stockPrices).getPercentageChanges();
        /** SIMULATE ALL OF TOMORROW'S POSSIBLE STOCK PRICES FROM HISTORICAL DATA*/
        int numTuple = priceChanges[0].length;
        double[][] tomorrowStockPrices = new double[numSym][numTuple];
        for(int i = 0; i < numSym; i++)
            for(int  j = 0; j < numTuple; j++)
                tomorrowStockPrices[i][j] = (priceChanges[i][j] * currentStockPrices[i]) + currentStockPrices[i];
        /** PRICE OPTIONS */
        double[][] tomorrowPutPrices = new double[numSym][numTuple];
        for(int i = 0; i < numSym; i++)
            for(int  j = 0; j < numTuple; j++)
                tomorrowPutPrices[i][j] = options[i].getBlackScholesPut(tomorrowStockPrices[i][j]);
        /** REVALUE PORTFOLIO FROM ALL POSSIBLE PERCENTAGE CHANGES*/
        double[] deltaP = new double[numTuple];
        for(int i = 0; i < numTuple;i++) {
            double sum = 0;
            for (int j = 0; j < numSym; j++)
                sum += (tomorrowStockPrices[j][i] * stockDelta[j]) + (tomorrowPutPrices[j][i] * optionDelta[j]);
            deltaP[i] = sum;
        }
        /** GET VaR FROM xTH DELTAP */
        Arrays.sort(deltaP);
        double index = (1-p.getConfidenceLevel())*deltaP.length;
        double VaR = (currentValue -  deltaP[(int) index]) * Math.sqrt(p.getTimeHorizon());
        System.out.println("\n\t\tValue at Risk: " + VaR);
        /** PRINT DATA TO CSV*/
        if (printFlag == 1){
            new Stats(tomorrowStockPrices).printMatrixToCSV(p.getSymbol(),"Historic stockPrices", p.getOutputPath());
            new Stats(tomorrowPutPrices).printMatrixToCSV(p.getSymbol(),"Historic putPrices", p.getOutputPath());
            new Stats(deltaP).printVectorToCSV("Portfolio Value", "Historic Portfolio Value", p.getOutputPath());
        }
        return VaR;
    }
}
