#include <jni.h>
#include <string.h>
#include <unistd.h>
//#include <>

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     io_github_caoshen_speex_Speex
 * Method:    open
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_io_github_caoshen_speex_Speex_open
        (JNIEnv *, jobject, jint) {

}

/*
 * Class:     io_github_caoshen_speex_Speex
 * Method:    getFrameSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_io_github_caoshen_speex_Speex_getFrameSize
        (JNIEnv *, jobject) {

}

/*
 * Class:     io_github_caoshen_speex_Speex
 * Method:    decode
 * Signature: ([B[SI)I
 */
JNIEXPORT jint JNICALL Java_io_github_caoshen_speex_Speex_decode
        (JNIEnv *, jobject, jbyteArray, jshortArray, jint) {

}

/*
 * Class:     io_github_caoshen_speex_Speex
 * Method:    encode
 * Signature: ([SI[BI)I
 */
JNIEXPORT jint JNICALL Java_io_github_caoshen_speex_Speex_encode
        (JNIEnv *, jobject, jshortArray, jint, jbyteArray, jint) {

}

/*
 * Class:     io_github_caoshen_speex_Speex
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_io_github_caoshen_speex_Speex_close
        (JNIEnv *, jobject) {

}

#ifdef __cplusplus
}
#endif