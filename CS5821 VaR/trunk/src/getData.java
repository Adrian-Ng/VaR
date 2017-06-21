import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * THIS PROCESS WILL ACCESS THE GOOGLE FINANCE API AND DOWNLOAD FINANCIAL DATA IN CSV FORMAT
 * THE INPUT ARGUMENTS ARE:
 *      1)  STOCK CODES, PIPE DELIMITED
 *      2)  INTEGER REPRESENTING THE NUMBER OF YEARS OF HISTORICAL DATA TO ACCESS
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

            //DEFINE SOME NAMING CONVENTION FOR OUTPUT CSV FILE
            String csv = sym + ".csv";
            //CREATE NEW FILE
            FileWriter writer = new FileWriter(csv);

            //SET urlstr
            String urlstr = "http://www.google.com/finance/historical?q=" + sym + "&startdate=" + fromstr + "&output=csv";

            System.out.println(urlstr);

            InputStream is = new URL(urlstr).openStream();
            //https://stackoverflow.com/questions/4120942/programatically-downloading-csv-files-with-java
            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] strings = inputLine.split(",");
                StringWriter stringWriter = new StringWriter();
                CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.EXCEL);
                csvPrinter.printRecord(strings);
                writer.write(stringWriter.toString());
                //System.out.println(stringWriter);
            }
            is.close();
            writer.flush();
            //https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html

            System.out.println("CSV file '" + csv + "' generated");
        }



    }
}
