package com.example.testgattclient;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class DeviceActivity extends Activity {

    private BluetoothDevice mDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Intent intent = getIntent();
        if (intent != null) {
            mDevice = intent.getParcelableExtra("device");
        }
        if (mDevice == null) {
            finish();
        }
    }
}
