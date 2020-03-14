package com.example.library.utils.timer;

import com.example.library.utils.LogUtils;

import java.util.concurrent.TimeoutException;

public abstract class TimerCallback implements Runnable {
    private String name;

    @Override
    public void run() {
        LogUtils.e(String.format("%s: Timer expired!!!", name));
        try {
            onTimerCallback();
        } catch (TimeoutException e) {
            LogUtils.e(e);
        }
    }

    public abstract void onTimerCallback() throws TimeoutException;

    public void onTimerCanceled() {
        // do nothing here
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
