package io.github.caoshen.bluetooth.spp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.github.caoshen.bluetooth.spp.BluetoothSpp;
import io.github.caoshen.bluetooth.spp.BluetoothState;
import io.github.caoshen.bluetooth.spp.OnConnectionStateChangedListener;
import io.github.caoshen.bluetooth.spp.R;

/**
 * @author caoshen
 * @date 2020/12/14
 */
public class ServerActivity extends AppCompatActivity {
    private static final String TAG = "ServerActivity";
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
        setContentView(R.layout.activity_server);

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

    public void startServer(View view) {
        BluetoothSpp.getInstance().start();
    }

    public void startChat(View view) {
        TerminalActivity.start(this);
    }
}
