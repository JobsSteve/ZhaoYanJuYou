package com.zhaoyan.juyou;

import java.util.Arrays;

import android.graphics.Bitmap;

import com.zhaoyan.common.util.BitmapUtilities;
import com.zhaoyan.communication.UserInfo;

public class AccountInfo {
	private int _id;
	private String userName;
	public final static int HEAD_ID_NOT_PRE_INSTALL = UserInfo.HEAD_ID_NOT_PRE_INSTALL;
	private int headId;
	private byte[] headData;
	private String accountZhaoyan;
	private String phoneNumber;
	private String email;
	private String accountQQ;
	private String accountRenren;
	private String accountSinaWeibo;
	private String accountTencentWeibo;
	private String signature;
	private int loginStatus;
	private int touristAccount;
	private long lastLoginTime;

	public int getDatabaseId() {
		return _id;
	}

	public void setDatabaseId(int id) {
		_id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getHeadId() {
		return headId;
	}

	public void setHeadId(int headId) {
		this.headId = headId;
	}

	public byte[] getHeadData() {
		return headData;
	}

	public void setHeadData(byte[] headData) {
		this.headData = headData;
	}

	public Bitmap getHeadBitmap() {
		Bitmap headBitmap = null;
		if (headData != null && headData.length > 0) {
			headBitmap = BitmapUtilities.byteArrayToBitmap(headData);
		}
		return headBitmap;
	}

	public void setHeadBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			headData = BitmapUtilities.bitmapToByteArray(bitmap);
		} else {
			headData = null;
		}
	}

	public String getAccountZhaoyan() {
		return accountZhaoyan;
	}

	public void setAccountZhaoyan(String accountZhaoyan) {
		this.accountZhaoyan = accountZhaoyan;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAccountQQ() {
		return accountQQ;
	}

	public void setAccountQQ(String accountQQ) {
		this.accountQQ = accountQQ;
	}

	public String getAccountRenren() {
		return accountRenren;
	}

	public void setAccountRenren(String accountRenren) {
		this.accountRenren = accountRenren;
	}

	public String getAccountSinaWeibo() {
		return accountSinaWeibo;
	}

	public void setAccountSinaWeibo(String accountSinaWeibo) {
		this.accountSinaWeibo = accountSinaWeibo;
	}

	public String getAccountTencentWeibo() {
		return accountTencentWeibo;
	}

	public void setAccountTencentWeibo(String accountTencentWeibo) {
		this.accountTencentWeibo = accountTencentWeibo;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public int getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(int loginStatus) {
		this.loginStatus = loginStatus;
	}

	public int getTouristAccount() {
		return touristAccount;
	}

	public void setTouristAccount(int touristAccount) {
		this.touristAccount = touristAccount;
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	@Override
	public String toString() {
		return "Account [_id=" + _id + ", userName=" + userName + ", headId="
				+ headId + ", headData=" + Arrays.toString(headData)
				+ ", accountZhaoyan=" + accountZhaoyan + ", phoneNumber="
				+ phoneNumber + ", email=" + email + ", accountQQ=" + accountQQ
				+ ", accountRenren=" + accountRenren + ", accountSinaWeibo="
				+ accountSinaWeibo + ", accountTencentWeibo="
				+ accountTencentWeibo + ", signature=" + signature
				+ ", loginStatus=" + loginStatus + ", touristAccount="
				+ touristAccount + ", lastLoginTime=" + lastLoginTime + "]";
	}

}
