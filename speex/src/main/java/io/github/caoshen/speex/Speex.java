package io.github.caoshen.speex;

/**
 * @author caoshen
 * @date 2020/11/20
 */
public class Speex {
    static {
        System.loadLibrary("speex");
    }

    public native int open(int compression);

    public native int getFrameSize();

    public native int decode(byte[] encoded, short lin[], int size);

    public native int encode(short lin[], int offset, byte[] encoded, int size);

    public native void close();
}
