package VaR;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adrian on 21/06/2017.
 */
public class Historic {


    //private static ArrayList<myData> alStocks;

    public static double equalWeight(String symbols) {
        ArrayList<myData> alStocks = getData.mapStocks.get(symbols);
        int size = alStocks.size();
        int i = 0;
        double price[] = new double[size];
        double arrU[] = new double [size-1];

        for (myData stock : alStocks) {
            price[i] = stock.getStock(3);
            i++;
        }
        //GENERATE ARRAY OF PRICE DIFFERENCES
        for (int j = 0; j < (size-1); j++)
            arrU[j] = Math.pow((price[j+1]-price[j])/price[j],2);

        //CALCULATE AVERAGE
        double sum = 0;
        for (int j = 0; j < (size-1); j++)
            sum += arrU[j];
        double avg = sum/(size-1);

        double sigma = Math.sqrt(avg);


        return sigma;
    }

    public static double calcEWMA(String symbols){
        double lambda = 0.94;
        ArrayList<myData> alStocks = getData.mapStocks.get(symbols);
        int size = alStocks.size();
        int i = 0;
        double price[] = new double[size];
        double arrU[] = new double [size-1];

        for (myData stock : alStocks) {
            price[i] = stock.getStock(3);
            i++;
        }
        //GENERATE ARRAY OF PRICE DIFFERENCES AND CALCULATE RATIO u^2
        for (int j = 0; j < (size-1); j++){
            arrU[j] = Math.pow((price[j+1]-price[j])/price[j],2);
        }
        double EWMA = arrU[0];
        for (int j = 1; j < (size-2);j++)
                EWMA = lambda * EWMA + (1-lambda) * arrU[j+1];

        double sigma = Math.sqrt(EWMA);
        return sigma;
    }



/*
    public static void getData(String[] symbols){

        System.out.println("Historic");
        for(String sym : symbols){
            System.out.println("\t" + sym);
            //ArrayList<myData> alStocks = new ArrayList<myData>();
            alStocks = getData.mapStocks.get(sym);
            int i=0;
            int j=0;
            for(myData stck : alStocks){
                i +=stck.getStock(3);
                //System.out.println(stck.getDate() + " " + stck.getStock(1));
                j++;
            }
            System.out.println("\tAverage Historic Stock: " + (double) i/j);
        }
        //return null;
    }
*/



    public static void main(String[] symbols){

        for (String sym : symbols) {
            System.out.println("\t" + sym);
            System.out.println("\t\tDaily Equal Weighted Volatility is: " + equalWeight(sym));
            System.out.println("\t\tYearly Equal Weighted Volatility is: " + equalWeight(sym) * Math.sqrt(252));
            System.out.println("\t\tDaily EWMA Volatility is: " + calcEWMA(sym));
            System.out.println("\t\tYearly EWMA Volatility is: " + calcEWMA(sym) * Math.sqrt(252));
        }

    }

}
