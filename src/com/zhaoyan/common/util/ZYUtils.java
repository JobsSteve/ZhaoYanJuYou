package com.zhaoyan.common.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.AppInfo;

public class ZYUtils {
	private static final String TAG = "DreamUtil";
	
	/**
	 * 插入�?��数据到已经排好序的list�?
	 * @param list 已经排好序的list
	 * @param appEntry 要插入的数据
	 * @return 将要插入的位�?
	 */
	public static int getInsertIndex(List<AppInfo> list, AppInfo appEntry){
		Collator sCollator = Collator.getInstance();
		for (int i = 0; i < list.size(); i++) {
			int ret = sCollator.compare(appEntry.getLabel(), list.get(i).getLabel());
			if (ret <=0 ) {
				return i;
			}
		}
		return list.size();
	}
	
	/**
	 * byte convert
	 * @param size like 3232332
	 * @return like 3.23M
	 */
//	public static String getFormatSize(long size){
//		if (size >= 1024 * 1024 * 1024){
//			Double dsize = (double) (size / (1024 * 1024 * 1024));
//			return new DecimalFormat("#.00").format(dsize) + "G";
//		}else if (size >= 1024 * 1024) {
//			Double dsize = (double) (size / (1024 * 1024));
//			return new DecimalFormat("#.00").format(dsize) + "M";
//		}else if (size >= 1024) {
//			Double dsize = (double) (size / 1024);
//			return new DecimalFormat("#.00").format(dsize) + "K";
//		}else {
//			return String.valueOf((int)size) + "B";
//		}
//	}
	
	public static String getFormatSize(double size){
		if (size >= 1024 * 1024 * 1024){
			Double dsize = size / (1024 * 1024 * 1024);
			return new DecimalFormat("#.00").format(dsize) + "G";
		}else if (size >= 1024 * 1024) {
			Double dsize = size / (1024 * 1024);
			return new DecimalFormat("#.00").format(dsize) + "M";
		}else if (size >= 1024) {
			Double dsize = size / 1024;
			return new DecimalFormat("#.00").format(dsize) + "K";
		}else {
			return String.valueOf((int)size) + "B";
		}
	}
	
	/**get date format*/
	public static String getFormatDate(long date){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = format.format(new Date(date));
		return dateString;
	}
	
	/** 
     * 格式化时间，将毫秒转换为�?秒格�?
     * @param time audio/video time like 12323312
     * @return the format time string like 00:12:23
     */  
	public static String mediaTimeFormat(long duration) {
		long hour = duration / (60 * 60 * 1000);
		String min = duration % (60 * 60 * 1000) / (60 * 1000) + "";
		String sec = duration % (60 * 60 * 1000) % (60 * 1000) + "";

		if (min.length() < 2) {
			min = "0" + duration / (1000 * 60) + "";
		}

		if (sec.length() == 4) {
			sec = "0" + sec;
		} else if (sec.length() == 3) {
			sec = "00" + sec;
		} else if (sec.length() == 2) {
			sec = "000" + sec;
		} else if (sec.length() == 1) {
			sec = "0000" + sec;
		}

		if (hour == 0) {
			return min + ":" + sec.trim().substring(0, 2);
		} else {
			String hours = "";
			if (hour < 10) {
				hours = "0" + hour;
			} else {
				hours = hours + "";
			}
			return hours + ":" + min + ":" + sec.trim().substring(0, 2);
		}
	}
	
	public static void showInfoDialog(Context context, String info){
		String title = context.getResources().getString(R.string.menu_info);
		showInfoDialog(context, title, info);
	}
	
	public static void showInfoDialog(Context context, String title, String info){
		new AlertDialog.Builder(context)
		.setTitle(title)
		.setMessage(info)
		.setPositiveButton(android.R.string.ok, null)
		.create().show();
	}
	
	/**set dialog dismiss or not*/
	public static void setDialogDismiss(DialogInterface dialog, boolean dismiss){
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, dismiss);
			dialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**Drawable convert to byte */
	public static synchronized byte[] drawableToByte(Drawable drawable) {
		if (drawable != null) {
			Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
					drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(bitmap);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			drawable.draw(canvas);
			int size = bitmap.getWidth() * bitmap.getHeight() * 4;
			// 创建�?��字节数组输出�?流的大小为size
			ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
			// 设置位图的压缩格式，质量�?00%，并放入字节数组输出流中
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
			// 将字节数组输出流转化为字节数组byte[]
			byte[] imagedata = baos.toByteArray();
			return imagedata;
		}
		return null;
	}
	
	/**
	 * get parent path
	 * @param path current file path
	 * @return parent path
	 */
	public static String getParentPath(String path){
		File file = new File(path);
		return file.getParent();
	}
	
	/**
	 * bytes tp chars
	 * 
	 * @param bytes
	 * @return
	 */
	public static char[] getChars(byte[] bytes) {
		Charset cs = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		CharBuffer cb = cs.decode(bb);
		return cb.array();
	}
	
	/**
	 * copy single file
	 * 
	 * @param srcPath
	 *           src file path
	 * @param desPath
	 *           des file path
	 * @return
	 * @throws Exception
	 */
	public static void fileStreamCopy(String srcPath, String desPath) throws IOException{
		Log.d(TAG, "fileStreamCopy.src:" + srcPath);
		Log.d(TAG, "fileStreamCopy.dec:" + desPath);
		if (new File(srcPath).isDirectory()) {
			Log.d(TAG, "copyFile error:" + srcPath + " is a directory.");
			return;
		}
		File files = new File(desPath);// 创建文件
		FileOutputStream fos = new FileOutputStream(files);
		byte buf[] = new byte[1024];
		InputStream fis = new BufferedInputStream(new FileInputStream(srcPath),
				8192 * 4);
		do {
			int read = fis.read(buf);
			if (read <= 0) {
				break;
			}
			fos.write(buf, 0, read);
		} while (true);
		fis.close();
		fos.close();
	}
}
