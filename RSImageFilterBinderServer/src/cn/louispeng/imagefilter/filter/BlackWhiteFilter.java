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
import cn.louispeng.imagefilter.renderscript.ScriptC_BlackWhiteFilter;

/**
 * @author pengluyu
 */
public class BlackWhiteFilter extends ImageFilter {
    public BlackWhiteFilter(Context context, Bitmap bitmapIn, Bitmap bitmapOut) {
        this(context, bitmapIn, bitmapOut, 0.5f);
    }

    public BlackWhiteFilter(Context context, Bitmap bitmapIn, Bitmap bitmapOut, float saturationFactor) {
        super(context, bitmapIn, bitmapOut);
    }

    @Override
    public void _process() {
        ScriptC_BlackWhiteFilter script = new ScriptC_BlackWhiteFilter(mRS, mContext.getResources(),
                R.raw.blackwhitefilter);

        script.set_gIn(mInAllocation);
        script.set_gOut(mOutAllocation);
        script.set_gScript(script);

        script.invoke_filter();
        mScript = script;
    }
}
