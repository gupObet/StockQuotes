package com.examples.jamiewong.stockquotes;

/**
 * Created by jamiewong on 3/23/17.
 */

public interface OnEventListener<T> {
    public void onSuccess(T object);
    public void onFailure(T object);
    //public void onFailure(/*Exception e*/int errorMessage);
}

