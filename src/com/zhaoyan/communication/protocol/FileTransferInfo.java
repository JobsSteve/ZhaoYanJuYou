package com.zhaoyan.communication.protocol;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.zhaoyan.common.file.APKFile;
import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.Log;

public class FileTransferInfo implements Serializable, Parcelable {
	private static final String TAG = "FileTransferInfo";
	/**
	 * 
	 */
	private static final long serialVersionUID = 6587172190406939461L;
	public String mFilePath;
	public String mFileName;
	public long mFileSize;
	private FileIcon mFileIcon;

	public FileTransferInfo() {
	}

	public FileTransferInfo(File file, Context context) {
		getFileInfo(file, context);
	}

	private void getFileInfo(File file, Context context) {
		int fileType = FileManager.getFileType(context, file);
		Log.d(TAG, "getFileInfo file = " + file.getName() + ", type = "
				+ fileType);
		switch (fileType) {
		case FileManager.APK:
			getApkFileInfo(file, context);
			break;

		default:
			getFileInfoDefault(file);
			break;
		}
	}

	private void getFileInfoDefault(File file) {
		mFilePath = file.getAbsolutePath();
		mFileName = file.getName();
		mFileSize = file.length();
		mFileIcon = null;
	}

	private void getApkFileInfo(File apk, Context context) {
		mFilePath = apk.getAbsolutePath();
		mFileName = APKFile.getApkName(context, apk.getAbsolutePath())
				+ ".apk";
		mFileSize = apk.length();

		Drawable icon = APKFile.getApkIcon(context,
				apk.getAbsolutePath());
		mFileIcon = new FileIcon(icon);

	}

	private FileTransferInfo(Parcel in) {
		readFromParcel(in);
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String path) {
		this.mFilePath = path;
	}

	/**
	 * @return the FileName
	 */
	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String name) {
		this.mFileName = name;
	}

	/**
	 * @return the FileSize
	 */
	public long getFileSize() {
		return mFileSize;
	}

	public void setFileSize(long size) {
		this.mFileSize = size;
	}

	/**
	 * Get the file icon, If there is no icon, return null.
	 * 
	 * @return If there is no icon, return null.
	 */
	public byte[] getFileIcon() {
		if (mFileIcon != null) {
			return mFileIcon.getFileIcon();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FileInfo [mFileName=" + mFileName + ", mFileSize=" + mFileSize
				+ "]";
	}

	public static final Parcelable.Creator<FileTransferInfo> CREATOR = new Parcelable.Creator<FileTransferInfo>() {

		@Override
		public FileTransferInfo createFromParcel(Parcel source) {
			return new FileTransferInfo(source);
		}

		@Override
		public FileTransferInfo[] newArray(int size) {
			return new FileTransferInfo[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(mFilePath);
		dest.writeString(mFileName);
		dest.writeLong(mFileSize);
	}

	public void readFromParcel(Parcel in) {
		mFilePath = in.readString();
		mFileName = in.readString();
		mFileSize = in.readLong();
	}

	class FileIcon implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3974137981951972275L;
		private byte[] mBitmapBytes;

		public FileIcon(Drawable icon) {
			Bitmap bitmap = drawableToBitmap(icon);
			mBitmapBytes = bitmapToByteArray(bitmap);
			Log.d(TAG, "FileIcon size = " + mBitmapBytes.length);
			bitmap.recycle();
		}

		public byte[] getFileIcon() {
			return mBitmapBytes;
		}
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
		return out.toByteArray();
	}

	public static Bitmap byteArrayToBitmap(byte[] data) {
		if (data.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}

	public static Drawable bitmapToDrawable(Bitmap bitmap) {
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
		return bitmapDrawable;
	}
}
