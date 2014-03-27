/**
 * @author pengluyu
 *
 * SaturationModifyFilter.java
 * 6:26:48 AM 2014
 */

package cn.louispeng.imagefilter.filter;

import android.content.Context;
import android.graphics.Bitmap;
import cn.louispeng.imagefilter.binderserver.R;
import cn.louispeng.imagefilter.renderscript.ScriptC_SaturationModifyFilter;

/**
 * @author pengluyu
 */
public class SaturationModifyFilter extends ImageFilter {
    private final float mSaturationFactor;

    public SaturationModifyFilter(Context context, Bitmap bitmapIn, Bitmap bitmapOut) {
        this(context, bitmapIn, bitmapOut, 0.5f);
    }

    public SaturationModifyFilter(Context context, Bitmap bitmapIn, Bitmap bitmapOut, float saturationFactor) {
        super(context, bitmapIn, bitmapOut);
        mSaturationFactor = saturationFactor;
    }

    @Override
    public void _process() {
        ScriptC_SaturationModifyFilter script = new ScriptC_SaturationModifyFilter(mRS, mContext.getResources(),
                R.raw.saturationmodifyfilter);

        script.set_gIn(mInAllocation);
        script.set_gOut(mOutAllocation);
        script.set_gSaturationFactor(mSaturationFactor);
        script.set_gScript(script);

        script.invoke_filter();
        mScript = script;
    }
}
