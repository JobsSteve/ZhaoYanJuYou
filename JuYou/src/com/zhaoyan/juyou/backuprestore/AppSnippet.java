package com.zhaoyan.juyou.backuprestore;

import com.zhaoyan.common.util.ZYUtils;

import android.R.integer;
import android.graphics.drawable.Drawable;

public class AppSnippet{
    private Drawable mIcon;
    private CharSequence mName;
    private String mPackageName;
    private long mSize;
    public String mFileName; //only for restore
    private boolean bInstalled;
    
    public AppSnippet(Drawable icon, CharSequence name, String packageName){
        mIcon = icon;
        mName = name;
        mPackageName = packageName;
    }
    
    public Drawable getIcon(){
        return mIcon;
    }
    
    public CharSequence getName(){
        return mName;
    }
    
    public String getPackageName(){
        return mPackageName;
    }
    
    public void setFileName(String filename){
        mFileName = filename;
    }
    
    public String getFileName(){
        return mFileName;
    }
    
    public void setFileSize(long size){
    	mSize = size;
    }
    
    public String getFormatFileSize(){
    	return ZYUtils.getFormatSize(mSize);
    }
    
    public void setInstalled(boolean bInstall){
    	bInstalled  = bInstall;
    }
    
    public boolean isInstalled(){
    	return bInstalled;
    }
}
