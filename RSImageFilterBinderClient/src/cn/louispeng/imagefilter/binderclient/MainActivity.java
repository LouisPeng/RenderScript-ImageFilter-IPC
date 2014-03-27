package cn.louispeng.imagefilter.binderclient;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Environment;
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
import cn.louispeng.imagefilter.bindercommon.FileUtils;
import cn.louispeng.imagefilter.bindercommon.FilterIDDefine;
import cn.louispeng.imagefilter.bindercommon.IImageFilterService;
import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithData;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithFilepath;
import cn.louispeng.imagefilter.bindercommon.ImageFileWithParcelFileDescriptor;
import cn.louispeng.imagefilter.bindercommon.ProfileUtil;

public class MainActivity extends Activity implements ServiceConnection {
    public static final String TAG = MainActivity.class.getSimpleName();

    // the id of a message to our response handler
    private static final int RESPONSE_WITH_PARCELFILEDESCRIPTOR_MESSAGE_ID = 1;
    private static final int RESPONSE_WITH_DATA_MESSAGE_ID = 2;
    private static final int RESPONSE_WITH_FILEPATH_MESSAGE_ID = 3;

    private IImageFilterService mService;

    private ImageView in;

    private ImageView out;

    private Bitmap mBitmapIn;

    private Bitmap mBitmapOut;

    private boolean inProcess = false;

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
            case RESPONSE_WITH_PARCELFILEDESCRIPTOR_MESSAGE_ID: {
                Log.d(TAG, "Handling response");
                MainActivity activity = mActivity.get();
                int result = message.arg1;
                ImageFileWithParcelFileDescriptor imageFile = (ImageFileWithParcelFileDescriptor) message.obj;
                if (0 == result) {
                    FileDescriptor inFD = imageFile.getParcelFileDescriptor().getFileDescriptor();
                    if (inFD == null) {
                        Log.d(TAG, "Failed to get memeory file descriptor.");
                    } else {
                        byte[] data = null;
                        try {
                            data = FileUtils.read(inFD);
                        } catch (Exception e) {
                            e.printStackTrace();
                            result = -1;
                        }

                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        activity.mBitmapOut.copyPixelsFromBuffer(buffer);
                        activity.out.invalidate();
                        activity.out.setVisibility(View.VISIBLE);
                    }
                }
                activity.inProcess = false;
                break;
            }
            case RESPONSE_WITH_DATA_MESSAGE_ID: {
                Log.d(TAG, "Handling response");
                MainActivity activity = mActivity.get();
                int result = message.arg1;
                ImageFileWithData imageFile = (ImageFileWithData) message.obj;
                if (0 == result) {
                    ByteBuffer buffer = ByteBuffer.wrap(imageFile.getData());
                    activity.mBitmapOut.copyPixelsFromBuffer(buffer);
                    activity.out.invalidate();
                    activity.out.setVisibility(View.VISIBLE);
                }
                activity.inProcess = false;
                break;
            }
            case RESPONSE_WITH_FILEPATH_MESSAGE_ID: {
                Log.d(TAG, "Handling response");
                MainActivity activity = mActivity.get();
                int result = message.arg1;
                ImageFileWithFilepath imageFile = (ImageFileWithFilepath) message.obj;
                if (0 == result) {
                    String filepath = imageFile.getFilepath();
                    if (filepath == null) {
                        Log.d(TAG, "Failed to get filepath.");
                    } else {
                        activity.mBitmapOut = BitmapFactory.decodeFile(filepath);
                        activity.out.setImageBitmap(activity.mBitmapOut);
                        activity.out.invalidate();
                        activity.out.setVisibility(View.VISIBLE);
                    }
                }
                activity.inProcess = false;
                break;
            }
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);

    // the responsibility of the responseListener is to receive call-backs
    // from the service when result is available
    private final IImageFilterServiceResponseListener mResponseListener = new IImageFilterServiceResponseListener.Stub() {
        // this method is executed on one of the pooled binder threads
        @Override
        public void onResponseWithParcelFileDescriptor(int result, ImageFileWithParcelFileDescriptor imageFile) throws RemoteException {
            ProfileUtil.checkpoint("AIDL callback in");
            Log.d(TAG, "Got response: " + result);
            Message message = mHandler.obtainMessage(RESPONSE_WITH_PARCELFILEDESCRIPTOR_MESSAGE_ID);
            message.arg1 = result;
            message.obj = imageFile;
            mHandler.sendMessage(message);
        }

        @Override
        public void onResponseWithData(int result, ImageFileWithData imageFile) throws RemoteException {
            ProfileUtil.checkpoint("AIDL callback in");
            Log.d(TAG, "Got response: " + result);
            Message message = mHandler.obtainMessage(RESPONSE_WITH_DATA_MESSAGE_ID);
            message.arg1 = result;
            message.obj = imageFile;
            mHandler.sendMessage(message);
        }

        @Override
        public void onResponseWithFilepath(int result, ImageFileWithFilepath imageFile) throws RemoteException {
            ProfileUtil.checkpoint("AIDL callback in");
            Log.d(TAG, "Got response: " + result);
            Message message = mHandler.obtainMessage(RESPONSE_WITH_FILEPATH_MESSAGE_ID);
            message.arg1 = result;
            message.obj = imageFile;
            mHandler.sendMessage(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBitmapIn = loadBitmap(R.raw.image2);
        in = (ImageView) findViewById(R.id.displayin);
        in.setImageBitmap(mBitmapIn);
        in.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mService && !inProcess) {
                    inProcess = true;
//                    TestFilterWithParcelFileDescriptor();
//                    TestFilterWithData();
                    TestFilterWithFilepath();
                }
            }

            private void TestFilterWithParcelFileDescriptor() {
                ProfileUtil.start("IPC-Profile");
                out.setVisibility(View.INVISIBLE);
                int BufferSize = mBitmapIn.getRowBytes() * mBitmapIn.getHeight();
                ByteBuffer bitmapBuf = ByteBuffer.allocate(BufferSize);
                bitmapBuf.order(ByteOrder.nativeOrder());
                mBitmapIn.copyPixelsToBuffer(bitmapBuf);
                ProfileUtil.checkpoint(TAG + " mBitmapIn.copyPixelsToBuffer");

                MemoryFile mFile = null;
                try {
                    mFile = new MemoryFile("ImageMemory", BufferSize);
                    mFile.allowPurging(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    inProcess = false;
                }
                ProfileUtil.checkpoint(TAG + " Create MemoryFile");

                if (null != mFile) {
                    try {
                        mFile.writeBytes(bitmapBuf.array(), 0, 0, BufferSize);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ProfileUtil.checkpoint(TAG + " mFile.writeBytes()");

                    ParcelFileDescriptor pfd = null;
                    try {
                        FileDescriptor fd = mFile.getFileDescriptor();
                        pfd = ParcelFileDescriptor.dup(fd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (null != pfd) {
                        ImageFileWithParcelFileDescriptor imageFile = new ImageFileWithParcelFileDescriptor(pfd, BufferSize, mBitmapIn
                                .getWidth(), mBitmapIn.getHeight());
                        try {
                            mService.filterWithParcelFileDescriptor(FilterIDDefine.BLACKWHITE, imageFile, mResponseListener);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            inProcess = false;
                        }
                    }

                    mFile.close();
                }

                ProfileUtil.checkpoint(TAG + " mService.filter()");
            }

            private void TestFilterWithData() {
                ProfileUtil.start("IPC-Profile");
                out.setVisibility(View.INVISIBLE);
                int BufferSize = mBitmapIn.getRowBytes() * mBitmapIn.getHeight();
                ByteBuffer bitmapBuf = ByteBuffer.allocate(BufferSize);
                bitmapBuf.order(ByteOrder.nativeOrder());
                mBitmapIn.copyPixelsToBuffer(bitmapBuf);
                ProfileUtil.checkpoint(TAG + " mBitmapIn.copyPixelsToBuffer");

                ImageFileWithData imageFile = new ImageFileWithData(bitmapBuf.array(), mBitmapIn.getWidth(), mBitmapIn.getHeight());
                try {
                    mService.filterWithData(FilterIDDefine.BLACKWHITE, imageFile, mResponseListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    inProcess = false;
                }

                ProfileUtil.checkpoint(TAG + " mService.filter()");
            }

            private void TestFilterWithFilepath() {
                final String inputFilepath = Environment.getExternalStorageDirectory() + "/DCIM/Camera/IMAG0003.jpg";
                ProfileUtil.start("IPC-Profile");
                out.setVisibility(View.INVISIBLE);
                Options opts = new Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(inputFilepath, opts);
                ImageFileWithFilepath imageFile = new ImageFileWithFilepath(inputFilepath, opts.outWidth, opts.outHeight);
                try {
                    mService.filterWithFilepath(FilterIDDefine.BLACKWHITE, imageFile, mResponseListener);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    inProcess = false;
                }

                ProfileUtil.checkpoint(TAG + " mService.filter()");
            }
        });

        out = (ImageView) findViewById(R.id.displayout);
        mBitmapOut = Bitmap.createBitmap(mBitmapIn.getWidth(), mBitmapIn.getHeight(), mBitmapIn.getConfig());
        out.setImageBitmap(mBitmapOut);
        out.setVisibility(View.INVISIBLE);
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
