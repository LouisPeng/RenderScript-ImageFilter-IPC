package cn.louispeng.imagefilter.bindercommon;

oneway interface IImageFilterServiceResponseListener {
    void onResponse(in int result, in ParcelFileDescriptor fd);
}