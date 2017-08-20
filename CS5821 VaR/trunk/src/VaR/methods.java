package VaR;
import org.apache.commons.math3.linear.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class methods {
    //instance variable
    private double[] singleStock;
    private double[] xStock;
    private double[] yStock;
    private double[][] multiStock;
    //constructor
    public methods(double[] singleStock)
    {
        this.singleStock = singleStock;
    }
    public methods(double[] xStock, double[] yStock){
        this.xStock = xStock;
        this.yStock = yStock;
    }
    public methods(double[][] multiStock)
    {
        this.multiStock = multiStock;
    }

    public double getMean(){
        int numTuple = singleStock.length;
        double sum = 0.0;
        for(int i = 0; i < numTuple; i++)
            sum += singleStock[i];
        return sum/numTuple;
    }

    public double getEWMAVariance(){
        double lambda = 0.94;
        int numTuple = xStock.length;
        double EWMA = xStock[numTuple-1]*yStock[numTuple-1];
        for (int i = 1; i < numTuple;i++)
            EWMA = lambda * EWMA + (1-lambda) * xStock[numTuple-1 - i]*yStock[numTuple-1 - i];
        return EWMA;
    }
    public double getEWMAVolatility(){
               return Math.sqrt(getEWMAVariance());
    }
    public double getVariance(){
        int numTuple =  singleStock.length;
        double sum = 0;
        for (int i = 0; i< numTuple; i++)
            sum += Math.pow((singleStock[i]),2);
        return sum/(numTuple-1);
    }
    public double getStandardDeviation(){
        return Math.sqrt(getVariance());
    }
    public double getCovariance(){
        int numTuples = xStock.length;
        //Calculation
        double sum = 0;
        for (int i = 0; i < numTuples; i++)
            sum +=(xStock[i])*(yStock[i]);
        return sum/(numTuples-1);
    }
    public double[][] getCovarianceMatrix(){
        int numSym = multiStock.length;
        double[][] covarianceMatrix = new double[numSym][numSym];
        for(int i = 0; i < numSym; i++)
            for(int j = 0;j < numSym; j++)
                covarianceMatrix[i][j] = new methods(multiStock[i], multiStock[j]).getCovariance();
        return covarianceMatrix;
    }
    public double[][] getCholeskyDecomposition(){
        double[][] covarianceMatrix = new methods(multiStock).getCovarianceMatrix();
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
                priceDiff[i][j] = ((multiStock[i][j]-multiStock[i][j+1])/multiStock[i][j+1]);
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
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDateTime Date = LocalDateTime.now();
    String dateStr = Date.format(formatter);
    public void printMatrixToCSV(String[] header, String title)throws IOException{
        //https://stackoverflow.com/questions/15364342/export-array-values-to-csv-file-java
        //https://stackoverflow.com/questions/34958829/how-to-save-a-2d-array-into-a-text-file-with-bufferedwriter
        int numCols = multiStock.length;
        int numRows = multiStock[0].length;
        BufferedWriter br = new BufferedWriter(new FileWriter(dateStr + " - " + title + ".csv"));
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < header.length ; i++){
            sb.append(header[i]);
            if (i < (header.length - 1))
                sb.append(",");
        }
        sb.append("\n");
        for(int i = 0; i < numRows;i++) {
            for (int j = 0; j < numCols; j++) {
                sb.append(multiStock[j][i] + "");
                if (j < (multiStock.length - 1))
                    sb.append(",");
            }
            sb.append("\n");
        }
        br.write(sb.toString());
        br.close();
    }
    public void printVectorToCSV(String header, String title) throws IOException{
        int numRows = singleStock.length;
        BufferedWriter br = new BufferedWriter(new FileWriter(dateStr + " - " + title + ".csv"));
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        for(int i = 0; i < numRows;i++) {
            sb.append(singleStock[i]);
            sb.append("\n");
        }
    }
    private int earlyexit = 0;
    private double λ;
    private double[] LevenBergMarquardt(double[] uSquaredArray){
/**
 * Numerical Recipes in C page 684
 * Given an initial guess for the set of fitted parameters a, the recommended Marquardt recipe is as follows:
 • Compute χ2(a) (= likelihood).
 • Pick a modest value for λ, say λ = 0.001.
 • (†) Solve the linear equations (15.5.14) for δa and evaluate χ2(a + δa).
 • If χ2(a + δa) ≥χ2(a), decrease λ by a factor of 10 (or any other substantial factor) and go back to (†).
 • If χ2(a + δa) < χ2(a), increase λ by a factor of 10, update the trial solution a ← a + δa, and go back to (†)
 */
        λ = 0.001; //non-dimensional fudge factor
        double[] parameters = new double[3];
        parameters[0] = 0.000001346;//omega
        parameters[1] = 0.08339;//alpha
        parameters[2] = 0.9101;//beta
        double likelihood = likelihood(uSquaredArray,parameters);
        while(true) {
            double[] trialParameters = getTrialParameters(parameters, uSquaredArray);
            double trialLikelihood = likelihood(uSquaredArray, trialParameters);
            if(earlyexit == 1) break;
            if (trialLikelihood > likelihood) {
                parameters = trialParameters;
                λ *= 0.1; //if successful, use a smaller fudge factor
                likelihood = trialLikelihood;
            }
            else λ *= 10; //if unsuccessful, use a larger fudge factor
        }
        return parameters;
    }
    private double[] getTrialParameters(double[] parameters, double[] uSquaredArray){
        double omega    = parameters[0];
        double alpha    = parameters[1]; //not to be confused with the alpha matrix in LevenbergMarquardt
        double beta     = parameters[2]; //not to be confused with the beta vector in LevenbergMarquardt
        int numTuple = uSquaredArray.length;
        //initialize derivatives of likelihood
        double dOmega       = 0.0;
        double dAlpha       = 0.0;
        double dBeta        = 0.0;
        //calculate variance
        double[] variance = new double[numTuple-1];
        variance[0] = uSquaredArray[0];
        for(int i = 1; i < variance.length; i++)
            variance[i] = omega + (alpha * uSquaredArray[i]) + (variance[i-1] * beta);
        for(int i = 1; i < variance.length; i++) {
            //calculate derivatives
          dOmega          += ((-1/variance[i])                             + (uSquaredArray[i]/Math.pow(variance[i],2)));
          dAlpha          += (-uSquaredArray[i]/variance[i])               + (Math.pow(uSquaredArray[i],2)/Math.pow(variance[i],2));
          dBeta           += (-variance[i-1]/variance[i])                  + ((uSquaredArray[i]*variance[i-1])/Math.pow(variance[i],2));
        }
        //populate vector Beta
        double[] vectorBeta = {-0.5*dOmega, 0.5*dAlpha, -0.5*dBeta};
        //System.out.println(Arrays.toString(vectorBeta));
        double[] trialParameters = new double[parameters.length];
        while(true) {
            //populate curvature matrix
            double[][] curvatureMatrix = {
                        {0.5*dOmega*dOmega * (1 + λ),   0.5*dOmega*dAlpha,              0.5*dOmega*dBeta}
                    ,   {0.5*dAlpha*dOmega,             0.5*dAlpha*dAlpha * (1 + λ),    0.5*dAlpha*dBeta}
                    ,   {0.5*dBeta*dOmega,              0.5*dBeta*dAlpha,               0.5*dBeta*dBeta * (1 + λ)}
            };
            //solve simultaneous equations to calculate deltaParameters
            RealMatrix coefficients = new Array2DRowRealMatrix(curvatureMatrix);
            DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
            RealVector constants = new ArrayRealVector(vectorBeta);
            RealVector solution = solver.solve(constants);
            //evaluate χ2(a + δa)
            //Stopping rule. If parameter changes are << 1 then stop.
            if ((solution.getEntry(0) + solution.getEntry(1) + solution.getEntry(2) ) < 0.00001){
                earlyexit = 1;
                break;
            }
            for (int i = 0; i < parameters.length; i++)
                trialParameters[i] = parameters[i] + solution.getEntry(i);
            if (trialParameters[0] < 0.0 //omega less than 0
                    || trialParameters[1] < 0.0 //alpha less than zero
                    || trialParameters[1] > 1.0 //alpha greater than 1
                    || trialParameters[2] < 0.0 //beta less than zero
                    || trialParameters[2] > (1 - trialParameters[1])
                    || trialParameters[1] + trialParameters[2] > 1
                    ) {
                λ *= 10; //use a larger fudge factor
                continue;
            }
            else
                break;
        }
        return trialParameters;
    }
    private double likelihood(double[] uSquaredArray, double parameters[]){
        double  omega    = parameters[0]
            ,   alpha    = parameters[1] //not to be confused with the alpha matrix in LevenbergMarquardt
            ,   beta     = parameters[2]; //not to be confused with the beta vector in LevenbergMarquardt
        int numTuple = uSquaredArray.length;
        //calculate variance
        double[] variance = new double[numTuple-1];
        variance[0] = uSquaredArray[0];
        for(int i = 1; i < variance.length; i++)
            variance[i] = omega + (alpha * uSquaredArray[i]) + (variance[i-1]* beta);
        //initialise likelihood
        double likelihood = 0;
        for(int i = 0; i < variance.length; i++)
            likelihood += -Math.log(variance[i]) - (uSquaredArray[i+1]/variance[i]);
        return likelihood;
    }
    public double getGARCH11Variance(){
        //Generalized Autoregressive Conditional Heteroskedastic Process
        int numTuple =  xStock.length;
        //uSquaredArray = array of returns
        double uSquared[] = new double [numTuple-1];
        for (int i = 0; i < (numTuple-1); i++)
            uSquared[uSquared.length - 1 - i] = xStock[i] * yStock[i];
        double[] parameters = LevenBergMarquardt(uSquared);
        double      omega = parameters[0]
                ,   alpha = parameters[1]
                ,   beta = parameters[2];
        double sigmaSquared = uSquared[0];
        for (int i = 1; i < uSquared.length
                ;i++)
            sigmaSquared = omega + (alpha*uSquared[i]) + (beta*sigmaSquared);
       return sigmaSquared;
    }
    public double getGARCH11Volatility(){
        return Math.sqrt(getGARCH11Variance());
    }

}

