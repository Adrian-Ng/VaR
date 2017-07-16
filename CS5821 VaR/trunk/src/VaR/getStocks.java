package VaR;

import java.io.*;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


/**
 * THIS PROCESS WILL ACCESS THE GOOGLE FINANCE API
 * THE INPUT ARGUMENTS ARE:
 * 1st Argument: Stock Symbols
 * 2nd Argument: Stock Deltas
 * 3rd Argument: Number of Days of Data
 * 4th Argument: Time Horizon
 * 5th Argument: Confidence Level

 *
 * THE ATTRIBUTES OF THE DATA ARE
 *      ﻿Date,Open,High,Low,Close,Volume
 *      We only use Close
 */

public class getStocks {

    public static BufferedReader getCSVfromURL(String urlstr) throws IOException{
        InputStream is = new URL(urlstr).openStream();
        //https://stackoverflow.com/questions/4120942/programatically-downloading-csv-files-with-java
        BufferedReader csv = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        return csv;
        //https://docs.oracle.com/javase/tutorial/networking/urls/readingURL.html
    }

    public static ArrayList<Double> parseCSV(BufferedReader in) throws IOException{
        String inputLine;
        ArrayList<Double> alData = new ArrayList<Double>();
        in.readLine();//SKIP HEADER
        while ((inputLine = in.readLine()) != null) {
            String[] strTuple = inputLine.split(",");
            /**Col 0: Date
             * Col 1: Open
             * Col 2: High
             * Col 3: Low
             * Col 4: Close
             * Col 5: Volume
             */
            alData.add(Double.parseDouble(strTuple[4]));
        }
        return alData;
    }

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

    public static double[][] main(String[] symbols, int intDays) throws IOException{
        System.out.println("=========================================================================");
        System.out.println("getStocks.java");
        System.out.println("=========================================================================");
        int numSym = symbols.length;
        //TO BE POPULATED
        double[][] stockPrices = new double[numSym][intDays];
        System.out.println("\tFetching VaR.Historic Stock Data from " + intDays + " working day(s) ago:");
        //GET STOCK DATA FOR EACH SYMBOL
        for (int i = 0; i < numSym; i++) {
            System.out.println("\t" + symbols[i]); // debugging
            int size;
            int incrementDays = intDays;
            //DO THIS UNTIL WE GET THIS RIGHT NUMBER OF ROWS OF DATA!
            do {
                String fromStrAPI = CalculateDateFromIntDays(incrementDays);
                //SET urlStrAPI
                String urlStrAPI = "http://www.google.com/finance/historical?q=" + symbols[i] + "&startdate=" + fromStrAPI + "&output=csv";
                BufferedReader csv = getCSVfromURL(urlStrAPI);
                //writeCSV(sym, in); //FOR DEBUGGING
                ArrayList<Double> alData = parseCSV(csv);
                csv.close();
                size = alData.size();
                //System.out.println(size);
                if (size >= intDays) {
                    System.out.println("\t\tRetrieved " + size + " rows of Stock data");
                    System.out.println("\t\t" + urlStrAPI);
                    //CONVERT ARRAY LIST DOUBLE TO ARRAY DOUBLE
                    for(int j = 0; j < intDays; j++)
                        stockPrices[i][j] = alData.get(j);
                    break;
                }
                incrementDays++;
            }while(size <= intDays);
        }
        return stockPrices;
    }
}
