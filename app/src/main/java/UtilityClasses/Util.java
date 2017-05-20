package UtilityClasses;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

/**
 * Created by jamiewong on 3/23/17.
 */

public class Util {

    private static String UTIL = "Util";

    public static String createLookupParams(String s) {
        
        StringBuilder sb = new StringBuilder(AppConstants.LOOKUP_API_BASE + AppConstants.OUT_LOOKUP_JSON + s);
        return sb.toString();
        
    }

    public static ArrayList<String> parseJsonLookup(String s, String symbol) {

        ArrayList<String> symbolList = new ArrayList<String>();

        if (s != null || s.length() != 0) {
            // Getting JSON Array node
            JSONArray symbolJsonArray = null;
            try {
                symbolJsonArray = new JSONArray(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject jsonObj = null;

            for (int i = 0; i < symbolJsonArray.length(); i++) {
                try {
                    jsonObj = symbolJsonArray.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jsonObj != null) {
                    try {
                        symbolList.add(jsonObj.getString(symbol));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        for (int i = 0; i < symbolList.size(); i++) {
            Log.d(UTIL, "symbol at " + i + " : " + symbolList.get(i));
        }

        return symbolList;

    }
}
