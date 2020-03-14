package com.example.library.proxy;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class ProxyInvocationHandler implements InvocationHandler, ProxyInterceptor, Callback {

    private Object subject;

    private ProxyInterceptor interceptor;

    private boolean weakRef;

    private boolean postUI;

    private Handler handler;

    public ProxyInvocationHandler(Object subject, ProxyInterceptor interceptor, boolean weakRef, boolean postUI) {
        this.subject = getObject(subject);
        this.interceptor = interceptor;
        this.weakRef = weakRef;
        this.postUI = postUI;
        handler = new Handler(Looper.getMainLooper(), this);
    }

    @Override
    public boolean onIntercept(Object object, Method method, Object[] args) {
        if (interceptor != null) {
            return interceptor.onIntercept(object, method, args);
        }
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object subject = getObject();

        if (!onIntercept(subject, method, args)) {
            ProxyBulk proxyBulk = new ProxyBulk(subject, method, args);;
            return postUI ? postSafeInvoke(proxyBulk) : safeInvoke(proxyBulk);
        }
        return null;
    }

    /**
     * 返回一个弱引用
     *
     * @param object object
     * @return object or weak reference
     */
    private Object getObject(Object object) {
        return weakRef ? new WeakReference<Object>(object) : object;
    }

    @SuppressWarnings("unchecked")
    private Object getObject() {
        if (weakRef) {
            return ((WeakReference<Object>) subject).get();
        } else {
            return subject;
        }
    }

    /**
     * 将代理发送到主线程执行
     *
     * @param bulk proxy bulk
     * @return null
     */
    private Object postSafeInvoke(ProxyBulk bulk) {
        Message message = handler.obtainMessage(0, bulk);
        message.sendToTarget();
        return null;
    }

    private Object safeInvoke(ProxyBulk bulk) {
        return bulk.safeInvoke();
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        ProxyBulk.saveInvoke(msg.obj);
        // true 表示 callback 对应的 handler 在 dispatchMessage 时不会执行 handleMessage 方法。
        return true;
    }
}
