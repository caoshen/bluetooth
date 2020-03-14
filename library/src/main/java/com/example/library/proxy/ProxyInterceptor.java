package com.example.library.proxy;

import java.lang.reflect.Method;

/**
 * 代理拦截器
 */
public interface ProxyInterceptor {
    boolean onIntercept(Object object, Method method, Object[] args);
}
