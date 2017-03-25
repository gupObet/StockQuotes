package com.examples.jamiewong.stockquotes;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import UtilityClasses.AppConstants;
import UtilityClasses.Util;

/**
 * Todo:  check network online or not
 * questions: repeat quote symbols should be removed?
 * fix the 501 case, for ex. GERN for lookup on GE, string cannot be converted to jsonObject, increasing
 * wait time on connect to webservice should fix it, but don't want user to wait too long
 * Uses asynctask thread pool executor to download asynchronously all the quotes
 */

public class MainActivity extends AppCompatActivity {

    //http://dev.markitondemand.com/MODApis/Api/v2/Quote/jsonp?symbol=AAPL&callback=myFunction
    private static final String QUOTE_API_BASE = "http://dev.markitondemand.com/Api/v2/Quote";
    private static final String OUT_QUOTE_JSON = "/jsonp?symbol=";
    private final String MAINACT = getClass().getSimpleName();
    private SearchView searchView;
    private ArrayList<String> retSymbolList;
    private ArrayList<String> retQuoteUrls;
    private ListView listViewQuotes;
    private ProgressBar progBar;
    List<ResponseQuotes> quotesList;
    private QuotesAdapter quotesAdapter;
    private ImageView closeButton;
    private ListView listViewFilter;
    private FilterAdapter filterAdapter;
    private ArrayList<String> filterList;
    private QuoteAsyncTask quoteTask;
    private ArrayList<String> retOneQuoteUrlArry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*searchView= (android.support.v7.widget.SearchView.SearchAutoComplete)
                findViewById(R.id.search_view);*/

        searchView = (SearchView) findViewById(R.id.search_view);
        listViewQuotes = (ListView) findViewById(R.id.lv_quotes);
        listViewFilter = (ListView) findViewById(R.id.lv_filter);
        progBar = (ProgressBar) findViewById(R.id.progBar);

        filterList = new ArrayList<>();
        filterAdapter = new FilterAdapter(getApplicationContext(), filterList);
        listViewFilter.setAdapter(filterAdapter);

        quotesList = Collections.synchronizedList(new ArrayList<ResponseQuotes>());

    }

    @Override
    protected void onResume() {
        super.onResume();

        //doesn't work
        listViewFilter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                //String selSymOrName = (String) adapterView.getItemAtPosition(i);

                retOneQuoteUrlArry = new ArrayList<>();

                Toast.makeText(getApplicationContext(), "got lvFilter onItemClick", Toast.LENGTH_LONG).show();

                String selSymOrName = filterAdapter.getItem(i);

                Toast.makeText(getApplication(), "selected: " + selSymOrName, Toast.LENGTH_LONG).show();

                String retOneQuoteUrl = createOneQuoteParam(selSymOrName);

                retOneQuoteUrlArry.add(retOneQuoteUrl);

                //not sure if I have to rename this bg task
                quoteTask = new
                        MainActivity.QuoteAsyncTask(retOneQuoteUrlArry);

                quoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                listViewFilter.setVisibility(View.GONE);
                listViewQuotes.setVisibility(View.VISIBLE);

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                Toast.makeText(getApplication(), s, Toast.LENGTH_LONG).show();

                String urlParams = Util.createLookupParams(s);

                BgTaskWebService bgTaskWebService = new BgTaskWebService(getApplicationContext(),
                        new OnEventListener() {
                            @Override
                            public void onSuccess(Object object) {
                                String symbolsString = (String) object;

                                Toast.makeText(getApplicationContext(), "SUCCESS: " + symbolsString,
                                        Toast.LENGTH_LONG).show();

                                retSymbolList = Util.parseJsonLookup(symbolsString, AppConstants.STOCK_SYMBOL);

                                retQuoteUrls = createQuoteParams(retSymbolList);

                                for (int i = 0; i < retQuoteUrls.size(); i++) {
                                    Log.d(MAINACT, "quoteUrl at " + i + ": " + retQuoteUrls.get(i));
                                }

                                //create a thread pool asynctask
                                quoteTask = new
                                        MainActivity.QuoteAsyncTask(retQuoteUrls);

                                quoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(getApplicationContext(), "ERROR: " + errorMessage,
                                        Toast.LENGTH_LONG).show();
                            }

                        });

                bgTaskWebService.execute(urlParams);

                listViewFilter.setVisibility(View.GONE);
                listViewQuotes.setVisibility(View.VISIBLE);


                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (TextUtils.isEmpty(s)) {
                    Toast.makeText(getApplication(), "clicked on X", Toast.LENGTH_LONG).show();

                    if(quotesAdapter!=null) {
                        quotesList.clear();
                        quotesAdapter.notifyDataSetChanged();
                        //listViewQuotes.setAdapter(null);

                        if(quoteTask!=null){
                            quoteTask.cancelTask();
                        }
                    }

                    //also clear the filter listview
                    if(filterAdapter!=null) {
                        filterAdapter.mFilterList.clear();
                        //filterList.clear();
                        filterAdapter.notifyDataSetChanged();
                        //listViewFilter.setAdapter(null);
                    }

                    return false;
                }

                //this made it work
                listViewFilter.setVisibility(View.VISIBLE);
                listViewQuotes.setVisibility(View.GONE);

                //only filter for queries size greater than 3, otherwise, too many results
                if(s.length()>=3) {

                    filterAdapter.getFilter().filter(s);

                    //filterAdapter.filter(s);
                    //Toast.makeText(getApplication(), "query >= 3", Toast.LENGTH_LONG).show();
                    //listViewFilter.invalidate();
                    //filterAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });



    }

    private String createOneQuoteParam(String selSymbol) {

        StringBuilder sb;

        sb = new StringBuilder(QUOTE_API_BASE + OUT_QUOTE_JSON + selSymbol);

        return sb.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private ArrayList<String> createQuoteParams(ArrayList<String> retSymbolList) {

        ArrayList<String> quoteUrls = new ArrayList<>();

        String selSymbol, oneQuoteUrl;

        for (int i = 0; i < retSymbolList.size(); i++) {

            selSymbol = retSymbolList.get(i);

            oneQuoteUrl = createOneQuoteParam(selSymbol);

            quoteUrls.add(oneQuoteUrl);

            /*sb = new StringBuilder(QUOTE_API_BASE + OUT_QUOTE_JSON + selSymbol);
            quoteUrls.add(sb.toString());*/
        }
        return quoteUrls;
    }


    private class QuoteAsyncTask extends AsyncTask<Void, List<ResponseQuotes>, Void> {

        private ArrayList<String> quoteUrls;
        private boolean cancelTask;

        public QuoteAsyncTask(ArrayList<String> retQuoteUrls) {
            quoteUrls = retQuoteUrls;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progBar.setVisibility(View.VISIBLE);

            quotesAdapter = new QuotesAdapter(getApplication(), quotesList);
            listViewQuotes.setAdapter(quotesAdapter);
            cancelTask=false;

        }

        public void cancelTask() {
            cancelTask=true;
        }

        @Override
        protected Void doInBackground(Void... voids) {


            String aJSONFormQuote;
            ResponseQuotes oneResponseQuote;

            for (int i = 0; i < quoteUrls.size(); i++) {

                if(cancelTask==false) {

                    String responseString = new WebConnect().connect(quoteUrls.get(i));

                    aJSONFormQuote = putInJsonFormat(responseString);

                    oneResponseQuote = parseJsonQuote(aJSONFormQuote);

                    try {
                        Thread.sleep(6000);  //5000 would still have some 501 response codes which is a failure
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }

                    quotesList.add(oneResponseQuote);
                    Log.d("MainActivity", "Requesting " + quoteUrls.get(i));

                    //update UI
                    publishProgress(quotesList);
                }
            }

            //return quoteResponses;
            return null;
        }

        @Override
        protected void onProgressUpdate(List<ResponseQuotes>... values) {
            super.onProgressUpdate();

            List<ResponseQuotes> updatedQuotesList = values[0];

            quotesAdapter.notifyDataSetChanged();
        }

        private String putInJsonFormat(String oneQuote) {

            //first part is everything in the first set of parentheses
            String firstPart = oneQuote.substring(0, 17);
            oneQuote = oneQuote.replace(firstPart, "");
            oneQuote = oneQuote.replaceAll("[()]", ""); //reg exp for parentheses

            return oneQuote;
        }

        private ResponseQuotes parseJsonQuote(/*ArrayList<String> quoteResponses*/String aJSONFormQuote) {

            JSONObject jsonObject = null;
            String errorString = "Request blockedExceeded requests/sec limit.";
            String status, name, symbol, lastPrice, change, changePercent, timeStamp;
            String msDate, marketCap, volume, changeYTD, changePercentYTD, high, low, open;


            ResponseQuotes aResponseQuote = new ResponseQuotes();
            boolean isJSONObject = false;

            try {

                jsonObject = new JSONObject(aJSONFormQuote);
                isJSONObject = true;

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (isJSONObject) {
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
            } else {
                aResponseQuote.setError(errorString);
            }
            return aResponseQuote;
        }
    }


}
