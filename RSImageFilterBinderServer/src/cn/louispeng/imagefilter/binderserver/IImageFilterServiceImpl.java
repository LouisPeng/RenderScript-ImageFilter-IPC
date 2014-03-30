/**
 * @author pengluyu
 *
 * IImageFilterServiceImpl.java
 * 11:19:51 PM 2014
 */

package cn.louispeng.imagefilter.binderserver;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import cn.louispeng.imagefilter.bindercommon.FileUtils;
import cn.louispeng.imagefilter.bindercommon.FilterIDDefine;
import cn.louispeng.imagefilter.bindercommon.IImageFilterService;
import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithData;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithFilepath;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithParcelFileDescriptor;
import cn.louispeng.imagefilter.bindercommon.ProfileUtil;
import cn.louispeng.imagefilter.filter.BlackWhiteFilter;
import cn.louispeng.imagefilter.filter.ImageFilter;
import cn.louispeng.imagefilter.filter.SaturationModifyFilter;

/**
 * @author pengluyu
 */
public class IImageFilterServiceImpl extends IImageFilterService.Stub {
    public static final String TAG = IImageFilterServiceImpl.class.getSimpleName();

    private final Context mContext;

    public IImageFilterServiceImpl(Context context) {
        mContext = context;
    }

    private ImageFilter createImageFilter(int effectID, Bitmap dataIn, Bitmap dataOut) {
        ImageFilter filter = null;
        switch (effectID) {
            case FilterIDDefine.BLACKWHITE:
                filter = new BlackWhiteFilter(mContext, dataIn, dataOut);
                break;
            case FilterIDDefine.SATURATION_MODIFY:
                filter = new SaturationModifyFilter(mContext, dataIn, dataOut);
                break;
            default:
                break;
        }

        return filter;
    }

    /*
     * (non-Javadoc)
     * @see cn.louispeng.imagefilter.bindercommon.IImageFilterService#filterWithParcelFileDescriptor(int,
     * cn.louispeng.imagefilter.bindercommon.ImageFileWithParcelFileDescriptor,
     * cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener)
     */
    @Override
    public void filterWithParcelFileDescriptor(final int effectID, final ImageFileWithParcelFileDescriptor imageFile,
            final IImageFilterServiceResponseListener listener) throws RemoteException {
        new Thread("FilterWithParcelFileDescriptor thread") {
            @Override
            public void run() {
                int result = 0;
                ImageFileWithParcelFileDescriptor resultImageFile = null;
                ProfileUtil.start("IPC-Profile");
                Log.d(TAG, "filter() effectID = " + effectID);
                MemoryFile mFile = null;
                FileDescriptor inFD = imageFile.getParcelFileDescriptor().getFileDescriptor();
                if (inFD == null) {
                    Log.d(TAG, "Failed to get memeory file descriptor.");
                } else {
                    byte[] data = null;
                    try {
                        data = FileUtils.read(inFD);
                        if (data.length != imageFile.getSize()) {
                            result = -1;
                            throw new IOException("data.length != imageFile.getSize()");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = -1;
                    }
                    ProfileUtil.checkpoint(TAG + " FileDescriptorUtil.read()");

                    if (0 == result) {
                        Bitmap inBitmap = Bitmap.createBitmap(imageFile.getWidth(), imageFile.getHeight(),
                                Config.ARGB_8888);
                        inBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data));
                        ProfileUtil.checkpoint(TAG + " inBitmap.copyPixelsFromBuffer()");
                        data = null;

                        Bitmap outBitmap = Bitmap.createBitmap(inBitmap);
                        ImageFilter filter = createImageFilter(effectID, inBitmap, outBitmap);
                        filter.process();
                        ProfileUtil.checkpoint(TAG + " filter.process()");
                        if (!inBitmap.sameAs(outBitmap)) {
                            inBitmap.recycle();
                        }

                        ByteBuffer outputDataBuffer = ByteBuffer.allocate(outBitmap.getRowBytes()
                                * outBitmap.getHeight());
                        outBitmap.copyPixelsToBuffer(outputDataBuffer);
                        ProfileUtil.checkpoint(TAG + " outBitmap.copyPixelsFromBuffer()");
                        outBitmap.recycle();
                        // Create MemoryFile for result
                        try {
                            mFile = new MemoryFile("ResultImageMemory", outputDataBuffer.capacity());
                            mFile.allowPurging(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            result = -1;
                        }

                        if (0 == result) {
                            ParcelFileDescriptor outPFD = null;
                            try {
                                mFile.writeBytes(outputDataBuffer.array(), 0, 0, outputDataBuffer.capacity());
                                outPFD = ParcelFileDescriptor.dup(mFile.getFileDescriptor());
                            } catch (IOException e) {
                                e.printStackTrace();
                                result = -1;
                            }

                            ProfileUtil.checkpoint(TAG + " FileDescriptorUtil.write");

                            if (0 == result) {
                                resultImageFile = new ImageFileWithParcelFileDescriptor(outPFD,
                                        outputDataBuffer.capacity(), imageFile.getWidth(), imageFile.getHeight());
                            }
                        }
                    }
                }

                if (listener != null) {
                    try {
                        listener.onResponseWithParcelFileDescriptor(result, resultImageFile);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    ProfileUtil.checkpoint(TAG + " listener.onResponse");
                }

                if (null != mFile) {
                    mFile.close();
                }
            }
        }.start();
    }

    @Override
    public void filterWithData(final int effectID, final ImageFileWithData imageFile,
            final IImageFilterServiceResponseListener listener) throws RemoteException {
        new Thread("FilterWithData thread") {
            @Override
            public void run() {
                int result = 0;
                ImageFileWithData resultImageFile = null;
                ProfileUtil.start("IPC-Profile");
                Log.d(TAG, "filter() effectID = " + effectID);

                Bitmap inBitmap = Bitmap.createBitmap(imageFile.getWidth(), imageFile.getHeight(), Config.ARGB_8888);
                inBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageFile.getData()));
                Bitmap outBitmap = Bitmap.createBitmap(inBitmap);
                ImageFilter filter = createImageFilter(effectID, inBitmap, outBitmap);
                filter.process();
                ProfileUtil.checkpoint(TAG + " filter.process()");
                
                if (!inBitmap.sameAs(outBitmap)) {
                    inBitmap.recycle();
                }
                
                ByteBuffer outputDataBuffer = ByteBuffer.allocate(outBitmap.getRowBytes() * outBitmap.getHeight());
                outBitmap.copyPixelsToBuffer(outputDataBuffer);
                outBitmap.recycle();

                if (0 == result) {
                    resultImageFile = new ImageFileWithData(outputDataBuffer.array(), imageFile.getWidth(),
                            imageFile.getHeight());
                }

                ProfileUtil.checkpoint(TAG + " FileDescriptorUtil.write");

                if (listener != null) {
                    try {
                        listener.onResponseWithData(result, resultImageFile);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ProfileUtil.checkpoint(TAG + " listener.onResponse");
                }
            }
        }.start();
    }

    @Override
    public void filterWithFilepath(final int effectID, final ImageFileWithFilepath imageFile,
            final IImageFilterServiceResponseListener listener) throws RemoteException {
        new Thread("FilterWithFilepath thread") {
            @Override
            public void run() {
                int result = 0;
                ImageFileWithFilepath resultImageFile = null;
                ProfileUtil.start("IPC-Profile");
                Log.d(TAG, "filter() effectID = " + effectID);
                File inFile = new File(imageFile.getFilepath());
                Bitmap inBitmap = BitmapFactory.decodeFile(imageFile.getFilepath());
                if (null == inBitmap) {
                    result = -1;
                }
                ProfileUtil.checkpoint(TAG + " BitmapFactory.decodeFile");

                if (0 == result) {
                    Bitmap outBitmap = Bitmap.createBitmap(inBitmap);
                    ImageFilter filter = createImageFilter(effectID, inBitmap, outBitmap);
                    filter.process();
                    ProfileUtil.checkpoint(TAG + " filter.process()");
                    if (!inBitmap.sameAs(outBitmap)) {
                        inBitmap.recycle();
                    }
                    
                    // Create MemoryFile for result
                    String outFilepath = inFile.getAbsolutePath() + "out.jpeg";
                    try {
                        outBitmap.compress(CompressFormat.JPEG, 100, new FileOutputStream(outFilepath));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        result = -1;
                    }

                    ProfileUtil.checkpoint(TAG + " outBitmap.compress");
                    outBitmap.recycle();

                    if (0 == result) {
                        if (0 == result) {
                            resultImageFile = new ImageFileWithFilepath(outFilepath, imageFile.getWidth(),
                                    imageFile.getHeight());
                        }
                    }
                }

                if (listener != null) {
                    try {
                        listener.onResponseWithFilepath(result, resultImageFile);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ProfileUtil.checkpoint(TAG + " listener.onResponse");
                }
            }
        }.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

}
