package com.simperium.simpletodo;

import com.simperium.client.Bucket;

import android.text.TextWatcher;

public class TodoCreator {

    protected Bucket<Todo> mTodoBucket;

    public TodoCreator(Bucket<Todo> todoBucket) {
        mTodoBucket = todoBucket;
    }

    public void onAfterTextChanged() {
        
    }

}