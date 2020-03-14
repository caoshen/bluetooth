package com.example.library.channel;

public interface IChannel {

    /**
     * 底层写数据
     *
     * @param bytes  bytes data
     * @param callback write callback
     */
    void write(final byte[] bytes, ChannelCallback callback);

    /**
     * 通知底层读到数据
     *
     * @param bytes bytes data
     */
    void onRead(final byte[] bytes);

    /**
     * 通知上层收到数
     *
     * @param bytes bytes data
     */
    void onRecv(byte[] bytes);

    /**
     * 上层发数据
     *
     * @param value send value
     * @param callback send callback
     */
    void send(byte[] value, ChannelCallback callback);

    /**
     * 释放资源
     */
    void close();
}
