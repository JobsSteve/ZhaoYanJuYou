package com.zhaoyan.communication.recovery;

import android.content.Context;
import android.database.ContentObserver;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.connect.ServerConnector;
import com.zhaoyan.communication.search.ServerSearcher;
import com.zhaoyan.juyou.provider.JuyouData;

public class ClientRecoveryAp extends Recovery {
	private static final String TAG = "ClientRecoveryAp";
	private Context mContext;
	private WifiManager mWifiManager;
	private String mApSsid;
	private Handler mHandler;
	private ServerSearchObserver mServerSearchObserver;
	private ServerSearcher mServerSearcher;

	public ClientRecoveryAp(Context context) {
		mContext = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		HandlerThread handlerThread = new HandlerThread("ClientRecoveryAp");
		handlerThread.start();
		mHandler = new ServerSearchHander(handlerThread.getLooper());
	}

	@Override
	protected boolean doRecovery() {
		Log.d(TAG, "doRecovery");
		UserManager userManager = UserManager.getInstance();
		User server = userManager.getServer();
		UserInfo serverInfo = UserHelper.getUserInfo(mContext, server);
		serverInfo.setType(JuyouData.User.TYPE_REMOTE_SEARCH_AP);
		Log.d(TAG, "doRecovery server userInfo = " + serverInfo);
		// Start search server.
		mServerSearcher = ServerSearcher.getInstance(mContext);
		mServerSearcher.stopSearch(ServerSearcher.SERVER_TYPE_ALL);
		mServerSearcher.clearServerInfo(ServerSearcher.SERVER_TYPE_ALL);
		mServerSearcher.startSearch(ServerSearcher.SERVER_TYPE_AP);

		mServerSearchObserver = new ServerSearchObserver(mContext, mHandler,
				serverInfo);
		mContext.getContentResolver().registerContentObserver(
				JuyouData.User.CONTENT_URI, true, mServerSearchObserver);

		return false;
	}

	private class ServerSearchHander extends Handler {

		public ServerSearchHander(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			mServerSearcher.stopSearch(ServerSearcher.SERVER_TYPE_ALL);
			mServerSearcher.clearServerInfo(ServerSearcher.SERVER_TYPE_ALL);

			UserInfo userInfo = (UserInfo) msg.obj;
			Log.d(TAG, "handleMessage userInfo = " + userInfo);
			ServerConnector serverConnector = new ServerConnector(mContext);
			serverConnector.connectServer(userInfo);

			recoveryFinish();
		}
	}

	private void recoveryFinish() {
		mContext.getContentResolver().unregisterContentObserver(
				mServerSearchObserver);
		mHandler.getLooper().quit();
	}

}
