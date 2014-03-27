/**
 * @author pengluyu
 *
 * ProfileUtil.java
 * 10:45:07 PM 2014
 */

package cn.louispeng.imagefilter.bindercommon;

import android.util.Log;

/**
 * @author pengluyu
 */
public class ProfileUtil {
    private static long mStartTime;

    private static long mCheckpoint;

    private static String mTag;

    public static void start(String tag) {
        mTag = tag;
        mStartTime = System.currentTimeMillis();
        mCheckpoint = mStartTime;
    }

    public static void checkpoint(String checkpointTag) {
        long checkpoint = System.currentTimeMillis();
        Log.d(mTag, "[" + checkpointTag + "]From start = " + (checkpoint - mStartTime) + ", from checkpoint = "
                + (checkpoint - mCheckpoint));
        mCheckpoint = checkpoint;
    }
}
