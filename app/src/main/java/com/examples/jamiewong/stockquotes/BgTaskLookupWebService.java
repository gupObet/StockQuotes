package com.examples.jamiewong.stockquotes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jamiewong on 3/23/17.
 */
class BgTaskLookupWebService extends AsyncTask<String, Void, String> {

    private OnEventListener<String> mCallback;
    private final String BGTWS = getClass().getSimpleName();
    private final Context mContest;
    private String responseString;
    private WebConnect webAccess;

    public BgTaskLookupWebService(Context context, OnEventListener callback) {
        mContest = context;
        mCallback=callback;
    }

    @Override
    protected String doInBackground(String... params) {

        String urlParams = params[0]; //urlParams;
        webAccess = new WebConnect();
        responseString=webAccess.connect(urlParams);

        return responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Toast.makeText(mContest, s, Toast.LENGTH_LONG).show();
        Log.d(BGTWS, s);

        if(mCallback!=null){
            if(webAccess.getResponseCode()==200) {
                mCallback.onSuccess(s);
            }
            else {
                //501, number of connections exceeded per second
                mCallback.onFailure(String.valueOf(webAccess.getResponseCode()));
            }
        }
    }

}
