package io.github.caoshen.opus;

/**
 * @author caoshen
 * @date 2020/12/31
 */
public class Opus {

    static {
        System.loadLibrary("opus");
    }

    public native long createOpusEncoder(int sampleRateInHz, int channel, int bitRate, int complexity);

//    public native int encodeOpus(long enc, short[] buffer, int offset, byte[] encoded);
//
//    public native void destroyOpusEncoder(long enc);
}
