package VaR;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Adrian on 23/08/2017.
 */
public class Results {

    //INSTANCE VARIABLES
    private HashMap<String, Double> varHash = new HashMap();
    private double  currentValue;

    //GETTERS
    public double getVarANEW(){
        return this.varHash.get("Analytical EW");
    }
    public double getVarANEWMA(){
        return this.varHash.get("Analytical EWMA");
    }
    public double getVarANGARCH(){
        return this.varHash.get("Analytical GARCH");
    }
    public double getVarHistorical(){
        return this.varHash.get("Historical");
    }
    public double getVarMCEW(){
        return this.varHash.get("Monte Carlo EW");
    }
    public double getVarMCEWMA(){
        return this.varHash.get("Monte Carlo EWMA");
    }
    public double getVarMCGARCH(){
        return this.varHash.get("Monte Carlo GARCH");
    }
    public double getCurrentValue(){
        return this.currentValue;
    }

    //SETTERS
    public void setVarANEW(double varANEW){
        this.varHash.put("Analytical EW", varANEW);
    }
    public void setVarANEWMA(double varANEWMA){
        this.varHash.put("Analytical EWMA", varANEWMA);
    }
    public void setVarANGARCH(double varANGARCH){
        this.varHash.put("Analytical GARCH", varANGARCH);
    }
    public void setVarHistorical(double varHistorical){
        this.varHash.put("Historical", varHistorical);
    }
    public void setVarMCEW(double varMCEW){
        this.varHash.put("Monte Carlo EW", varMCEW);
    }
    public void setVarMCEWMA(double varMCEWMA){
        this.varHash.put("Monte Carlo EWMA", varMCEWMA);
    }
    public void setVarMCGARCH(double varMCGARCH){
        this.varHash.put("Monte Carlo GARCH", varMCGARCH);
    }
    public void setCurrentValue(double currentValue){
        this.currentValue = currentValue;
    }


    public void OutputCSV(Parameters p, ArrayList<BackTestData> ArrayListBT) throws IOException{
        BufferedWriter br = new BufferedWriter(new FileWriter(p.getOutputPath() + "rawData.csv"));
        StringBuilder sb = new StringBuilder();
        String header = "Data (years),Time Horizon (days),Confidence Level,Portfolio Value,Measure,VaR,Significance Level,Coverage Test,Lower Interval,Upper Interval,Violations,Reject";

        sb.append(header);
        sb.append("\n");
        for(BackTestData BT : ArrayListBT){
            String Measure = BT.getMeasure();
            double VaR = varHash.get(Measure);
            double epsilon = BT.getepsilon();
            String Coverage = BT.getCoverage();
            int lower = BT.getLower();
            int upper = BT.getUpper();
            int violations = BT.getViolations();
            boolean reject = BT.getReject();

            sb.append(p.getDataYears() + ",");
            sb.append(p.getTimeHorizon() + ",");
            sb.append(p.getConfidenceLevel() + ",");
            sb.append(currentValue + ",");
            sb.append(Measure + ",");
            sb.append(VaR + ",");
            sb.append(epsilon + ",");
            sb.append(Coverage + ",");
            sb.append(lower + ",");
            sb.append(upper + ",");
            sb.append(violations + ",");
            sb.append(reject + ",");
            sb.append("\n");
        }
        br.write(sb.toString());
        br.close();
        
    }

}
