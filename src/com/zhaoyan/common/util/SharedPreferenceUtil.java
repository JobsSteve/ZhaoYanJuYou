package com.zhaoyan.common.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
	public static final String NAME = "zhaoyan";

	public static SharedPreferences getSharedPreference(Context context) {
		return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
	}

}
