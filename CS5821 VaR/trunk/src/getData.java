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

    public static void parseCSV(String sym, String fromstr) throws IOException{
        //SET urlstr
        String urlstr = "http://www.google.com/finance/historical?q=" + sym + "&startdate=" + fromstr + "&output=csv";
        System.out.println("\t\t" + urlstr);
        InputStream is = new URL(urlstr).openStream();
        //https://stackoverflow.com/questions/4120942/programatically-downloading-csv-files-with-java
        BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        writeCSV(sym, in);
        is.close();
        //https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
    }

    public static void writeCSV(String sym, BufferedReader in) throws IOException{
        //DEFINE SOME NAMING CONVENTION FOR OUTPUT CSV FILE
        String csv = sym + ".csv";
        //CREATE NEW FILE
        FileWriter writer = new FileWriter(csv);
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            String[] strings = inputLine.split(",");
            StringWriter stringWriter = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.EXCEL);
            csvPrinter.printRecord(strings);
            writer.write(stringWriter.toString());
            //http://www.programcreek.com/java-api-examples/index.php?api=org.apache.commons.csv.CSVPrinter
        }
        writer.flush();
        System.out.println("\t\tCSV file '" + csv + "' generated");
    }

    public static void main(String args[]) throws IOException{
        // TAKE USER INPUT AND SPLIT IT
        String[] symbols = args[0].split("\\|");
        // NUMBER OF YEARS TO LOOK AT
        int yearint = Integer.parseInt(args[1]);
        //INITIALIZE VARIABLES
        Calendar from = Calendar.getInstance();
        from.add(Calendar.YEAR,-yearint); // from n years ago

        String fromstr = new SimpleDateFormat("MMM+dd,+yyyy").format(from.getTimeInMillis());
        //CONVERT COMMA TO UNICODE
        fromstr = fromstr.replace(",","%2C");

        System.out.println("Fetching Historic Stock Data from " + yearint + " year(s) ago:");
        for (String sym : symbols) {
            System.out.println("\n\t" + sym); // debugging
                parseCSV(sym, fromstr);
        }
    }
}
