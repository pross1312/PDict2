#define NOB_IMPLEMENTATION
#define NOB_STRIP_PREFIX
#include "src/nob.h"

#define OUTPUT_DIR "build"
#define SOURCE_DIR "src"
#define NDK_PATH "/home/dvtuong/android-sdk/ndk/android-ndk-r27c"
#define RAYLIB_DIR "raylib-5.5"
#define SQLITE_DIR "sqlite3"
#define SDK_PATH "/home/dvtuong/android-sdk"
#define ANDROID_VERSION "35"
#define BUILD_TOOLS SDK_PATH"/build-tools/"ANDROID_VERSION".0.0"
#define APP_NAME "PDict"
#define ANDROID_PACKAGE_NAME "com.pdict"
#define LABEL APP_NAME
#define APK_PATH OUTPUT_DIR"/"APP_NAME".apk"
#define ADB "adb"
#define ANDROID_JAR SDK_PATH"/platforms/android-"ANDROID_VERSION"/android.jar"
#define ANDROID_MANIFEST_PATH OUTPUT_DIR"/AndroidManifest.xml"
#define ANDROID_ACTIVITY_NAME "android.app.NativeActivity"

#define ANDROID_ABI "arm64-v8a"
#define ANDROID_CC NDK_PATH"/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android"ANDROID_VERSION"-clang"
// CC_ARM64:=$(NDK)/toolchains/llvm/prebuilt/$(OS_NAME)/bin/aarch64-linux-android$(ANDROIDVERSION)-clang
// CC_ARM32:=$(NDK)/toolchains/llvm/prebuilt/$(OS_NAME)/bin/armv7a-linux-androideabi$(ANDROIDVERSION)-clang
// CC_x86:=$(NDK)/toolchains/llvm/prebuilt/$(OS_NAME)/bin/i686-linux-android$(ANDROIDVERSION)-clang
// CC_x86_64=$(NDK)/toolchains/llvm/prebuilt/$(OS_NAME)/bin/x86_64-linux-android$(ANDROIDVERSION)-clang
// CFLAGS_ARM64:=-m64
// CFLAGS_ARM32:=-mfloat-abi=softfp -m32
// CFLAGS_x86:=-march=i686 -mssse3 -mfpmath=sse -m32
// CFLAGS_x86_64:=-march=x86-64 -msse4.2 -mpopcnt -m64

bool build_native_android_lib(Cmd *cmd) {
    if (!mkdir_if_not_exists(OUTPUT_DIR"/lib")) return false;
    if (!mkdir_if_not_exists(OUTPUT_DIR"/lib/"ANDROID_ABI)) return false;

    const char *sqlite_lib_path = OUTPUT_DIR"/lib/"ANDROID_ABI"/libsqlite.so";
    const char *sqlite_source = SQLITE_DIR"/sqlite3.c";
    if (needs_rebuild1(sqlite_lib_path, sqlite_source)) {
        cmd_append(cmd, ANDROID_CC, "-Wall", "-Wextra",
                        "-DANDROID", "-DANDROIDVERSION="ANDROID_VERSION,
                        "-Os", "-Wall",
                        "-m64", "-fPIC",
                        "-o", sqlite_lib_path,
                        sqlite_source,
                        "-s", "-shared");
        if (!cmd_run_sync_and_reset(cmd)) return 1;
    }
    cmd_append(cmd, ANDROID_CC,
                    "-DANDROID", "-DANDROIDVERSION="ANDROID_VERSION,
                    "-ffunction-sections", "-Os", "-fdata-sections", "-Wall", "-fvisibility=hidden",

                    "-I"SQLITE_DIR,
                    "-I"RAYLIB_DIR"/include",
                    "-I"NDK_PATH"/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include",
                    "-I"NDK_PATH"/sources/android/native_app_glue",
                    "-I"SOURCE_DIR"/helper",

                    "-m64", "-fPIC",
                     "-o", OUTPUT_DIR"/lib/"ANDROID_ABI"/lib"APP_NAME".so",

                    "-Wall", "-Wextra", SOURCE_DIR"/main.c",
                    "-Wno-unused-variable", SOURCE_DIR"/helper/jni_helper.c",

                    "-L"NDK_PATH"/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/lib",
                    "-L"RAYLIB_DIR"/lib", "-L"OUTPUT_DIR"/lib/"ANDROID_ABI,
                    "-Wl,--gc-sections"/*, "-Wl,-Map=output.map"*/, "-s",
                    "-lm", "-lGLESv3", "-lEGL", "-landroid", "-llog", "-lOpenSLES", "-l:libraylib.a", "-lsqlite",
                    "-shared", "-uANativeActivity_onCreate");
    return cmd_run_sync_and_reset(cmd);
}

bool build_sqlite_lib(Cmd *cmd) {
}


bool generate_android_manifest() {
    const char android_manifest_src[] = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n"\
"<manifest xmlns:tools=\"http://schemas.android.com/tools\" xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\""ANDROID_PACKAGE_NAME"\">\n"\
"	<uses-sdk android:minSdkVersion=\"22\"\n"\
"          android:targetSdkVersion=\""ANDROID_VERSION"\" />\n"\
"    <uses-permission android:name=\"android.permission.SET_RELEASE_APP\"/>\n"\
"    <application android:debuggable=\"true\" android:hasCode=\"false\" android:label=\""LABEL"\" tools:replace=\"android:icon,android:theme,android:allowBackup,label\" android:icon=\"@mipmap/icon\">\n"\
"        <activity android:configChanges=\"keyboardHidden|orientation\" android:label=\""LABEL"\" android:name=\""ANDROID_ACTIVITY_NAME"\" android:exported=\"true\">\n"\
"            <meta-data android:name=\"android.app.lib_name\" android:value=\""APP_NAME"\"/>\n"\
"            <intent-filter>\n"\
"                <action android:name=\"android.intent.action.MAIN\"/>\n"\
"                <category android:name=\"android.intent.category.LAUNCHER\"/>\n"\
"            </intent-filter>\n"\
"        </activity>\n"\
"    </application>\n"\
"</manifest>";
    nob_log(NOB_INFO, "Generate %zu -> %s", sizeof(android_manifest_src), ANDROID_MANIFEST_PATH);
    if (!write_entire_file(ANDROID_MANIFEST_PATH, android_manifest_src, sizeof(android_manifest_src)*sizeof(*android_manifest_src)-1)) return false;
    return true;
}

bool build_android_apk(Cmd *cmd, const char *keystore, const char *keystore_pass) {
    cmd_append(cmd, BUILD_TOOLS"/aapt", "package",
            "-f", "-F", APK_PATH,
            "-I", ANDROID_JAR,
            "-M", ANDROID_MANIFEST_PATH,
            "-A", "android_res/asset",
            "-S", "android_res/res",
            /*"-A", "android_res/assets",*/
            "-v",
            "--target-sdk-version", ANDROID_VERSION);
    if (!cmd_run_sync_and_reset(cmd)) return false;

    size_t snapshot = temp_save();
    const char *current_dir = get_current_dir_temp();
    if (!set_current_dir(OUTPUT_DIR)) return false;
    cmd_append(cmd, BUILD_TOOLS"/aapt", "add", APP_NAME".apk", "lib/"ANDROID_ABI"/libPDict.so", "lib/"ANDROID_ABI"/libsqlite.so");
    if (!cmd_run_sync_and_reset(cmd)) return false;
    if (!nob_set_current_dir(current_dir)) return false;

	cmd_append(cmd, BUILD_TOOLS"/apksigner", "sign", "--ks-pass", temp_sprintf("pass:%s", keystore_pass), "--ks", keystore, APK_PATH);
    if (!cmd_run_sync_and_reset(cmd)) return false;

    temp_rewind(snapshot);
    return true;
}

bool push_and_run_to_device(Cmd *cmd) {
    cmd_append(cmd, ADB, "install", "-r", APK_PATH);
    if (!cmd_run_sync_and_reset(cmd)) return false;
    cmd_append(cmd, ADB, "shell", "am", "start", "-n", ANDROID_PACKAGE_NAME"/"ANDROID_ACTIVITY_NAME);
    return cmd_run_sync_and_reset(cmd);
}

void usage(const char *program) {
    fprintf(stderr, "Usage: %s <keystore> <keystore_pass>\n", program);
    fprintf(stderr, " Note: You can generate a keystore with this command `keytool -genkey -v -keystore <keystore_file> -keyalg RSA -keysize 2048 -validity 10000 -storepass <keystore_pass> -dname 'CN=%s, OU=ID, O=pdict, L=pdict, S=pdict, C=VN'`\n", ANDROID_PACKAGE_NAME);
}

int main(int argc, char **argv)
{
    NOB_GO_REBUILD_URSELF(argc, argv);
    const char *program = nob_shift(argv, argc);
    if (argc != 2) {
        usage(program);
        return 1;
    }
    const char *keystore_file = nob_shift(argv, argc);
    const char *keystore_pass = nob_shift(argv, argc);


    Nob_Cmd cmd = {0};
    if (!mkdir_if_not_exists(OUTPUT_DIR)) return 1;
    if (!build_native_android_lib(&cmd)) return 1;
    if (!generate_android_manifest()) return 1;
    if (!build_android_apk(&cmd, keystore_file, keystore_pass)) return 1;
    if (!push_and_run_to_device(&cmd)) return 1;
    return 0;
}
