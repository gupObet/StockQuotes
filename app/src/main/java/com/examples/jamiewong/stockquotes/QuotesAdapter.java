package com.examples.jamiewong.stockquotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by jamiewong on 3/21/17.
 */

class QuotesAdapter extends BaseAdapter {


    private final LayoutInflater quotesInflator;
    private final ArrayList<ResponseQuotes> quotesList;

    public QuotesAdapter(Context context, ArrayList<ResponseQuotes> retQuotesList) {
        quotesList=retQuotesList;
        quotesInflator = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return quotesList.size();
    }

    @Override
    public Object getItem(int i) {
        return quotesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i; //i=position
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder;

        if(view == null){
            view = quotesInflator.inflate(R.layout.row_item, null);
            holder = new ViewHolder();
            holder.tvName = (TextView) view.findViewById(R.id.tv_name);
            holder.tvSymbol = (TextView) view.findViewById(R.id.tv_symbol);
            holder.tvLastPrice = (TextView) view.findViewById(R.id.tv_last_price);
            holder.tvTimestamp = (TextView) view.findViewById(R.id.tv_time_stamp);
            holder.tvHigh = (TextView) view.findViewById(R.id.tv_high);
            holder.tvLow = (TextView) view.findViewById(R.id.tv_low);
            holder.tvOpen = (TextView) view.findViewById(R.id.tv_open);

            view.setTag(holder);
        }
        else{
            holder= (ViewHolder) view.getTag();
        }

        holder.tvSymbol.setText("Symbol: " + quotesList.get(i).getSymbol());
        holder.tvName.setText("Name: "  + quotesList.get(i).getName());
        holder.tvLastPrice.setText("LastPrice: " + quotesList.get(i).getLastPrice());
        holder.tvTimestamp.setText("Timestamp: " + quotesList.get(i).getTimeStamp());
        holder.tvHigh.setText("High: " + quotesList.get(i).getHigh());
        holder.tvLow.setText("Low: " + quotesList.get(i).getLow());
        holder.tvOpen.setText("Open: " + quotesList.get(i).getOpen());

        //return null;
        return view;
    }


    static class ViewHolder{
        TextView tvName;
        TextView tvSymbol;
        TextView tvLastPrice;
        TextView tvTimestamp;
        TextView tvHigh;
        TextView tvLow;
        TextView tvOpen;
    }
}
