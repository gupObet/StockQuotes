package com.examples.jamiewong.stockquotes;

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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import UtilityClasses.AppConstants;
import UtilityClasses.Util;

/**
 * Please see the attached readme for documentation
 */

public class MainActivity extends AppCompatActivity {

    //http://dev.markitondemand.com/MODApis/Api/v2/Quote/jsonp?symbol=AAPL&callback=myFunction
    private static final String QUOTE_API_BASE = "http://dev.markitondemand.com/Api/v2/Quote";
    private static final String OUT_QUOTE_JSON = "/jsonp?symbol=";
    private final String MAINACT = getClass().getSimpleName();
    private static boolean cancelTask = false;
    private SearchView searchView;
    private ArrayList<String> retSymbolList;
    private ArrayList<String> retQuoteUrls;
    private ListView listViewQuotes;
    private ProgressBar progBar;
    List<ResponseQuotes> quotesList;
    private QuotesAdapter quotesAdapter;
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
                retOneQuoteUrlArry = new ArrayList<>();
                Toast.makeText(getApplicationContext(), "got lvFilter onItemClick", Toast.LENGTH_LONG).show();
                String selSymOrName = filterAdapter.getItem(i);
                Toast.makeText(getApplication(), "selected: " + selSymOrName, Toast.LENGTH_LONG).show();
                String retOneQuoteUrl = createOneQuoteParam(selSymOrName);
                retOneQuoteUrlArry.add(retOneQuoteUrl);
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

                BgTaskLookupWebService bgTaskWebService = new BgTaskLookupWebService(getApplicationContext(),
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
                            public void onFailure(Object object) {
                                int errorCode = (int) object;
                                Toast.makeText(getApplicationContext(), "ERROR: " + errorCode,
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

                    if (quotesAdapter != null) {
                        if (quoteTask != null) {
                            quoteTask.cancel(true);
                            quoteTask.cancelTask();
                        }

                        quotesList.clear();
                        quotesAdapter.notifyDataSetChanged();
                        //listViewQuotes.setAdapter(null);
                    }

                    //also clear the filter listview
                    if (filterAdapter != null) {
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
                if (s.length() >= 3) {
                    filterAdapter.getFilter().filter(s);
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
        }
        return quoteUrls;
    }

    private class QuoteAsyncTask extends AsyncTask<Void, List<ResponseQuotes>, Void> {

        private ArrayList<String> quoteUrls;

        public QuoteAsyncTask(ArrayList<String> retQuoteUrls) {
            quoteUrls = retQuoteUrls;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progBar.setVisibility(View.VISIBLE);
            quotesAdapter = new QuotesAdapter(getApplication(), quotesList);
            listViewQuotes.setAdapter(quotesAdapter);
            cancelTask = false;
        }

        public synchronized void cancelTask() {
            cancelTask = true;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String aJSONFormQuote;
            ResponseQuotes oneResponseQuote = null;
            String responseString, currentQuote;
            int responseCode = 0;

            for (int i = 0; i < quoteUrls.size(); i++) {
                if (cancelTask == false) {
                    WebConnect webConnect = new WebConnect();
                    currentQuote = quoteUrls.get(i);
                    responseString = webConnect.connect(currentQuote);

                    /*get response code, if 501 or any error code that is not 200,
                    tr  connect again after one x sec*/
                    if (webConnect.getResponseCode() != 200) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        responseString = webConnect.connect(currentQuote);
                        responseCode = webConnect.getResponseCode();
                        Log.d(MAINACT, "second attempt to reconnect for same url");
                        Log.d(MAINACT, "reconnect response code: " + responseCode);

                        if (responseCode != 200) {
                            String firstPart = currentQuote.substring(0, currentQuote.indexOf("=") + 1);
                            currentQuote = currentQuote.replace(firstPart, "");
                        }
                    }

                    aJSONFormQuote = putInJsonFormat(responseString);
                    oneResponseQuote = parseJsonQuote(aJSONFormQuote, responseCode, currentQuote);

                    try {
                        //5000 would still have some 501 response codes which is a failure
                        Thread.sleep(6000);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }

                    if (cancelTask == true) {
                        Log.d(MAINACT, "cancelTask: " + cancelTask);
                        break;
                    }
                    quotesList.add(oneResponseQuote);
                    Log.d("MainActivity", "Requesting " + currentQuote);

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
            /*if(cancelTask==true){
                quotesList.clear();
            }*/
            quotesAdapter.notifyDataSetChanged();
        }

        private String putInJsonFormat(String oneQuote) {

            //first part is everything in the first set of parentheses
            String firstPart = oneQuote.substring(0, 17);
            oneQuote = oneQuote.replace(firstPart, "");
            oneQuote = oneQuote.replaceAll("[()]", ""); //reg exp for parentheses

            return oneQuote;
        }

        private ResponseQuotes parseJsonQuote(String aJSONFormQuote, int responseCode, String currentQuote) {

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
                aResponseQuote.setError(responseCode + " for " + currentQuote.toString());
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
            }

            return aResponseQuote;
        }
    }

}
