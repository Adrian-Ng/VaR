import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;

import java.io.IOException;
import java.util.Calendar;
import java.util.Map;


/**
 * Created by Adrian on 17/06/2017.
 */


public class getData {

    public static void main(String args[]) throws IOException{

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR,-5); // from 5 years ago




        String[] symbols = args[0].split("\\|");

        System.out.println("Fetching Stocks:");
        for (String sym : symbols)
            System.out.println("\t" + sym);

        Map<String, Stock> stocks = YahooFinance.get(symbols,from,to, Interval.WEEKLY);

        for (String stck : symbols)
            System.out.println(stocks.get(stck));



/*
        Stock stock = YahooFinance.get("GOOG");
        System.out.println(stock.getHistory());

*/

    }
}
