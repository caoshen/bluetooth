package io.github.caoshen.bluetooth.spp;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author caoshen
 * @date 2020/12/15
 */
public class BluetoothSpp {
    private static final String TAG = "BluetoothSpp";

    private Set<BluetoothDevice> mConnectedDeviceSet = new HashSet<>();

    private final Handler HANDLER = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BluetoothState.MESSAGE_STATE_CHANGE: {
                    int arg1 = msg.arg1;
                    Log.d(TAG, "handleMessage: arg1:" + arg1);
                    for (OnConnectionStateChangedListener listener : mStateCallbacks) {
                        listener.onStateChange(arg1);
                    }
                    break;
                }
                case BluetoothState.MESSAGE_DEVICE_NAME: {
                    Bundle data = msg.getData();
                    BluetoothDevice device = data.getParcelable(BluetoothState.DEVICE);
                    mConnectedDeviceSet.add(device);
                    for (OnDeviceConnectionListener listener : mDeviceCallbacks) {
                        listener.onDeviceConnected(device);
                    }
                    break;
                }
                case BluetoothState.MESSAGE_READ: {
                    byte[] buffer = (byte[]) msg.obj;
                    String message = new String(buffer);
                    Bundle data = msg.getData();
                    BluetoothDevice device = data.getParcelable(BluetoothState.DEVICE);
                    for (OnDataReceivedListener listener : mDataCallbacks) {
                        listener.onDataReceived(device, message);
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }
    };

    private Context mContext;
    private BluetoothService mBluetoothService;

    private final List<OnConnectionStateChangedListener> mStateCallbacks = new ArrayList<>();
    private final List<OnDeviceConnectionListener> mDeviceCallbacks = new ArrayList<>();
    private final List<OnDataReceivedListener> mDataCallbacks = new ArrayList<>();

    private BluetoothSpp() {

    }

    public void init(Context context) {
        mContext = context;
        mBluetoothService = new BluetoothService(context, HANDLER);
    }

    public void connect(BluetoothDevice device) {
        mBluetoothService.connect(device);
    }

    public void start() {
        mBluetoothService.start();
    }

    public static BluetoothSpp getInstance() {
        return BluetoothSppInner.INSTANCE;
    }

    public void registerStateCallback(OnConnectionStateChangedListener callback) {
        synchronized (mStateCallbacks) {
            mStateCallbacks.add(callback);
        }
    }

    public void unRegisterStateCallback(OnConnectionStateChangedListener callback) {
        synchronized (mStateCallbacks) {
            mStateCallbacks.remove(callback);
        }
    }

    public void registerDeviceCallback(OnDeviceConnectionListener callback) {
        synchronized (mDeviceCallbacks) {
            mDeviceCallbacks.add(callback);
        }
    }

    public void unRegisterDeviceCallback(OnDeviceConnectionListener callback) {
        synchronized (mDeviceCallbacks) {
            mDeviceCallbacks.remove(callback);
        }
    }

    public void registerDataCallback(OnDataReceivedListener callback) {
        synchronized (mDataCallbacks) {
            mDataCallbacks.add(callback);
        }
    }

    public void unRegisterDataCallback(OnDataReceivedListener callback) {
        synchronized (mDataCallbacks) {
            mDataCallbacks.remove(callback);
        }
    }

    public void send(String message, boolean hasCrlf, BluetoothDevice device) {
        if (mBluetoothService.getState() == BluetoothState.STATE_CONNECTED) {
            if (hasCrlf) {
                message += System.lineSeparator();
            }
            mBluetoothService.write(message.getBytes(), device);
        }
    }

    public Set<BluetoothDevice> getConnectedDeviceSet() {
        return mConnectedDeviceSet;
    }

    private static class BluetoothSppInner {
        private static final BluetoothSpp INSTANCE = new BluetoothSpp();
    }
}
