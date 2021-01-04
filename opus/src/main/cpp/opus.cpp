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
        opus_encoder_ctl(pOpusEnc, OPUS_SET_VBR_CONSTRAINT(true));
        opus_encoder_ctl(pOpusEnc, OPUS_SET_BITRATE(bitRate * 1000));
        opus_encoder_ctl(pOpusEnc, OPUS_SET_COMPLEXITY(complexity));
        opus_encoder_ctl(pOpusEnc, OPUS_SET_SIGNAL(OPUS_SIGNAL_VOICE));
        opus_encoder_ctl(pOpusEnc, OPUS_SET_LSB_DEPTH(16));
        opus_encoder_ctl(pOpusEnc, OPUS_SET_DTX(0));
        opus_encoder_ctl(pOpusEnc, OPUS_SET_INBAND_FEC(0));
        opus_encoder_ctl(pOpusEnc, OPUS_SET_PACKET_LOSS_PERC(0));
    }

    return (jlong) pOpusEnc;
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    // register natives
    jclass clazz = env->FindClass("io/github/caoshen/opus/Opus");
    if (clazz == NULL) {
        return JNI_ERR;
    }
    JNINativeMethod g_methods[] = {
            {"createOpusEncoder", "(IIII)J", (void *) createOpusEncoder}
    };
    jint result = env->RegisterNatives(clazz, g_methods, sizeof(g_methods) / sizeof(g_methods[0]));
    return JNI_VERSION_1_6;
}

