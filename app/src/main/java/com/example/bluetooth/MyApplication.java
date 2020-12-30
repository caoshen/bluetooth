package com.example.bluetooth;

import android.app.Application;

import io.github.caoshen.bluetooth.spp.BluetoothSpp;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothSpp.getInstance().init(this);
    }
}
