package com.example.library.channel.packet;

import androidx.annotation.NonNull;

public class InvalidPacket extends Packet {
    @Override
    public String getName() {
        return INVALID;
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @NonNull
    @Override
    public String toString() {
        return "InvalidPackage{}";
    }
}
