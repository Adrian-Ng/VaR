package VaR;

import org.apache.commons.math3.fitting.*;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;

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
/*
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

        //GENERATE ARRAY OF PRICE DIFFERENCES AND CALCULATE RATIO u^2
        double arrU[] = new double [numTuple-1];
        for (int i = 0; i < (numTuple-1); i++)
            arrU[i] = Math.pow((singleStock[i+1]-singleStock[i])/singleStock[i],2);
        double EWMA = arrU[0];
        for (int i = 1; i < (numTuple-2);i++)
            EWMA = lambda * EWMA + (1-lambda) * arrU[i+1];
        double sigma = Math.sqrt(EWMA);
        return sigma;
    }
*/
    public double getEqualWeightVolatility() {
        int numTuple =  xStock.length;
        //GENERATE ARRAY OF PRICE DIFFERENCES AND CALCULATE RATIO u and v
        double ratioU[] = new double [numTuple-1];
        double ratioV[] = new double [numTuple-1];
        for (int i = 0; i < (numTuple-1); i++) {
            ratioU[i] = (xStock[i + 1] - xStock[i]) / xStock[i];
            ratioV[i] = (yStock[i + 1] - yStock[i]) / yStock[i];
        }
        //CALCULATE AVERAGE
        double sum = 0;
        for (int i = 0; i < (numTuple-1); i++)
            sum += ratioU[i]*ratioV[i];
        double avg = sum/(numTuple-1);
        double sigma = Math.sqrt(avg);
        return sigma;
    }

public double getEWMAVolatility(){
    double lambda = 0.94;
    int numTuple =  xStock.length;
    //GENERATE ARRAY OF PRICE DIFFERENCES AND CALCULATE RATIO u and v
    double ratioU[] = new double [numTuple-1];
    double ratioV[] = new double [numTuple-1];
    for (int i = 0; i < (numTuple-1); i++) {
        ratioU[i] = (xStock[i + 1] - xStock[i]) / xStock[i];
        ratioV[i] = (yStock[i + 1] - yStock[i]) / yStock[i];
    }
    int numDiff = ratioU.length;
    double EWMA = ratioU[numDiff-1]*ratioV[numDiff-1];
    for (int i = 1; i < numDiff;i++)
        EWMA = lambda * EWMA + (1-lambda) * ratioU[numDiff-1 - i]*ratioV[numDiff-1 - i];
    double sigma = Math.sqrt(EWMA);
    return sigma;
}

private double likelihood;
private double[] derivatives = new double[3]; //derivatives as in partial differentiation. Not to be confused with the financial instrument!
private double[] LevenBergMarquardt(double[] ratioU, double[] ratioV){
/**
 * Numerical Recipes in C page 684
 * Given an initial guess for the set of fitted parameters a, the recommended Marquardt recipe is as follows:
 • Compute χ2(a) (= likelihood).
 • Pick a modest value for λ, say λ = 0.001.
 • (†) Solve the linear equations (15.5.14) for δa and evaluate χ2(a + δa).
 • If χ2(a + δa) ≥χ2(a), increase λ by a factor of 10 (or any other substantial factor) and go back to (†).
 • If χ2(a + δa) < χ2(a), decrease λ by a factor of 10, update the trial solution a ← a + δa, and go back to (†)
 */

    double lambda = 0.9101;
    double alpha = 1-lambda;
    double beta = lambda;
    double omega = 0.000001347;
    double[] parameters = new double[3];
    parameters[0] = omega;
    parameters[1] = alpha;
    parameters[2] = beta;


    likelihood(ratioU,ratioV,parameters);
    double[][] Hessian = getHessianMatrix();
    double λ = 0.001;




    return parameters;}

private void likelihood(double[] ratioU, double[] ratioV, double parameters[]){
    double omega    = parameters[0];
    double alpha    = parameters[1]; //not to be confused with the alpha matrix in LevenbergMarquardt
    double beta     = parameters[2]; //not to be confused with the beta vector in LevenbergMarquardt

    int numTuple1 = ratioU.length;
    //calculate initial variance
    double variance = ratioU[numTuple1-1]*ratioV[numTuple1-1];
    //calculate initial likelihood
    likelihood = -Math.log(variance) - (ratioU[numTuple1-1]*ratioV[numTuple1-1])/variance;
    //initialize derivatives of likelihood
    double dOmega = 0.0;
    double dAlpha = 0.0;
    double dBeta = 0.0;
    for(int i = 1; i < (numTuple1-1); i++) {
        double uSquared = ratioU[numTuple1-1 - i]*ratioV[numTuple1-1 - i];
        double newVariance = omega + alpha*uSquared + beta*variance;
        likelihood += Math.log(newVariance) - (uSquared)/newVariance;
        //calculate derivatives
        dOmega  += -(1/newVariance) + (uSquared)/Math.pow(newVariance,2);
        dAlpha  += -(Math.pow(uSquared,2)/newVariance) + (Math.pow(uSquared,2)/Math.pow(newVariance,2));
        dBeta   += -(variance/newVariance) + (uSquared*variance)/(Math.pow(newVariance,2));
        variance = newVariance;
    }
    derivatives[0] = dOmega;
    derivatives[1] = dAlpha;
    derivatives[2] = dBeta;
}

private double[][] getHessianMatrix(){
    double[][] Hessian = new double[derivatives.length][derivatives.length];
    for(int i = 0; i < derivatives.length; i ++)
        for(int j = 0; j < derivatives.length; j++)
            Hessian[i][i] = derivatives[i]*derivatives[j];
    return Hessian;
}


    public void getGARCH11(){
        //Generalized Autoregressive Conditional Heteroskedastic Process
        int numTuple =  xStock.length;


        //GENERATE ARRAY OF PRICE DIFFERENCES AND CALCULATE RATIO u and v
        double ratioU[] = new double [numTuple-1];
        double ratioV[] = new double [numTuple-1];
        for (int i = 0; i < (numTuple-1); i++) {
            ratioU[i] = (xStock[i + 1] - xStock[i]) / xStock[i];
            ratioV[i] = (yStock[i + 1] - yStock[i]) / yStock[i];
        }
        //OPTIMISE PARAMETERS VIA LevenbergMarquardt algorithm
        double parameters[] = LevenBergMarquardt(ratioU,ratioV);

        //double sigma = Math.sqrt(GARCH);
        //return sigma;
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

    public double[][] getAbsoluteChanges(){
        int numSym = multiStock.length;
        int numTuples = multiStock[0].length;
        double[][] priceDiff = new double[numSym][numTuples - 1];
        for  (int i = 0;i < numSym;i++)
            for (int j = 0; j < numTuples - 1; j++)
                priceDiff[i][j] = multiStock[i][j]-multiStock[i][j+1];
        return priceDiff;
    }

}

