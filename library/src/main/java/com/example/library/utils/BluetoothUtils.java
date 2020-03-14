package com.example.library.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.UUID;

public class BluetoothUtils {


    public static boolean isConnected(BluetoothDevice device) {
        return getBluetoothConnectionState(device) == BluetoothGatt.STATE_CONNECTED;
    }

    public static boolean isBluetoothOpen() {
        return getBluetoothAdapter().isEnabled();
    }

    /**
     * open bluetooth.Need bluetooth admin permission.
     */
    public static void openBluetooth() {
        getBluetoothAdapter().enable();
    }

    /**
     * open bluetooth use a permission dialog
     *
     * @param activity    activity
     * @param requestCode requestCode
     */
    public static void openBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    public static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }


    public static int getBluetoothConnectionState(BluetoothDevice device) {
        return getBluetoothManager().getConnectionState(device, BluetoothProfile.GATT);
    }

    public static int getBluetoothConnectionState(String mac) {
        BluetoothDevice device = getBluetoothDevice(mac);
        if (device != null) {
            return getBluetoothConnectionState(device);
        } else {
            return -1;
        }
    }

    public static List<BluetoothDevice> getConnectedDevices() {
        return getBluetoothManager().getConnectedDevices(BluetoothProfile.GATT);
    }

    private static BluetoothDevice getBluetoothDevice(String mac) {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (adapter != null) {
            return adapter.getRemoteDevice(mac);
        } else {
            return null;
        }
    }

    public static BluetoothManager getBluetoothManager() {
        return (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public static BluetoothGattCharacteristic getCharacteristic(BluetoothGatt gatt, UUID serviceUUID, UUID characterUUID) {
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service != null) {
            return service.getCharacteristic(characterUUID);
        } else {
            return null;
        }
    }

    /**
     * enable or disable notification
     *
     * @param gatt      gatt
     * @param service   service uuid
     * @param character character uuid
     * @param enable    enable
     * @return result
     */
    public static boolean setCharacteristicNotification(BluetoothGatt gatt, UUID service, UUID character, boolean enable) {
        if (gatt == null) {
            return false;
        }
        if (service == null) {
            return false;
        }
        if (character == null) {
            return false;
        }

        BluetoothGattCharacteristic characteristic = getCharacteristic(gatt, service, character);
        if (characteristic == null) {
            return false;
        }
        if (!gatt.setCharacteristicNotification(characteristic, enable)) {
            return false;
        }

        UUID clientCharConfig = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(clientCharConfig);
        if (descriptor == null) {
            return false;
        }

        byte[] value = enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;

        if (!descriptor.setValue(value)) {
            return false;
        }

        return gatt.writeDescriptor(descriptor);
    }

    public static BluetoothGattServer openGattServer(BluetoothGattServerCallback callback) {
        Context context = getContext();
        return getBluetoothManager().openGattServer(context, callback);
    }

    public static BluetoothLeAdvertiser getAdvertiser() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (!adapter.isEnabled()) {
            return null;
        }
        if (adapter.isMultipleAdvertisementSupported()) {
            return adapter.getBluetoothLeAdvertiser();
        }
        return null;
    }

    public static Context getContext() {
        return ContextUtils.getContext();
    }

}
