import org.apache.commons.csv.CSVFormat;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Map;


/**
 * Created by Adrian on 17/06/2017.
 */


public class getData {

    public static void main(String args[]) throws IOException{

        StringWriter stringWriter = new StringWriter();
        CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.EXCEL);


        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR,-5); // from 5 years ago

        String[] symbols = args[0].split("\\|");

        System.out.println("Fetching Stocks:");
        for (String sym : symbols)
            System.out.println("\t" + sym);

        Map<String, Stock> stocks = YahooFinance.get(symbols,from,to, Interval.WEEKLY);

        for (String stck : symbols) {
            System.out.println(stocks.get(stck).getHistory());
            String csv = "test " + stck + ".csv";
            csvPrinter.printRecord(stocks.get(stck).getHistory());

            FileWriter writer = new FileWriter(csv);
            writer.write(stringWriter.toString());
            writer.flush();
        }


/*
        Stock stock = YahooFinance.get("GOOG");
        System.out.println(stock.getHistory());

*/

    }
}
