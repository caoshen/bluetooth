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
    private BluetoothServerSocket mServerSocket;
    private boolean isRunning = true;

    private AcceptThread mAcceptThread1;
    private AcceptThread mAcceptThread2;
    private ConnectThread mConnectThread;

    private HashMap<BluetoothDevice, ConnectedThread> mConnectedMap;

    private int mState;

    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        mState = BluetoothState.STATE_NONE;
        mConnectedMap = new HashMap<>();
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

        if (mAcceptThread1 == null) {
            mAcceptThread1 = new AcceptThread("accept-thread-1");
            mAcceptThread1.start();
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        if (mState == BluetoothState.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectThread == null) {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        }
        setState(BluetoothState.STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
//        if (mAcceptThread1 != null) {
//            mAcceptThread1.cancel();
//            mAcceptThread1 = null;
//        }

        ConnectedThread connectedThread = new ConnectedThread(socket, device);
        connectedThread.start();
        mConnectedMap.put(device, connectedThread);

        Message message = Message.obtain(mHandler, BluetoothState.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putParcelable(BluetoothState.DEVICE, device);
        message.setData(bundle);
        mHandler.sendMessage(message);

        setState(BluetoothState.STATE_CONNECTED);
    }

    private class AcceptThread extends Thread {

        private final String mName;

        public AcceptThread(String name) {
            mName = name;
            try {
                mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_ANDROID_DEVICE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            setName("AcceptThread:" + mName);
            BluetoothSocket socket = null;

            while (isRunning) {
                try {
                    Log.d(TAG, "accept thread: " + getName() + ", accept");
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        Log.d(TAG, "accept thread: " + getName() + ", connected");
                        connected(socket, socket.getRemoteDevice());

//                        switch (mState) {
//                            case BluetoothState.STATE_LISTEN:
//                            case BluetoothState.STATE_CONNECTING: {
//                                connected(socket, socket.getRemoteDevice());
//                                break;
//                            }
//                            case BluetoothState.STATE_NONE:
//                            case BluetoothState.STATE_CONNECTED: {
//                                try {
//                                    socket.close();
//                                } catch (IOException exception) {
//                                    exception.printStackTrace();
//                                }
//                                break;
//                            }
//                            default: {
//                                break;
//                            }
//                        }
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

    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;

        private BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device) {
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
                Log.e(TAG, "run: ConnectThread: exception:" + exception);
                exception.printStackTrace();

                try {
                    mSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "run: ConnectThread: close:" + e);
                    e.printStackTrace();
                }
                connectionFailed();
                return;
            }

            synchronized (BluetoothService.this) {
                mConnectThread = null;
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

    private void connectionFailed() {
        // start the service over to restart listening mode
        start();
    }

    private void connectionLost() {

    }

    private class ConnectedThread extends Thread {

        private final BluetoothDevice mDevice;
        private BluetoothSocket mSocket;

        private InputStream mInputStream;

        private OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket, BluetoothDevice device) {
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

    public void write(byte[] buffer, BluetoothDevice device) {
        ConnectedThread connectedThread = mConnectedMap.get(device);
        if (connectedThread == null) {
            Log.e(TAG, "write: no connected thread for:" + device.getName() + ", address:" + device.getAddress());
            return;
        }
        synchronized (this) {
            if (mState != BluetoothState.STATE_CONNECTED) {
                return;
            }
            connectedThread.write(buffer);
        }
    }

}
