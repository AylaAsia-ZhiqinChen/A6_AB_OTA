LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := SystemOTA

LOCAL_MODULE_TAGS := optional

LOCAL_CERTIFICATE := platform

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PRIVILEGED_MODULE := true

LOCAL_USE_AAPT2 := true

LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.appcompat_appcompat \


LOCAL_SRC_FILES := $(call all-java-files-under, app/src/main/java) \
                $(call all-renderscript-files-under, app/src/main/java)


LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/app/src/main/res

LOCAL_MANIFEST_FILE := app/src/main/AndroidManifest.xml

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)


include $(call all-makefiles-under,$(LOCAL_PATH))
