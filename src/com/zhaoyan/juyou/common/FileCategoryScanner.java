package com.zhaoyan.juyou.common;

import java.io.File;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.LogFile;
import com.zhaoyan.juyou.activity.FileCategoryActivity;

import android.content.Context;

/**
 * Scan directory to get specified types files. The scan is implements by
 * multiple threads.
 * 
 */
public class FileCategoryScanner {
	private String[] mFilterType;
	private int mType = -1;
	private File mRootDir;
	private File[] mRootDirs;
	private Context mContext;
	private FileCategoryScanListener mListener;
	private Vector<FileInfo> mFileInfos;
	private int mScanningDirCount = 0;
	private ExecutorService mExecutorService;

	private boolean mIsScanning = false;

	/**
	 * 
	 * @param context
	 *            ApplicationContext
	 * @param rootDir
	 *            the directory to scan. must be a directory or
	 *            IllegalArgumentException will be throwed.
	 * @param filterType
	 *            null means all type.
	 */
	public FileCategoryScanner(Context context, File rootDir,
			String[] filterType, int type) {
		if (!rootDir.isDirectory()) {
			throw new IllegalArgumentException(
					"FileCategoryScanner, rootDir must be a directory.");
		}
		mContext = context.getApplicationContext();
		mFileInfos = new Vector<FileInfo>();
		mRootDir = rootDir;
		mFilterType = filterType;
		mType = type;
	}
	
	public FileCategoryScanner(Context context, File[] rootDirs,
			String[] filterType, int type){
		for(File file : rootDirs){
			if (!file.isDirectory()) {
				throw new IllegalArgumentException(
						"FileCategoryScanner, rootDir must be a directory.");
			}
		}
		mContext = context.getApplicationContext();
		mFileInfos = new Vector<FileInfo>();
		mRootDirs = rootDirs;
		mFilterType = filterType;
		mType = type;
	}

	public void startScan() {
		if (mIsScanning) {
			return;
		}
		if (mListener != null) {
			mListener.onScanStart();
		}
		mScanningDirCount = 0;
		mExecutorService = Executors.newCachedThreadPool();
		if (null == mRootDir) {
			scanDirs(mRootDirs);
		}else {
			scanDir(mRootDir);
		}
	}

	public boolean isScanning() {
		return mIsScanning;
	}

	public void cancelScan() {
		if (mIsScanning) {
			mExecutorService.shutdown();
			if (mListener != null) {
				mListener.onScanCancel();
			}
			mIsScanning = false;
		}
	}

	public void setScanListener(FileCategoryScanListener listener) {
		mListener = listener;
	}

	private class ScanRunnable implements Runnable {
		private File mDir;

		public ScanRunnable(File dir) {
			if (!dir.isDirectory()) {
				throw new IllegalArgumentException(
						"ScanThread, dir must be a directory.");
			}
			mDir = dir;
		}

		@Override
		public void run() {
			File[] files = mDir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						//do not load juyou log file
						if (!LogFile.LOG_FOLDER_NAME.equals(file.getAbsolutePath())) {
							scanDir(file);
						}
					} else {
						categoryFile(file);
					}
				}
			}
			scanDirFinish();
		}
	}
	
	private class ScansRunnable implements Runnable {
		private File[] mDirs;

		public ScansRunnable(File[] dirs) {
			for(File file : dirs){
				if (!file.isDirectory()) {
					throw new IllegalArgumentException(
							"FileCategoryScanner, rootDir must be a directory.");
				}
			}
			mDirs = dirs;
		}

		@Override
		public void run() {
			for(File file : mDirs){
				scanDir(file);
			}
			scanDirFinish();
		}
	}

	private synchronized void scanDir(File dir) {
		mScanningDirCount++;
		mExecutorService.execute(new ScanRunnable(dir));
	}
	
	private synchronized void scanDirs(File[] dirs){
		mScanningDirCount++;
		mExecutorService.execute(new ScansRunnable(dirs));
	}

	private void categoryFile(File file) {
		String name = file.getName();
		boolean isMatched = false;
		if (mFilterType != null) {
			for (String filter : mFilterType) {
				if (name.endsWith(filter)) {
					isMatched = true;
					break;
				}
			}
		} else {
			isMatched = true;
		}

		if (isMatched) {
			FileInfo fileInfo = new FileInfo(file.getName());
			fileInfo.fileDate = file.lastModified();
			fileInfo.filePath = file.getAbsolutePath();
			fileInfo.fileSize = file.length();
			fileInfo.isDir = false;
			fileInfo.type = FileManager.getFileType(mContext, file);
			mFileInfos.add(fileInfo);
		}
	}

	private synchronized void scanDirFinish() {
		mScanningDirCount--;
		if (mScanningDirCount == 0) {
			scanDirAllFinish();
		}
	}

	private void scanDirAllFinish() {
		mIsScanning = false;
		try {
			if (FileCategoryActivity.TYPE_APK == mType) {
				Collections.sort(mFileInfos, FileInfo.getNameComparator());
			}else {
				Collections.sort(mFileInfos, FileInfo.getDateComparator());
			}
		} catch (Exception e) {
		}
		if (mListener != null) {
			mListener.onScanComplete(mFileInfos);
		}
		mExecutorService.shutdown();
	}

	public interface FileCategoryScanListener {
		/**
		 * Scan is started. Notice, this method runs in the same thread with the
		 * invoker.
		 */
		void onScanStart();

		/**
		 * Scan is complete.Notice, this method runs in the different thread
		 * with the invoker.
		 * 
		 * @param fileInfos
		 */
		void onScanComplete(Vector<FileInfo> fileInfos);

		/**
		 * Scan is canceled when scanning. this method runs in the same thread
		 * with the invoker.
		 */
		void onScanCancel();
	}
}
