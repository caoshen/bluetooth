package com.example.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickBondDevices(View view) {
        startActivity(new Intent(this, BondDevicesActivity.class));
    }

    public void onClickStartServer(View view) {
        startActivity(new Intent(this, ServerActivity.class));
    }
}
