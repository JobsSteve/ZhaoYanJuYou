package com.zhaoyan.juyou.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.file.MultiMediaScanner;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.dialog.InfoDialog;
import com.zhaoyan.juyou.dialog.ZyEditDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;

public class FileInfoManager {
	private static final String TAG = "FileInfoManager";
	
	private int renameFlag = 0;
	/**
	 * show rename dialog
	 * 
	 * @param fileInfo
	 *            the file info
	 * @param position
	 *            the click position
	 */
	public void showRenameDialog(final Context context, final List<FileInfo> list) {
		final ZyEditDialog editDialog = new ZyEditDialog(context);
		editDialog.setTitle(R.string.rename);
		editDialog.setEditStr(list.get(renameFlag).fileName);
		editDialog.selectAll();
		editDialog.showIME(true);
		editDialog.setPositiveButton(R.string.ok, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				String newName = editDialog.getEditTextStr();
				String oldPath = list.get(renameFlag).filePath;
				list.get(renameFlag).fileName = newName;
				list.get(renameFlag).filePath = rename(
						new File(list.get(renameFlag).filePath),
						newName);
				String newPath = list.get(renameFlag).filePath;
				renameFlag++;
				if (renameFlag < list.size()) {
					editDialog.setEditStr(list.get(renameFlag).fileName);
					editDialog.selectAll();
					editDialog.refreshUI();
				} else {
					dialog.dismiss();
					renameFlag = 0;
				}
				
				List<String> pathsList = new ArrayList<String>();
				pathsList.add(oldPath);
				pathsList.add(newPath);
				MultiMediaScanner.scanFiles(context, pathsList, null);
			}
		});		
		editDialog.setNegativeButton(R.string.cancel, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				renameFlag++;
				if (renameFlag < list.size()) {
					editDialog.setEditStr(list.get(renameFlag).fileName);
					editDialog.selectAll();
					editDialog.refreshUI();
				} else {
					dialog.dismiss();
					renameFlag = 0;
				}
			}
		});
		editDialog.show();
	}
	
	/**
	 * rename the file
	 * @param oldFile
	 * @param newName
	 */
	public String rename(File oldFile, String newName){
			String parentPath = oldFile.getParent(); // 取得上一级目录
			File newFile = new File(parentPath + "/" + newName);
			oldFile.renameTo(newFile);
			return newFile.getAbsolutePath();
	}
	
	/**
	 * show file info dialog
	 * @param context
	 * @param list 
	 */
	public void showInfoDialog(Context context, List<FileInfo> list){
		GetFileSizeTask task = new GetFileSizeTask(context, list);
		task.execute();
	}
	
	private class GetFileSizeTask extends AsyncTask<Void, Void, Void>{
		long size = 0;
		int fileNum = 0;
		int folderNum = 0;
		InfoDialog infoDialog = null;
		int type;
		List<FileInfo> fileList;
		Context context;
		
		GetFileSizeTask(Context context, List<FileInfo> list){
			fileList = list;
			if (list.size() == 1) {
				type = InfoDialog.SINGLE_FILE;
			}else {
				type = InfoDialog.MULTI;
			}
			
			this.context = context;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "doInBackground");
			File file = null;
			switch (type) {
			case InfoDialog.SINGLE_FILE:
				FileInfo fileInfo1 = fileList.get(0);
				String filename = fileInfo1.fileName;
				
				String fileType = "";
				if (fileInfo1.isDir) {
					fileType = "文件夹";
					infoDialog.setTitle(R.string.info_folder_info);
					infoDialog.setFileType(InfoDialog.FOLDER, fileType);
				}else {
					fileType = FileManager.getExtFromFilename(filename);
					if ("".equals(fileType)) {
						fileType = "未知";
					}
					infoDialog.setTitle(R.string.info_file_info);
					infoDialog.setFileType(InfoDialog.FILE, fileType);
				}
				
				infoDialog.setFileName(filename);
				infoDialog.setFilePath(ZYUtils.getParentPath(fileInfo1.filePath));
				infoDialog.setModifyDate(fileInfo1.fileDate);
				infoDialog.updateSingleFileUI();
				
				file = new File(fileInfo1.filePath);
				getFileSize(file);
				break;
			case InfoDialog.MULTI:
				infoDialog.setTitle(R.string.info_file_info);
				infoDialog.updateTitle();
				for(FileInfo info : fileList){
					file = new File(info.filePath);
					getFileSize(file);
				}
				break;
			default:
				break;
			}
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			infoDialog = new InfoDialog(context,type);
			infoDialog.show();
			infoDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
					infoDialog = null;
				}
			});
		}
		
		@Override
		protected void onProgressUpdate(Void...values) {
			super.onProgressUpdate(values);
			infoDialog.updateUI(size, fileNum, folderNum);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute.");
			infoDialog.scanOver();
		}
		
		private void getFileSize(File file){
			if (isCancelled()) {
				return;
			}else {
				if (file.isHidden()) {
					//do not shwo hidden file size
					//do nothing
				}else {
					if (file.isDirectory()) {
						folderNum ++ ;
						File[] files = file.listFiles();
						for(File file2 : files){
							getFileSize(file2);
						}
					}else {
						fileNum ++;
						size += file.length();
					}
					onProgressUpdate();
				}
			}
		}
	}
	
	private static final String kuohu1 = ")";
	private static final String kuohu2 = "(";
	/**
	 * auto rename
	 * @param oldName
	 * @return newName
	 */
	public static String autoRename(String oldName){
		String newName = "";
		String tempName = "";
		String extensionName = "";
		int index = oldName.lastIndexOf(".");
		if (index == -1) {
			tempName = oldName;
		}else {
			//得到除去扩展名的文件名，如：abc
			tempName = oldName.substring(0, oldName.lastIndexOf("."));
			extensionName =  oldName.substring(index);
		}
		
		//得到倒数第一个括弧的位置
		int kuohuoIndex1 = tempName.lastIndexOf(kuohu1);
		//得到倒数第二个括弧的位置
		int kuohuoIndex2 = tempName.lastIndexOf(kuohu2);
		if (kuohuoIndex1 != tempName.length() - 1) {
			newName = tempName + "(2)" + extensionName;
		}else {
			//得到括弧里面的String
			String str = tempName.substring(kuohuoIndex2 + 1, kuohuoIndex1);
			try {
				int num = Integer.parseInt(str);
				newName =  tempName.substring(0, kuohuoIndex2) + "(" + (num + 1) + ")"+ extensionName;
			} catch (NumberFormatException e) {
				newName = tempName + "(2)" + extensionName;
			}
		}
		return newName;
	}
	
	/**
	 * get file size
	 * @param file
	 * @return
	 */
	public long getFileSize(File file){
		long len = 0;
		FileInputStream fis = null;
		if (file.exists()) {
			try {
			fis = new FileInputStream(file);
			len = fis.available();
			fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			Log.e(TAG + ".getFileSize", file.getAbsolutePath() + " is not exist.");
		}
		
		return len;
	}
	
	/**
	 * get folder size
	 * @param file the dir file
	 * @return
	 */
	public long getFolderSize(File file){
		long size = 0;
		if (!file.isDirectory()) {
			Log.e(TAG + ".getFolderSize", file.getAbsolutePath() + " is not dir.");
			return 0;
		}
		File[] files = file.listFiles();
		for(File file2 : files){
			if (file2.isDirectory()) {
				size += getFileSize(file2);
			}else {
				size += file2.length();
			}
		}
		
		return size;
	}
	
	/**
	 * get file num that in the dir.
	 * @param file the dir file.
	 * @return
	 */
	public int getFileCount(File file){
		int count = 0;
		if (!file.isDirectory()) {
			Log.e(TAG + ".getFileCount", file.getAbsolutePath() + " is not dir.");
			return 0;
		}
		
		File[] files = file.listFiles();
		count = files.length;
		
		for(File file2 : files){
			if (file2.isDirectory()) {
				count += getFileCount(file);
				count --;
			}
		}
		
		return count;
	}

	private List<NavigationRecord> mNavigationList = new LinkedList<FileInfoManager.NavigationRecord>();
	
	/**
     * This method gets the previous navigation directory path
     * 
     * @return the previous navigation path
     */
    public NavigationRecord getPrevNavigation() {
        while (!mNavigationList.isEmpty()) {
            NavigationRecord navRecord = mNavigationList.get(mNavigationList.size() - 1);
            removeFromNavigationList();
            String path = navRecord.getRecordPath();
            if (!TextUtils.isEmpty(path)) {
                if (new File(path).exists()) {
                    return navRecord;
                }
            }
        }
        return null;
    }

    /**
     * This method adds a navigationRecord to the navigation history
     * 
     * @param navigationRecord the Record
     */
    public void addToNavigationList(NavigationRecord navigationRecord) {
        if (mNavigationList.size() <= 20) {
            mNavigationList.add(navigationRecord);
        } else {
            mNavigationList.remove(0);
            mNavigationList.add(navigationRecord);
        }
    }

    /**
     * This method removes a directory path from the navigation history
     */
    public void removeFromNavigationList() {
        if (!mNavigationList.isEmpty()) {
            mNavigationList.remove(mNavigationList.size() - 1);
        }
    }

    /**
     * This method clears the navigation history list. Keep the root path only
     */
    protected void clearNavigationList() {
        mNavigationList.clear();
    }

	/** record current path navigation */
	public static class NavigationRecord {
		private String path;
		private int top;
		private FileInfo selectedFile;

		public NavigationRecord(String path, int top, FileInfo fileInfo) {
			this.path = path;
			this.top = top;
			this.selectedFile = fileInfo;
		}

		public String getRecordPath() {
			return path;
		}

		public void setRecordPath(String path) {
			this.path = path;
		}

		public int getTop() {
			return top;
		}

		public void setTop(int top) {
			this.top = top;
		}

		public FileInfo getSelectedFile() {
			return selectedFile;
		}

		public void setSelectFile(FileInfo selectFile) {
			this.selectedFile = selectFile;
		}
	}
}
