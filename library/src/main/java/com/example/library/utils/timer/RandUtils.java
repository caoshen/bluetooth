package com.example.library.utils.timer;

import java.util.Random;

public class RandUtils {
    private static Random mRandom;

    static {
        mRandom = new Random(System.currentTimeMillis());
    }

    public static int nextInt(int bound) {
        return mRandom.nextInt(bound);
    }
}
