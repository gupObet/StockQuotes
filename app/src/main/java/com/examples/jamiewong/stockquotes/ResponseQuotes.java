package com.examples.jamiewong.stockquotes;

/**
 * Created by jamiewong on 3/20/17.
 */

class ResponseQuotes {
    private String Status;
    private String Name;
    private String Symbol;
    private String LastPrice;
    private String Change;
    private String ChangePercent;
    private String Timestamp;
    private String MSDate;
    private String MarketCap;
    private String Volume;
    private String ChangeYTD;
    private String ChangePercentYTD;
    private String High;
    private String Low;
    private String Open;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public String getLastPrice() {
        return LastPrice;
    }

    public void setLastPrice(String lastPrice) {
        LastPrice = lastPrice;
    }

    public String getChange() {
        return Change;
    }

    public void setChange(String change) {
        Change = change;
    }

    public String getChangePercent() {
        return ChangePercent;
    }

    public void setChangePercent(String changePercent) {
        ChangePercent = changePercent;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getMSDate() {
        return MSDate;
    }

    public void setMSDate(String MSDate) {
        this.MSDate = MSDate;
    }

    public String getMarketCap() {
        return MarketCap;
    }

    public void setMarketCap(String marketCap) {
        MarketCap = marketCap;
    }

    public String getVolume() {
        return Volume;
    }

    public void setVolume(String volume) {
        Volume = volume;
    }

    public String getChangeYTD() {
        return ChangeYTD;
    }

    public void setChangeYTD(String changeYTD) {
        ChangeYTD = changeYTD;
    }

    public String getChangePercentYTD() {
        return ChangePercentYTD;
    }

    public void setChangePercentYTD(String changePercentYTD) {
        ChangePercentYTD = changePercentYTD;
    }

    public String getHigh() {
        return High;
    }

    public void setHigh(String high) {
        High = high;
    }

    public String getLow() {
        return Low;
    }

    public void setLow(String low) {
        Low = low;
    }

    public String getOpen() {
        return Open;
    }

    public void setOpen(String open) {
        Open = open;
    }
}
