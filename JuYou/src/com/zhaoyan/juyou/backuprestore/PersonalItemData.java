package com.zhaoyan.juyou.backuprestore;

import android.R.integer;

import com.zhaoyan.juyou.R;

public class PersonalItemData {

    private int mType;
    private boolean mIsEnable;
    private int mCount;
    
    public PersonalItemData(int type, boolean isEnable) {
        mType = type;
        mIsEnable = isEnable;
    }

    public int getType() {
        return mType;
    }

    public int getIconId() {
        int ret = ModuleType.TYPE_INVALID;
        switch (mType) {
        case ModuleType.TYPE_CONTACT:
            ret = R.drawable.backup_contact;
            break;

        case ModuleType.TYPE_SMS:
            ret = R.drawable.backup_sms;
            break;

        case ModuleType.TYPE_PICTURE:
            ret = R.drawable.backup_wallpaper;
            break;

        case ModuleType.TYPE_MUSIC:
            ret = R.drawable.backup_music;
            break;
        default:
            break;
        }
        return ret;
    }

    public int getTextId() {
        int ret = ModuleType.TYPE_INVALID;
        switch (mType) {
        case ModuleType.TYPE_CONTACT:
            ret = R.string.contact_module;
            break;
        case ModuleType.TYPE_SMS:
            ret = R.string.message_sms;
            break;
        case ModuleType.TYPE_PICTURE:
            ret = R.string.picture_module;
            break;
        case ModuleType.TYPE_MUSIC:
            ret = R.string.music_module;
            break;
        default:
            break;
        }
        return ret;
    }

    public boolean isEnable() {
        return mIsEnable;
    }

    public void setEnable(boolean enable) {
        mIsEnable = enable;
    }
    
    public int getCount(){
    	return mCount;
    }
    
    public void setCount(int count){
    	mCount = count;
    }
}
