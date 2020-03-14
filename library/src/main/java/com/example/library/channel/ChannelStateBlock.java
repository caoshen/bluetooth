package com.example.library.channel;

public class ChannelStateBlock {

    /**
     * 状态
     */
    public ChannelState state;

    /**
     * 事件
     */
    public ChannelEvent event;

    /**
     * 事件处理
     */
    public IChannelStateHandler handler;

    public ChannelStateBlock(ChannelState state, ChannelEvent event, IChannelStateHandler handler) {
        this.state = state;
        this.event = event;
        this.handler = handler;
    }
}
