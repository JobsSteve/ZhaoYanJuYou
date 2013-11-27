package com.zhaoyan.communication;

import android.graphics.Bitmap;

import com.dreamlink.communication.aidl.User;

public class UserInfo {
	public final static int HEAD_ID_NOT_PRE_INSTALL = -1;
	private User mUser;
	private Bitmap mHeadBitmap;
	private int mHeadId = 0;
	private boolean mIsLocal = false;

	public UserInfo() {

	}

	public void setIsLocal(boolean isLocal) {
		mIsLocal = isLocal;
	}

	public boolean isLocal() {
		return mIsLocal;
	}

	public int getHeadId() {
		return mHeadId;
	}

	public void setHeadId(int id) {
		mHeadId = id;
	}

	public User getUser() {
		return mUser;
	}

	public void setUser(User user) {
		mUser = user;
	}

	public Bitmap getHeadBitmap() {
		return mHeadBitmap;
	}

	public void setHeadBitmap(Bitmap bitmap) {
		mHeadBitmap = bitmap;
	}
}
