package VaR;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Created by Adrian on 24/08/2017.
 */
public class Parameters {

    //INSTANCE VARIABLES
    private String[] Symbol;
    private int[] stockDelta;
    private int[] optionsDelta;
    private int dataYears;
    private int timeHorizon;
    private double confidenceLevel;
    //GETTERS
    public String[] getSymbol(){
        return this.Symbol;
    }
    public int[] getStockDelta(){return this.stockDelta;}
    public int[] getOptionsDelta(){return this.optionsDelta;}
    public int getDataYears(){
        return this.dataYears;
    }
    public double getTimeHorizon(){
        return this.timeHorizon;
    }
    public double getConfidenceLevel(){
        return this.confidenceLevel;
    }
    //SETTERS
    public void setSymbol(String strSymbol){
        this.Symbol = strSymbol.split("\\|");
    }//SPLIT PIPE DELIMITED LIST OF SYMBOLS
    public void setStockDelta(String strStockDelta){
        //SPLIT PIPE DELIMITED LIST OF STOCK DELTA
        String[] splitStockDelta = strStockDelta.split("\\|");
        int[] stockDelta = new int[splitStockDelta.length];
        for(int i = 0; i< stockDelta.length;i++)
            stockDelta[i] = Integer.parseInt(splitStockDelta[i]);
        this.stockDelta = stockDelta;
    }
    public void setOptionsDelta(String strOptionsDelta){
        //SPLIT PIPE DELIMITED LIST OF OPTIONS DELTA
        String[] splitOptionDelta = strOptionsDelta.split("\\|");
        int[] optionDelta = new int[splitOptionDelta.length];
        for(int i = 0; i< optionDelta.length;i++)
            optionDelta[i] = Integer.parseInt(splitOptionDelta[i]);
        this.optionsDelta = optionDelta;
    }
    public void setTimeHorizon(String strTimeHorizon){
        this.timeHorizon = Integer.parseInt(strTimeHorizon);
    }
    public void setDataYears(String strDataYears){
        this.dataYears = Integer.parseInt(strDataYears);
    }
    public void setConfidenceLevel(String strConfidenceLevel){
        this.confidenceLevel = Double.parseDouble(strConfidenceLevel);
    }

    //INSTANCE METHODS
    public int getSumOptions(){
        int sum = 0;
        for(int i = 0; i < optionsDelta.length; i++)
            sum += optionsDelta[i];
        return sum;
    }
    public int getNumSym(){
        return Symbol.length;
    }
    public String getOutputPath(){
        //format date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime Date = LocalDateTime.now();
        String dateStr = Date.format(formatter);
        //Prepare directory if there are options
        String strOptions;
        if(getSumOptions() == 0)
            strOptions = "";
        else
            strOptions = " - Options";
        //make directory for output
        String outputPath = dateStr
                + " VaR - "
                + Arrays.toString(getSymbol())
                + strOptions + " - "
                + getDataYears()
                + " years - "
                + getTimeHorizon()
                + " day horizon - "
                + getConfidenceLevel() + " confidence" +  "/";
        new File(outputPath).mkdir();
        //Excel doesn't like it when you open up files of the same name. So we prefix with a unique hash!
        outputPath = outputPath + outputPath.hashCode() + " - ";
        return outputPath;
    }
}
