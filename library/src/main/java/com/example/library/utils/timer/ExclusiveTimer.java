package com.example.library.utils.timer;

import android.os.Handler;
import android.os.Looper;

/**
 * 排他的计时器，启动timer时会给之前的timer清除，只保留一个
 */
public class ExclusiveTimer {
    private Handler mHandler;

    private TimerCallback mCallback;

    /**
     * 在哪个线程起的 timer 超时了就必须在那个线程回调
     * @param duration
     * @param name
     * @param callback
     */
    public synchronized void start(int duration, String name, TimerCallback callback) {
        abandonOldTimer();
        Looper looper = Looper.myLooper();
        if (looper == null) {
            // 如果当前线程 looper 为空，就用主线程的 looper
            looper = Looper.getMainLooper();
            mHandler = new Handler(looper);
            callback.setName(name);
            // 延迟 duration 时间执行
            mHandler.postDelayed(callback, duration);
        }
    }

    public synchronized void stop() {
        abandonOldTimer();
    }

    private void abandonOldTimer() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mCallback != null) {
            // 移除 timer，回调 onCancel 方法
            mCallback.onTimerCanceled();
            mCallback = null;
        }
    }
}
