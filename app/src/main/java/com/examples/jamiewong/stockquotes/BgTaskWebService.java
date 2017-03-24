package com.examples.jamiewong.stockquotes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by jamiewong on 3/23/17.
 */


class BgTaskWebService extends AsyncTask<String, Void, String> {

    private OnEventListener<String> mCallback;
    private final String BGTWS = getClass().getSimpleName();
    private final Context mContest;
    private String responseString;
    private WebConnect webAccess;

    public BgTaskWebService(Context context, OnEventListener callback) {
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
                mCallback.onFailure("connection error: connection exceeded per second");
            }
        }
    }

}
