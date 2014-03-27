
package cn.louispeng.imagefilter.bindercommon;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

public class ImageFile implements Parcelable {

    private final ParcelFileDescriptor mPFD;

    private final long mSize;

    private final int mWidth;

    private final int mHeight;

    public ImageFile(ParcelFileDescriptor pfd, long size, int width, int height) {
        if (null == pfd) {
            throw new NullPointerException("pfd must not be null");
        }
        mPFD = pfd;
        mSize = size;
        mWidth = width;
        mHeight = height;
    }

    public ParcelFileDescriptor getParcelFileDescriptor() {
        return mPFD;
    }

    public long getSize() {
        return mSize;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(mPFD, flags);
        parcel.writeLong(mSize);
        parcel.writeInt(mWidth);
        parcel.writeInt(mHeight);
    }

    public static final Parcelable.Creator<ImageFile> CREATOR = new Parcelable.Creator<ImageFile>() {
        public ImageFile createFromParcel(Parcel in) {
            ParcelFileDescriptor pfd = in.readParcelable(ParcelFileDescriptor.class.getClassLoader());
            long size = in.readLong();
            int width = in.readInt();
            int height = in.readInt();
            return new ImageFile(pfd, size, width, height);
        }

        public ImageFile[] newArray(int size) {
            return new ImageFile[size];
        }
    };
}
