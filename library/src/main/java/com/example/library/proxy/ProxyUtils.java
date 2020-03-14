package com.example.library.proxy;

import java.lang.reflect.Proxy;

/**
 * 代理工具类
 */
public class ProxyUtils {

    public static <T> T getProxy(Object object, Class<?>[] interfaces, ProxyInterceptor interceptor, boolean weakRef, boolean postUI) {
        ClassLoader classLoader = object.getClass().getClassLoader();
        ProxyInvocationHandler invocationHandler = new ProxyInvocationHandler(object, interceptor, weakRef, postUI);
        return (T) Proxy.newProxyInstance(classLoader, interfaces, invocationHandler);
    }

    public static <T> T getProxy(Object object, Class<?> aInterface, ProxyInterceptor interceptor, boolean weakRef, boolean postUI) {
        return getProxy(object, new Class<?>[]{aInterface}, interceptor, weakRef, postUI);
    }

    public static <T> T getProxy(Object object, Class<?> aInterface, ProxyInterceptor interceptor) {
        return getProxy(object, aInterface, interceptor, false, false);
    }

    public static <T> T getProxy(Object object, ProxyInterceptor interceptor) {
        return getUIProxy(object, object.getClass().getInterfaces(), interceptor);
    }

    public static <T> T getWeakUIProxy(Object object, Class<?> clazz) {
        return getProxy(object, clazz, null, true, true);
    }

    public static <T> T getUIProxy(Object object) {
        return getUIProxy(object, object.getClass().getInterfaces(), null);
    }

    public static <T> T getUIProxy(Object object, Class<?> clazz) {
        return getUIProxy(object, new Class<?>[]{clazz}, null);
    }

    public static <T> T getUIProxy(Object object, Class<?> clazz, ProxyInterceptor interceptor) {
        return getUIProxy(object, new Class<?>[]{clazz}, interceptor);
    }

    public static <T> T getUIProxy(Object object, ProxyInterceptor interceptor) {
        return getUIProxy(object, object.getClass().getInterfaces(), interceptor);
    }

    public static <T> T getUIProxy(Object object, Class<?>[] interfaces, ProxyInterceptor interceptor) {
        return getProxy(object, interfaces, interceptor, false, true);
    }

}
