package com.example.library.channel.packet;

import java.nio.ByteBuffer;

public class CTRPacket extends Packet {

    private int frameCount;
    public CTRPacket(int frames) {
        this.frameCount = frames;
    }

    @Override
    public String getName() {
        return CTR;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.wrap(BUFFER);
        buffer.putShort((short) Packet.SN_CTR);
        buffer.put((byte) Packet.TYPE_CMD);
        // ctr包的command暂设为空
        buffer.put((byte) 0);
        // 帧数量
        buffer.putShort((short) frameCount);
        return buffer.array();
    }

    @Override
    public String toString() {
        return "CTRPacket{" +
                "frameCount=" + frameCount +
                '}';
    }
}
