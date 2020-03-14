package com.example.library.utils;

public class ByteUtils {

    public static final byte[] EMPTY_BYTES = new byte[]{};

    public static boolean isEmpty(byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    public static byte[] getNonEmptyByte(byte[] bytes) {
        if (bytes == null) {
            return EMPTY_BYTES;
        } else {
            return bytes;
        }
    }

    public static String byteToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!isEmpty(bytes)) {
            for (byte aByte : bytes) {
                // 每个字节转换位2位16进制数，比如 0x01
                stringBuilder.append(String.format("%02x", aByte));
            }
        }
        return stringBuilder.toString();
    }

    public static byte[] stringToBytes(String text) {
        int len = text.length();
        // 两个字母是一个字节，比如 "01" -> 0x01
        byte[] bytes = new byte[(len + 1) / 2];
        for (int i = 0; i < len; i += 2) {
            // 如果剩余长度大于 2，就取 2
            int size = Math.min(2, len - i);
            String sub = text.substring(i, i + size);
            // 先转换位16进制数，再放入字节数组
            bytes[i / 2] = (byte) Integer.parseInt(sub, 16);
        }
        return bytes;
    }

    public static byte[] fromInt(int n) {
        // 一个整数是 4 个字节
        byte[] bytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            // little endian
            bytes[i] = (byte) (n >>> (i * 8));
        }
        return bytes;
    }

    public static byte[] fromLong(long n) {
        // 一个长整型是 8 个字节
        byte[] bytes = new byte[8];

        for (int i = 0; i < 8; i++) {
            // little endian
            bytes[i] = (byte) (n >>> (i * 8));
        }
        return bytes;
    }

    public static byte[] fromShort(short n) {
        // 一个短整型是 2 个字节
        byte[] bytes = new byte[2];

        for (int i = 0; i < 2; i++) {
            // little endian
            bytes[i] = (byte) (n >>> (i * 8));
        }
        return bytes;
    }

    /**
     * 判断字节数组的每一位是否想等
     *
     * @param lBytes left bytes
     * @param rBytes right bytse
     * @return is equal
     */
    public static boolean byteEquals(byte[] lBytes, byte[] rBytes) {
        if (lBytes == null && rBytes == null) {
            return true;
        }
        if (lBytes == null || rBytes == null) {
            return false;
        }

        int lLen = lBytes.length;
        int rLen = rBytes.length;
        if (lLen != rLen) {
            return false;
        }
        for (int i = 0; i < lLen; i++) {
            if (lBytes[i] != rBytes[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] get(byte[] bytes, int offset, int len) {
        byte[] result = new byte[len];
        System.arraycopy(bytes, offset, result, 0, len);
        return result;
    }

    public static byte[] get(byte[] bytes, int offset) {
        return get(bytes, offset, bytes.length - offset);
    }

    public static boolean equals(byte[] array1, byte[] array2, int len) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length < len || array2.length < len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(byte[] array1, byte[] array2) {
        return equals(array1, array2, Math.min(array1.length, array2.length));
    }

}
