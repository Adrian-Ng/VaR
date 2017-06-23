package VaR;

/**
 * Created by Adrian on 22/06/2017.
 */
public class myData {
    //INSTANCE VARIABLES
    private String date;
    private double stock[] = new double[4];
    private int vol;

    //GETTERS
    public String getDate(){
        return this.date;
    }

    public double getStock(int i){
        return this.stock[i];
    }

    public int getVolume(){
        return this.vol;
    }


    //SETTERS
    public void setDate(String inDate){
        this.date = inDate;
    }

    public void setStock(int i, double inStock){
        this.stock[i] = inStock;
    }

    public void setVol(int inVol){
        this.vol = inVol;
    }

}
