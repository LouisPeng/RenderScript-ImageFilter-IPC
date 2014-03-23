
package cn.louispeng.imagefilter.binderclient;

import cn.louispeng.imagefilter.bindercommon.FileDescriptorUtil;
import cn.louispeng.imagefilter.bindercommon.FilterIDDefine;
import cn.louispeng.imagefilter.bindercommon.IImageFilterService;
import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;
import cn.louispeng.imagefilter.bindercommon.MemoryFileUtil;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends Activity implements ServiceConnection {
    public static final String TAG = MainActivity.class.getSimpleName();

    // the id of a message to our response handler
    private static final int RESPONSE_MESSAGE_ID = 1;

    private IImageFilterService mService;

    private ImageView in;

    private ImageView out;

    private Bitmap mBitmapIn;

    private Bitmap mBitmapOut;

    private MemoryFile mFile;

    private final static WeakReference<Handler> mResponseHandlerRef = new WeakReference<Handler>(new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case RESPONSE_MESSAGE_ID:
                    Log.d(TAG, "Handling response");

                    break;
            }
        }
    });

    // the responsibility of the responseListener is to receive call-backs
    // from the service when our FibonacciResponse is available
    private final IImageFilterServiceResponseListener mResponseListener = new IImageFilterServiceResponseListener.Stub() {
        // this method is executed on one of the pooled binder threads
        @Override
        public void onResponse(int result) throws RemoteException {
            Log.d(TAG, "Got response: " + result);
            Handler handler = mResponseHandlerRef.get();
            if (null != handler) {
                Message message = handler.obtainMessage(RESPONSE_MESSAGE_ID, result);
                handler.sendMessage(message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBitmapIn = loadBitmap(R.raw.image2);
        in = (ImageView)findViewById(R.id.displayin);
        in.setImageBitmap(mBitmapIn);
        in.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mService) {
                    int BufferSize = mBitmapIn.getRowBytes() * mBitmapIn.getHeight();
                    ByteBuffer bitmapBuf = ByteBuffer.allocate(BufferSize);
                    bitmapBuf.order(ByteOrder.nativeOrder());
                    mBitmapIn.copyPixelsToBuffer(bitmapBuf);
                    try {
                        mFile = new MemoryFile("OutMemory", BufferSize);
                    } catch (IOException ex) {
                        Log.i(TAG, "Failed to create memory file.");
                        ex.printStackTrace();
                    }

                    try {
                        mFile.writeBytes(bitmapBuf.array(), 0, 0, BufferSize);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    FileDescriptor fd = MemoryFileUtil.getFileDescriptor(mFile);
                    ParcelFileDescriptor pfd = null;
                    try {
                        pfd = ParcelFileDescriptor.dup(fd);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    try {
                        mService.filter(FilterIDDefine.BLACKWHITE, pfd, mBitmapIn.getWidth(), mBitmapIn.getHeight(),
                                mResponseListener);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        out = (ImageView)findViewById(R.id.displayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        if (!bindService(new Intent(IImageFilterService.class.getName()), this, BIND_AUTO_CREATE)) {
            Log.w(TAG, "Failed to bind to service");
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        unbindService(this);
        super.onStop();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected() to " + name);
        mService = IImageFilterService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected() to " + name);
        mService = null;
    }

    private Bitmap loadBitmap(int resource) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }
}
