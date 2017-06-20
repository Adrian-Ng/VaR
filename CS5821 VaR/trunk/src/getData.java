import jdk.internal.util.xml.impl.Input;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.Interval;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVParser;
import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;


/**
 * Created by Adrian on 17/06/2017.
 */


public class getData {




    public static void main(String args[]) throws IOException{
        // TAKE USER INPUT AND SPLIT IT
        String[] symbols = args[0].split("\\|");

        //INITIALIZE VARIABLES
        Calendar from = Calendar.getInstance();
        from.add(Calendar.YEAR,-5); // from 5 years ago

        String fromstr = new SimpleDateFormat("MMM+dd,+yyyy").format(from.getTimeInMillis());
        fromstr = fromstr.replace(",","%2C");

        System.out.println("Fetching Stocks:");
        for (String sym : symbols) {
            System.out.println("\t" + sym); // debugging
            //SET urlstr
            String urlstr = "http://www.google.com/finance/historical?q=" + sym + "&startdate=" + fromstr + "&output=csv";

            System.out.println(urlstr);

            InputStream is = new URL(urlstr).openStream();
            Reader rdr = new InputStreamReader(is, "UTF-8");
            BufferedReader in = new BufferedReader(rdr);

            String inputLine;
            while ((inputLine = in.readLine()) != null)
                System.out.println(inputLine);
            is.close();


        }

        

    }
}
