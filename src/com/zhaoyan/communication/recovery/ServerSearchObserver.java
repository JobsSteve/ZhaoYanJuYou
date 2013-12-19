package com.zhaoyan.communication.recovery;

import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;

public class ServerSearchObserver extends ContentObserver {
	private Handler mHandler;
	private Context mContext;
	private UserInfo mUserInfo;

	public ServerSearchObserver(Context context, Handler handler,
			UserInfo userInfo) {
		super(handler);
		mContext = context;
		mHandler = handler;
		mUserInfo = userInfo;
	}

	@Override
	public void onChange(boolean selfChange) {
		String selection = JuyouData.User.USER_NAME + "='"
				+ mUserInfo.getUser().getUserName() + "'";
		UserInfo userInfo = UserHelper.getUserInfo(mContext, selection);
		if (userInfo != null) {
			Message message = mHandler.obtainMessage();
			message.obj = userInfo;
			message.sendToTarget();
		}
	}
}
