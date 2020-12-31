#define _JNILOG_TAG "opus_jni"

#include <assert.h>
#include <pthread.h>
#include "libopus/include/opus.h"
#include <jni.h>

//public native long createOpusEncoder(int sampleRateInHz, int channel, int bitRate);
//
//public native int encodeOpus(long enc, short[] buffer, int offset, byte[] encoded);
//
//public native void destroyOpusEncoder(long enc);

//https://github.com/qingkouwei/AndroidOpusTools/blob/master/library/src/main/cpp/media_jni.c

static jlong
createOpusEncoder(JNIEnv *env, jobject thiz, jint sampleRate, jint channel, jint bitRate, jint complexity) {
    int error;
    OpusEncoder *pOpusEnc = opus_encoder_create(sampleRate, channel, OPUS_APPLICATION_RESTRICTED_LOWDELAY, &error);

    if (pOpusEnc) {
        opus_encoder_ctl(pOpusEnc, OPUS_SET_VBR(0));

    }

    return (jlong) pOpusEnc;
}