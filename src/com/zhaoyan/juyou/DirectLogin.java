package com.zhaoyan.juyou;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;

public class DirectLogin implements ILogin {
	private static final String TAG = "DirectLogin";
	private Context mContext;

	public DirectLogin(Context context) {
		mContext = context;
	}

	@Override
	public boolean login() {
		Log.d(TAG, "login");
		UserInfo userInfo = UserHelper.loadLocalUser(mContext);
		if (userInfo == null) {
			Log.d(TAG, "login(), there is no saved user info.");
			// This is the first time launch. Set user info.
			userInfo = new UserInfo();
			// Name and id.
			User user = new User();
			user.setUserName(android.os.Build.MANUFACTURER);
			user.setUserID(0);
			userInfo.setUser(user);
			// Head
			userInfo.setHeadId(0);
			// Type
			userInfo.setType(JuyouData.User.TYPE_LOCAL);
			UserHelper.saveLocalUser(mContext, userInfo);
		}
		return true;
	}
}
