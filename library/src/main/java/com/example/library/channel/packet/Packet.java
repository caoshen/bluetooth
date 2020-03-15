package com.example.library.channel.packet;

import java.nio.ByteBuffer;

/**
 * 包分为流控包和数据包，流控包又包括指令包和 ACK包
 * 每个包的头两个字节是sn，如果sn=0，则表示流控包，否则表示数据包。sn 序列号
 * 如果是流控包，则 sn后面紧跟一个字节是 type，如果type是0表示指令包，type是1表示ACK包。
 * type后面紧跟一个字节表示cmd，cmd后面紧跟一个int表示parameter。
 */
public abstract class Packet {

    static final int BUFFER_SIZE = 20;

    static final byte[] BUFFER = new byte[BUFFER_SIZE];

    /**
     * 流控包，零表示流控包，非零都表示数据包
     */
    static final int SN_CTR = 0;

    /**
     * 指令包，type 是 0
     */
    public static final int TYPE_CMD = 0x00;
    /**
     * ACK 包，type 是 1
     */
    public static final int TYPE_ACK = 0x01;

    public static final String ACK = "ack";
    public static final String CTR = "ctr";
    public static final String DATA = "data";
    public static final String INVALID = "invalid";

    private static class Header {
        /**
         * 序列号
         */
        int sn;
        /**
         * packet 类型
         */
        int type;
        /**
         * 命令
         */
        int command;
        /**
         * 命令参数
         */
        int parameter;
        /**
         * 值
         */
        byte[] value;
    }

    static class Bytes {
        byte[] value;
        int start;
        // [start, end) 前闭后开
        int end;

        public Bytes(byte[] value, int start, int end) {
            this.value = value;
            this.start = start;
            this.end = end;
        }

        public Bytes(byte[] value, int start) {
            this(value, start, value.length);
        }

        public int getSize() {
            return end - start;
        }
    }

    private static Header parse(byte[] bytes) {
        Header header = new Header();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        header.sn = buffer.getShort();
        header.value = bytes;

        if (header.sn == SN_CTR) {
            header.type = buffer.get();
            header.command = buffer.get();
            header.parameter = buffer.getInt();
        }
        return header;
    }

    public static Packet getPacket(byte[] bytes) {
        Header header = parse(bytes);

        if (header.sn == 0) {
            return getFlowPacket(header);
        } else {
            return getDataPacket(header);
        }
    }

    private static Packet getDataPacket(Header header) {
        return new DataPacket(header.sn, new Bytes(header.value, 2));
    }

    private static Packet getFlowPacket(Header header) {
        if (header.type == 0) {
            // ctr，右移 16 位
            int frames = header.parameter >> 16;
            return new CTRPacket(frames);
        } else if (header.type == 1) {
            // ack，右移 16 位
            int status = header.parameter >> 16;
            // 低16位
            int seq = header.parameter & 0xffff;
            return new ACKPacket(status, seq);
        } else {
            // invalid
            return new InvalidPacket();
        }
    }

    public abstract String getName();

    public abstract byte[] toBytes();
}
