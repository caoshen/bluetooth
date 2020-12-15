package io.github.caoshen.bluetooth.spp;

import android.app.Application;

/**
 * @author caoshen
 * @date 2020/12/15
 */
public class SppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothSpp.getInstance().init(this);
    }
}
