package com.examples.jamiewong.stockquotes;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Todo:  Add check for network state, before making http request
 * check for errors, ex: retSymbolList is ever null
 * questions: repeat quote symbols
 */

public class MainActivity extends AppCompatActivity {

    private static final String LOOKUP_API_BASE = "http://dev.markitondemand.com/Api/v2/Lookup";
    private static final String OUT_LOOKUP_JSON = "/json?input=";
    private static final String STOCK_SYMBOL = "Symbol";
    //http://dev.markitondemand.com/MODApis/Api/v2/Quote/jsonp?symbol=AAPL&callback=myFunction
    private static final String QUOTE_API_BASE = "http://dev.markitondemand.com/Api/v2/Quote";
    private static final String OUT_QUOTE_JSON = "/jsonp?symbol=";
    private final String MAINACT = getClass().getSimpleName();
    private SearchView searchView;
    private HttpURLConnection conn;
    private StringBuilder stringBuilder;
    private ArrayList<String> retSymbolList;
    private ArrayList<String> retQuoteUrls;

    //private android.support.v7.widget.SearchView.SearchAutoComplete searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*searchView= (android.support.v7.widget.SearchView.SearchAutoComplete)
                findViewById(R.id.search_view);*/

        searchView= (SearchView) findViewById(R.id.search_view);

    }

    @Override
    protected void onResume() {
        super.onResume();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                Toast.makeText(getApplication(), s, Toast.LENGTH_LONG).show();

                String urlParams = createLookupParams(s);

                new BgTaskWebService().execute(urlParams);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private String createLookupParams(String s) {

        StringBuilder sb = new StringBuilder(LOOKUP_API_BASE + OUT_LOOKUP_JSON + s);

        return sb.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    class BgTaskWebService extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String urlParams = params[0]; //urlParams;

            String responseString = connect(urlParams);

            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast.makeText(getApplication(), s, Toast.LENGTH_LONG).show();

            Log.d(MAINACT, s);

            retSymbolList = parseJsonLookup(s, STOCK_SYMBOL);

            retQuoteUrls = createQuoteParams(retSymbolList);

            for(int i=0; i<retQuoteUrls.size(); i++){
                Log.d("BgTaskWebService", "quoteUrl at " + i + ": " + retQuoteUrls.get(i));
            }

            //create a thread pool asynctask
            QuoteAsyncTask quoteTask = new QuoteAsyncTask(retQuoteUrls);
            quoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            //Todo: investigate why
            /*quoteTask.execute();  //using this without specifying sdk level, will automatically
            launch multiple threads, for example, "GE" will launch 8 threads
            */
        }

        private ArrayList<String> parseJsonLookup(String s, String symbol) {

            ArrayList<String> symbolList = new ArrayList<String>();

            if(s!=null || s.length()!=0){

                // Getting JSON Array node
                JSONArray symbolJsonArray = null;
                try {
                    symbolJsonArray = new JSONArray(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject jsonObj = null;

                for(int i=0; i<symbolJsonArray.length(); i++){

                    try {
                        jsonObj = symbolJsonArray.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(jsonObj!=null){
                        try {
                            symbolList.add(jsonObj.getString(symbol));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            for(int i=0; i<symbolList.size(); i++){
                Log.d(MAINACT, "symbol at " + i + " : " + symbolList.get(i));
            }

            return symbolList;

        }

        private ArrayList<String> createQuoteParams(ArrayList<String> retSymbolList) {

            ArrayList<String> quoteUrls = new ArrayList<>();

            StringBuilder sb;
            String selSymbol;

            for(int i=0; i<retSymbolList.size(); i++) {

                selSymbol=retSymbolList.get(i);
                sb = new StringBuilder(QUOTE_API_BASE + OUT_QUOTE_JSON + selSymbol);
                quoteUrls.add(sb.toString());

            }

            return quoteUrls;
        }

    }

    private class QuoteAsyncTask extends AsyncTask<Object, Object, ArrayList<String>> {

        private final ArrayList<String> quoteUrls;

        public QuoteAsyncTask(ArrayList<String> retQuoteUrls) {
            quoteUrls=retQuoteUrls;
        }

        @Override
        protected ArrayList<String> doInBackground(Object... voids) {

            ArrayList<String> quoteResponses = new ArrayList<>();

            for(int i=0; i<quoteUrls.size(); i++){

                try {
                    Thread.sleep(6000);  //5000 would still have some 501 response codes which is a failure
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }

                String responseString=connect(quoteUrls.get(i));

                quoteResponses.add(responseString);
            }

            return quoteResponses;
        }

        @Override
        protected void onPostExecute(ArrayList<String> quoteResponses) {
            super.onPostExecute(quoteResponses);

            for(int i=0; i<quoteResponses.size(); i++){
                Log.d("QuoteAsyncTask", "response " + i + ": " + quoteResponses.get(i));
            }

            parseJsonQuote(quoteResponses);
        }

        private void parseJsonQuote(ArrayList<String> quoteResponses) {

            /**
             *  ArrayList<String> symbolList = new ArrayList<String>();

             if(s!=null || s.length()!=0){

             // Getting JSON Array node
             JSONArray symbolJsonArray = null;
             try {
             symbolJsonArray = new JSONArray(s);
             } catch (JSONException e) {
             e.printStackTrace();
             }

             JSONObject jsonObj = null;

             for(int i=0; i<symbolJsonArray.length(); i++){

             try {
             jsonObj = symbolJsonArray.getJSONObject(i);
             } catch (JSONException e) {
             e.printStackTrace();
             }

             if(jsonObj!=null){
             try {
             symbolList.add(jsonObj.getString(symbol));
             } catch (JSONException e) {
             e.printStackTrace();
             }
             }
             }

             }

             for(int i=0; i<symbolList.size(); i++){
             Log.d(MAINACT, "symbol at " + i + " : " + symbolList.get(i));
             }

             return symbolList;
             */

            ArrayList<ResponseQuotes> quotesList = new ArrayList<>();
        }
    }



    //Todo: make this its own class, instead of own method
    private String connect(String urlParams) {

        InputStream inputStream;
        try {

            Log.d(MAINACT, "url param: " + urlParams);
            URL url = new URL(urlParams);

            conn = (HttpURLConnection) url.openConnection();
            //conn.setConnectTimeout(1000);
            conn.setDoOutput(false);  //makes a big difference

            int responseCode = conn.getResponseCode();
            Log.d(MAINACT, "conn.getRespnseCode: " + responseCode);

            if(responseCode!=HttpURLConnection.HTTP_OK){
                 inputStream = conn.getErrorStream();
            }
            else{
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
           /* Log.d("Connect", conn.getErrorStream().toString());
            try {
                Log.d("Connect", conn.getResponseMessage().toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }*/
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return stringBuilder.toString();

    }




}
