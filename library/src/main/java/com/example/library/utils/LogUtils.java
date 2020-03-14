package com.example.library.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 日志类
 */
public class LogUtils {

    private static final String LOG_TAG = "bush";

    public static void e(String message) {
        Log.e(LOG_TAG, message);
    }

    public static void w(String message) {
        Log.w(LOG_TAG, message);
    }

    public static void i(String message) {
        Log.i(LOG_TAG, message);
    }

    public static void d(String message) {
        Log.i(LOG_TAG, message);
    }

    public static void v(String message) {
        Log.v(LOG_TAG, message);
    }

    public static void e(Throwable throwable) {
        e(getThrowableString(throwable));
    }

    public static void w(Throwable throwable) {
        w(getThrowableString(throwable));
    }

    private static String getThrowableString(Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        while ( throwable != null) {
            throwable.printStackTrace(printWriter);
            throwable = throwable.getCause();
        }

        String text = writer.toString();
        printWriter.close();
        return text;
    }
}
