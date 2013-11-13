package com.zhaoyan.juyou.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.dialog.InfoDialog;

public class FileInfoManager {
	private static final String TAG = FileInfoManager.class.getName();
	public static final int TEXT = 0x01;
	public static final int HTML = 0x02;
	public static final int WORD = 0x03;
	public static final int EXCEL = 0x04;
	public static final int PPT = 0x05;
	public static final int PDF = 0x06;
	public static final int AUDIO = 0x07;
	public static final int VIDEO = 0x08;
	public static final int CHM = 0x09;
	public static final int APK = 0x10;
	public static final int ARCHIVE = 0x11;
	public static final int IMAGE = 0x12;
	public static final int UNKNOW = 0x20;

	public static final int TYPE_DEFAULT = 0x20;//32
	public static final int TYPE_EBOOK = 0x21;
	public static final int TYPE_VIDEO = 0x22;
	public static final int TYPE_DOC = 0x23;
	public static final int TYPE_APK = 0x24;//36
	public static final int TYPE_ARCHIVE = 0x25;
	public static final int TYPE_BIG_FILE = 0x26;
	public static final int TYPE_IMAGE = 0x27;
	public static final int TYPE_AUDIO = 0x28;//40
	
	/**
	 * save num in sharedPrefernce
	 */
	//work document
	public static final String DOC_NUM = "doc_num";
	//ebook file
	public static final String EBOOK_NUM = "ebook_num";
	//app install package
	public static final String APK_NUM = "app_num";
	//archive file
	public static final String ARCHIVE_NUM = "archive_num";

	private Context context;

	public FileInfoManager(Context context) {
		this.context = context;
	}

	/**
	 * android获取一个用于打开Audio文件的Intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getAudioFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "audio/*");
		return intent;
	}

	/**
	 * 获取一个打开文本文件的Intent
	 * 
	 * @param param
	 *            file path
	 * */
	public static Intent getTextFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "text/plain");
		return intent;
	}

	/**
	 * android获取一个用于打开PPT文件的intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getPptFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
		return intent;
	}

	/**
	 * android获取一个用于打开Excel文件的intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getExcelFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.ms-excel");
		return intent;
	}

	/**
	 * android获取一个用于打开Word文件的intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getWordFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/msword");
		return intent;
	}

	/**
	 * android获取一个用于打开PDF文件的intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getPdfFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/pdf");
		return intent;
	}

	/**
	 * android获取一个用于打开Image文件的Intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getImageFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "image/*");
		return intent;
	}

	/**
	 * android获取一个用于打开VIDEO文件的Intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getVIDEOFileIntent(String param) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("oneshot", 0);
		intent.putExtra("configchange", 0);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "video/*");
		return intent;
	}

	/**
	 * android获取一个用于打开apk文件的intent
	 * 
	 * @param param
	 *            file path
	 * @return
	 */
	public static Intent getApkFileIntent(String param) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(new File(param));
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		return intent;
	}

	// 判断文件类型，根据不同类型设置图标
	public FileInfo getFileInfo(File currentFile) {
		Drawable currentIcon = null;
		int fileType = FileInfoManager.TYPE_DEFAULT;
		// 取得文件路径
		String filePath = currentFile.getPath();

		// 根据文件名来判断文件类型，设置不同的图标
		int result = fileFilter(filePath);
		switch (result) {
		case TEXT:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_txt);
			fileType = FileInfoManager.TYPE_EBOOK;
			break;
		// case HTML:
		// currentIcon = context.getResources().getDrawable(R.drawable.webtext);
		// break;
		case IMAGE:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_image);
			fileType = FileInfoManager.TYPE_IMAGE;
			break;
		case AUDIO:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_audio);
			fileType = FileInfoManager.TYPE_AUDIO;
			break;
		case VIDEO:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_video);
			fileType = FileInfoManager.TYPE_VIDEO;
			break;
		case WORD:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_doc);
			fileType = FileInfoManager.TYPE_DOC;
			break;
		case PPT:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_ppt);
			fileType = FileInfoManager.TYPE_DOC;
			break;
		case EXCEL:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_xls);
			fileType = FileInfoManager.TYPE_DOC;
			break;
		case PDF:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_pdf);
			fileType = FileInfoManager.TYPE_DOC;
			break;
		case ARCHIVE:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_rar);
			fileType = FileInfoManager.TYPE_ARCHIVE;
			break;
		case APK:
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_apk);
			fileType = FileInfoManager.TYPE_APK;
			break;
		default:
			// 默认
			currentIcon = context.getResources().getDrawable(
					R.drawable.icon_file);
			break;
		}
		FileInfo fileInfo = new FileInfo(currentFile.getName());
		fileInfo.fileSize = currentFile.length();
		fileInfo.fileDate = currentFile.lastModified();
		fileInfo.filePath = currentFile.getAbsolutePath();
		fileInfo.isDir = false;
		fileInfo.type = fileType;
		fileInfo.icon = currentIcon;
		return fileInfo;
	}
	
	// 判断文件类型，根据不同类型设置图标
		public HistoryInfo getHistoryInfo(HistoryInfo historyInfo) {
			HistoryInfo info = historyInfo;
			Drawable currentIcon = null;
			int fileType = FileInfoManager.TYPE_DEFAULT;
			// 取得文件路径
			String filePath = historyInfo.getFile().getAbsolutePath();

			// 根据文件名来判断文件类型，设置不同的图标
			int result = fileFilter(filePath);
			switch (result) {
			case TEXT:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_txt);
				fileType = FileInfoManager.TYPE_EBOOK;
				break;
			// case HTML:
			// currentIcon = context.getResources().getDrawable(R.drawable.webtext);
			// break;
			case IMAGE:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_image);
				fileType = FileInfoManager.TYPE_IMAGE;
				break;
			case AUDIO:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_audio);
				fileType = FileInfoManager.TYPE_AUDIO;
				break;
			case VIDEO:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_video);
				fileType = FileInfoManager.TYPE_VIDEO;
				break;
			case WORD:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_doc);
				fileType = FileInfoManager.TYPE_DOC;
				break;
			case PPT:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_ppt);
				fileType = FileInfoManager.TYPE_DOC;
				break;
			case EXCEL:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_xls);
				fileType = FileInfoManager.TYPE_DOC;
				break;
			case PDF:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_pdf);
				fileType = FileInfoManager.TYPE_DOC;
				break;
			case ARCHIVE:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_rar);
				fileType = FileInfoManager.TYPE_ARCHIVE;
				break;
			case APK:
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_apk);
				fileType = FileInfoManager.TYPE_APK;
				break;
			default:
				// 默认
				currentIcon = context.getResources().getDrawable(
						R.drawable.icon_file);
				break;
			}
			info.setFileType(fileType);
			info.setIcon(currentIcon);
			return info;
		}

	/**
	 * 未安装的程序通过apk文件获取icon
	 * 
	 * @param path
	 *            apk文件路径
	 * @return apk的icon
	 */
	public Drawable getApkIcon(String path) {
		String apkPath = path; // apk 文件所在的路径
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();

			typeArgs = new Class<?>[] { File.class, String.class,
					DisplayMetrics.class, int.class };

			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);

			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");

			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);

			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			typeArgs = new Class[1];
			typeArgs[0] = String.class;

			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;

			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			Resources res = context.getResources();

			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();

			Constructor<Resources> resCt = Resources.class
					.getConstructor(typeArgs);

			valueArgs = new Object[3];

			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);

			if (info != null) {
				if (info.icon != 0) {
					Drawable icon = res.getDrawable(info.icon);
					return icon;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return context.getResources().getDrawable(
				R.drawable.icon_apk);
	}

	public int fileFilter(String filepath) {
		// 首先取得文件名
		String fileName = new File(filepath).getName();
		int ret;

		if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingEbook))) {
			// text
			ret = TEXT;
		}
		// else if (checkEndsWithInStringArray(fileName,
		// context.getResources().getStringArray(R.array.fileEndingWebText))) {
		// //html ...
		// ret = HTML;
		// }
		else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingImage))) {
			// Images
			ret = IMAGE;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingAudio))) {
			// audios
			ret = AUDIO;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingVideo))) {
			// videos
			ret = VIDEO;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingApk))) {
			// apk
			ret = APK;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingWord))) {
			// word
			ret = WORD;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingPpt))) {
			// ppt
			ret = PPT;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingExcel))) {
			// excel
			ret = EXCEL;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingArchive))) {
			// packages
			ret = ARCHIVE;
		} else if (checkEndsWithInStringArray(fileName, context.getResources()
				.getStringArray(R.array.fileEndingPdf))) {
			// pdf
			ret = PDF;
		} else {
			ret = UNKNOW;
		}

		return ret;
	}

	// 通过文件名判断是什么类型的文件
	private boolean checkEndsWithInStringArray(String checkItsEnd,
			String[] fileEndings) {
		String str = checkItsEnd.toLowerCase();
		for (String aEnd : fileEndings) {
			if (str.endsWith(aEnd))
				return true;
		}
		return false;
	}

	/**
	 * open file
	 * 
	 * @param filePath
	 *            file path
	 */
	public void openFile(String filePath) {
		Intent intent = null;

		// 根据不同的文件，执行不同的打开方式
		// 根据文件名来判断文件类型，设置不同的图标
		int result = fileFilter(filePath);
		switch (result) {
		case TEXT:
			intent = getTextFileIntent(filePath);
			break;
		case IMAGE:
			intent = getImageFileIntent(filePath);
			break;
		case AUDIO:
			intent = getAudioFileIntent(filePath);
			break;
		case VIDEO:
			intent = getVIDEOFileIntent(filePath);
			break;
		case WORD:
			intent = getWordFileIntent(filePath);
			break;
		case PPT:
			intent = getPptFileIntent(filePath);
			break;
		case EXCEL:
			intent = getExcelFileIntent(filePath);
			break;
		case PDF:
			intent = getPdfFileIntent(filePath);
			break;
		case ARCHIVE:
			// 待实现
			break;
		case APK:
			intent = getApkFileIntent(filePath);
			break;
		default:
			// 默认
			break;
		}
		// open
		if (intent != null) {
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, e.toString());
				Toast.makeText(context, R.string.open_file_fail,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(context, R.string.open_file_fail,
					Toast.LENGTH_SHORT).show();
		}
	}

	public boolean deleteFile(String filePath) {
		File file = new File(filePath);
		return deleteFile(file);
	}

	/**
	 * API11 以后多了一个MediaStore.Files这个类， 应该是Google为了方便管理，将系统中的所有文件都保存到了一个表files中
	 * 这样的话，删除也方便 而2.3的话，如果要删除你得去不同的表中删除，比如Audio，Images，Video 3.0以下系统可以使用该方法
	 * 
	 * @param path
	 */
	public boolean deleteFileInMediaStore(Uri uri, String path) {
		Log.d(TAG, "deleteFileInMediaStore:" + path);
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		
		if (!deleteFile(path)) {
			return false;
		}

		String where = MediaStore.Audio.Media.DATA + "=?";
		String[] whereArgs = new String[] { path };
		ContentResolver cr = context.getContentResolver();
		try {
			cr.delete(uri, where, whereArgs);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "Error in delete file in media store:" + e.toString());
		}
		
		return true;
	}

	/**
	 * 3.0以上系统，才可以使用该方法，删除多媒体文件
	 */
	public boolean deleteFileInMediaStore(String path) {

		if (TextUtils.isEmpty(path)) {
			return false;
		}
		
		if (!deleteFile(path)) {
			return false;
		}
		
		Uri uri = null;
		// 不能用Files这个类，这个类API11 以后才支持
		// Uri uri = MediaStore.Files.getContentUri("external");
		String where = MediaStore.Files.FileColumns.DATA + "=?";
		String[] whereArgs = new String[] { path };
		ContentResolver cr = context.getContentResolver();
		try {
			cr.delete(uri, where, whereArgs);
		} catch (Exception e) {
			// TODO: handle exception
			Log.e(TAG, "Error in delete file in media store:" + e.toString());
		}
		
		return true;
	}

	public boolean deleteFile(File file) {
		if (!file.exists()) {
			return false;
		}

		return file.delete();
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

	public void showInfoDialog(FileInfo fileInfo) {
		String info = getFileInfo(fileInfo);
		ZYUtils.showInfoDialog(context, fileInfo.fileName, info);
	}
	
	public void showInfoDialog(List<FileInfo> list){
		GetFileSizeTask task = new GetFileSizeTask(list);
		task.execute();
	}
	
	private class GetFileSizeTask extends AsyncTask<Void, Void, Void>{
		long size = 0;
		int fileNum = 0;
		int folderNum = 0;
		InfoDialog infoDialog = null;
		int type;
		List<FileInfo> fileList;
		
		
		GetFileSizeTask(List<FileInfo> list){
			fileList = list;
			if (list.size() == 1) {
				if (fileList.get(0).isDir) {
					type = InfoDialog.SINGLE_FOLDER;
				}else {
					type = InfoDialog.SINGLE_FILE;
				}
			}else {
				type = InfoDialog.MULTI;
			}
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "doInBackground");
			File file = null;
			switch (type) {
			case InfoDialog.SINGLE_FILE:
			case InfoDialog.SINGLE_FOLDER:
				FileInfo fileInfo = fileList.get(0);
				infoDialog.updateUI(fileInfo.fileName, fileInfo.filePath, fileInfo.fileDate);
				file = new File(fileInfo.filePath);
				getFileSize(file);
				break;
			case InfoDialog.MULTI:
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
		}
		
		private void getFileSize(File file){
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

	private String getFileInfo(FileInfo fileInfo) {
		String path = fileInfo.filePath.substring(0, fileInfo.filePath.lastIndexOf("/"));
		String type = "";
		if (fileInfo.isDir) {
			type = "文件夹";
		}else {
			type = "文件";
		}
		String result = "类型:" + type + ZYConstant.ENTER
				+ "位置:" + path + ZYConstant.ENTER
				+ "大小:" + fileInfo.getFormatFileSize() + ZYConstant.ENTER
				+ "修改日期:" + fileInfo.getFormateDate();
		return result;
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
    protected NavigationRecord getPrevNavigation() {
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
    protected void addToNavigationList(NavigationRecord navigationRecord) {
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
    protected void removeFromNavigationList() {
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
