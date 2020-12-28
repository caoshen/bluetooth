package com.example.bluetooth;

import android.app.Application;

import com.example.library.utils.ContextUtils;

import io.github.caoshen.bluetooth.spp.BluetoothSpp;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ContextUtils.setContext(this);
        BluetoothSpp.getInstance().init(this);
    }
}
