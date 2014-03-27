/**
 * @author pengluyu
 *
 * IImageFilterServiceImpl.java
 * 11:19:51 PM 2014
 */

package cn.louispeng.imagefilter.binderserver;

import cn.louispeng.imagefilter.bindercommon.FileDescriptorUtil;
import cn.louispeng.imagefilter.bindercommon.IImageFilterService;
import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;
import cn.louispeng.imagefilter.bindercommon.ImageFile;
import cn.louispeng.imagefilter.bindercommon.ProfileUtil;
import cn.louispeng.imagefilter.filter.ImageFilter;
import cn.louispeng.imagefilter.filter.SaturationModifyFilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.os.Environment;
import android.os.MemoryFile;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author pengluyu
 */
public class IImageFilterServiceImpl extends IImageFilterService.Stub {
    public static final String TAG = IImageFilterServiceImpl.class.getSimpleName();

    private final Context mContext;

    public IImageFilterServiceImpl(Context context) {
        mContext = context;
    }

    /**
     * Save bitmap data to file
     * 
     * @param data
     * @param width
     * @param height
     */
    private void outputBitmapDataArray(final byte[] data, final int width, final int height, final String filepath) {
        File outputFile = new File(filepath);
        Bitmap outBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        outBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(data));
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }

        if (null != outStream) {
            if (!outBitmap.compress(CompressFormat.JPEG, 100, outStream)) {
                Log.d(TAG, "Failed to get memeory file descriptor.");
            }
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ImageFilter createImageFilter(int effectID, byte[] dataIn, byte[] dataOut) {
        ImageFilter filter;
        filter = new SaturationModifyFilter(mContext, dataIn, dataOut);
        return filter;
    }

    /*
     * (non-Javadoc)
     * @see cn.louispeng.imagefilter.bindercommon.IImageFilterService#filter(int,
     * cn.louispeng.imagefilter.bindercommon.ImageFile, cn.louispeng.imagefilter.
     * bindercommon.IImageFilterServiceResponseListener)
     */
    @Override
    public void filter(final int effectID, final ImageFile imageFile, final IImageFilterServiceResponseListener listener)
            throws RemoteException {
        new Thread("Filter thread") {
            @Override
            public void run() {
                int result = -1;
                ProfileUtil.start(TAG);
                Log.d(TAG, "filter() effectID = " + effectID);
                FileDescriptor fd = imageFile.getParcelFileDescriptor().getFileDescriptor();
                if (fd == null) {
                    Log.d(TAG, "Failed to get memeory file descriptor.");
                } else {
                    byte[] data = null;
                    try {
                        data = FileDescriptorUtil.read(fd);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    ProfileUtil.checkpoint("FileDescriptorUtil.read finish");

                    if (null != data) {
                        if (BuildConfig.DEBUG) {
                            String outputFilepath = Environment.getExternalStorageDirectory() + "/test/" + TAG
                                    + "-in.jpeg";
                            outputBitmapDataArray(data, imageFile.getWidth(), imageFile.getHeight(), outputFilepath);
                        }

                        byte[] outputDataBuffer = new byte[data.length];
                        ImageFilter filter = createImageFilter(effectID, data, outputDataBuffer);
                        filter.process();
                        ProfileUtil.checkpoint("filter.process() finish");

                        if (BuildConfig.DEBUG) {
                            String outputFilepath = Environment.getExternalStorageDirectory() + "/test/" + TAG
                                    + "-out.jpeg";
                            outputBitmapDataArray(outputDataBuffer, imageFile.getWidth(), imageFile.getHeight(),
                                    outputFilepath);
                        }

                        try {
                            MemoryFile file = new MemoryFile(fd, (int)imageFile.getSize());
                            file.writeBytes(outputDataBuffer, 0, 0, outputDataBuffer.length);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        ProfileUtil.checkpoint("FileDescriptorUtil.write finish");
                    }
                    result = 0;
                }

                if (listener != null) {
                    try {
                        listener.onResponse(result);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    ProfileUtil.checkpoint("listener.onResponse finish");
                }
            }
        }.start();

    }
}
