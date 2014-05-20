package com.zhaoyan.juyou.backuprestore;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;

import android.content.Context;


public class ModuleType {
    private static final String TAG = "ModuleType";
    public static final int TYPE_INVALID = 0x0;
    public static final int TYPE_CONTACT = 0x1;
    public static final int TYPE_SMS = 0x2;
    public static final int TYPE_APP = 0x10;
    public static final int TYPE_PICTURE = 0x20;
    public static final int TYPE_MUSIC = 0x80;

    public static String getModuleStringFromType(Context context, int type) {
        int resId = 0;
        switch (type) {
        case ModuleType.TYPE_CONTACT:
            resId = R.string.contact_module;
            break;

        case ModuleType.TYPE_SMS:
        	resId = R.string.message_sms;
        	break;

        case ModuleType.TYPE_PICTURE:
            resId = R.string.picture_module;
            break;

        case ModuleType.TYPE_MUSIC:
            resId = R.string.music_module;
            break;
        case ModuleType.TYPE_APP:
        	resId = R.string.app_module;
        	break;

        default:
            break;
        }
        Log.d(TAG, "getModuleStringFromType: resId = " + resId);
        return context.getResources().getString(resId);
    }

    private ModuleType() {
    }
}
