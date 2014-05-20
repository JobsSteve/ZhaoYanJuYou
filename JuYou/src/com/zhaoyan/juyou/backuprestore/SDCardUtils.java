package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.Constants.ModulePath;
import com.zhaoyan.juyou.common.ZyStorageManager;

public class SDCardUtils {
	private static final String TAG = "SDCardUtils";
	public final static int MINIMUM_SIZE = 512;

	public static String getInternalStorage() {
		// return StorageManagerEx.getInternalStoragePath();
		return null;
	}

	public static String getExternalStoragePath(Context context) {
		Log.d(TAG, "getExternalStoragePath");
		String[] paths = null;
		String result = null;
		
		ZyStorageManager zyStorageManager = ZyStorageManager.getInstance(context);
		paths = zyStorageManager.getVolumePaths();

		if (paths == null) {
			// do nothing
		}

		if (paths.length == 1) {
			result = paths[0];
		}

		if (paths.length >= 2) {
			result = paths[1];
		}
		Log.d(TAG, "getExternalStoragePath: path is " + result);
		return result;
	}

	public static String getSDStatueMessage(Context context) {
		String message = context.getString(R.string.nosdcard_notice);
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_SHARED)
				|| status.equals(Environment.MEDIA_UNMOUNTED)) {
			message = context.getString(R.string.sdcard_busy_message);
		}
		return message;
	}

	public static String getStoragePath(Context context) {
		String storagePath = getExternalStoragePath(context);
		if (storagePath == null) {
			return null;
		}
		storagePath = storagePath + File.separator + "backup";
		Log.d(TAG, "getStoragePath: path is " + storagePath);
		File file = new File(storagePath);
		if (file != null) {
			if (file.exists() && file.isDirectory()) {
				File temp = new File(storagePath + File.separator
						+ ".BackupRestoretemp");
				boolean ret;
				if (temp.exists()) {
					ret = temp.delete();
				} else {
					try {
						ret = temp.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
						Log.e(TAG, "getStoragePath: " + e.getMessage());
						ret = false;
					} finally {
						temp.delete();
					}
				}
				if (ret) {
					return storagePath;
				} else {
					return null;
				}

			} else if (file.mkdir()) {
				return storagePath;
			}
		}
		return null;
	}

	public static String getPersonalDataBackupPath(Context context) {
		Log.d(TAG, "getPersonalDataBackupPath");
		String path = getStoragePath(context);
		if (path != null) {
			return path + File.separator + ModulePath.FOLDER_DATA;
		}
		return path;
	}

	public static String getAppsBackupPath(Context context) {
		Log.d(TAG, "getAppsBackupPath");
		String path = getStoragePath(context);
		if (path != null) {
			return path + File.separator + ModulePath.FOLDER_APP;
		}
		return path;
	}

	public static boolean isSdCardAvailable(Context context) {
		return (getStoragePath(context) != null);
	}
}
