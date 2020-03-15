package com.example.library.channel.packet;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;

public class ACKPacket extends Packet {

    /**
     * 数据同步成功
     */
    public static final int SUCCESS = 0;

    /**
     * 设备就绪
     */
    public static final int READY = 1;

    /**
     * 设备繁忙
     */
    public static final int BUSY = 2;

    /**
     * 同步超时
     */
    public static final int TIMEOUT = 3;

    /**
     * 取消同步
     */
    public static final int CANCEL = 4;

    /**
     * 同步丢包
     */
    public static final int SYNC = 5;

    /**
     * 状态
     */
    private int status;

    /**
     * 序号从 1 开始
     */
    private int seq;

    public ACKPacket(int status) {
        this(status, 0);
    }

    public ACKPacket(int status, int seq) {
        this.status = status;
        this.seq = seq;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Override
    public String getName() {
        return ACK;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.wrap(BUFFER);
        // sn, 2 byte
        buffer.putShort(((short) Packet.SN_CTR));
        // type, 1 byte
        buffer.put((byte) Packet.TYPE_ACK);
        // cmd, 1 byte
        buffer.put((byte) 0);
        // status, 2 byte
        buffer.putShort((short) status);
        // seq, 2 byte
        buffer.putShort((short) seq);
        return buffer.array();
    }

    @Override
    public String toString() {
        return "ACKPacket{" +
                "status=" + getStatusDesc(status) +
                ", seq=" + seq +
                '}';
    }

    private String getStatusDesc(int status) {
        for (Field field :
                getClass().getDeclaredFields()) {
            // 获取所有常量
            if ((field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) > 0) {
                try {
                    // 获取和 status 相同的常量
                    if (field.get(null) == Integer.valueOf(status)) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return String.valueOf(status);
    }
}
