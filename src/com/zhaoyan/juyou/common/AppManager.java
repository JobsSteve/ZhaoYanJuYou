package com.zhaoyan.juyou.common;

import java.io.File;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.provider.AppData;

public class AppManager {
	private static final String TAG = "AppManager";
	
	public static final int NORMAL_APP = 0;
	public static final int GAME_APP = 1;
	public static final int ZHAOYAN_APP = 2;
	public static final int ERROR_APP = -1;
	
	public static final String ACTION_REFRESH_APP = "intent.aciton.refresh.app";
	public static final String ACTION_SEND_TO_APP = "intent.aciton.send.to.app";
	public static final String ACTION_ADD_MYGAME = "intent.cation.add.mygame";
	public static final String ACTION_REMOVE_MYGAME = "intent.action.remove.mygame";
	public static final String ACTION_SEND_TO_GAME = "intent.aciton.send.to.game";
	public static final String NORMAL_APP_SIZE = "normal_app_size";
	public static final String GAME_APP_SIZE = "game_app_size";
	
	public static final String EXTRA_INFO = "info";
	
	public static final int NORMAL_APP_MENU = 0x00;
	public static final int GAME_APP_MENU_XX = 0x01;
	public static final int GAME_APP_MENU_MY = 0x02;
	public static int menu_type = -1;
	public static final int MENU_INFO = 0x10;
	public static final int MENU_SHARE = 0x11;
	public static final int MENU_UNINSTALL = 0x12;
	public static final int MENU_MOVE = 0x13;

	public static List<ApplicationInfo> getAllApps(PackageManager pm) {
		List<ApplicationInfo> allApps = pm.getInstalledApplications(0);
		return allApps;
	}
	
	 /**
     * accord package name to get is game app from game db
     * @param pkgName package name
     * @return true,is game app </br>false, is normal app
     */
    public static boolean  isGameApp(Context context, String pkgName){
    	String selectionString = AppData.App.PKG_NAME + "=?" ;
    	String args[] = {pkgName};
    	boolean ret = false;
    	Cursor cursor = context.getContentResolver().query(AppData.AppGame.CONTENT_URI, 
    			null, selectionString, args, null);
    	if (cursor.getCount() <= 0) {
			ret = false;
		}else {
			ret = true;
		}
    	cursor.close();
    	return ret;
    }
    
    public static boolean isMyApp(Context context, String packageName, PackageManager pm){
    	//get we app
		Intent appIntent = new Intent(ZYConstant.APP_ACTION);
		appIntent.addCategory(Intent.CATEGORY_DEFAULT);
		// 通过查询，获得所有ResolveInfo对象.
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(appIntent, 0);
		for (ResolveInfo resolveInfo : resolveInfos) {
			String pkgName = resolveInfo.activityInfo.packageName; // 获得应用程序的包名
			if (packageName.equals(pkgName)) {
				return true;
			}
		}
		return false;
    }
    
    public static ContentValues getValuesByAppInfo(AppInfo appInfo){
    	ContentValues values = new ContentValues();
    	values.put(AppData.App.PKG_NAME, appInfo.getPackageName());
    	values.put(AppData.App.LABEL, appInfo.getLabel());
    	values.put(AppData.App.VERSION, appInfo.getVersion());
    	values.put(AppData.App.APP_SIZE, appInfo.getAppSize());
    	values.put(AppData.App.DATE, appInfo.getDate());
    	values.put(AppData.App.TYPE, appInfo.getType());
    	values.put(AppData.App.ICON, appInfo.getIconBlob());
    	values.put(AppData.App.PATH, appInfo.getInstallPath());
    	return values;
    }
    
    /**
     * accord package name to get the app entry and position
     * @param packageName 
     * @return int[0]:(0,normal app;1:game app)
     * </br>
     * 	int[1]:(the position in the list)
     */
    public static int[] getAppInfo(String packageName, List<AppInfo> normalAppList, List<AppInfo> gameAppList, List<AppInfo> myAppList){
    	int[] result = new int[2];
    	
    	for (int i = 0; i < normalAppList.size(); i++) {
			AppInfo appInfo = normalAppList.get(i);
			if (packageName.equals(appInfo.getPackageName())) {
				//is normal app
				result[0] = AppManager.NORMAL_APP;
				result[1] = i;
				return result;
			}
		}
    	
    	for (int i = 0; i < gameAppList.size(); i++) {
			AppInfo appInfo = gameAppList.get(i);
			if (packageName.equals(appInfo.getPackageName())) {
				//is game app
				result[0] = AppManager.GAME_APP;
				result[1] = i;
				return result;
			}
		}
    	
    	for (int i = 0; i < myAppList.size(); i++) {
			AppInfo appInfo = myAppList.get(i);
			if (packageName.equals(appInfo.getPackageName())) {
				//is my app
				result[0] = AppManager.ZHAOYAN_APP;
				result[1] = i;
				return result;
			}
		}
    	
    	return null;
    }
    
    /**return the position according to packagename*/
    public static int getAppPosition(String packageName, List<AppInfo> list){
    	for (int i = 0; i < list.size(); i++) {
			AppInfo appInfo = list.get(i);
			if (packageName.equals(appInfo.getPackageName())) {
				return i;
			}
		}
    	return -1;
    }
    
	public static void installApk(Context context, String apkFilePath) {
		if (apkFilePath.endsWith(".apk")) {
			installApk(context, new File(apkFilePath));
		}
	}

	public static void installApk(Context context, File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Uri uri = Uri.fromFile(file);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		context.startActivity(intent);
	}
	
	public static void uninstallApp(Context context, String packageName){
		Uri packageUri = Uri.parse("package:" + packageName);
		Intent deleteIntent = new Intent();
		deleteIntent.setAction(Intent.ACTION_DELETE);
		deleteIntent.setData(packageUri);
		context.startActivity(deleteIntent);
	}
	
	public static String getAppLabel(String packageName, PackageManager pm){
		ApplicationInfo applicationInfo = null;
		try {
			applicationInfo = pm.getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "getAppLabel.name not found:" + packageName);
			Log.e(TAG, e.toString());
			return null;
		}
		return applicationInfo.loadLabel(pm).toString();
	}
	
	public static String getAppVersion(String packageName, PackageManager pm){
		String version = "";
		try {
			version = pm.getPackageInfo(packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "getAppVersion.name not found:" + packageName);
			e.printStackTrace();
		}
		return version;
	}
	
	public static String getAppSourceDir(String packageName, PackageManager pm){
		ApplicationInfo applicationInfo = null;
		try {
			applicationInfo = pm.getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "getAppSourceDir:" + packageName + " name not found.");
			e.printStackTrace();
		}
		return applicationInfo.sourceDir;
	}
}
