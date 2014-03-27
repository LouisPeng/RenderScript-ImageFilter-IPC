package cn.louispeng.imagefilter.bindercommon;

import cn.louispeng.imagefilter.bindercommon.ImageFileWithParcelFileDescriptor;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithData;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithFilepath;

oneway interface IImageFilterServiceResponseListener {
    void onResponseWithParcelFileDescriptor(in int result, in ImageFileWithParcelFileDescriptor image);
    void onResponseWithData(in int result, in ImageFileWithData image);
    void onResponseWithFilepath(in int result, in ImageFileWithFilepath image);
}