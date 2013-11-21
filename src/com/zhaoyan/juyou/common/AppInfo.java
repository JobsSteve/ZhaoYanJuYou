package com.zhaoyan.juyou.common;

import java.io.File;

import com.zhaoyan.common.util.ZYUtils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class AppInfo implements Parcelable{
	private  ApplicationInfo mInfo;
	private File mApkFile;
	/**App name*/
	private String mLabel;
	/**App Icon*/
	private Drawable mIcon;
	private boolean mMounted;
	/**
	 * App Type</br>
	 * 0:normal app
	 * 1:game app
	 * 2:zhaoyan app
	 * */
	private int type;
	/**app package name*/
	private String packageName;
	/**app version*/
	private String version;
	private PackageManager pm;
	private Intent launchIntent;
	
	private AppInfo(Parcel in){
		readFromParcel(in);
	}
	
	public AppInfo(Context context){
		mApkFile = null;
		pm = context.getPackageManager();
	}
	
	public AppInfo(Context context, ApplicationInfo info) {
		mInfo = info;
		mApkFile = new File(info.sourceDir);
		pm = context.getPackageManager();
	}

	public ApplicationInfo getApplicationInfo() {
		return mInfo;
	}

	public String getLabel() {
		return mLabel;
	}

	@Override
	public String toString() {
		return mLabel;
	}

	public void loadLabel() {
		if (mLabel == null || !mMounted) {
			if (!mApkFile.exists()) {
				mMounted = false;
				mLabel = mInfo.packageName;
			} else {
				mMounted = true;
				CharSequence label = mInfo.loadLabel(pm);
				mLabel = label != null ? label.toString() : mInfo.packageName;
			}
		}
	}
	
	public void setLable(String label){
		mLabel = label;
	}
	
	public Drawable getAppIcon(){
		return mIcon;
	}
	
	public void setAppIcon(Drawable icon){
		mIcon = icon;
	}
	
	public Intent getLaunchIntent(){
		return launchIntent;
	}
	
	public void setLaunchIntent(Intent intent){
		this.launchIntent = intent;
	}
	
	public void loadVersion(){
		try {
			this.version = pm.getPackageInfo(packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public String getVersion(){
		return this.version;
	}
	
	public long getAppSize(){
		return mApkFile.length();
	}
	
	public String getFormatSize(){
		long appSize = mApkFile.length();
		return ZYUtils.getFormatSize(appSize);
	}
	
	public void setPackageName(String packageName){
		this.packageName = packageName;
	}
	
	public String getPackageName(){
		return this.packageName;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType(){
		return type;
	}
	
	
	/**get app installed path
	 * </br>
	 * like: /data/app/xxx.apk
	 * */
	public String getInstallPath(){
		return mApkFile.getAbsolutePath();
	}
	
	/**get app install date*/
	public String getFormatDate(){
		long date = mApkFile.lastModified();
		return ZYUtils.getFormatDate(date);
	}
	
	public long getDate(){
		return mApkFile.lastModified();
	}
	
	public byte[] getIconBlob(){
		return ZYUtils.drawableToByte(mIcon);
	}
	
	public static final Parcelable.Creator<AppInfo> CREATOR  = new Parcelable.Creator<AppInfo>() {

		@Override
		public AppInfo createFromParcel(Parcel source) {
			return new AppInfo(source);
		}

		@Override
		public AppInfo[] newArray(int size) {
			return new AppInfo[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}
	
	public void readFromParcel(Parcel in){
	}
	
}
