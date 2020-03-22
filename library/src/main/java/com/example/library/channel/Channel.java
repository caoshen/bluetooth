package com.example.library.channel;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.example.library.channel.packet.ACKPacket;
import com.example.library.channel.packet.CTRPacket;
import com.example.library.channel.packet.DataPacket;
import com.example.library.channel.packet.Packet;
import com.example.library.proxy.ProxyBulk;
import com.example.library.proxy.ProxyInterceptor;
import com.example.library.proxy.ProxyUtils;
import com.example.library.utils.ByteUtils;
import com.example.library.utils.ContextUtils;
import com.example.library.utils.LogUtils;
import com.example.library.utils.timer.ExclusiveTimer;
import com.example.library.utils.timer.TimerCallback;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public abstract class Channel implements IChannel {

    private static final int TIMEOUT = 5000;
    private static final int MSG_WRITE_CALLBACK = 1;

    private ChannelState mCurrentState = ChannelState.IDLE;

    private final String mName;

    private byte[] mBytesToWrite;

    /**
     * 收到的包
     */
    private SparseArray<Packet> mPacketRecv;
    private int mCurrentSync;

    /**
     * 发端要记录总字节数
     */
    private int mTotalBytes;

    /**
     * 收端要记录总帧数
     */
    private int mFrameCount;

    private ChannelCallback mChannelCallback;

    private Handler mWorkerHandler;

    private IChannel mChannel;

    private int mLastSync;

    private ExclusiveTimer mTimer;




    /**
     * 收到同步包的回调
     */
    private final IChannelStateHandler mSyncPacketHandler = new IChannelStateHandler() {
        @Override
        public void handleState(Object... args) {
            assertRuntime(false);
            if (args.length < 1) {
                return;
            }
            DataPacket dataPacket = (DataPacket) args[0];
            // 对比序列号
            if (dataPacket.getSeq() != mCurrentSync) {
                LogUtils.w("sync packet not matched!!");
                return;
            }

            if (!onDataPacketRecvd(dataPacket)) {
                LogUtils.w("sync packet repeated!!");
                return;
            }
            // 使用last sync 保存序列号
            mLastSync = mCurrentSync;
            mCurrentSync = 0;

            startSyncPacket();
        }
    };

    /**
     * 收到数据包的回调
     */
    private final IChannelStateHandler mRecvDataHandler = new IChannelStateHandler() {
        @Override
        public void handleState(Object... args) {
            assertRuntime(false);
            if (args.length < 1) {
                return;
            }
            DataPacket dataPacket = (DataPacket) args[0];
            if (!onDataPacketRecvd(dataPacket)) {
                LogUtils.w("dataPacket repeated!!");
                return;
            }
            if (dataPacket.getSeq() == mFrameCount) {
                // 如果最后一帧收到了，说明对端发送完毕了
                startSyncPacket();
            } else {
                // 等到接受数据包，5秒超时
                startTimer(TIMEOUT, "Wait for data packet", new TimerCallback() {
                    @Override
                    public void onTimerCallback() throws TimeoutException {
                        startSyncPacket();
                    }
                });
            }
        }
    };

    /**
     * 收到流控包的回调
     */
    private final IChannelStateHandler mRecvCTRHandler = new IChannelStateHandler() {
        @Override
        public void handleState(Object... args) {
            assertRuntime(false);

            CTRPacket ctrPacket = (CTRPacket) args[0];
            mFrameCount = ctrPacket.getFrameCount();
            ACKPacket ackPacket = new ACKPacket(ACKPacket.READY);

            setCurrentStatus(ChannelState.READY);

            performWrite(ackPacket, new ChannelCallback() {

                @Override
                public void onCallback(int code) {
                    assertRuntime(false);
                    if (code == Code.SUCCESS) {
                        setCurrentStatus(ChannelState.READING);
                        startTimer("Wait for first data packet");
                    } else {
                        resetChannelStatus();
                    }
                }
            });
        }
    };

    private final IChannelStateHandler mWaitStartACKHandler = new IChannelStateHandler() {
        @Override
        public void handleState(Object... args) {
            assertRuntime(false);
            setCurrentStatus(ChannelState.WAIT_START_ACK);
            startTimer("Wait for start ack");
        }
    };


    private TimerCallback mTimeoutHandler = new TimerCallback() {
        @Override
        public void onTimerCallback() throws TimeoutException {
            assertRuntime(false);
            onSendCallback(Code.TIMEOUT);
            resetChannelStatus();
        }
    };

    /**
     * 收到 ACK 包的回调
     */
    private final IChannelStateHandler mRecvACKHandler = new IChannelStateHandler() {
        @Override
        public void handleState(Object... args) {
            assertRuntime(false);

            ACKPacket ackPacket = (ACKPacket) args[0];

            switch (ackPacket.getStatus()) {
                case ACKPacket.READY: {
                    stopTimer();
                    setCurrentStatus(ChannelState.WRITING);
                    sendDataPacket(0, true);
                    break;
                }
                case ACKPacket.SYNC: {
                    int index = ackPacket.getSeq();
                    if (index >= 1 && index <= mFrameCount) {
                        sendDataPacket(index - 1, false);
                        startTimer("wait for next sync ack");
                    }
                    break;
                }
                case ACKPacket.SUCCESS: {
                    onSendCallback(Code.SUCCESS);
                    resetChannelStatus();
                    break;
                }
                default: {
                    onSendCallback(Code.FAIL);
                    resetChannelStatus();
                }
            }
        }
    };

    /**
     * 状态机
     */
    private final ChannelStateBlock[] STATE_MACHINE = {
            new ChannelStateBlock(ChannelState.READY, ChannelEvent.SEND_CTR, mWaitStartACKHandler),
            new ChannelStateBlock(ChannelState.WAIT_START_ACK, ChannelEvent.RECV_ACK, mRecvACKHandler),
            new ChannelStateBlock(ChannelState.SYNC, ChannelEvent.RECV_ACK, mRecvACKHandler),
            new ChannelStateBlock(ChannelState.IDLE, ChannelEvent.RECV_CTR, mRecvCTRHandler),
            new ChannelStateBlock(ChannelState.READING, ChannelEvent.RECV_DATA, mRecvDataHandler),
            new ChannelStateBlock(ChannelState.SYNC_ACK, ChannelEvent.RECV_DATA, mSyncPacketHandler)
    };

    /**
     * 这个函数主要是为了记录写出去的所有包
     * 执行写要放在 UI 线程
     *
     * @param packet packet
     */
    private void performWrite(Packet packet, final ChannelCallback callback) {
        assertRuntime(false);

        // 此次为防止底层写没回调，故抛异常提示，只是为了方便调试，没实际用处
        // 这里假设底层的写是一定有回调的，因为底层写超时也应该有回调
        if (callback == null) {
            throw new NullPointerException("callback can't be null");
        }

        startTimer("Wait for write operation");

        final byte[] bytes = packet.toBytes();
        LogUtils.w(String.format("%s: %s", getLogTag(), packet));

        ContextUtils.post(new Runnable() {
            @Override
            public void run() {
                // write 在主线程执行，callback 在子线程执行
                write(bytes, new WriteCallback(callback));
            }
        });

    }

    private class WriteCallback implements ChannelCallback {

        private final ChannelCallback mCallback;

        public WriteCallback(ChannelCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onCallback(int code) {
            stopTimer();
            mWorkerHandler.obtainMessage(MSG_WRITE_CALLBACK, code, 0, mCallback)
                    .sendToTarget();
        }
    }

    private void sendStartFlowPacket() {
        assertRuntime(false);

        CTRPacket flowPacket = new CTRPacket(mFrameCount);

        performWrite(flowPacket, new ChannelCallback() {
            @Override
            public void onCallback(int code) {
                assertRuntime(false);

                if (code == Code.SUCCESS) {
                    onPostState(ChannelEvent.SEND_CTR);
                } else {
                    onSendCallback(Code.FAIL);
                    resetChannelStatus();
                }
            }
        });
    }

    private void onSendCallback(int code) {
        assertRuntime(false);

        LogUtils.v(String.format("%s: code=%d", getLogTag(), code));

        if (mChannelCallback != null) {
            mChannelCallback.onCallback(code);
        }
    }

    private boolean onDataPacketRecvd(DataPacket packet) {
        assertRuntime(false);

        // 如果对端包发重复了，则直接忽略。sparse array 里面已经有这个 packet
        if (mPacketRecv.get(packet.getSeq()) != null) {
            return false;
        }

        if (packet.getSeq() == mFrameCount) {
            packet.setLastFrame();
        }

        mPacketRecv.put(packet.getSeq(), packet);
        mTotalBytes += packet.getDataLength();
        stopTimer();

        return true;
    }

    /**
     * 认为对端发送完毕了，可以开始同步了
     */
    private void startSyncPacket() {
        assertRuntime(false);
        LogUtils.v(getLogTag());

        startTimer("Wait for sync packet");

        if (!syncLostPacket()) {
            // 所有包都同步完了

            final byte[] bytes = getTotalRecvdBytes();

            if (!ByteUtils.isEmpty(bytes)) {
                ACKPacket ackPacket = new ACKPacket(ACKPacket.SUCCESS);
                performWrite(ackPacket, new ChannelCallback() {
                    @Override
                    public void onCallback(int code) {
                        assertRuntime(false);
                        resetChannelStatus();

                        if (code == Code.SUCCESS) {
                            dispatchOnReceive(bytes);
                        }
                    }
                });
            } else {
                resetChannelStatus();
            }
        } else {
            // 什么都不做
        }
    }

    private void dispatchOnReceive(byte[] bytes) {
        LogUtils.e(String.format("%s.onReceive: %s", mName, new String(bytes)));
        ContextUtils.post(new RecvCallback(bytes));
    }

    private class RecvCallback implements Runnable {

        private final byte[] mBytes;

        public RecvCallback(byte[] bytes) {
            mBytes = bytes;
        }

        @Override
        public void run() {
            onRecv(mBytes);
        }
    }

    private byte[] getTotalRecvdBytes() {
        assertRuntime(false);

        if (mPacketRecv.size() != mFrameCount) {
            throw new IllegalStateException();
        }

        LogUtils.v(String.format("%s: totalBytes = %d", getLogTag(), mTotalBytes));

        ByteBuffer buffer = ByteBuffer.allocate(mTotalBytes);

        for (int i = 1; i <= mFrameCount; i++) {
            DataPacket packet = (DataPacket) mPacketRecv.get(i);

            packet.fillByteBuffer(buffer);

            if (i == mFrameCount) {
                if (!checkCRC(buffer.array(), packet.getCrc())) {
                    LogUtils.e("check crc failed!!");
                    return ByteUtils.EMPTY_BYTES;
                }
            }
        }

        return buffer.array();
    }

    private boolean syncLostPacket() {
        assertRuntime(false);

        LogUtils.v(getLogTag());

        int i;

        for (i = mLastSync + 1; i <= mFrameCount; i++) {
            if (mPacketRecv.get(i) == null) {
                break;
            }
        }

        if (i <= mFrameCount) {
            mCurrentSync = i;

            ACKPacket ackPacket = new ACKPacket(ACKPacket.SYNC, i);
            performWrite(ackPacket, new ChannelCallback() {
                @Override
                public void onCallback(int code) {
                    assertRuntime(false);
                    if (code == Code.SUCCESS) {
                        setCurrentStatus(ChannelState.SYNC_ACK);
                        startTimer("Wait for sync dta coming");
                    } else {
                        resetChannelStatus();
                    }
                }
            });

            return true;
        }

        return false;
    }

    private void resetChannelStatus() {
        assertRuntime(false);
        LogUtils.v(getLogTag());
        stopTimer();
        setCurrentStatus(ChannelState.IDLE);
        mBytesToWrite = null;
        mFrameCount = 0;
        mChannelCallback = null;
        mPacketRecv.clear();
        mCurrentSync = 0;
        mLastSync = 0;
        mTotalBytes = 0;
    }

    /**
     *
     * @param index 包的索引，从 0 开始
     * @param looped 是否要循环发送下一个包，在 sync 阶段是不用 loop 的
     */
    private void sendDataPacket(final int index, final boolean looped) {
        assertRuntime(false);

        if (index >= mFrameCount) {
            LogUtils.v(String.format("%s: all packets sended!!", getLogTag()));
            setCurrentStatus(ChannelState.SYNC);
            startTimer(TIMEOUT * 3, "Wait for sync ack");
            return;
        }

        LogUtils.v(String.format("%s: index = %d, looped = %b", getLogTag(), index + 1, looped));

        int start = index * 18;
        int end = Math.min(mBytesToWrite.length, (index + 1) * 18); // 开区间

        DataPacket dataPacket = new DataPacket(index + 1, mBytesToWrite, start, end);

        performWrite(dataPacket, new ChannelCallback() {
            @Override
            public void onCallback(int code) {
                assertRuntime(false);
                if (code != Code.SUCCESS) {
                    LogUtils.w(String.format(">>> packet %d write failed", index));
                }
                if (looped) {
                    sendDataPacket(index + 1, looped);
                }
            }
        });
    }

    private void setCurrentStatus(ChannelState state) {
        assertRuntime(false);
        LogUtils.v(String.format("%s: state = %s", getLogTag(), state));
        mCurrentState = state;
    }

    private void onPostState(ChannelEvent event, Object... args) {
        assertRuntime(false);

        LogUtils.v(String.format("%s: state = %s, event = %s",
                getLogTag(), mCurrentState, event));

        for (ChannelStateBlock block : STATE_MACHINE) {
            if (block.state == mCurrentState && block.event == event) {
                block.handler.handleState(args);
                break;
            }
        }
    }

    private void assertRuntime(boolean sync) {
        Looper target = sync ? Looper.getMainLooper() : mWorkerHandler.getLooper();
        if (Looper.myLooper() != target) {
            throw new RuntimeException();
        }
    }

    private void performOnRead(byte[] bytes) {
        assertRuntime(false);

        Packet packet = Packet.getPacket(bytes);

        LogUtils.w(String.format("%s: %s", getLogTag(), packet));

        switch (packet.getName()) {
            case Packet.ACK: {
                onPostState(ChannelEvent.RECV_ACK, packet);
                break;
            }
            case Packet.DATA: {
                onPostState(ChannelEvent.RECV_DATA, packet);
                break;
            }
            case Packet.CTR: {
                onPostState(ChannelEvent.RECV_CTR, packet);
                break;
            }
            default: {
                // 非法的包直接忽略
                LogUtils.w(String.format("%s: invalid packet: %s", getLogTag(), packet));
                break;
            }
        }
    }

    /**
     * channel proxy
     */
    private IChannel mChannelImpl = new IChannel() {
        @Override
        public void write(byte[] bytes, ChannelCallback callback) {
            throw new UnsupportedOperationException("write");
        }

        @Override
        public void onRead(byte[] bytes) {
            performOnRead(Arrays.copyOf(bytes, bytes.length));
        }

        @Override
        public void onRecv(byte[] bytes) {
            throw new UnsupportedOperationException("onRecv");
        }

        @Override
        public void send(byte[] value, ChannelCallback callback) {
            performSend(value, callback);
        }

        @Override
        public void close() {
            performClose();
        }
    };

    private void performClose() {
        LogUtils.w(String.format("%s", getLogTag()));
        resetChannelStatus();
        mWorkerHandler.getLooper().quit();
    }

    private void performSend(byte[] value, ChannelCallback callback) {
        assertRuntime(false);

        if (mCurrentState != ChannelState.IDLE) {
            callback.onCallback(Code.BUSY);
            return;
        }

        LogUtils.w(String.format("%s: (%s)", getLogTag(), new String(value)));

        mCurrentState = ChannelState.READY;
        mChannelCallback = ProxyUtils.getUIProxy(callback);

        mTotalBytes = value.length;
        mFrameCount = getFrameCount(mTotalBytes);

        mBytesToWrite = Arrays.copyOf(value, value.length + 2);
        byte[] crc = CRC16.get(value);
        System.arraycopy(crc, 0, mBytesToWrite, value.length, 2);

        sendStartFlowPacket();
    }

    private final ProxyInterceptor mInterceptor = new ProxyInterceptor() {
        @Override
        public boolean onIntercept(Object object, Method method, Object[] args) {
            mWorkerHandler.obtainMessage(0, new ProxyBulk(object, method, args))
                    .sendToTarget();
            return true;
        }
    };

    private final Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_WRITE_CALLBACK) {
                // 如果 msg obj 是一个 callback，就回调
                ChannelCallback callback = (ChannelCallback) msg.obj;
                callback.onCallback(msg.arg1);
            } else {
                if (msg.obj instanceof ProxyBulk) {
                    ProxyBulk.saveInvoke(msg.obj);
                }
            }
            return false;
        }
    };

    private String getLogTag() {
        return String.format("%s.%s", mName, ContextUtils.getCurrentMethodName());
    }

    /**
     * 末尾追加两个字节的 crc，每包发 18 个字节
     *
     * @param totalBytes total bytes
     * @return 分包数
     */
    private int getFrameCount(int totalBytes) {
        int total = totalBytes + 2;
        return (int) Math.ceil(total / 18.0);
    }

    private void startTimer(String name) {
        startTimer(TIMEOUT, name);
    }

    private void startTimer(int duration, String name) {
        startTimer(duration, name, mTimeoutHandler);
    }

    private void startTimer(int duration, String name, TimerCallback callback) {
        assertRuntime(false);
        // channel name and timer name
        mTimer.start(duration, String.format("%s.%s", mName, name), callback);
    }

    private void stopTimer() {
        mTimer.stop();
    }

    private boolean checkCRC(byte[] bytes, byte[] crc0) {
        // 16 位 CRC 校验
        return ByteUtils.equals(crc0, CRC16.get(bytes));
    }

    public Channel(String name) {
        mName = name;
        mPacketRecv = new SparseArray<>();
        mChannel = ProxyUtils.getProxy(mChannelImpl, mInterceptor);

        HandlerThread thread = new HandlerThread(getClass().getSimpleName());
        thread.start();
        mWorkerHandler = new Handler(thread.getLooper(), mCallback);

        mTimer = new ExclusiveTimer();
    }

    @Override
    public void onRead(byte[] bytes) {
        mChannel.onRead(bytes);
    }

    @Override
    public void send(byte[] value, ChannelCallback callback) {
        mChannel.send(value, callback);
    }

    @Override
    public void close() {
        mChannel.close();
    }

    // write 和 onRecv 接口对外暴露，不使用 mChannel 代理。 Channel 是一个抽象类，没有实现所有接口

}
