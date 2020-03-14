package com.example.library.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProxyBulk {
    public Object object;
    public Method method;
    public Object[] args;

    public ProxyBulk(Object object, Method method, Object[] args) {
        this.object = object;
        this.method = method;
        this.args = args;
    }

    public Object safeInvoke() {
        Object result = null;
        try {
            result = method.invoke(object, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Object saveInvoke(Object object) {
        return ((ProxyBulk) object).safeInvoke();
    }
}
