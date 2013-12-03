package com.zhaoyan.juyou.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.common.file.FileManager;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class FileOperationHelper {
	private static final String TAG = "FileOperationHelper";
	/**
	 * current operation fileinfo list.
	 */
	private List<FileInfo> mCurFilesList = new ArrayList<FileInfo>();
	
	private OnOperationListener mOperationListener = null;
	
	public static final int MSG_COPY_CUT_TO_CHILD = 0;
	public interface OnOperationListener{
		public void onFinished();
		public void onNotify(int msg);
		/**
		 * @param fileName current copy file name
		 * @param count current copy list file count
		 * @param filesize current copy file size(bytes)
		 * @param copysize copy size of current copy file(bytes)
		 */
		public void onRefreshFiles(String fileName, int count, long filesize, long copysize);
	}
	
	public void setOnOperationListener(OnOperationListener listener){
		mOperationListener = listener;
	}
	
	public void cancelOperationListener(){
		if (null != mOperationListener) {
			mOperationListener = null;
		}
	}
	
	/**
	 * record current operation fileinfos
	 * @param files
	 */
	public void copy(List<FileInfo> files) {
        copyFileList(files);
    }
	
	private void copyFileList(List<FileInfo> files) {
        synchronized(mCurFilesList) {
        	mCurFilesList.clear();
            for (FileInfo f : files) {
            	mCurFilesList.add(f);
            }
        }
    }
	
	/**
	 * start copy file from old path to new path
	 * @param newPath new path
	 * @return 
	 */
	public boolean doCopy(String newPath){
		Log.d(TAG, "doCopy:" + newPath);
		if (TextUtils.isEmpty(newPath)) {
			Log.e(TAG, "doCopy:" + newPath+ " is empty");
			return false;
		}
		
		final String _path = newPath;
		asnycExecute(new Runnable() {
			@Override
			public void run() {
				//count file nums
				int fileCounts = 0;
				File file = null;
				for(FileInfo f : mCurFilesList){
					file = new File(f.filePath);
					fileCounts += count(file);
				}
				mOperationListener.onRefreshFiles(null, fileCounts, -1, -1);
				
				boolean copyToChild = false;
				for (FileInfo f : mCurFilesList) {
					if (mStopCopy) {
						return;
					}
					
					if (FileManager.containsPath(f.filePath, _path)) {
						//cannot copy or cut a dir to it's child dir
						copyToChild = true;
						continue;
					}
					copyFiles(f, _path);
				}
				if (copyToChild) {
					mOperationListener.onNotify(MSG_COPY_CUT_TO_CHILD);
				}
				clear();
			}
		});
		
		return true;
	}
	
	int copyCount = 0;
	private void copyFiles(FileInfo fileInfo, String path){
		if (null == fileInfo || null == path) {
			 Log.e(TAG, "copyFiles: null parameter");
			return;
		}
		
		//copy
		String srcPath = fileInfo.filePath;
		String fileName = fileInfo.fileName;
		String desPath = FileManager.makePath(path, fileName);
		
		//if desFile is exist,auto rename
		if (new File(desPath).exists()) {
			fileName = FileInfoManager.autoRename(fileName);
			desPath = FileManager.makePath(path, fileName);
		}
		
		if (fileInfo.isDir) {
			copyFolder(srcPath, desPath);
		} else {
			copyCount ++;
			mOperationListener.onRefreshFiles(fileName, copyCount, new File(srcPath).length() , -1);
			copyFile(srcPath, desPath);
		}
	}
	
	/**
	 * folder copy
	 * @param srcPath source folder path
	 * @param desPath destination folder path
	 * @return
	 */
	private boolean copyFolder(String srcPath, String desPath) {
		File srcFile = new File(srcPath);
		String[] srcFileNameList = srcFile.list();
		if (srcFileNameList.length == 0) {
			//copy a empty folder
			return new File(desPath).mkdirs();
		}
		
		// create desPath folder
		if (!new File(desPath).mkdirs()) {
			return false;
		}
		
		File tempFile = null;
		for (String name : srcFileNameList) {
			if (srcPath.endsWith(File.separator)) {
				tempFile = new File(srcFile + name);
			} else {
				tempFile = new File(srcFile + File.separator + name);
			}

			if (tempFile.isFile()) {
				copyCount ++;
				mOperationListener.onRefreshFiles(name, copyCount, tempFile.length(), -1);
				copyFile(tempFile.getAbsolutePath(), desPath + File.separator
						+ name);
			} else if (tempFile.isDirectory()) {
				// is a child folder
				copyFolder(srcPath + File.separator + name, desPath
						+ File.separator + name);
			}
		}
		
		return true;
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
	private boolean copyFile(String srcPath, String desPath){
		if (new File(srcPath).isDirectory()) {
			Log.d(TAG, "copyFile error:" + srcPath + " is a directory.");
			return false;
		}
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		try {
			File srcFile = new File(srcPath);
			if (srcFile.exists()) {
				inputStream = new FileInputStream(srcPath);
				outputStream = new FileOutputStream(desPath);
				byte[] buffer = new byte[1024 * 10];
				int totalred = 0;
				int byteread = 0;
				while ((byteread = inputStream.read(buffer)) != -1) {
					totalred += byteread;
					outputStream.write(buffer, 0, byteread);
					mOperationListener.onRefreshFiles(null, -1, -1, totalred);
				}
				outputStream.flush();
				outputStream.close();
				inputStream.close();
			}else {
				Log.e(TAG, srcPath + " is not exist");
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * start cut files to new path
	 * @param newPath new path
	 * @return
	 */
	public boolean doCut(String newPath) {
		Log.d(TAG, "doCut:" + newPath);
		if (TextUtils.isEmpty(newPath)) {
			Log.e(TAG, "doCut:" + newPath+ " is empty");
			return false;
		}

		final String _path = newPath;
		asnycExecute(new Runnable() {
			@Override
			public void run() {
				//count file nums
				int fileCounts = 0;
				File file = null;
				for(FileInfo f : mCurFilesList){
					file = new File(f.filePath);
					fileCounts += count(file);
				}
				mOperationListener.onRefreshFiles(null, fileCounts, -1, -1);
				
				boolean cut_to_child = false;
				for (FileInfo f : mCurFilesList) {
					if (mStopCopy) {
						return;
					}
					
					if (FileManager.containsPath(f.filePath, _path)) {
						//cannot copy or cut a dir to it's child dir
						cut_to_child = true;
						continue;
					}
					copyCount ++;
					mOperationListener.onRefreshFiles(f.fileName, copyCount, -1, -1);
					moveFile(f, _path);
				}
				if (cut_to_child) {
					mOperationListener.onNotify(MSG_COPY_CUT_TO_CHILD);
				}
				clear();
			}
		});
		
		return true;
	}
	
	private boolean moveFile(FileInfo f, String dest) {
        Log.v(TAG, "MoveFile >>> " + f.filePath + "," + dest);

        if (f == null || dest == null) {
            Log.e(TAG, "CopyFile: null parameter");
            return false;
        }

        File file = new File(f.filePath);
        String newPath = FileManager.makePath(dest, f.fileName);
        try {
            return file.renameTo(new File(newPath));
        } catch (SecurityException e) {
            Log.e(TAG, "Fail to move file," + e.toString());
        }
        return false;
    }
	
	private void asnycExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				synchronized (mCurFilesList) {
					_r.run();
				}
				
				if (mOperationListener != null) {
					mOperationListener.onFinished();
				}
				return null;
			}
		}.execute();
    }
	
	public void clear() {
		Log.d(TAG, "clear");
		synchronized (mCurFilesList) {
			mCurFilesList.clear();
		}
		
		copyCount = 0;
		mStopCopy = false;
	}
	
	private int count(File f){
		int count = 0;
		if (f.isDirectory()) {
			File[] files = f.listFiles();
			for(File file : files){
				count += count(file);
			}
		}else {
			count ++;
		}
		
		return count;
	}
	
	private boolean mStopCopy = false;
	public void stopCopy(){
		mStopCopy = true;
		clear();
	}
}
