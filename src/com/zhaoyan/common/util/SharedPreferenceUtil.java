package com.zhaoyan.common.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
	public static final String NAME = "zhaoyan";
	
	public static final String DEFAULT_SAVE_PATH = "DEFAULT_SAVE_PATH";
	public static final String SDCARD_PATH = "sdcard_path";
	public static final String INTERNAL_PATH = "internal_path";

	public static SharedPreferences getSharedPreference(Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}

}
