package VaR;

/**
 * Created by Adrian on 05/08/2017.
 */
public class PortfolioInfo {

    public static double print(Parameters p, double[][] stockPrices, optionsData[] options){
        System.out.println("=========================================================================");
        System.out.println("PortfolioInfo.java");
        System.out.println("=========================================================================");
        int numSym = p.getNumSym();
        //initialize arrays
        double[] currentStockPrices = new double[numSym];
        double[][] strikePrices = new double[numSym][];
        double[][] currentPutPrices = new double[numSym][];
        int[] daystoMaturity = new int[numSym];
        // get Parameters
        String[] symbol = p.getSymbol();
        int[] stockDelta = p.getStockDelta();
        int[] optionDelta = p.getOptionsDelta();
        //get Options data
        for (int i = 0; i < numSym; i++) {
            strikePrices[i] = options[i].getStrikePrices();
            daystoMaturity[i] = options[i].getDaystoMaturity();
            currentPutPrices[i] = options[i].getPutPrices();
        }
        /**
         * CALCULATE PERCENTAGE CHANGE IN STOCK PRICE
         */
        double[][] priceChanges = new Stats(stockPrices).getPercentageChanges();
        double currentValue = 0;
        /** LOOP THROUGH EACH STOCK*/
        for (int i = 0; i < numSym; i++) {
            int numPuts = currentPutPrices[i].length;
            currentStockPrices[i] = stockPrices[i][0];
            System.out.println("\t" + symbol[i]);
            /**PRINT STOCK VARIABLES*/
            System.out.println("\t\tStock Variables:");
            System.out.println("\t\t\tDelta:\t\t\t\t"       + stockDelta[i]);
            System.out.println("\t\t\tCurrent Price:\t\t"   + currentStockPrices[i]);
            System.out.println("\t\t\tValue:\t\t\t\t"       + stockDelta[i]*currentStockPrices[i]);
            System.out.printf("\t\t\tMean Price Change:\t" + "%.4f", new Stats(priceChanges[i]).getMean());
            /**PRINT OPTIONS VARIABLES*/
            System.out.println("\n\t\tPut Variables:");
            System.out.println("\t\t\tDelta:\t\t\t\t"       + optionDelta[i]);
            System.out.println("\t\t\tCurrent Price:\t\t"   + currentPutPrices[i][numPuts-1]);
            System.out.println("\t\t\tValue:\t\t\t\t"       + optionDelta[i]*currentPutPrices[i][numPuts-1]);
            System.out.println("\t\t\tStrike:\t\t\t\t"      + strikePrices[i][numPuts-1]);
            System.out.println("\t\t\tDays To Maturity:\t"  + daystoMaturity[i]);
            /** SUM TO CURRENT VALUE*/
            currentValue += stockDelta[i] * currentStockPrices[i] + optionDelta[i] * currentPutPrices[i][numPuts-1];
        }
        System.out.println("\n\tCurrent Portfolio Value:\t" + currentValue);
        return currentValue;
    }
}
