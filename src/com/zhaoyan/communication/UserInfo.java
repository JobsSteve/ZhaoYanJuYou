package com.zhaoyan.communication;

import android.graphics.Bitmap;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.juyou.provider.JuyouData;

public class UserInfo {
	public final static int HEAD_ID_NOT_PRE_INSTALL = -1;
	private User mUser;
	private Bitmap mHeadBitmap;
	private int mHeadId = 0;
	private String mIpAddress;
	private int mType;
	private String mSsid;

	public UserInfo() {

	}

	public String getSsid() {
		return mSsid;
	}

	public void setSsid(String ssid) {
		mSsid = ssid;
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}

	public String getIpAddress() {
		return mIpAddress;
	}

	public void setIpAddress(String ipAddress) {
		mIpAddress = ipAddress;
	}

	@Deprecated
	public void setIsLocal(boolean isLocal) {
		if (isLocal) {
			mType = JuyouData.User.TYPE_LOCAL;
		} else {
			mType = JuyouData.User.TYPE_REMOTE;
		}
	}

	public boolean isLocal() {
		return mType == JuyouData.User.TYPE_LOCAL;
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

	@Override
	public String toString() {
		return "UserInfo [mUser=" + mUser + ", mHeadBitmap size ="
				+ mHeadBitmap + ", mHeadId=" + mHeadId
				+ ", mIpAddress=" + mIpAddress + ", mType=" + mType
				+ ", mSsid=" + mSsid + "]";
	}
}
