package com.zhaoyan.communication.recovery;

import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;

public class ServerRecovery extends Recovery {
	private Context mContext;

	public ServerRecovery(Context context) {
		mContext = context;
	}

	@Override
	protected boolean doRecovery() {
		boolean result = false;
		UserInfo localUserInfo = UserHelper.loadLocalUser(mContext);
		int networkType = localUserInfo.getNetworkType();
		if (networkType == JuyouData.User.NETWORK_AP) {
			ServerRecoveryAp serverRecoveryAP = new ServerRecoveryAp(mContext);
			result = serverRecoveryAP.attemptRecovery();
		} else if (networkType == JuyouData.User.NETWORK_WIFI) {
			ServerRecoveryWifi serverRecoveryWifi = new ServerRecoveryWifi(
					mContext);
			result = serverRecoveryWifi.attemptRecovery();
		}
		return result;
	}

}
