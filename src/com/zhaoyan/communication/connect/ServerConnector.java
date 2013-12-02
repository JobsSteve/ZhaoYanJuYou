package com.zhaoyan.communication.connect;

import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.Context;
import android.util.Log;

public class ServerConnector {
	private static final String TAG = "ServerConnector";
	private Context mContext;
	private static ServerConnector mInstance;

	public static synchronized ServerConnector getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ServerConnector(context);
		}
		return mInstance;
	}

	public ServerConnector(Context context) {
		mContext = context.getApplicationContext();
	}

	public void connectServer(UserInfo userInfo) {
		Log.d(TAG, "connectServer userinfo = " + userInfo);
		switch (userInfo.getType()) {
		case JuyouData.User.TYPE_REMOTE_SEARCH_AP:
			connectServerAp(userInfo.getSsid());
			break;
		case JuyouData.User.TYPE_REMOTE_SEARCH_LAN:
			connectServerLan(userInfo.getIpAddress());
			break;

		default:
			break;
		}
	}

	private void connectServerLan(String serverIp) {
		ServerConnectorLan serverConnectorLan = new ServerConnectorLan(mContext);
		serverConnectorLan.connectServer(serverIp);
	}

	private void connectServerAp(String ssidAp) {
		ServerConnectorAp serverConnectorAp = new ServerConnectorAp(mContext);
		serverConnectorAp.connectServer(ssidAp);
	}

}
