package com.examples.jamiewong.stockquotes;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by jamiewong on 3/23/17.
 */

public class WebConnect {

    private HttpURLConnection conn;
    private StringBuilder stringBuilder;
    private final String WEBCONNECT = getClass().getSimpleName();
    private IOException myException;
    int responseCode;


    /*class SymbolResults {
        private String symbolResponse;
        private IOException e;
    }*/

    //Todo: make this its own class, instead of own method
    protected String connect(String urlParams) {

        InputStream inputStream;
        try {

            Log.d(WEBCONNECT, "url param: " + urlParams);
            URL url = new URL(urlParams);

            conn = (HttpURLConnection) url.openConnection();
            //conn.setConnectTimeout(1000);
            conn.setDoOutput(false);  //makes a big difference

            responseCode = conn.getResponseCode();
            Log.d(WEBCONNECT, "conn.getRespnseCode: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                inputStream = conn.getErrorStream();
            } else {
                //file not found for url here, on responseCode=501
                inputStream = conn.getInputStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            //BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();

        } catch (java.io.IOException e) {
            e.printStackTrace();
            myException = e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        /*SymbolResults symbolResults = new SymbolResults();
        symbolResults.symbolResponse=stringBuilder.toString();
        symbolResults.e=myException;
        return symbolResults;*/

        return stringBuilder.toString();

    }

    protected int getResponseCode(){
        return responseCode;
    }
}
