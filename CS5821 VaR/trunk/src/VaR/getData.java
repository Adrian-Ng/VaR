package VaR;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import java.io.*;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


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
        in.readLine();//SKIP HEADER
        while ((inputLine = in.readLine()) != null) {
            String[] strTuple = inputLine.split(",");
            //System.out.println(Arrays.toString(strTuple));
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
        System.out.println("=========================================================================");
        System.out.println("getData.java");
        System.out.println("=========================================================================");
        /**
         * 1st Argument: Stock Symbols
         * 2nd Argument: Stock Deltas
         * 3rd Argument: Number of Days of Data
         * 4th Argument: Time Horizon
         * 5th Argument: Confidence Level
         */
        //SPLIT PIPE DELIMITED LIST OF STOCK SYMBOLS
        String[] symbols = args[0].split("\\|");
        int numSym = symbols.length;
        //SPLIT PIPE DELIMITED LIST OF STOCK DELTA
        String[] strStockDelta = args[1].split("\\|");
        int[] stockDelta = new int[strStockDelta.length];
        for(int i = 0; i< strStockDelta.length;i++)
            stockDelta[i] = Integer.parseInt(strStockDelta[i]);
        // NUMBER OF DAYS IN THE PAST TO LOOK AT
        int intDays = Integer.parseInt(args[2]);
        // TIME HORIZON
        int timeHorizonN = Integer.parseInt(args[3]);
        // CONFIDENCE INTERVAL
        double confidenceX = Double.parseDouble(args[4]);

        double[][] stockPrices = new double[numSym][intDays];


        System.out.println("\tFetching VaR.Historic Stock Data from " + intDays + " working day(s) ago:");
        //GET FINANCIAL DATA FOR EACH SYMBOL
        for (int i = 0; i < numSym; i++) {
            System.out.println("\t" + symbols[i]); // debugging
            int size;
            int incrementDays = intDays;
            //DO THIS UNTIL WE GET THIS RIGHT NUMBER OF ROWS OF DATA!
            do {
                String fromStrAPI = CalculateDateFromIntDays(incrementDays);
                //SET urlStrAPI
                String urlStrAPI = "http://www.google.com/finance/historical?q=" + symbols[i] + "&startdate=" + fromStrAPI + "&output=csv";
                BufferedReader in = parseCSV(symbols[i], urlStrAPI);
                //writeCSV(sym, in); //FOR DEBUGGING
                ArrayList<Double> alData = setData(in);
                in.close();
                size = alData.size();
                //System.out.println(size);
                if (size >= intDays) {
                    System.out.println("\t\tRetrieved " + size + " rows of data");
                    System.out.println("\t\t" + urlStrAPI);
                    //CONVERT ARRAY LIST DOUBLE TO ARRAY DOUBLE
                    for(int j = 0; j < intDays; j++)
                        stockPrices[i][j] = alData.get(j);
                    break;
                }
                incrementDays++;
            }while(size <= intDays);
        }
        Analytical.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        Linear.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        MonteCarlo.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        Historic.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
        //BackTest.main(symbols, stockPrices,stockDelta, timeHorizonN, confidenceX);
    }
}
