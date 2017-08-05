package VaR;

/**
 * Created by Adrian on 05/08/2017.
 */
public class PortfolioInfo {

    public static void print(String[] symbol, double[][] stockPrices, int[] stockDelta, optionsData[] options, int[] optionDelta){
        System.out.println("=========================================================================");
        System.out.println("PortfolioInfo.java");
        System.out.println("=========================================================================");
        int numSym = symbol.length;
        double[] currentStockPrices = new double[numSym];
        double[][] strikePrices = new double[numSym][];
        double[][] currentPutPrices = new double[numSym][];
        long[] daystoMaturity = new long[numSym];
        for (int i = 0; i < numSym; i++) {
            strikePrices[i] = options[i].getStrikePrices();
            daystoMaturity[i] = options[i].getDaystoMaturity();
            currentPutPrices[i] = options[i].getPutPrices();
        }
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
            /**PRINT OPTIONS VARIABLES*/
            System.out.println("\t\tPut Variables:");
            System.out.println("\t\t\tDelta:\t\t\t\t"       + optionDelta[i]);
            System.out.println("\t\t\tCurrent Price:\t\t"   + currentPutPrices[i][numPuts-1]);
            System.out.println("\t\t\tValue:\t\t\t\t"       + optionDelta[i]*currentPutPrices[i][numPuts-1]);
            System.out.println("\t\t\tStrike:\t\t\t\t"      + strikePrices[i][numPuts-1]);
            System.out.println("\t\t\tDays To Maturity:\t"  + daystoMaturity[i]);
            /** SUM TO CURRENT VALUE*/
            currentValue += stockDelta[i] * currentStockPrices[i] + optionDelta[i] * currentPutPrices[i][numPuts-1];
        }
        System.out.println("\n\tCurrent Portfolio Value:\t" + currentValue);
    }
}
