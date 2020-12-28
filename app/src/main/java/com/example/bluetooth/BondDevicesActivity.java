package com.example.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.caoshen.bluetooth.spp.BluetoothSpp;
import io.github.caoshen.bluetooth.spp.BluetoothState;
import io.github.caoshen.bluetooth.spp.OnConnectionStateChangedListener;

/**
 * 发起 SPP 连接的 client
 *
 * @author caoshen
 * @date 2020/12/14
 */
public class BondDevicesActivity extends AppCompatActivity {
    private static final String TAG = "BondDevicesActivity";
    private OnConnectionStateChangedListener mCallback;

    private void showConnectionState(int stateCode) {
        switch (stateCode) {
            case BluetoothState.STATE_NONE: {
                Toast.makeText(this, "STATE NONE", Toast.LENGTH_SHORT).show();
                break;
            }
            case BluetoothState.STATE_LISTEN: {
                Toast.makeText(this, "STATE LISTEN", Toast.LENGTH_SHORT).show();
                break;
            }
            case BluetoothState.STATE_CONNECTING: {
                Toast.makeText(this, "STATE CONNECTING", Toast.LENGTH_SHORT).show();
                break;
            }
            case BluetoothState.STATE_CONNECTED: {
                Toast.makeText(this, "STATE CONNECTED", Toast.LENGTH_SHORT).show();
                TerminalActivity.start(this);
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        RecyclerView recyclerViewDevices = findViewById(R.id.recyclerview_device_list);

        // layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewDevices.setLayoutManager(layoutManager);

        // adapter
        DevicesRecyclerViewAdapter adapter = new DevicesRecyclerViewAdapter();
        adapter.setOnItemClickListener(new DevicesRecyclerViewAdapter.OnItemClickListener<BluetoothDevice>() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, BluetoothDevice data) {
                BluetoothSpp.getInstance().connect(data);
            }
        });
        recyclerViewDevices.setAdapter(adapter);

        // item decoration
        recyclerViewDevices.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));

        // bind devices
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        List<BluetoothDevice> devices = new ArrayList<>(bondedDevices);
        adapter.submitList(devices);

        mCallback = new OnConnectionStateChangedListener() {
            @Override
            public void onStateChange(int state) {
                showConnectionState(state);
            }
        };
        BluetoothSpp.getInstance().registerStateCallback(mCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothSpp.getInstance().unRegisterStateCallback(mCallback);
    }
}
