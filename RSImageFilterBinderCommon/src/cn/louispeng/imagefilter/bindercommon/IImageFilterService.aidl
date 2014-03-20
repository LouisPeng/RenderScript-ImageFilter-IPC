package cn.louispeng.imagefilter.bindercommon;

import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;

oneway interface IImageFilterService {
    void filter(in int filterID, in ParcelFileDescriptor fd, in IImageFilterServiceResponseListener listener);
}