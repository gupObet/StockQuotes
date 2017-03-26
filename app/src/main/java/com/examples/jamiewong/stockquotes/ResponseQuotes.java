package com.examples.jamiewong.stockquotes;

/**
 * Created by jamiewong on 3/20/17.
 */

class ResponseQuotes {
    private String status;
    private String name;
    private String symbol;
    private String lastPrice;
    private String change;
    private String changePercent;
    private String timeStamp;
    private String msDate;
    private String marketCap;
    private String volume;
    private String changeYTD;
    private String changePercentYTD;
    private String high;
    private String low;
    private String open;
    private String error;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(String lastPrice) {
        this.lastPrice = lastPrice;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMsDate() {
        return msDate;
    }

    public void setMsDate(String msDate) {
        this.msDate = msDate;
    }

    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getChangeYTD() {
        return changeYTD;
    }

    public void setChangeYTD(String changeYTD) {
        this.changeYTD = changeYTD;
    }

    public String getChangePercentYTD() {
        return changePercentYTD;
    }

    public void setChangePercentYTD(String changePercentYTD) {
        this.changePercentYTD = changePercentYTD;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
