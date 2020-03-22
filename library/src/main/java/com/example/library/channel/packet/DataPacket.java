package com.example.library.channel.packet;

import com.example.library.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class DataPacket extends Packet {

    private int seq;

    private Bytes bytes;

    // only last frame has crc
    private byte[] crc;

    public DataPacket(int seq, Bytes bytes) {
        this.seq = seq;
        this.bytes = bytes;
    }

    public DataPacket(int seq, byte[] value, int start, int end) {
        this(seq, new Bytes(value, start, end));
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public Bytes getBytes() {
        return bytes;
    }

    public void setBytes(Bytes bytes) {
        this.bytes = bytes;
    }

    public byte[] getCrc() {
        return crc;
    }

    public void setCrc(byte[] crc) {
        this.crc = crc;
    }

    public int getDataLength() {
        return bytes.getSize();
    }

    public void setLastFrame() {
        // 将 bytes 的 end 往前移 2 位
        bytes.end -= 2;
        // 得到后 2 位的 byte
        crc = ByteUtils.get(bytes.value, bytes.end, 2);
    }

    @Override
    public String getName() {
        return DATA;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer;
        int packetSize = getDataLength() + 2;
        if (packetSize == BUFFER_SIZE) {
            Arrays.fill(BUFFER, (byte) 0);
            buffer = ByteBuffer.wrap(BUFFER);
        } else {
            buffer = ByteBuffer.allocate(packetSize);
        }
        buffer.putShort((short) seq);
        fillByteBuffer(buffer);
        return buffer.array();
    }

    public void fillByteBuffer(ByteBuffer buffer) {
        buffer.put(bytes.value, bytes.start, getDataLength());
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "seq=" + seq +
                ", size=" + bytes.getSize() +
                ", bytes=" + bytes +
                ", crc=" + Arrays.toString(crc) +
                '}';
    }
}
