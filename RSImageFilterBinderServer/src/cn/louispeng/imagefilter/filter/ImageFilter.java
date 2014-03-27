/**
 * @author pengluyu
 *
 * ImageFilter.java
 * 6:08:04 AM 2014
 */

package cn.louispeng.imagefilter.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptC;
import android.util.Log;
import cn.louispeng.imagefilter.bindercommon.ProfileUtil;

/**
 * @author pengluyu
 */
public abstract class ImageFilter {

    protected final Context mContext;

    protected final Bitmap mBitmapIn;

    protected final Bitmap mBitmapOut;

    private long startTime;

    protected RenderScript mRS;

    protected Allocation mInAllocation;

    protected Allocation mOutAllocation;

    protected ScriptC mScript;

    public ImageFilter(Context context, Bitmap bitmapIn, Bitmap bitmapOut) {
        mContext = context;
        mBitmapIn = bitmapIn;
        mBitmapOut = bitmapOut;
        assert (null != mContext && null != bitmapIn && null != bitmapOut);
    }

    protected void preProcess() {
        mRS = RenderScript.create(mContext);
        mInAllocation = Allocation.createFromBitmap(mRS, mBitmapIn);
        mOutAllocation = Allocation.createFromBitmap(mRS, mBitmapOut);
    }

    protected void _preProcess() {
    }

    protected abstract void _process();

    protected void _postProcess() {
    }

    public void process() {
        ProfileUtil.start("ImageFilter");
        startTime = System.currentTimeMillis();
        preProcess();
        _preProcess();
        Log.d("profile", getClass().getSimpleName() + " preProcess use " + (System.currentTimeMillis() - startTime));
        _process();
        Log.d("profile", getClass().getSimpleName() + " process use " + (System.currentTimeMillis() - startTime));
        _postProcess();
        postProcess();
        Log.d("profile", getClass().getSimpleName() + " postProcess use " + (System.currentTimeMillis() - startTime));
    }

    protected void postProcess() {
        mOutAllocation.copyTo(mBitmapOut);
        mScript.destroy();
        mScript = null;
        mInAllocation.destroy();
        mInAllocation = null;
        mOutAllocation.destroy();
        mOutAllocation = null;
        mRS.destroy();
        mRS = null;
        System.gc();
    }
};
