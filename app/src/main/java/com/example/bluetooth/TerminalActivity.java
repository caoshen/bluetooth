package com.example.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

import io.github.caoshen.bluetooth.spp.BluetoothSpp;
import io.github.caoshen.bluetooth.spp.BluetoothState;
import io.github.caoshen.bluetooth.spp.OnConnectionStateChangedListener;
import io.github.caoshen.bluetooth.spp.OnDataReceivedListener;
import io.github.caoshen.bluetooth.spp.OnDeviceConnectionListener;

/**
 * @author caoshen
 * @date 2020/12/15
 */
public class TerminalActivity extends AppCompatActivity {
    private static final String KEY_SERVER = "key_server";
    TextView textStatus;
    TextView textRead;
    EditText etMessage;
    private OnConnectionStateChangedListener mCallback;

    private Set<BluetoothDevice> mConnectedDeviceSet = new HashSet<>();
    private OnDeviceConnectionListener mDeviceConnectionListener;
    private OnDataReceivedListener mDataListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        textRead = (TextView) findViewById(R.id.textRead);
        textStatus = (TextView) findViewById(R.id.textStatus);
        etMessage = (EditText) findViewById(R.id.etMessage);

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etMessage.getText().length() != 0) {
                    for (BluetoothDevice device : BluetoothSpp.getInstance().getConnectedDeviceSet()
                    ) {
                        BluetoothSpp.getInstance().send(etMessage.getText().toString(), true, device);
                    }
                    etMessage.setText("");
                }
            }
        });

        mCallback = new OnConnectionStateChangedListener() {
            @Override
            public void onStateChange(int state) {
                showConnectionState(state);
            }
        };
        mDeviceConnectionListener = new OnDeviceConnectionListener() {
            @Override
            public void onDeviceConnected(BluetoothDevice device) {
                mConnectedDeviceSet.add(device);
            }
        };
        mDataListener = new OnDataReceivedListener() {
            @Override
            public void onDataReceived(BluetoothDevice device, String data) {
                textRead.append(data + " from " + device.getName() + "\n");
            }
        };
        BluetoothSpp.getInstance().registerStateCallback(mCallback);
        BluetoothSpp.getInstance().registerDeviceCallback(mDeviceConnectionListener);
        BluetoothSpp.getInstance().registerDataCallback(mDataListener);

        Intent intent = getIntent();
        if (intent != null) {
            boolean isServer = intent.getBooleanExtra(KEY_SERVER, false);
            if (isServer) {
                BluetoothSpp.getInstance().start();
            }
        }
    }

    private void showConnectionState(int state) {
        switch (state) {
            case BluetoothState.STATE_NONE: {
                textStatus.setText("STATE NONE");
                break;
            }
            case BluetoothState.STATE_LISTEN: {
                textStatus.setText("STATE LISTEN");
                break;
            }
            case BluetoothState.STATE_CONNECTING: {
                textStatus.setText("STATE CONNECTING");
                break;
            }
            case BluetoothState.STATE_CONNECTED: {
                textStatus.setText("STATE CONNECTED");
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothSpp.getInstance().unRegisterDataCallback(mDataListener);
        BluetoothSpp.getInstance().unRegisterDeviceCallback(mDeviceConnectionListener);
        BluetoothSpp.getInstance().unRegisterStateCallback(mCallback);
    }

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean isServer) {
        Intent intent = new Intent(context, TerminalActivity.class);
        intent.putExtra(KEY_SERVER, isServer);
        context.startActivity(intent);
    }
}
