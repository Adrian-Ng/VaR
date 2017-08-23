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
    private double  currentValue
                ,   confidenceLevel;
    private int     dataYears
                ,   timeHorizon;
    //GETTERS
    public double getVarStDev(){
        return this.varHash.get("StDev");
    }

    public double getVarEWMA(){
        return this.varHash.get("EWMA");
    }

    public double getVarGARCH(){
        return this.varHash.get("GARCH");
    }

    public double getVarMonteCarlo(){
        return this.varHash.get("Monte Carlo");
    }
    public double getVarHistorical(){
        return this.varHash.get("Historical");
    }
    public double getCurrentValue(){
        return this.currentValue;
    }
    public double getDataYears(){
        return this.dataYears;
    }
    public double getTimeHorizon(){
        return this.timeHorizon;
    }
    public double getConfidenceLevel(){
        return this.timeHorizon;
    }
    //SETTERS
    public void setVarStDev(double varStDev){
        this.varHash.put("StDev", varStDev);
    }

    public void setVarEWMA(double varEWMA){
        this.varHash.put("EWMA", varEWMA);
    }
    public void setVarGARCH(double varGARCH){
        this.varHash.put("GARCH", varGARCH);
    }
    public void setVarMonteCarlo(double varMonteCarlo){
        this.varHash.put("Monte Carlo", varMonteCarlo);
    }
    public void setVarHistorical(double varHistorical){
        this.varHash.put("Historical", varHistorical);
    }
    public void setCurrentValue(double currentValue){
        this.currentValue = currentValue;
    }
    public void setConfidenceLevel(double confidenceLevel){
        this.confidenceLevel = confidenceLevel;
    }
    public void setDataYears(int dataYears){
        this.dataYears = dataYears;
    }
    public void setTimeHorizon(int timeHorizon){
        this.timeHorizon = timeHorizon;
    }

    public void OutputCSV(ArrayList<BackTestData> ArrayListBT, String relativePath) throws IOException{
        BufferedWriter br = new BufferedWriter(new FileWriter(relativePath + "rawData.csv"));
        StringBuilder sb = new StringBuilder();
        String header = "Data (years),Time Horizon (days),Confidence Level,Portfolio Value,Measure,VaR,Significance Level,Coverage Test,Lower Interva,Upper Interval,Violations,Reject";

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

            sb.append(dataYears + ",");
            sb.append(timeHorizon + ",");
            sb.append(confidenceLevel + ",");
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
