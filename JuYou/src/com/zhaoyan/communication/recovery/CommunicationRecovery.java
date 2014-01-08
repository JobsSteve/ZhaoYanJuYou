package com.zhaoyan.communication.recovery;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;
import android.util.Log;

public class CommunicationRecovery extends Recovery {
	private static final String TAG = "CommunicationRecovery";
	private Context mContext;
	private ServerRecovery mServerRecovery;
	private ClientRecovery mClientRecovery;

	public CommunicationRecovery(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	protected boolean doRecovery() {
		Log.d(TAG, "doRecovery()");
		boolean result = false;
		if (mServerRecovery != null) {
			result = mServerRecovery.attemptRecovery();
		} else if (mClientRecovery != null) {
			result = mClientRecovery.attemptRecovery();
		}
		Log.d(TAG, "doRecovery() result = " + result);
		return result;
	}

	@Override
	public void getLastSatus() {
		UserManager userManager = UserManager.getInstance();
		User server = userManager.getServer();
		User local = userManager.getLocalUser();
		if (server.getUserID() == local.getUserID()) {
			mServerRecovery = new ServerRecovery(mContext);
			mServerRecovery.getLastSatus();
		} else {
			mClientRecovery = new ClientRecovery(mContext);
			mClientRecovery.getLastSatus();
		}
	}
}
