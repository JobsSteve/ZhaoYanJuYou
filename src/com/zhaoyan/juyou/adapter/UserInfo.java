package com.zhaoyan.juyou.adapter;

import android.graphics.drawable.Drawable;

/**
 * Data for network fragment user list view.
 * 
 */
public class UserInfo {
	private Drawable mUserIcon;
	private String mUserName;
	private String mUserStatus;

	public UserInfo(Drawable icon, String name, String status) {
		mUserIcon = icon;
		mUserName = name;
		mUserStatus = status;
	}

	/**
	 * @return the mUserIcon
	 */
	public Drawable getUserIcon() {
		return mUserIcon;
	}

	/**
	 * @return the mUserName
	 */
	public String getUserName() {
		return mUserName;
	}

	/**
	 * @return the mUserStatus
	 */
	public String getUserStatus() {
		return mUserStatus;
	}

}
