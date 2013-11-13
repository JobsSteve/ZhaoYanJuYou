package com.zhaoyan.common.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

import android.content.Context;
import android.os.Environment;

/**
 * This class is used for write log to file. It is useful for debug. Log file is
 * located at /data/data/com.dreamlink.communication/files/</br>
 * 
 * Usage:</br>
 * 
 * 1. {@link #open()}, Before write, we need to open the file to prepare
 * writing.</br>
 * 
 * 2. {@link #writeLog(String)}, Write log to file.</br>
 * 
 * 3. {@link #close()}, After finish all writing, do not forget to close the
 * file.;
 * 
 */
public class LogFile {
	private static final String TAG = "LogFile";
	private File mFile;
	private FileWriter mWriter;
	private static final String LOG_FOLDER_NAME = "ZhaoYanLog";

	/**
	 * Create log file use the file name.
	 * 
	 * @param fileName
	 */
	public LogFile(String fileName) {
		this(null, fileName);
	}

	/**
	 * Create log file use the file name.
	 * 
	 * @param context
	 * @param fileName
	 *            file name.
	 */
	public LogFile(Context context, String fileName) {
		createFile(fileName);
	}

	private void createFile(String fileName) {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ LOG_FOLDER_NAME
				+ File.separator + TimeUtil.getDate();
		String filePath = path + File.separator + fileName;
		mFile = new File(filePath);
		if (!mFile.exists()) {
			try {
				mFile.getParentFile().mkdirs();
				mFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "Creat file error. File is " + filePath + ". " + e);
			}
		}
	}

	/**
	 * Before write, we need to open the file to prepare writing.
	 */
	public boolean open() {
		if (mFile != null && !mFile.exists()) {
			try {
				mFile.getParentFile().mkdirs();
				mFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG,
						"Creat file error. File is " + mFile.getAbsolutePath()
								+ ". " + e);
				return false;
			}
		}
		try {
			mWriter = new FileWriter(mFile);
		} catch (IOException e) {
			Log.e(TAG, "Open file error. " + e);
		}
		return mWriter != null;
	}

	/**
	 * Write log to file. </br>
	 * 
	 * Tips: add \n to make log more readable.
	 * 
	 * @param log
	 */
	public void writeLog(String log) {
		if (mWriter == null) {
			if (!open()) {
				Log.e(TAG, "writeLog error. open() file error.");
				return;
			}
		}

		try {
			mWriter.write(log);
			mWriter.flush();
		} catch (IOException e) {
			Log.e(TAG, "writeLog write error." + e);
		}
	}

	public void writeLog(byte[] logs) {
		if (mWriter == null) {
			if (!open()) {
				Log.e(TAG, "writeLog error. open() file error.");
				return;
			}
		}
		char[] log = ZYUtils.getChars(logs);
		try {
			mWriter.write(log);
			mWriter.flush();
		} catch (IOException e) {
			Log.e(TAG, "writeLog write error." + e);
		}
	}

	/**
	 * After finish all writing, do not forget to close the file.
	 */
	public void close() {
		if (mWriter == null) {
			return;
		}
		try {
			mWriter.close();
		} catch (IOException e) {
			Log.e(TAG, "close error." + e);
		}
	}
}
