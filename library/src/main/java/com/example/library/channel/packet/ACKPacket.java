package com.example.library.channel.packet;

public class ACKPacket extends Packet {
    public ACKPacket(int status, int seq) {
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
