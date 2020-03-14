package com.example.library.channel.packet;

public class InvalidPacket extends Packet {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }
}
