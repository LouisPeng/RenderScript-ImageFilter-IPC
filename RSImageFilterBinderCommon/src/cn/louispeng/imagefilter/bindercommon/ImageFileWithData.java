package cn.louispeng.imagefilter.bindercommon;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageFileWithData implements Parcelable {

    private final byte[] mData;

    private final int mWidth;

    private final int mHeight;

    public ImageFileWithData(byte[] data, int width, int height) {
        if (null == data || 0 == data.length) {
            throw new NullPointerException("data must not be null");
        }
        mData = data;
        mWidth = width;
        mHeight = height;
    }

    public byte[] getData() {
        return mData;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mData.length);
        parcel.writeByteArray(mData);
        parcel.writeInt(mWidth);
        parcel.writeInt(mHeight);
    }

    public static final Parcelable.Creator<ImageFileWithData> CREATOR = new Parcelable.Creator<ImageFileWithData>() {
        @Override
        public ImageFileWithData createFromParcel(Parcel in) {
            int size = in.readInt();
            byte[] data = new byte[size];
            in.readByteArray(data);
            int width = in.readInt();
            int height = in.readInt();
            return new ImageFileWithData(data, width, height);
        }

        @Override
        public ImageFileWithData[] newArray(int size) {
            return new ImageFileWithData[size];
        }
    };
}
