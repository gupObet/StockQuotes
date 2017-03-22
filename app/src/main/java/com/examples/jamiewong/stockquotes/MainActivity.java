package com.examples.jamiewong.stockquotes;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
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
 * fix the 501 case, for ex. GERN for loolup GE, string cannot be converted to jsonObject
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
    private ListView listViewQuotes;
    private ProgressBar progBar;

    //private android.support.v7.widget.SearchView.SearchAutoComplete searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*searchView= (android.support.v7.widget.SearchView.SearchAutoComplete)
                findViewById(R.id.search_view);*/

        searchView= (SearchView) findViewById(R.id.search_view);
        listViewQuotes = (ListView) findViewById(R.id.lv_quotes);
        progBar = (ProgressBar) findViewById(R.id.progBar);

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
        protected void onPreExecute() {
            super.onPreExecute();

            progBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected ArrayList<String> doInBackground(Object... voids) {

            ArrayList<String> quoteResponses = new ArrayList<>();

            for(int i=0; i<quoteUrls.size(); i++){

                String responseString=connect(quoteUrls.get(i));


                try {
                    Thread.sleep(1000);  //5000 would still have some 501 response codes which is a failure
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }

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

            ArrayList<String> jsonFormQuoteResponses = putInJsonFormat(quoteResponses);
            ArrayList<ResponseQuotes> retQuotesList = parseJsonQuote(jsonFormQuoteResponses);
            displayQuotesListView(retQuotesList);
            progBar.setVisibility(View.GONE);
        }

        private void displayQuotesListView(ArrayList<ResponseQuotes> retQuotesList) {

            //declare baseAdapter here
            QuotesAdapter quotesAdapter = new QuotesAdapter(getApplication(), retQuotesList);
            listViewQuotes.setAdapter(quotesAdapter);

            //don't need to implement onClickListener
        }

        private ArrayList<String> putInJsonFormat(ArrayList<String> quoteResponses) {

            String oneQuote;

            for(int j=0; j<quoteResponses.size(); j++){
                oneQuote = quoteResponses.get(j);

                //first part is everything in the first set of parentheses
                String firstPart = oneQuote.substring(0, 17);
                oneQuote = oneQuote.replace(firstPart, "");
                oneQuote = oneQuote.replaceAll("[()]", ""); //reg exp for parentheses

                Log.d("QuoteAsyncTask", "firstPart: " + firstPart);
                Log.d("QuoteAsyncTask", "formatted quote: " + oneQuote);

                quoteResponses.set(j, oneQuote);

                Log.d("QuoteAsyncTask", "quote response at " + j + ": " + quoteResponses.get(j));

            }

            return quoteResponses;
        }

        //each of the elements in quoteResponses is a json object
        private ArrayList<ResponseQuotes> parseJsonQuote(ArrayList<String> quoteResponses) {

            ArrayList<ResponseQuotes> quotesList = new ArrayList<>();

            JSONObject jsonObject = null;
            String errorString = "Request blockedExceeded requests/sec limit.";
            String status, name, symbol, lastPrice, change, changePercent, timeStamp;
            String msDate, marketCap, volume, changeYTD, changePercentYTD, high, low, open;

            for(int j=0; j<quoteResponses.size(); j++) {

                ResponseQuotes aResponseQuote = new ResponseQuotes();
                boolean isJSONObject=false;

                    try {

                        jsonObject = new JSONObject(quoteResponses.get(j));
                        isJSONObject=true;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(isJSONObject) {
                        try {

                            status = jsonObject.getString("Status");
                            aResponseQuote.setStatus(status);

                            name = jsonObject.getString("Name");
                            aResponseQuote.setName(name);
                            symbol = jsonObject.getString("Symbol");
                            aResponseQuote.setSymbol(symbol);
                            lastPrice = jsonObject.getString("LastPrice");
                            aResponseQuote.setLastPrice(lastPrice);
                            change = jsonObject.getString("Change");
                            aResponseQuote.setChange(change);
                            changePercent = jsonObject.getString("ChangePercent");
                            aResponseQuote.setChangePercent(changePercent);
                            timeStamp = jsonObject.getString("Timestamp");
                            aResponseQuote.setTimeStamp(timeStamp);
                            msDate = jsonObject.getString("MSDate");
                            aResponseQuote.setMsDate(msDate);
                            marketCap = jsonObject.getString("MarketCap");
                            aResponseQuote.setMarketCap(marketCap);
                            volume = jsonObject.getString("Volume");
                            aResponseQuote.setVolume(volume);
                            changeYTD = jsonObject.getString("ChangeYTD");
                            aResponseQuote.setChangeYTD(changeYTD);
                            changePercentYTD = jsonObject.getString("ChangePercentYTD");
                            aResponseQuote.setChangePercentYTD(changePercentYTD);
                            high = jsonObject.getString("High");
                            aResponseQuote.setHigh(high);
                            low = jsonObject.getString("Low");
                            aResponseQuote.setLow(low);
                            open = jsonObject.getString("Open");
                            aResponseQuote.setOpen(open);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        aResponseQuote.setError(errorString);
                    }

                quotesList.add(j, aResponseQuote);

            }

            //verify if quotesList has all the data
            for (int i=0; i<quotesList.size(); i++){
                Log.d("ParseQuotes", "errorString: " + quotesList.get(i).getError());  //only exist if error code 501, ceeded limit
                Log.d("ParseQuotes", "status: " + quotesList.get(i).getStatus());
                Log.d("ParseQuotes", "name: " + quotesList.get(i).getName());
                Log.d("ParseQuotes", "symbol: " + quotesList.get(i).getSymbol());
                Log.d("ParseQuotes", "change: " + quotesList.get(i).getChange());
                Log.d("ParseQuotes", "changePercent: " + quotesList.get(i).getChangePercent());
                Log.d("ParseQuotes", "timeStamp: " + quotesList.get(i).getTimeStamp());
                Log.d("ParseQuotes", " msDate: " + quotesList.get(i).getMsDate());
                Log.d("ParseQuotes", "marketCap: " + quotesList.get(i).getMarketCap());
                Log.d("ParseQuotes", "volume: " + quotesList.get(i).getVolume());
                Log.d("ParseQuotes", "changeYTD: " + quotesList.get(i).getChangeYTD());
                Log.d("ParseQuotes", "changePercentYTD: " + quotesList.get(i).getChangePercentYTD());
                Log.d("ParseQuotes", "high: " + quotesList.get(i).getHigh());
                Log.d("ParseQuotes", "low: " + quotesList.get(i).getLow());
                Log.d("ParseQuotes", "open: " + quotesList.get(i).getOpen());

            }

            return quotesList;
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
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return stringBuilder.toString();

    }

}
