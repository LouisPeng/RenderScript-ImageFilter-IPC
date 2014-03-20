/**
 * @author pengluyu
 *
 * ImageFilterBinderService.java
 * 11:18:36 PM 2014
 */

package cn.louispeng.imagefilter.binderservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author pengluyu
 */
public class ImageFilterBinderService extends Service {
    private static final String TAG = "ImageFilterBinderService";

    private IImageFilterServiceImpl service;

    @Override
    public void onCreate() {
        super.onCreate();
        this.service = new IImageFilterServiceImpl();
        Log.d(TAG, "onCreate()");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return this.service;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        this.service = null;
        super.onDestroy();
    }
}
