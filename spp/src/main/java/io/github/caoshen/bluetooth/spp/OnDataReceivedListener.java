package io.github.caoshen.bluetooth.spp;

import android.bluetooth.BluetoothDevice;

/**
 * @author caoshen
 * @date 2020/12/15
 */
public interface OnDataReceivedListener {
    void onDataReceived(BluetoothDevice device, String data);

}
