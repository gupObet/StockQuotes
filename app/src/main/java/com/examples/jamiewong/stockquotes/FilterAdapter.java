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
import java.util.ArrayList;
import UtilityClasses.AppConstants;
import UtilityClasses.Util;

/**
 * Created by jamiewong on 3/23/17.
 */

class FilterAdapter extends BaseAdapter implements Filterable {

    private static final String FILTERADAPTER = "FILTERADAPTER";
    private final Context mContext;
    protected ArrayList<String> mFilterList;
    private ValueFilter valueFilter;
    private LayoutInflater inflater;


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

        ViewHolder viewHolder;

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

        Log.d("FilterClass", "getView at: " + i);

        return view;
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
