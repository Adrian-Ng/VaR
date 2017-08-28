package VaR;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Adrian on 22/08/2017.
 */
public class runTests {

    public static void main(String args[]) throws IOException{
        String[][] arguments = {


                    {"GOOG", "100",  "0", "5", "1", "0.95"}
                ,   {"GOOG", "100",  "0", "5", "1", "0.99"}

                ,   {"GOOG|MSFT", "100|200",  "0|0", "5", "1", "0.95"}
                ,   {"GOOG|MSFT", "100|200",  "0|0", "5", "1", "0.99"}

                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "0|0|0", "5", "1", "0.95"}
                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "0|0|0", "5", "1", "0.99"}

                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "10|20|10", "5", "1", "0.95"}
                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "10|20|10", "5", "1", "0.99"}

                ,   {"GOOG", "100",  "0", "5", "10", "0.95"}
                ,   {"GOOG", "100",  "0", "5", "10", "0.99"}

                ,   {"GOOG|MSFT", "100|200",  "0|0", "5", "10", "0.95"}
                ,   {"GOOG|MSFT", "100|200",  "0|0", "5", "10", "0.99"}

                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "0|0|0", "5", "10", "0.95"}
                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "0|0|0", "5", "10", "0.99"}

                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "10|20|10", "5", "10", "0.95"}
                ,   {"GOOG|MSFT|AAPL", "100|200|100",  "10|20|10", "5", "10", "0.99"}

        };
        for(int i = 0; i < arguments.length;i++)
            ValueAtRisk.main(arguments[i]);

    }
}
