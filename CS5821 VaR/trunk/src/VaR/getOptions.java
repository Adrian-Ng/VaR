package VaR;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;


import com.google.gson.*;


/**
 * Created by Adrian on 15/07/2017.
 */
public class getOptions {



    public static JsonObject getJSONfromURL(String urlStrAPI) throws IOException{
        //https://stackoverflow.com/a/21964051 user2654569
        URL url = new URL(urlStrAPI);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
        JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.

        //System.out.println(rootobj);
        return rootobj;
    }

    public static optionsData getOptionsfromJSON(JsonObject json){
        JsonObject expiry = json.get("expiry").getAsJsonObject();
        String expiryYear = expiry.get("y").toString();
        String expiryMonth = expiry.get("m").toString();
        String expiryDay = expiry.get("d").toString();
        System.out.println(expiryYear + "-" + expiryMonth + "-" + expiryDay);

        optionsData options = new optionsData();

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
            //System.out.println(putPrices[i]);
        }

        JsonArray jsonCalls = json.get("calls").getAsJsonArray();
        int numCalls = jsonCalls.size();
        double[] callPrices = new double[numCalls];
        for(int i = 0; i < numCalls; i++){
            JsonElement jsonElementCall = jsonCalls.get(i);
            JsonObject jsonObjectCall = jsonElementCall.getAsJsonObject();
            try {
                callPrices[i] = Double.parseDouble(jsonObjectCall.get("p").getAsString().replace(",", ""));
            } catch(NumberFormatException e){
                callPrices[i] = Double.NaN;
            }
            //System.out.println(callPrices[i]);
        }

        options.setCallPrices(callPrices);
        options.setPutPrices(putPrices);
        options.setStrikePrices(strikePrices);
        return options;
    }


    public static void main(String[] symbols)  throws IOException{
        System.out.println("=========================================================================");
        System.out.println("getOptions.java");
        System.out.println("=========================================================================");
        int numSym = symbols.length;
        /**
         * GET OPTIONS CHAIN FOR EACH SYMBOL
         */
        for(int i = 0; i < numSym; i++){
            String urlStrAPI = "http://www.google.com/finance/option_chain?q=" + symbols[i] + "&output=json";
            JsonObject json = getJSONfromURL(urlStrAPI);

           optionsData options = getOptionsfromJSON(json);
        }

    }
}
