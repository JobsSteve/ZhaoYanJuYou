package com.zhaoyan.communication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.ProtocolCommunication.ILoginRequestCallBack;
import com.zhaoyan.communication.ProtocolCommunication.ILoginRespondCallback;

public class LoginService extends Service implements ILoginRequestCallBack,
		ILoginRespondCallback {
	private static final String TAG = "LoginService";
	private ProtocolCommunication mProtocolCommunication;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		mProtocolCommunication = ProtocolCommunication.getInstance();
		mProtocolCommunication.setLoginRequestCallBack(this);
		mProtocolCommunication.setLoginRespondCallback(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()");
		// Restart when be killed.
		return START_STICKY;
	}

	@Override
	public void onLoginSuccess(User localUser, SocketCommunication communication) {
		Log.d(TAG, "onLoginSuccess");
	}

	@Override
	public void onLoginFail(int failReason, SocketCommunication communication) {
		Log.d(TAG, "onLoginFail");
	}

	@Override
	public void onLoginRequest(UserInfo userInfo,
			SocketCommunication communication) {
		// TODO auto respond.
		Log.d(TAG, "onLoginRequest user = " + userInfo + ", communication = "
				+ communication);
		mProtocolCommunication.respondLoginRequest(userInfo, communication,
				true);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		mProtocolCommunication.setLoginRequestCallBack(null);
		mProtocolCommunication.setLoginRespondCallback(null);
		super.onDestroy();
	}
}
