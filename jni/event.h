#ifndef DOLPHIN_H_
#define DOLPHIN_H_

#include <jni.h>
#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <sys/wait.h>
#include <android/log.h>
#include <sys/inotify.h>

#include <stdint.h>
#include <fcntl.h>
#include <sys/ioctl.h>
//#include <linux/input.h> // this does not compile
#include <errno.h>

#define LOG_TAG "event"
#define DEBUG

#ifdef DEBUG
#define LOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, "%s : %s : %d ---> "format"%s",__FILE__,__FUNCTION__,__LINE__,##__VA_ARGS__,"\n");
#else
#define LOGD(format, ...) 0;
#endif

#define LOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, format"%s",##__VA_ARGS__,"\n");
#define LOGV(format, ...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, format"%s",##__VA_ARGS__,"\n");

struct input_event {
	struct timeval time;
	__u16 type;
	__u16 code;
	__s32 value;
};

#endif /* DOLPHIN_H_ */

