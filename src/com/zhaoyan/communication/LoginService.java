package com.zhaoyan.communication;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.CallBacks.ILoginRequestCallBack;
import com.zhaoyan.communication.CallBacks.ILoginRespondCallback;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LoginService extends Service implements ILoginRequestCallBack,
		ILoginRespondCallback {
	private static final String TAG = "LoginService";
	private SocketCommunicationManager mCommunicationManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		mCommunicationManager = SocketCommunicationManager.getInstance();
		mCommunicationManager.setLoginRequestCallBack(this);
		mCommunicationManager.setLoginRespondCallback(this);
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
		mCommunicationManager
				.respondLoginRequest(userInfo, communication, true);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		mCommunicationManager.setLoginRequestCallBack(null);
		mCommunicationManager.setLoginRespondCallback(null);
		super.onDestroy();
	}
}
