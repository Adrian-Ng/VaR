package VaR;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.io.*;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * THIS PROCESS WILL ACCESS THE GOOGLE FINANCE API AND WRITE FINANCIAL DATA TO CSV
 * THE INPUT ARGUMENTS ARE:
 *      1)  STOCK CODES, PIPE DELIMITED
 *      2)  INTEGER REPRESENTING THE NUMBER OF YEARS OF HISTORICAL DATA TO ACCESS
 *
 * THE ATTRIBUTES OF THE DATA ARE
 *      ﻿Date,Open,High,Low,Close,Volume
 */

public class getData {



    public static BufferedReader parseCSV(String sym, String urlstr) throws IOException{
        InputStream is = new URL(urlstr).openStream();
        //https://stackoverflow.com/questions/4120942/programatically-downloading-csv-files-with-java
        BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        return in;
        //https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
    }

    public static ArrayList<Double> setData(BufferedReader in) throws IOException{
        String inputLine;
        ArrayList<Double> alData = new ArrayList<Double>();
        in.readLine();
        while ((inputLine = in.readLine()) != null) {
            String[] strTuple = inputLine.split(",");
            //myData thisTuple = new myData();
            //SET myData
            //thisTuple.setDate(strTuple[0]);                                //Date
            //thisTuple.setStock(0,Double.parseDouble(strTuple[1]));      //Open
            //thisTuple.setStock(1,Double.parseDouble(strTuple[2]));      //High
            //thisTuple.setStock(2,Double.parseDouble(strTuple[3]));      //Low
            //thisTuple.setStock(3,Double.parseDouble(strTuple[4]));      //Close
            //thisTuple.setVol(Integer.parseInt(strTuple[5]));               //Volume
            //SET ARRAYLIST
            alData.add(Double.parseDouble(strTuple[4]));
        }
        return alData;
    }
/*

    THIS THING WILL SAVE THE DATA TO A CSV FILE
    ONLY USED FOR DEBUGGING
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
        //System.out.println("\t\tCSV file '" + csv + "' generated");
    }
*/
    public static String CalculateDateFromIntDays(int intDays){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM+dd,+yyyy");
        int i = 0;
        int j = 0;
        while(i < intDays){
            if(LocalDateTime.now().minusDays(j).getDayOfWeek() == DayOfWeek.SATURDAY)
                i--;
             else if (LocalDateTime.now().minusDays(j).getDayOfWeek()== DayOfWeek.SUNDAY)
                i--;
            i++;
            j++;
        }
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(j);
        //https://stackoverflow.com/questions/22463062/how-to-parse-format-dates-with-localdatetime-java-8
        String fromstr = daysAgo.format(formatter);
        //CONVERT COMMA TO UNICODE
        fromstr = fromstr.replace(",","%2C");
        return fromstr;
    }


    public static void main(String args[]) throws IOException{
        HashMap<String, ArrayList<Double>> mapStocks = new HashMap<String, ArrayList<Double>>();
        // TAKE USER INPUT AND SPLIT IT
        String[] symbols = args[0].split("\\|");
        // NUMBER OF DAYS IN THE PAST TO LOOK AT
        int intDays = Integer.parseInt(args[1]);
        System.out.println("Fetching VaR.Historic Stock Data from " + intDays + " working day(s) ago:");
        for (String sym : symbols) {
            System.out.println("\n\t" + sym); // debugging
            int size;
            int decrementDays = intDays;
            //DO THIS UNTIL WE GET THIS RIGHT NUMBER OF ROWS OF DATA!
            do {
                String fromstr = CalculateDateFromIntDays(decrementDays);
                //SET urlstr
                String urlstr = "http://www.google.com/finance/historical?q=" + sym + "&startdate=" + fromstr + "&output=csv";
                BufferedReader in = parseCSV(sym, urlstr);
                //writeCSV(sym, in); //FOR DEBUGGING
                ArrayList<Double> alData = setData(in);
                in.close();
                size = alData.size() + 1; //ADD ONE TO ACCOMMODATE HEADER
                if (size == intDays) {
                    System.out.println("Retrieved " + size + " rows of data");
                    System.out.println("\t\t" + urlstr);
                    mapStocks.put(sym, alData);
                }
                decrementDays++;
            }while(size <= intDays);
        }
        Analytical.main(mapStocks);
        //MonteCarlo.main(mapStocks);
    }
}
