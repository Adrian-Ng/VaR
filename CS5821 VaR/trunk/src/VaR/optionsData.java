package VaR;

/**
 * Created by Adrian on 22/06/2017.
 */
public class optionsData {
    //INSTANCE VARIABLES
    private double[] callPrices;
    private double[] putPrices;
    private double[] strikePrices;
    private double[] daystoMaturity;
    //GETTERS
    public double[] getCallPrices(){
        return this.callPrices;
    }
    public double[] getPutPrices(){
        return this.putPrices;
    }
    public double[] getStrikePrices(){
        return this.strikePrices;
    }
    public double[] getDaystoMaturity(){
        return this.daystoMaturity;
    }
    //SETTERS
    public void setCallPrices(double[] callPrices){
        this.callPrices = callPrices;
    }
    public void setPutPrices(double[] putPrices){
        this.putPrices = putPrices;
    }
    public void setStrikePrices(double[] strikePrices){
        this.strikePrices = strikePrices;
    }
    public void setDaystoMaturity(double[] daystoMaturity){
        this.daystoMaturity = daystoMaturity;
    }
}
