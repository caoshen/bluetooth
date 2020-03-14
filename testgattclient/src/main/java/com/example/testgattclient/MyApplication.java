package com.example.testgattclient;

import android.app.Application;

import com.example.library.utils.ContextUtils;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtils.setContext(this);
    }
}
