package com.example.testgattclient;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static class Pdu {
        int type;
        byte[] data;
    }

    public static List<Pdu> parse(byte[] bytes) {
        List<Pdu> pdus = new ArrayList<>();
        parse(bytes, pdus, 0);
        return pdus;
    }

    private static void parse(byte[] bytes, List<Pdu> pdus, int offset) {
        // len --- type --- value
        if (offset >= bytes.length - 2) {
            return;
        }
        // get len from bytes
        int len = bytes[offset];
        if (len == 0) {
            return;
        }
        int type = bytes[offset + 1];
        int start = offset + 2;
        int end = offset + len;

        // exceed length
        if (end >= bytes.length) {
            end = bytes.length - 1;
            len = end - start + 1;
        }
        Pdu pdu = new Pdu();
        // make bits accord to its complement
        pdu.type = type & 0xff;
        pdu.data = new byte[len - 1];
        System.arraycopy(bytes, start, pdu.data, 0, len - 1);
        pdus.add(pdu);
        // parse recursive
        parse(bytes, pdus, end + 1);
    }
}
