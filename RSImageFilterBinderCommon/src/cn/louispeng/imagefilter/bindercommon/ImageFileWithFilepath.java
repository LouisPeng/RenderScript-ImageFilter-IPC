package cn.louispeng.imagefilter.bindercommon;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageFileWithFilepath implements Parcelable {

    private final String mFilepath;

    private final int mWidth;

    private final int mHeight;

    public ImageFileWithFilepath(String filepath, int width, int height) {
        if (null == filepath) {
            throw new NullPointerException("filepath must not be null");
        }
        mFilepath = filepath;
        mWidth = width;
        mHeight = height;
    }

    public String getFilepath() {
        return mFilepath;
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
        parcel.writeString(mFilepath);
        parcel.writeInt(mWidth);
        parcel.writeInt(mHeight);
    }

    public static final Parcelable.Creator<ImageFileWithFilepath> CREATOR = new Parcelable.Creator<ImageFileWithFilepath>() {
        @Override
        public ImageFileWithFilepath createFromParcel(Parcel in) {
            String filepath = in.readString();
            int width = in.readInt();
            int height = in.readInt();
            return new ImageFileWithFilepath(filepath, width, height);
        }

        @Override
        public ImageFileWithFilepath[] newArray(int size) {
            return new ImageFileWithFilepath[size];
        }
    };
}
