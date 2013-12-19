package com.zhaoyan.communication;

import java.io.Serializable;

import android.graphics.Bitmap;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.BitmapUtilities;
import com.zhaoyan.juyou.provider.JuyouData;

public class UserInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2266214532436896251L;
	public final static int HEAD_ID_NOT_PRE_INSTALL = -1;
	private User mUser;
	private byte[] mHeadBitmapData;
	private int mHeadId = 0;
	private String mIpAddress;
	private int mType;
	private String mSsid;
	private int mStatus;
	private int mNetworkType;

	public UserInfo() {

	}

	public void setNetworkType(int type) {
		mNetworkType = type;
	}

	public int getNetworkType() {
		return mNetworkType;
	}

	public int getStatus() {
		return mStatus;
	}

	public void setStatus(int Status) {
		mStatus = Status;
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
		Bitmap headBitmap = null;
		if (mHeadBitmapData != null && mHeadBitmapData.length > 0) {
			headBitmap = BitmapUtilities.byteArrayToBitmap(mHeadBitmapData);
		}
		return headBitmap;
	}

	public void setHeadBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			mHeadBitmapData = BitmapUtilities.bitmapToByteArray(bitmap);
		} else {
			mHeadBitmapData = null;
		}
	}

	public void setHeadBitmapData(byte[] data) {
		mHeadBitmapData = data;
	}

	public byte[] getHeadBitmapData() {
		return mHeadBitmapData;
	}

	@Override
	public String toString() {
		return "UserInfo [mUser=" + mUser + ", mHeadBitmapData="
				+ mHeadBitmapData + ", mHeadId=" + mHeadId + ", mIpAddress="
				+ mIpAddress + ", mType=" + mType + ", mSsid=" + mSsid
				+ ", mStatus=" + mStatus + ", mNetworkType=" + mNetworkType
				+ "]";
	}
}
