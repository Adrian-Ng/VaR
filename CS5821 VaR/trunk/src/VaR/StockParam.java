package VaR;


/**
 * Created by Adrian on 08/07/2017.
 */
public class StockParam {
    //instance variable
    private double[] singleStock;


    private double[] xStock;
    private double[] yStock;

    private double[][] multiStock;
    //constructor
    public StockParam(double[] singleStock)
    {
        this.singleStock = singleStock;

    }

    public StockParam(double[] xStock, double[] yStock){
        this.xStock = xStock;
        this.yStock = yStock;
    }

    public StockParam(double[][] multiStock)
    {
        this.multiStock = multiStock;
    }

    public double getEqualWeightVolatility() {
        int numTuple =  singleStock.length;
        double arrU[] = new double [numTuple-1];
        //GENERATE ARRAY OF PRICE DIFFERENCES
        for (int i = 0; i < (numTuple-1); i++)
            arrU[i] = Math.pow((singleStock[i+1]-singleStock[i])/singleStock[i],2);
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
        int numTuple =  singleStock.length;
        double arrU[] = new double [numTuple-1];
        //GENERATE ARRAY OF PRICE DIFFERENCES AND CALCULATE RATIO u^2
        for (int i = 0; i < (numTuple-1); i++)
            arrU[i] = Math.pow((singleStock[i+1]-singleStock[i])/singleStock[i],2);
        double EWMA = arrU[0];
        for (int i = 1; i < (numTuple-2);i++)
            EWMA = lambda * EWMA + (1-lambda) * arrU[i+1];
        double sigma = Math.sqrt(EWMA);
        return sigma;
    }

    public double getMean(){
        int numTuple =  singleStock.length;
        double sum = 0;
        for (int i = 0; i< numTuple; i++)
            sum += singleStock[i];
        return sum/numTuple;
    }

    public double getVariance(){
        int numTuple =  singleStock.length;
        double mean = getMean();
        double sum = 0;
        for (int i = 0; i< numTuple; i++)
            sum += Math.pow((singleStock[i] - mean),2);
        return sum/(numTuple-1);
    }

    public double getStandardDeviation(){
        return Math.sqrt(getVariance());
    }

    public double getCovariance(){
        int numTuples = xStock.length;
        //Get Mean
        double meanX = new StockParam(xStock).getMean();
        double meanY = new StockParam(yStock).getMean();
        //Calculation
        double sum = 0;
        for (int i = 0; i < numTuples; i++)
            sum +=(xStock[i] - meanX)*(yStock[i] - meanY);
        return sum/(numTuples-1);
    }

    public double[][] getCovarianceMatrix(){
        int numSym = multiStock.length;
        double[] meanVector = new double[numSym];
        for(int i = 0; i < numSym; i++)
            meanVector[i] = new StockParam(multiStock[i]).getMean();
        double[][] covarianceMatrix = new double[numSym][numSym];
        for(int i = 0; i < numSym; i++)
            for(int j = 0;j < numSym; j++)
                covarianceMatrix[i][j] = new StockParam(multiStock[i], multiStock[j]).getCovariance();
        return covarianceMatrix;
    }

    public double[][] getCholeskyDecomposition(){
        double[][] covarianceMatrix = new StockParam(multiStock).getCovarianceMatrix();
        double[][] cholesky = new double[covarianceMatrix.length][covarianceMatrix.length];
        //Initialize the matrix as all zeroes
        for(int i = 0; i < cholesky.length;i++)
            for(int j = 0; j < cholesky.length;j++)
                cholesky[i][j] = 0;

        //Start at the top right
        cholesky[0][0] = Math.sqrt(covarianceMatrix[0][0]);
        for(int i = 1; i < covarianceMatrix.length; i++)
            for(int j = 0; j <= i; j++)
                if(i == j && j > 0 ) {
                    double sum = 0;
                    for(int k = 0; k < j;k++)
                        sum += Math.pow(cholesky[i][k],2);
                    cholesky[i][j] = Math.sqrt(covarianceMatrix[i][j] - sum);
                }
                else if (i != j) {
                    cholesky[i][j] = covarianceMatrix[i][j] / cholesky[j][j];
                }
        return cholesky;
    }

    public double[][] getPercentageChanges(){
        int numSym = multiStock.length;
        int numTuples = multiStock[0].length;
        double[][] priceDiff = new double[numSym][numTuples - 1];
        for  (int i = 0;i < numSym;i++)
            for (int j = 0; j < numTuples - 1; j++)
                priceDiff[i][j] = (multiStock[i][j]/multiStock[i][j+1]);
        return priceDiff;
    }

}

