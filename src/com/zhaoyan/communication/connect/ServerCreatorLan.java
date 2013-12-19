package com.zhaoyan.communication.connect;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;

public class ServerCreatorLan {
	private static final String TAG = "ServerCreatorLan";
	private Context mContext;
	private ServerCreateAndDiscovery mServerCreateAndDiscovery;

	public ServerCreatorLan(Context context) {
		mContext = context;
	}

	public void createServer() {
		if (!NetWorkUtil.isWifiConnected(mContext)) {
			Log.e(TAG, "createServer fail, wifi is not connected");
			return;
		}
		mServerCreateAndDiscovery = new ServerCreateAndDiscovery(mContext,
				ServerCreator.TYPE_LAN);
		mServerCreateAndDiscovery.start();
	}

	public void stopServer() {
		if (mServerCreateAndDiscovery != null) {
			mServerCreateAndDiscovery.stopServerAndDiscovery();
			mServerCreateAndDiscovery = null;
		}
	}

}
