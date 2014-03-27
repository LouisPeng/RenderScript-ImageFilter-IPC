/**
 * @author pengluyu
 *
 * ImageFilter.java
 * 6:08:04 AM 2014
 */

package cn.louispeng.imagefilter.filter;

import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptC;
import android.util.Log;

/**
 * @author pengluyu
 */
public abstract class ImageFilter {
    protected final Context mContext;

    protected final byte[] mBitmapIn;

    protected final byte[] mBitmapOut;

    private long startTime;

    protected RenderScript mRS;

    protected Allocation mInAllocation;

    protected Allocation mOutAllocation;

    protected ScriptC mScript;

    public ImageFilter(Context context, byte[] bitmapIn, byte[] bitmapOut) {
        mContext = context;
        mBitmapIn = bitmapIn;
        mBitmapOut = bitmapOut;
        assert (null != mContext && null != bitmapIn && null != bitmapOut);
    }

    protected void preProcess() {
        mRS = RenderScript.create(mContext);
        Element rgba8888 = Element.RGBA_8888(mRS);
        mInAllocation = Allocation.createSized(mRS, rgba8888, mBitmapIn.length / 4);
        mInAllocation.copyFrom(mBitmapIn);
        mOutAllocation = Allocation.createSized(mRS, rgba8888, mBitmapOut.length / 4);
    }

    protected void _preProcess() {
    }

    protected abstract void _process();

    protected void _postProcess() {
    }

    public void process() {
        preProcess();
        startTime = System.currentTimeMillis();
        _preProcess();
        _process();
        Log.d("profile", getClass().getSimpleName() + " use " + (System.currentTimeMillis() - startTime));
        _postProcess();
        postProcess();
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
