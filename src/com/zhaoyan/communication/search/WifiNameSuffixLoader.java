package com.zhaoyan.communication.search;

import com.zhaoyan.common.util.SharedPreferenceUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This class is used for fixing a bug: If we generate a new WiFi AP name suffix
 * every time when create a WiFi AP server, it may happen that we get the old
 * WiFi AP when do WiFi scan, but actually, the old WiFi AP is not exist. Since
 * the old WiFi AP name is different from the new one, we can not distinguish
 * whether they are established from the same user. So the old WiFi AP and the
 * new WiFi AP is show in server list both. That is not we want.</br>
 * 
 * The solution is that we make the WiFi AP name not change in the duration of
 * one application launch. When application is launched, create a new WiFi AP
 * name suffix use {@link #createNewWifiSuffixName(Context)}. When create
 * server, get the WiFi AP name use {@link #getWifiNameSuffix(Context)}.
 * 
 */
public class WifiNameSuffixLoader {
	private static final String WIFI_NAME_SUFFIX = "wifi_name_suffix";

	/**
	 * create a new WiFi AP name suffix
	 * 
	 * @param context
	 */
	public static String createNewWifiSuffixName(Context context) {
		SharedPreferences preferences = SharedPreferenceUtil
				.getSharedPreference(context);
		Editor editor = preferences.edit();
		String wifiNameSuffix = WiFiNameEncryption.generateWiFiNameSuffix();
		editor.putString(WIFI_NAME_SUFFIX, wifiNameSuffix);
		editor.commit();
		return wifiNameSuffix;
	}

	/**
	 * Get WiFi AP name suffix from shared preference.
	 * 
	 * @param context
	 * @return
	 */
	public static String getWifiNameSuffix(Context context) {
		SharedPreferences preferences = SharedPreferenceUtil
				.getSharedPreference(context);
		String wifiNameSuffix = preferences.getString(WIFI_NAME_SUFFIX, "");
		return wifiNameSuffix;
	}
}
