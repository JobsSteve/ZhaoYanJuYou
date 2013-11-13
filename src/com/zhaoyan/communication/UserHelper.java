package com.zhaoyan.communication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.dreamlink.communication.lib.SystemInfo;
import com.dreamlink.communication.lib.util.ArrayUtil;
import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.SharedPreferenceUtil;

public class UserHelper {
	private static final String TAG = "UserHelper";
	public static final String KEY_NAME = "name";
	public static final String KEY_NAME_DEFAULT = "Unkown name";

	/**
	 * Get the set name, if name is not set, return null
	 * 
	 * @param context
	 * @return
	 */
	public static String getUserName(Context context) {
		SharedPreferences sharedPreferences = SharedPreferenceUtil
				.getSharedPreference(context);
		String name = sharedPreferences.getString(KEY_NAME, KEY_NAME_DEFAULT);
		if (KEY_NAME_DEFAULT.equals(name) || TextUtils.isEmpty(name)) {
			return null;
		}
		return name;
	}

	/**
	 * Load use info from shared preference.
	 * 
	 * @param user
	 */
	public static User loadUser(Context context) {
		SharedPreferences sharedPreferences = SharedPreferenceUtil
				.getSharedPreference(context);
		String name = sharedPreferences.getString(KEY_NAME, KEY_NAME_DEFAULT);
		User user = new User();
		user.setUserName(name);
		user.setSystemInfo(new SystemInfo().getLocalSystemInfo());
		return user;
	}

	/**
	 * Save use info to shared preference.
	 * 
	 * @param user
	 */
	public static void saveUser(Context context, User user) {
		if (!TextUtils.isEmpty(user.getUserName())) {
			SharedPreferences sharedPreferences = SharedPreferenceUtil
					.getSharedPreference(context);
			Editor editor = sharedPreferences.edit();
			editor.putString(KEY_NAME, user.getUserName());
			editor.commit();
		} else {
			Log.d(TAG, "saveUser: user name is empty, abort.");
		}
	}

	public static byte[] encodeUser(User user) {
		byte[] data = ArrayUtil.objectToByteArray(user);
		return data;
	}

	public static User decodeUser(byte[] data) {
		User user = (User) ArrayUtil.byteArrayToObject(data);
		return user;
	}
}
