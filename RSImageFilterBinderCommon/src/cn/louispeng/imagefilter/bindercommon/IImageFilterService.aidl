package cn.louispeng.imagefilter.bindercommon;

import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;
import cn.louispeng.imagefilter.bindercommon.ImageFile;

oneway interface IImageFilterService {
    void filter(in int filterID, in ImageFile image, in IImageFilterServiceResponseListener listener);
}