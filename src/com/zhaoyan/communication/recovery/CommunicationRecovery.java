package com.zhaoyan.communication.recovery;

import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;
import android.util.Log;

public class CommunicationRecovery extends Recovery {
	private static final String TAG = "CommunicationRecovery";
	private Context mContext;

	public CommunicationRecovery(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	protected boolean doRecovery() {
		Log.d(TAG, "doRecovery()");
		boolean result = false;
		UserInfo localUserInfo = UserHelper.loadLocalUser(mContext);
		int status = localUserInfo.getStatus();
		if (status == JuyouData.User.STATUS_SERVER_CREATED) {
			ServerRecovery serverRecovery = new ServerRecovery(mContext);
			result = serverRecovery.attemptRecovery();
		} else if (status == JuyouData.User.STATUS_CONNECTED) {
			ClientRecovery clientRecovery = new ClientRecovery(mContext);
			result = clientRecovery.attemptRecovery();
		}
		return result;
	}
}
