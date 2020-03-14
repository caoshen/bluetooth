package com.example.library.channel.packet;

public class CTRPacket extends Packet {
    public CTRPacket(int frames) {
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
