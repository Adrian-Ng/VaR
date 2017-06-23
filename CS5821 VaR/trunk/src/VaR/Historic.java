package VaR;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adrian on 21/06/2017.
 */
public class Historic {


    private static ArrayList<myData> alStocks;

    public static float calcEWMA;




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



/*
    public static void main(String args[]){

        getData.mapStocks.get();

    }*/

}
