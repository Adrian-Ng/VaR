package VaR;

/**
 * Created by Adrian on 23/08/2017.
 */
public class BackTestData {

    //INSTANCE VARIABLES
    private String Measure;
    private double epsilon;
    private String Coverage;
    private int lower;
    private int upper;
    private int violations;
    private boolean reject;

    //GETTERS
    public String getMeasure(){
        return this.Measure;
    }
    public double getepsilon(){
        return this.epsilon;
    }
    public String getCoverage(){
        return this.Coverage;
    }
    public int getLower(){
        return this.lower;
    }
    public int getUpper(){
        return this.upper;
    }
    public int getViolations(){
        return this.violations;
    }
    public boolean getReject(){
        return this.reject;
    }
    //SETTERS
    public void setMeasure(String Measure){
        this.Measure = Measure;
    }
    public void setEpsilon(double epsilon){
        this.epsilon = epsilon;
    }
    public void setCoverage(String Coverage){
        this.Coverage = Coverage;
    }
    public void setLower(int lower){ this.lower = lower; }
    public void setUpper(int upper) {this.upper = upper; }
    public void setViolations(int violations){this.violations = violations;}
    public void setReject(boolean reject) {this.reject = reject; }


}
