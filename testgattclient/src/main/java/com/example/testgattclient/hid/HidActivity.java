package com.example.testgattclient.hid;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppQosSettings;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.concurrent.Executor;

public class HidActivity extends Activity {
    private BluetoothProfile.ServiceListener mProfileServiceListener = new BluetoothProfile.ServiceListener() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == BluetoothProfile.HEALTH) {
                mHidDevice = (BluetoothHidDevice) proxy;
                // 获取代理对象之后就进行注册

            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == BluetoothProfile.HEALTH) {
                mBluetoothHealth = null;
            }
        }
    };
    private Object mBluetoothHealth;
    private BluetoothHidDevice mHidDevice;
    private Executor executor = new Executor() {
        @Override
        public void execute(Runnable command) {

        }
    };
    @RequiresApi(api = Build.VERSION_CODES.P)
    private BluetoothHidDevice.Callback callback = new BluetoothHidDevice.Callback() {
        @Override
        public void onAppStatusChanged(BluetoothDevice pluggedDevice, boolean registered) {
            super.onAppStatusChanged(pluggedDevice, registered);
        }

        @Override
        public void onConnectionStateChanged(BluetoothDevice device, int state) {
            super.onConnectionStateChanged(device, state);
        }

        @Override
        public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {
            super.onGetReport(device, type, id, bufferSize);
        }

        @Override
        public void onSetReport(BluetoothDevice device, byte type, byte id, byte[] data) {
            super.onSetReport(device, type, id, data);
        }

        @Override
        public void onSetProtocol(BluetoothDevice device, byte protocol) {
            super.onSetProtocol(device, protocol);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothAdapter.getDefaultAdapter().getProfileProxy(getApplicationContext(),
                mProfileServiceListener, BluetoothProfile.HID_DEVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void registerApp() {
        BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                HidConsts.NAME, HidConsts.DESCRIPTION, HidConsts.PROVIDER,
                BluetoothHidDevice.SUBCLASS1_COMBO, HidConsts.DESCRIPTOR);
        BluetoothHidDeviceAppQosSettings inQos = new BluetoothHidDeviceAppQosSettings(
                BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED, 200, 2, 200,
                10000 /* 10ms */, 10000 /* 10 ms */);
        BluetoothHidDeviceAppQosSettings outQos = new BluetoothHidDeviceAppQosSettings(
                BluetoothHidDeviceAppQosSettings.SERVICE_GUARANTEED, 900, 9, 900,
                10000 /* 10ms */, 10000 /* 10 ms */);

        boolean result = mHidDevice.registerApp(sdp, inQos, outQos, executor, callback);


    }

    public void connect() {
//        mHidDevice.connect();
    }
}
