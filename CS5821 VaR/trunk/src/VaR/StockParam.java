package VaR;


import org.apache.commons.math3.Field;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.linear.MatrixUtils;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.util.Decimal64Field;
import org.nd4j.linalg.util.BigDecimalMath;
import org.apache.commons.math3.util.BigReal;
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
    private FieldMatrix name;
    private BigDecimal curvatureMatrix;
    private double[] vectorBeta = new double[3]; //derivatives as in partial differentiation. Not to be confused with the financial instrument!
    private double[] LevenBergMarquardt(double[] uSquaredArray){
/**
 * Numerical Recipes in C page 684
 * Given an initial guess for the set of fitted parameters a, the recommended Marquardt recipe is as follows:
 • Compute χ2(a) (= likelihood).
 • Pick a modest value for λ, say λ = 0.001.
 • (†) Solve the linear equations (15.5.14) for δa and evaluate χ2(a + δa).
 • If χ2(a + δa) ≥χ2(a), increase λ by a factor of 10 (or any other substantial factor) and go back to (†).
 • If χ2(a + δa) < χ2(a), decrease λ by a factor of 10, update the trial solution a ← a + δa, and go back to (†)
 */
        double λ = 0.00001; //non-dimensional fudge factor
        //double[] parameters = new double[3];
        BigDecimal[] parameters = new BigDecimal[3];
        parameters[0] = new BigDecimal(Math.random()*0.0003); //omega
        parameters[1] = new BigDecimal(Math.random()*0.1); //alpha
        parameters[2] = new BigDecimal(Math.random()*0.9); //beta
        System.out.println(Arrays.toString(parameters));
        double likelihood = likelihood(uSquaredArray,parameters, λ);
        int epoch = 0;
        while(epoch < 10000) {

            double[] vectorBeta = this.vectorBeta;
            //name = MatrixUtils.crea
            //solve simultaneous equations to calculate deltaParameters
            //MatrixUtils.createFieldMatrix(BigReal(curvatureMatrix),3,3);
            
            RealMatrix coefficients = new Array2DRowRealMatrix();
            DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
            RealVector constants = new ArrayRealVector(vectorBeta);
            RealVector solution = solver.solve(constants);
            double[] deltaParameters = new double[parameters.length];
            for(int i = 0; i < deltaParameters.length; i++)
                deltaParameters[i] = solution.getEntry(i);
            //System.out.println(Arrays.toString(deltaParameters));
            //evaluate χ2(a + δa)
            double[] trialParameters = new double[parameters.length];
            for (int i = 0; i < parameters.length; i++)
                trialParameters[i] = parameters[i] + deltaParameters[i];
            double trialLikelihood = likelihood(uSquaredArray, trialParameters, λ);
            //System.out.println(trialLikelihood);
            if (trialParameters[0] < 0.0 || trialParameters[1] < 0.0 || trialParameters[1] > 1.0 || trialParameters[2] < 0.0 || trialParameters[2] > (1-trialParameters[1])|| trialParameters[1] + trialParameters[2] > 1)

                λ *= 0.1; //use a larger fudge factor
            else if (trialLikelihood > likelihood) {
                parameters = trialParameters;
                λ *= 10; //if successful, use a smaller fudge factor
                likelihood = trialLikelihood;
            }
        /*else if ( trialLikelihood < 0)
            λ *= 10; //if unsuccessful, use a larger fudge factor*/
            else {
                λ *= 0.1; //if unsuccessful, use a larger fudge factor
                //likelihood = likelihood(uSquaredArray, parameters, λ);
            }
            epoch++;
            //System.out.println(likelihood);
            //System.out.println(Arrays.toString(trialParameters));
            if(parameters[1]+parameters[2] >= 1.0)
                break;
        }
        System.out.println(likelihood);
        return parameters;
    }

    private double likelihood(double[] uSquaredArray, BigDecimal parameters[], double λ){
        BigDecimal omega    = parameters[0];
        BigDecimal alpha    = parameters[1]; //not to be confused with the alpha matrix in LevenbergMarquardt
        BigDecimal beta     = parameters[2]; //not to be confused with the beta vector in LevenbergMarquardt
        int numTuple = uSquaredArray.length;

        //initialize derivatives of likelihood
        BigDecimal dOmega       = BigDecimal.valueOf(0.0);
        BigDecimal dAlpha       = BigDecimal.valueOf(0.0);
        BigDecimal dBeta        = BigDecimal.valueOf(0.0);

        BigDecimal dOmegadOmega = BigDecimal.valueOf(0.0);
        BigDecimal dAlphadAlpha = BigDecimal.valueOf(0.0);
        BigDecimal dBetadBeta   = BigDecimal.valueOf(0.0);

        BigDecimal dOmegadAlpha = BigDecimal.valueOf(0.0);
        BigDecimal dOmegadBeta  = BigDecimal.valueOf(0.0);
        BigDecimal dAlphadBeta  = BigDecimal.valueOf(0.0);

        //calculate initial variance
        BigDecimal variance = new BigDecimal(uSquaredArray[numTuple-1]);
        //calculate initial likelihood
        double log = -Math.log(uSquaredArray[numTuple-1]) - 1;
        BigDecimal likelihood = new BigDecimal(log);
        for(int i = 0; i < (numTuple-1); i++) {
            BigDecimal uSquared = new BigDecimal(uSquaredArray[numTuple-1 - i]);
            BigDecimal newVariance = omega.add(alpha.multiply(uSquared)).add(beta.multiply(variance));
            //omega + alpha.multiply(uSquared) + beta.multiply(variance);
            //log = -Math.log(newVariance)

            BigDecimal bdLog = new BigDecimalMath().log(newVariance).multiply(new BigDecimal(-1));
            likelihood.add(bdLog).subtract(uSquared.divide(newVariance));
            //likelihood += -BigDecimalMath.log.(newVariance) - (uSquared)/newVariance;
            //calculate derivatives
        /*dOmega          += -1*(1/newVariance)                              + (uSquared/Math.pow(newVariance,2));
        dAlpha          += -1*(uSquared/newVariance)                       + (Math.pow(uSquared,2)/Math.pow(newVariance,2));
        dBeta           += -1*(variance/newVariance)                       + ((uSquared*variance)/Math.pow(newVariance,2));*/
            dOmega
                    .add(new BigDecimal(-1).divide(newVariance))
                    .add(uSquared.divide(newVariance.multiply(newVariance)));
            dAlpha
                    .add(new BigDecimal(-1).multiply(uSquared).divide(newVariance))
                    .add(uSquared.multiply(variance).divide(newVariance.multiply(newVariance)));
            dBeta
                    .add(new BigDecimal(-1).multiply(variance).divide(newVariance))
                    .add(uSquared.multiply(variance).divide(newVariance.multiply(newVariance)));


        /*    dOmegadOmega    += (1/Math.pow(newVariance,2))                     - ((2 * uSquared)/Math.pow(newVariance,3));
            dAlphadAlpha    += (Math.pow(uSquared,2)/Math.pow(newVariance,2))  - ((2 * Math.pow(uSquared,3))/Math.pow(newVariance,3));
            dBetadBeta      += (Math.pow(variance,2)/Math.pow(newVariance,2))  - ((2 * uSquared * Math.pow(variance,2))/Math.pow(newVariance,3));*/

            dOmegadOmega
                    .add(new BigDecimal(1).divide(newVariance.multiply(newVariance)))
                    .subtract((new BigDecimal(2).multiply(uSquared)).divide(newVariance.multiply(newVariance.multiply(newVariance))));
            dAlphadAlpha
                    .add((uSquared.multiply(uSquared)).divide(newVariance.multiply(newVariance)))
                    .subtract((new BigDecimal(2).multiply(uSquared.multiply(uSquared.multiply(uSquared)))).divide(newVariance.multiply(newVariance.multiply(newVariance))) );

            

            dOmegadAlpha    += (uSquared/Math.pow(newVariance,2))              - ((2 * Math.pow(uSquared,2))/Math.pow(newVariance,3));
            dOmegadBeta     += (variance/Math.pow(newVariance,2))              - ((2 * uSquared*variance)/Math.pow(newVariance,3));
            dAlphadBeta     += ((uSquared*variance)/Math.pow(newVariance,2))   - ((2 * Math.pow(uSquared,2) * variance)/Math.pow(newVariance,3));

            variance = newVariance;
        }
        double[] vectorBeta = {-1*dOmega, -1*dAlpha, -1*dBeta};
        this.vectorBeta = vectorBeta;
        //populate curvature matrix
        double[][] curvatureMatrix =    {   {0.5*dOmegadOmega*(1+λ) , 0.5*dOmegadAlpha      , 0.5*dOmegadBeta}
                ,   {0.5*dOmegadAlpha       , 0.5*dAlphadAlpha*(1+λ), 0.5*dAlphadBeta}
                ,   {0.5*dOmegadBeta        , 0.5*dAlphadBeta       , 0.5*dBetadBeta*(1+λ)}};
        this.curvatureMatrix = curvatureMatrix;
        return likelihood;
    }

    public double getGARCH11(){
        //Generalized Autoregressive Conditional Heteroskedastic Process
        int numTuple =  xStock.length;
        //uSquaredArray = array of returns
        double uSquaredArray[] = new double [numTuple-1];
        for (int i = 0; i < (numTuple-1); i++) {
            double ratioU = (xStock[i + 1] - xStock[i]) / xStock[i];
            double ratioV = (yStock[i + 1] - yStock[i]) / yStock[i];
            uSquaredArray[i] = ratioU*ratioV;
        }
        //OPTIMISE PARAMETERS VIA LevenbergMarquardt algorithm
        double parameters[] = LevenBergMarquardt(uSquaredArray);
        System.out.println(Arrays.toString(parameters));
        double omega = parameters[0];
        double alpha = parameters[1];
        double beta = parameters[2];
        //calculate long term variance
        double sigma = omega/(1-alpha-beta);
        //return long term volatility
        return Math.sqrt(sigma);
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

