package com.example.library.channel.packet;

public class DataPacket extends Packet {
    public DataPacket(int sn, Bytes bytes) {
        super();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
