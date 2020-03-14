package com.example.library.utils;

import java.util.UUID;

public class UUIDUtils {
    /**
     * 手机蓝牙各类服务对应的UUID。5到8个位置不是固定的，即可变化的16位数。其他位是固定的。总共16个字节，128位
     */
    private static final String UUID_FORMAT = "0000%04x-0000-1000-8000-00805f9b34fb";

    /**
     * 从 16 位数构造 UUID
     *
     * @param value value
     * @return UUID
     */
    public static UUID makeUUID(int value) {
        return UUID.fromString(String.format(UUID_FORMAT, value));
    }

    /**
     * 从 UUID 计算值。先取前64位，然后无符号右移32位，得到 value。
     *
     * @param uuid uuid
     * @return value
     */
    public static int getValue(UUID uuid) {
        return ((int) (uuid.getMostSignificantBits() >>> 32));
    }
}
