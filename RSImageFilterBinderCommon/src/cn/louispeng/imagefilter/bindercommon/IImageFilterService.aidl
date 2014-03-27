package cn.louispeng.imagefilter.bindercommon;

import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithParcelFileDescriptor;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithData;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithFilepath;

oneway interface IImageFilterService {
    void filterWithParcelFileDescriptor(in int filterID, in ImageFileWithParcelFileDescriptor image, in IImageFilterServiceResponseListener listener);
    void filterWithData(in int filterID, in ImageFileWithData image, in IImageFilterServiceResponseListener listener);
    void filterWithFilepath(in int filterID, in ImageFileWithFilepath image, in IImageFilterServiceResponseListener listener);
}