package com.zhaoyan.juyou.common;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;

import com.zhaoyan.common.util.Log;

/**
 * 使用发射的方法获得内置和sdcard的路径
 * @author Yuri
 * @date 20140505
 */
public class ZyStorageManager {
	private static final String TAG = "ZyStorageManager";
	public static final String SEPERATOR = "/";
	public static final String NO_EXTERNAL_SDCARD = "no_external_sdcard | no_mounted";
	public static final String NO_INTERNAL_SDCARD = "no_internal_sdcard";
	
	public static final int INTERNAL = 0;
	public static final int SDCARD = 1;
	
	private StorageManager mStorageManager;
	private Method mMethodGetPaths;
	
	private static ZyStorageManager mInstance = null;
	
	private ZyStorageManager(Context context){
		init(context);
	}
	
	public static ZyStorageManager getInstance(Context context){
		if (mInstance == null) {
			mInstance = new ZyStorageManager(context);
		}
		return mInstance;
	}
	
	private void init(Context context){
		mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
		try {
			mMethodGetPaths = mStorageManager.getClass().getMethod(
					"getVolumePaths");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 获取存储位置数组，通过反射方式 
	 * 默认第一个路径是内置存储
	 * 如果只有一个那么就是内置存储
	 * @return
	 */
	public String[] getVolumePaths() {
		String[] paths = null;
		try {
			paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return paths;
	}
	
	public static long getAvailableBlockSize(String path){
		StatFs stat = new StatFs(path);
		long blocksize = stat.getBlockSize();
		long availableblocks = stat.getAvailableBlocks();
		return availableblocks * blocksize;
	}
	
	public static long getTotalBlockSize(String path){
		StatFs stat = new StatFs(path);
		long blocksize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blocksize;
	}
	
	
	/***
	 * is external exist
	 */
	public boolean isSdcardMounted(){
		String state = Environment.getExternalStorageState();
		String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(sdcardPath);
		if (Environment.MEDIA_MOUNTED.equals(state) && file.listFiles().length < 0) {
			return false;
		}
		Log.d(TAG, "isSdcardMounted:" + state + "\n" + sdcardPath);
		return true;
	}
	
	/**
	 * get show path</br>
	 * like: /mnt/sdcard/JuYou</br>
	 * you can get return /JuYou
	 * @param rootPath current root path    like:/mnt/sdcard/
	 * @param path  current path  like: /mnt/sdcard/JuYou
	 * @return  /JuYou
	 */
	public static String getShowPath(String rootPath, String path){
		int len = rootPath.length();
		String result = path.substring(len);
		Log.d(TAG, "getShowPath=" + result);
		return result;
	}
}
