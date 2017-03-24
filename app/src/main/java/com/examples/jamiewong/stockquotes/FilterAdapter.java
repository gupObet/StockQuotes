package com.examples.jamiewong.stockquotes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import UtilityClasses.AppConstants;
import UtilityClasses.Util;

/**
 * Created by jamiewong on 3/23/17.
 */

class FilterAdapter extends BaseAdapter implements Filterable{

    private final Context mContext;
    protected ArrayList<String> mFilterList;
    private ValueFilter valueFilter;
    //private ArrayList<String> resultList;
    private LayoutInflater inflater;
    private TextView tvSymbol;


    public FilterAdapter(Context applicationContext, ArrayList<String> filterList) {
        mContext = applicationContext;
        mFilterList=filterList;
        inflater = LayoutInflater.from(mContext);
    }

    public class ViewHolder {
        TextView tvSugSymbol;
    }

    @Override
    public int getCount() {
        return mFilterList.size();
    }

    @Override
    public String getItem(int position) {
        return mFilterList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        view = inflater.inflate(R.layout.filter_row_item, null);
        tvSymbol=(TextView) view.findViewById(R.id.tv_filter_row);
        tvSymbol.setText(mFilterList.get(i));

        tvSymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, mFilterList.get(i) + " at: " + i, Toast.LENGTH_LONG).show();
            }
        });

        /*ViewHolder viewHolder;

        if(view==null){
            viewHolder = new ViewHolder();
            view = inflater.inflate(R.layout.filter_row_item, null);

            viewHolder.tvSugSymbol = (TextView) view.findViewById(R.id.tv_filter_row);
            view.setTag(viewHolder);
        }
        else {
            viewHolder= (ViewHolder) view.getTag();
        }

        viewHolder.tvSugSymbol.setText(mFilterList.get(i));

        //TODO:

        Log.d("FilterClass", "getView at: " + i);
        selectASymbol(view, i);*/

       /* viewHolder.tvSugSymbol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selSym = ((TextView) view).getText().toString();
                Log.d("INFilter", "listviewfilter selected: " + selSym);

            }
        });*/



        return view;
    }

   private void selectASymbol(View v, final int i) {

       v.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Toast.makeText(mContext, mFilterList.get(i), Toast.LENGTH_LONG).show();
           }
       });

        /*v.setOnClickListener(new view.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selSymbolOrName = mFilterList.get(i);

                Log.d("INFilter", "listviewfilter selected: " + selSymbolOrName);

                Toast.makeText(mContext, "in Filter selected: " + selSymbolOrName, Toast.LENGTH_LONG).show();
            }
        });*/
    }

    @Override
    public Filter getFilter() {

        if(valueFilter == null){
            valueFilter = new ValueFilter();
        }

        return valueFilter;
    }

    private class ValueFilter extends Filter{

        private final String FILTERCLASS = getClass().getSimpleName();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults filterResults = new FilterResults();

            //create urlParams for lookup string
            String filterUrlParam = Util.createLookupParams(constraint.toString());

            Log.d(FILTERCLASS, "filter url params: " + filterUrlParam );

            if(constraint!=null && constraint.length()>0){
                //ArrayList<String> parsedLookupResults = new ArrayList();

                WebConnect webConnect = new WebConnect();
                String lookupResults = webConnect.connect(filterUrlParam);
                int responseCode = webConnect.getResponseCode();

                if(responseCode==200){
                    mFilterList=Util.parseJsonLookup(lookupResults, AppConstants.STOCK_SYMBOL);
                }
            }

            filterResults.values=mFilterList;
            filterResults.count=mFilterList.size();

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

            notifyDataSetChanged();

            /*if(filterResults != null && filterResults.count > 0) {
                notifyDataSetChanged();
            }
            else{
                notifyDataSetInvalidated();
            }*/
        }
    }

}
