package com.zhaoyan.common.util;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapUtilities {

	public BitmapUtilities() {
		// TODO Auto-generated constructor stub
	}

	public static Bitmap getBitmapThumbnail(String path, int width, int height) {
		Bitmap bitmap = null;
		// 这里可以按比例缩小图片：
		/*
		 * BitmapFactory.Options opts = new BitmapFactory.Options();
		 * opts.inSampleSize = 4;//宽和高都是原来的1/4 bitmap =
		 * BitmapFactory.decodeFile(path, opts);
		 */

		/*
		 * 进一步的， 如何设置恰当的inSampleSize是解决该问题的关键之一。BitmapFactory.
		 * Options提供了另一个成员inJustDecodeBounds。
		 * 设置inJustDecodeBounds为true后，decodeFile并不分配空间
		 * ，但可计算出原始图片的长度和宽度，即opts.width和opts.height。
		 * 有了这两个参数，再通过一定的算法，即可得到一个恰当的inSampleSize。
		 */
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = Math.max((int) (opts.outHeight / (float) height),
				(int) (opts.outWidth / (float) width));
		opts.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(path, opts);
		return bitmap;
	}

	public static Bitmap getBitmapThumbnail(Bitmap bmp, int width, int height) {
		Bitmap bitmap = null;
		if (bmp != null) {
			int bmpWidth = bmp.getWidth();
			int bmpHeight = bmp.getHeight();
			if (width != 0 && height != 0) {
				Matrix matrix = new Matrix();
				float scaleWidth = ((float) width / bmpWidth);
				float scaleHeight = ((float) height / bmpHeight);
				matrix.postScale(scaleWidth, scaleHeight);
				bitmap = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight,
						matrix, true);
			} else {
				bitmap = bmp;
			}
		}
		return bitmap;
	}

	public static byte[] bitmapToByteArray(Bitmap bitmap) {
		return bitmapToByteArray(bitmap, Bitmap.CompressFormat.PNG);
	}

	public static byte[] bitmapToByteArray(Bitmap bitmap,
			Bitmap.CompressFormat format) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bitmap.compress(format, 100, out);
		return out.toByteArray();
	}

	public static Bitmap byteArrayToBitmap(byte[] data) {
		if (data.length == 0) {
			return null;
		}
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}

}
