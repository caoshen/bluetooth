package io.github.caoshen.bluetooth.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author caoshen
 * @date 2020/12/11
 */
class BluetoothService {
    private static final String TAG = "BluetoothService";

    private static final UUID UUID_ANDROID_DEVICE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private static final String NAME_SECURE = "Bluetooth Secure";

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private final ExecutorService mExecutor;
    private BluetoothServerSocket mServerSocket;
    private boolean isRunning = true;

    private HashMap<BluetoothDevice, ConnectedRunnable> mConnectedMap;

    private int mState;
    private Runnable mAcceptRunnable;
    private ConnectRunnable mConnectRunnable;

    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mState = BluetoothState.STATE_NONE;
        mConnectedMap = new HashMap<>();
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        mExecutor = new ThreadPoolExecutor(corePoolSize * 2, corePoolSize * 2 + 1,
                30L, TimeUnit.SECONDS,
                new SynchronousQueue<>());
    }

    private synchronized void setState(int state) {
        mState = state;
        Message message = Message.obtain(mHandler, BluetoothState.MESSAGE_STATE_CHANGE, state, -1);
        mHandler.sendMessage(message);
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        setState(BluetoothState.STATE_LISTEN);

        if (mAcceptRunnable == null) {
            mAcceptRunnable = new AcceptRunnable("accept-runnable");
            mExecutor.execute(mAcceptRunnable);
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        if (mState == BluetoothState.STATE_CONNECTING) {
            if (mConnectRunnable != null) {
                mConnectRunnable.cancel();
                mConnectRunnable = null;
            }
        }

        if (mConnectRunnable == null) {
            mConnectRunnable = new ConnectRunnable(device);
            mExecutor.execute(mConnectRunnable);
        }
        setState(BluetoothState.STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        ConnectedRunnable connectedRunnable = new ConnectedRunnable(socket, device);
        mExecutor.execute(connectedRunnable);

        // save all devices connected to my socket
        mConnectedMap.put(device, connectedRunnable);

        Message message = Message.obtain(mHandler, BluetoothState.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BluetoothState.DEVICE, device);
        message.setData(bundle);
        mHandler.sendMessage(message);

        setState(BluetoothState.STATE_CONNECTED);
    }

    private void connectionFailed() {
        // start the service over to restart listening mode
        start();
    }

    private void connectionLost() {

    }

    public void write(byte[] buffer, BluetoothDevice device) {
        ConnectedRunnable connectedRunnable = mConnectedMap.get(device);
        if (connectedRunnable == null) {
            Log.e(TAG, "write: not connected for:" + device.getName() + ", address:" + device.getAddress());
            return;
        }
        synchronized (this) {
            if (mState != BluetoothState.STATE_CONNECTED) {
                return;
            }
            connectedRunnable.write(buffer);
        }
    }

    private class AcceptRunnable implements Runnable {

        private final String mName;

        public AcceptRunnable(String name) {
            mName = name;
            try {
                mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_ANDROID_DEVICE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BluetoothSocket socket = null;

            while (isRunning) {
                try {
                    Log.d(TAG, "accept runnable: accept.");
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        Log.d(TAG, "accept runnable: connected.");
                        connected(socket, socket.getRemoteDevice());
                    }
                }
            }
        }

        public void cancel() {
            try {
                mServerSocket.close();
                mServerSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void kill() {
            isRunning = false;
        }

    }
    private class ConnectRunnable implements Runnable {

        private BluetoothSocket mSocket;

        private BluetoothDevice mDevice;

        public ConnectRunnable(BluetoothDevice device) {
            mDevice = device;
            try {
                mSocket = device.createRfcommSocketToServiceRecord(UUID_ANDROID_DEVICE);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void run() {
            mAdapter.cancelDiscovery();

            try {
                mSocket.connect();
            } catch (IOException exception) {
                Log.e(TAG, "run: exception:" + exception);
                exception.printStackTrace();

                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "run: close:" + e);
                    e.printStackTrace();
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothService.this) {
                mConnectRunnable = null;
            }

            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

    }

    private class ConnectedRunnable implements Runnable {

        private final BluetoothDevice mDevice;

        private BluetoothSocket mSocket;

        private InputStream mInputStream;

        private OutputStream mOutputStream;

        public ConnectedRunnable(BluetoothSocket socket, BluetoothDevice device) {
            mSocket = socket;
            mDevice = device;

            try {
                mInputStream = socket.getInputStream();
                mOutputStream = socket.getOutputStream();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[4096];
            ArrayList<Integer> arrByte = new ArrayList<>();

            while (true) {
                try {
                    int len = mInputStream.read(buffer);
                    if (len < 0) {
                        continue;
                    }
                    byte[] data = new byte[len];
                    System.arraycopy(buffer, 0, data, 0, len);

                    Message message = Message.obtain(mHandler, BluetoothState.MESSAGE_READ,
                            data.length, -1, data);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(BluetoothState.DEVICE, mDevice);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } catch (IOException exception) {
                    connectionLost();
                    BluetoothService.this.start();
                    exception.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mOutputStream.write(buffer);
                Message message = Message.obtain(mHandler, BluetoothState.MESSAGE_WRITE,
                        -1, -1, buffer);
                mHandler.sendMessage(message);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException exception) {
            }
        }

    }

}
