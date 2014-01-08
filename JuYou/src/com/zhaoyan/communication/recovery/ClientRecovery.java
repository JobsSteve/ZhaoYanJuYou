package com.zhaoyan.communication.recovery;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;
import android.util.Log;

public class ClientRecovery extends Recovery {
	private static final String TAG = "ClientRecovery";
	private Context mContext;
	private ClientRecoveryAp mClientRecoveryAp;
	private ClientRecoveryWifi mClientRecoveryWifi;

	public ClientRecovery(Context context) {
		mContext = context;
	}

	@Override
	protected boolean doRecovery() {
		boolean result = false;
		if (mClientRecoveryAp != null) {
			result = mClientRecoveryAp.attemptRecovery();
		} else if (mClientRecoveryWifi != null) {
			result = mClientRecoveryWifi.attemptRecovery();
		}
		Log.d(TAG, "doRecovery() result = " + result);
		return result;
	}

	@Override
	public void getLastSatus() {
		UserManager userManager = UserManager.getInstance();
		User server = userManager.getServer();
		UserInfo serverInfo = UserHelper.getUserInfo(mContext, server);
		int networkType = serverInfo.getNetworkType();
		if (networkType == JuyouData.User.NETWORK_AP) {
			mClientRecoveryAp = new ClientRecoveryAp(mContext);
			mClientRecoveryAp.getLastSatus();
		} else if (networkType == JuyouData.User.NETWORK_WIFI) {
			mClientRecoveryWifi = new ClientRecoveryWifi(
					mContext);
			mClientRecoveryWifi.getLastSatus();
		}
	}

}
