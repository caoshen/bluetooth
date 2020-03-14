package com.example.library.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

public class ContextUtils {

    private static Context mContext;

    private static Handler mHandler;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static Handler getHandler() {
        return mHandler;
    }

    public static void setHandler(Handler handler) {
        mHandler = handler;
    }

    public static void post(Runnable runnable) {
        mHandler.post(runnable);
    }

    public static String getCurrentThreadName() {
        // why use index 4 ?
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        return stackTraceElement.getMethodName();
    }

    public static void assertRuntime(boolean mainThread) {
        if (mainThread && Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException();
        }

        if (!mainThread && Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException();
        }

    }
}
