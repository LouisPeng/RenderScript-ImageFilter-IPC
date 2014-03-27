LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := RSImageFilterBinderCommon
LOCAL_SRC_FILES := RSImageFilterBinderCommon.cpp

include $(BUILD_SHARED_LIBRARY)
