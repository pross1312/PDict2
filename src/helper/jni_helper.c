#include <android_native_app_glue.h>
#include <raylib.h>
#include <jni.h>
#include "jni_helper.h"
// from: https://github.com/cnlohr/rawdrawandroid
const char* AndroidGetExternalFilesDir(struct android_app *gapp) {
	const struct JNINativeInterface * env = 0;
	const struct JNINativeInterface ** envptr = &env;
	const struct JNIInvokeInterface ** jniiptr = gapp->activity->vm;
	const struct JNIInvokeInterface * jnii = *jniiptr;

	jnii->AttachCurrentThread( jniiptr, &envptr, NULL);
	env = (*envptr);
	jclass activityClass = env->FindClass( envptr, "android/app/NativeActivity");
	jobject lNativeActivity = gapp->activity->clazz;

    	jmethodID mid_getExtStorage = env->GetMethodID(envptr,activityClass,"getExternalFilesDir", "(Ljava/lang/String;)Ljava/io/File;");
    	jobject obj_File = env->CallObjectMethod(envptr,lNativeActivity, mid_getExtStorage, NULL);
    	jclass cls_File = env->FindClass(envptr,"java/io/File");
    	jmethodID mid_getPath = env->GetMethodID(envptr,cls_File, "getPath", "()Ljava/lang/String;");
    	jstring obj_Path = (jstring) env->CallObjectMethod(envptr,obj_File, mid_getPath);
    	const char* path = env->GetStringUTFChars(envptr,obj_Path, NULL);
    	//printf("EXTERNAL PATH = %s\n", path);
    	env->ReleaseStringUTFChars(envptr,obj_Path, path);	
	jnii->DetachCurrentThread( jniiptr );
	return path;
}
