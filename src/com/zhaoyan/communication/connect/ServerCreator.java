package com.zhaoyan.communication.connect;

import android.content.Context;

public class ServerCreator {
	private static final String TAG = "ServerCreator";
	public static final String ACTION_SERVER_CREATED = "com.zhaoyan.communication.search2.ServerCreator.ACTION_SERVER_CREATED";
	public static final int TYPE_LAN = 1;
	public static final int TYPE_AP = 2;
	private ServerCreatorAp mServerCreatorAp;
	private ServerCreatorLan mServerCreatorLan;
	private Context mContext;

	private static ServerCreator mInstance;

	public static ServerCreator getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ServerCreator(context);
		}
		return mInstance;
	}

	private ServerCreator(Context context) {
		mContext = context.getApplicationContext();
	}

	public void createServer(int type) {
		switch (type) {
		case TYPE_AP:
			mServerCreatorAp = new ServerCreatorAp(mContext);
			mServerCreatorAp.createServer();
			break;
		case TYPE_LAN:
			mServerCreatorLan = new ServerCreatorLan(mContext);
			mServerCreatorLan.createServer();
			break;

		default:
			break;
		}
	}

	public void stopServer() {
		if (mServerCreatorAp != null) {
			mServerCreatorAp.stopServer();
			mServerCreatorAp = null;
		}
		if (mServerCreatorLan != null) {
			mServerCreatorLan.stopServer();
			mServerCreatorLan = null;
		}
	}
}
