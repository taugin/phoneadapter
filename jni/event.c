#include <assert.h>
#include "event.h"

#define JNIREG_CLASS "com/android/phoneadapter/EventSender"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL native_sendevent(JNIEnv *env, jobject obj, jint fd, jstring type, jstring code, jstring value) {
    char ptype[256] = {0};
    const char *tmp2 = (*env)->GetStringUTFChars(env, type, 0);
    strcpy(ptype, tmp2);
    (*env)->ReleaseStringUTFChars(env, type, tmp2);

    char pcode[256] = {0};
    const char *tmp3 = (*env)->GetStringUTFChars(env, code, 0);
    strcpy(pcode, tmp3);
    (*env)->ReleaseStringUTFChars(env, code, tmp3);
    
    char pvalue[256] = {0};
    const char *tmp4 = (*env)->GetStringUTFChars(env, value, 0);
    strcpy(pvalue, tmp4);
    (*env)->ReleaseStringUTFChars(env, value, tmp4);
	
	char cmd[256] = {0};
	sprintf(cmd, "%s %s %s", ptype, pcode, pvalue);
	LOGD("%s", cmd);
	char *argv[] = {ptype, pcode, pvalue};
	/*
	int pid = fork();

    if (pid == 0) {
        execvp("/system/bin/sendevent", argv);
        LOGE("execvp fail!");
    }
    int status;
    waitpid(pid, &status, WNOHANG);
    */
    sendevent(fd, argv);
}

JNIEXPORT jint JNICALL native_open(JNIEnv *env, jobject obj, jstring device) {
    char pdevice[256] = {0};
    const char *tmp1 = (*env)->GetStringUTFChars(env, device, 0);
    strcpy(pdevice, tmp1);
    (*env)->ReleaseStringUTFChars(env, device, tmp1);
	int fd = open(pdevice, O_RDWR);
	LOGD("fd : %d",fd);
	return fd;
}

JNIEXPORT void JNICALL native_close(JNIEnv *env, jobject obj, jint fd) {
	LOGD("fd : %d", fd);
	if (fd != -1) {
		close(fd);
	}
}

int sendevent(int fd, char *argv[])
{
    struct input_event event;
	/*
    if (ioctl(fd, EVIOCGVERSION, &version)) {
		LOGD("Get Version Error");
        return 1;
    }*/
    int ret;
    memset(&event, 0, sizeof(event));
    event.type = atoi(argv[0]);
    event.code = atoi(argv[1]);
    event.value = atoi(argv[2]);
    ret = write(fd, &event, sizeof(event));
    if(ret < sizeof(event)) {
        LOGD("write event failed, %s\n", strerror(errno));
        return -1;
    }
    return 0;
}

static JNINativeMethod gMethods[] = {
	{ "open", 		"(Ljava/lang/String;)I", (void*)native_open },
	{ "close", 		"(I)V", (void*)native_close },
	{ "sendevent",  "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", (void*)native_sendevent }
};

/*
* Register several native methods for one class.
*/
static int registerNativeMethods(JNIEnv* env, const char* className,
        JNINativeMethod* gMethods, int numMethods)
{
	jclass clazz;
	clazz = (*env)->FindClass(env, className);
	if (clazz == NULL) {
		LOGD("clazz == NULL");
		return JNI_FALSE;
	}
	if ((*env)->RegisterNatives(env, clazz, gMethods, numMethods) < 0) {
		LOGD("RegisterNatives result < 0");
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

/*
* Register native methods for all classes we know about.
*/
static int registerNatives(JNIEnv* env)
{
	if (!registerNativeMethods(env, JNIREG_CLASS, gMethods, sizeof(gMethods) / sizeof(gMethods[0]))) {
		LOGD("RegisterNatives result < 0");
		return JNI_FALSE;
	}

	return JNI_TRUE;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;

	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		LOGD("GetEnv != JNI_OK");
		return -1;
	}
	assert(env != NULL);

	if (!registerNatives(env)) {//注册
		LOGD("registerNatives Failure");
		return -1;
	}
	/* success -- return valid version number */
	result = JNI_VERSION_1_4;
	LOGD("Native function register success");
	return result;
}

#ifdef __cplusplus
}
#endif

