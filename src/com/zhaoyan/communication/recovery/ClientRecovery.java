package com.zhaoyan.communication.recovery;

import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;

public class ClientRecovery extends Recovery {
	private Context mContext;

	public ClientRecovery(Context context) {
		mContext = context;
	}

	@Override
	protected boolean doRecovery() {
		boolean result = false;
		UserInfo localUserInfo = UserHelper.loadLocalUser(mContext);
		int networkType = localUserInfo.getNetworkType();
		if (networkType == JuyouData.User.NETWORK_AP) {
			ClientRecoveryAp clientRecoveryAp = new ClientRecoveryAp(mContext);
			result = clientRecoveryAp.attemptRecovery();
		} else if (networkType == JuyouData.User.NETWORK_WIFI) {
			ClientRecoveryWifi clientRecoveryWifi = new ClientRecoveryWifi(
					mContext);
			result = clientRecoveryWifi.attemptRecovery();
		}
		return result;
	}

}
