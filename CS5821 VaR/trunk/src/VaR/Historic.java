package VaR;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Adrian on 21/06/2017.
 */
public class Historic {


    public static double[][] calculatePriceChanges(double[][] stockPrices){
        int numSym = stockPrices.length;
        int numTuples = stockPrices[0].length;
        double[][] priceDiff = new double[numSym][numTuples - 1];
        for  (int i = 0;i < numSym;i++)
            for (int j = 0; j < numTuples - 1; j++)
                priceDiff[i][j] = stockPrices[i][j] - stockPrices[i][j+1];
        return priceDiff;
    }

    public static void main(String[] symbol, double[][] stockPrices){

        long  portfolioPi[]     = {100,200};
        double  confidenceX     = 0.99;
        int     timeHorizonN    = 1;

        double currentPrice[] = new double[symbol.length];
        double[] deltaP = new double[stockPrices[0].length-1];

        System.out.println("=========================================================================");
        System.out.println("Historic.java");
        System.out.println("=========================================================================");

        double[][] priceChanges = calculatePriceChanges(stockPrices);

        for (int i = 0; i < symbol.length; i++) {
            currentPrice[i] = stockPrices[i][0];
            System.out.println("\t\t" + portfolioPi[i] + " stocks in " + symbol[i]);
        }



        for(int i = 0; i < priceChanges[0].length;i++) {
            double sum = 0;
            for (int j = 0; j < symbol.length; j++)
                sum += priceChanges[j][i] * portfolioPi[j];
            deltaP[i] = sum;

        }
        Arrays.sort(deltaP);
        System.out.println("\t\t" + deltaP[(int) (1-confidenceX)*deltaP.length]);



    }


}
