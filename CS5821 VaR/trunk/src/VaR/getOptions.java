package VaR;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.google.gson.*;

public class getOptions {
    private static JsonObject getJSONfromURL(String urlStrAPI) throws IOException{
        //https://stackoverflow.com/a/21964051 user2654569
        URL url = new URL(urlStrAPI);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        // Convert to a JsonObject
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject(); //JSON object in Java
        return rootobj;
    }

    private static int getNumDaystoExpiry(String expiryYear, String expiryMonth, String expiryDayofMonth) {
        DateFormat format = new SimpleDateFormat("yyyy MM d", Locale.ENGLISH);
        Date expiryDate = null;
        Date currentDate = new Date();
        try {
            expiryDate = format.parse(expiryYear + " " + expiryMonth + " " + expiryDayofMonth);
        } catch (ParseException e){
            e.printStackTrace();
        }
        System.out.println("\t\tOptions Expiration Date: " + expiryDate);
        //https://stackoverflow.com/questions/20165564/calculating-days-between-two-dates-with-in-java
        long diff = expiryDate.getTime() - currentDate.getTime();
        int NumDaystoExpiry = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
        return NumDaystoExpiry;
    }

    private static optionsData getOptionsfromJSON(JsonObject json) {
        //https://stackoverflow.com/questions/4216745/java-string-to-date-conversion
        optionsData options = new optionsData();
        JsonObject expiry = json.get("expiry").getAsJsonObject();
        String expiryYear = expiry.get("y").toString();
        String expiryMonth = expiry.get("m").toString();
        String expiryDayofMonth = expiry.get("d").toString();
        int NumDaystoExpiry = getNumDaystoExpiry(expiryYear, expiryMonth, expiryDayofMonth);
        System.out.println("\t\tDays to Expiration: " + NumDaystoExpiry);
        JsonArray jsonPuts = json.get("puts").getAsJsonArray();
        int numPuts = jsonPuts.size();
        double[] strikePrices = new double[numPuts];
        double[] putPrices = new double[numPuts];
        for(int i = 0; i < numPuts; i++){
            JsonElement jsonElementPut = jsonPuts.get(i);
            JsonObject jsonObjectPut = jsonElementPut.getAsJsonObject();
            try {
                strikePrices[i] = Double.parseDouble(jsonObjectPut.get("strike").getAsString().replace(",",""));
            } catch(NumberFormatException e){
                strikePrices[i] = Double.NaN;
            }
            try {
                putPrices[i] = Double.parseDouble(jsonObjectPut.get("p").getAsString().replace(",", ""));
            } catch(NumberFormatException e) {
                putPrices[i] = Double.NaN;
            }
        }
        JsonArray jsonCalls = json.get("calls").getAsJsonArray();
        int numCalls = jsonCalls.size();
        double[] callPrices = new double[numCalls];
        for(int i = 0; i < numCalls; i++) {
            JsonElement jsonElementCall = jsonCalls.get(i);
            JsonObject jsonObjectCall = jsonElementCall.getAsJsonObject();
            try {
                callPrices[i] = Double.parseDouble(jsonObjectCall.get("p").getAsString().replace(",", ""));
            } catch (NumberFormatException e) {
                callPrices[i] = Double.NaN;
            }
        }
        options.setCallPrices(callPrices);
        options.setPutPrices(putPrices);
        options.setStrikePrices(strikePrices);
        options.setDaystoMaturity(NumDaystoExpiry);
        return options;
    }

    public static optionsData[] main(String[] symbols)  throws IOException{
        System.out.println("=========================================================================");
        System.out.println("getOptions.java");
        System.out.println("=========================================================================");
        int numSym = symbols.length;
        /**
         * GET OPTIONS CHAIN FOR EACH SYMBOL
         */
        optionsData[] options = new optionsData[numSym];
        for(int i = 0; i < numSym; i++){
            String urlStrAPI = "http://www.google.com/finance/option_chain?q=" + symbols[i] + "&output=json";
            JsonObject json = getJSONfromURL(urlStrAPI);
            System.out.println("\t\t" + urlStrAPI);
            options[i] = getOptionsfromJSON(json);
        }
        return options;
    }
}
