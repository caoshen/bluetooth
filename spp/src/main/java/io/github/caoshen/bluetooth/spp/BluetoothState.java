package io.github.caoshen.bluetooth.spp;

import android.os.Handler;

/**
 * @author caoshen
 * @date 2020/12/11
 */
public class BluetoothState {
    public static final int STATE_NONE = 0;

    public static final int STATE_LISTEN = 1;

    public static final int STATE_CONNECTING = 2;

    public static final int STATE_CONNECTED = 3;

    public static final int STATE_NULL = -1;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE = "device";
}
