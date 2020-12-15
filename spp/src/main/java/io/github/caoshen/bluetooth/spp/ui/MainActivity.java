package io.github.caoshen.bluetooth.spp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.caoshen.bluetooth.spp.R;

/**
 * @author caoshen
 * @date 2020/12/15
 */
public class MainActivity extends AppCompatActivity {
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
