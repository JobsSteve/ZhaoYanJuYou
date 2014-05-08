package com.zhaoyan.juyou.common;

import com.zhaoyan.juyou.R;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;

public class GuanJiaLauncherUtil {

	/**
	 * Get guanjia main page launcher icon resource id for display icons.
	 * 
	 * @param context
	 * @return
	 */
	public static int[] getMainLauncherIcons(Context context) {
		TypedArray icons = context.getResources().obtainTypedArray(
				R.array.gj_launcher_item_icon);
		int iconsIds[] = new int[icons.length()];
		for (int i = 0; i < iconsIds.length; i++) {
			iconsIds[i] = icons.getResourceId(i, -1);
		}
		icons.recycle();
		return iconsIds;
	}

	/**
	 * Get guanjia main page launcher name for display.
	 * 
	 * @param context
	 * @return
	 */
	public static String[] getMainLauncherName(Context context) {
		String[] names = context.getResources().getStringArray(
				R.array.gj_launcher_item_text);
		return names;
	}

	/**
	 * Get guanjia main page launcher class name for launch activity.
	 * 
	 * @param context
	 * @return
	 */
	public static String[] getMainLauncherClassName(Context context) {
		String[] classname = context.getResources().getStringArray(
				R.array.gj_launcher_item_classname);
		return classname;
	}
	
	/**
	 * Get guanjia more page launcher icon resource id for display icons.
	 * 
	 * @param context
	 * @return
	 */
	public static int[] getMoreLauncherIcons(Context context) {
		TypedArray icons = context.getResources().obtainTypedArray(
				R.array.gj_launcher_more_item_icon);
		int iconsIds[] = new int[icons.length()];
		for (int i = 0; i < iconsIds.length; i++) {
			iconsIds[i] = icons.getResourceId(i, -1);
		}
		icons.recycle();
		return iconsIds;
	}

	/**
	 * Get guanjia more page launcher name for display.
	 * 
	 * @param context
	 * @return
	 */
	public static String[] getMoreLauncherName(Context context) {
		String[] names = context.getResources().getStringArray(
				R.array.gj_launcher_more_item_text);
		return names;
	}

	/**
	 * Get guanjia more page launcher class name for launch activity.
	 * 
	 * @param context
	 * @return
	 */
	public static String[] getMoreLauncherClassName(Context context) {
		String[] classname = context.getResources().getStringArray(
				R.array.gj_launcher_more_item_classname);
		return classname;
	}

	/**
	 * Get Intent for launch activity.
	 * 
	 * @param context
	 * @param className
	 * @return
	 */
	public static Intent getLaunchIntent(Context context, String className) {
		Intent intent = new Intent();
		intent.setClassName(context, className);
		return intent;
	}
	
}
