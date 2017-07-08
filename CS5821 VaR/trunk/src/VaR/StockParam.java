package VaR;

import java.util.ArrayList;

/**
 * Created by Adrian on 08/07/2017.
 */
public class StockParam {
    //instance variable
    private ArrayList<Double> singleStock;
    private int numTuple;
    //constructor
    public StockParam(ArrayList<Double> singleStock)
    {
        this.singleStock = singleStock;
        numTuple =  singleStock.size();
    }

    public double getEqualWeightVolatility () {
        double arrU[] = new double [numTuple-1];
        //GENERATE ARRAY OF PRICE DIFFERENCES
        for (int i = 0; i < (numTuple-1); i++)
            arrU[i] = Math.pow((singleStock.get(i+1)-singleStock.get(i))/singleStock.get(i),2);
        //CALCULATE AVERAGE
        double sum = 0;
        for (int i = 0; i < (numTuple-1); i++)
            sum += arrU[i];
        double avg = sum/(numTuple-1);
        double sigma = Math.sqrt(avg);
        return sigma;
    }

    public double getEWMAVolatility(){
        double lambda = 0.94;
        double arrU[] = new double [numTuple-1];
        //GENERATE ARRAY OF PRICE DIFFERENCES AND CALCULATE RATIO u^2
        for (int i = 0; i < (numTuple-1); i++)
            arrU[i] = Math.pow((singleStock.get(i+1)-singleStock.get(i))/singleStock.get(i),2);
        double EWMA = arrU[0];
        for (int i = 1; i < (numTuple-2);i++)
            EWMA = lambda * EWMA + (1-lambda) * arrU[i+1];
        double sigma = Math.sqrt(EWMA);
        return sigma;
    }

    public double getMean(){
        double sum = 0;
        for (int i = 0; i< numTuple; i++)
            sum += singleStock.get(i);
        return sum/numTuple;
    }

    public double getVariance(){
        double mean = getMean();
        double sum = 0;
        for (int i = 0; i< numTuple; i++)
            sum += Math.pow((singleStock.get(i) - mean),2);
        return sum/numTuple;
    }



}

