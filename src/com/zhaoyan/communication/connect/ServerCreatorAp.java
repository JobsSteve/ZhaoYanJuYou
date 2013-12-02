package com.zhaoyan.communication.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.net.WiFiAP;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.search.WiFiNameEncryption;
import com.zhaoyan.communication.search.WifiNameSuffixLoader;

public class ServerCreatorAp {
	private static final String TAG = "ServerCreatorAp";
	private Context mContext;
	private BroadcastReceiver mWifiApBroadcastReceiver;
	private boolean mIsCreated = false;
	private ServerCreateAndDiscovery mServerCreateAndDiscovery;

	public ServerCreatorAp(Context context) {
		mContext = context;
	}

	public void createServer() {
		if (mIsCreated) {
			return;
		}
		Log.d(TAG, "createServer");
		mIsCreated = true;
		// Disalbe AP if needed.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}
		// Enable AP.
		enableAP();

		mWifiApBroadcastReceiver = new WifiApBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WiFiAP.ACTION_WIFI_AP_STATE_CHANGED);
		mContext.registerReceiver(mWifiApBroadcastReceiver, filter);
	}

	public void stopServer() {
		if (!mIsCreated) {
			return;
		}
		Log.d(TAG, "stopServer");
		mIsCreated = false;
		try {
			mContext.unregisterReceiver(mWifiApBroadcastReceiver);
		} catch (Exception e) {
		}
		if (mServerCreateAndDiscovery != null) {
			mServerCreateAndDiscovery.stopServerAndDiscovery();
		}
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}
	}

	private void enableAP() {
		// Get wifi ap name.
		String wifiAPName = null;
		String wifiNameSuffix = WifiNameSuffixLoader
				.getWifiNameSuffix(mContext);
		UserInfo userInfo = UserHelper.loadLocalUser(mContext);

		if (TextUtils.isEmpty(wifiNameSuffix)) {
			wifiNameSuffix = WifiNameSuffixLoader
					.createNewWifiSuffixName(mContext);
			wifiAPName = WiFiNameEncryption.generateWiFiName(userInfo.getUser()
					.getUserName(), userInfo.getHeadId(), wifiNameSuffix);
		} else {
			wifiAPName = WiFiNameEncryption.generateWiFiName(userInfo.getUser()
					.getUserName(), userInfo.getHeadId(), wifiNameSuffix);
		}

		String wifiAPPassword = WiFiNameEncryption.getWiFiPassword(wifiAPName);
		NetWorkUtil
				.setWifiAPEnabled(mContext, wifiAPName, wifiAPPassword, true);
	}

	private class WifiApBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive, action: " + action);
			if (WiFiAP.ACTION_WIFI_AP_STATE_CHANGED.equals(action)) {
				int state = intent.getIntExtra(WiFiAP.EXTRA_WIFI_AP_STATE,
						WiFiAP.WIFI_AP_STATE_FAILED);
				handleWifiApChanged(state);
			}
		}

		private void handleWifiApChanged(int wifiApState) {
			switch (wifiApState) {
			case WiFiAP.WIFI_AP_STATE_ENABLED:
				Log.d(TAG, "WIFI_AP_STATE_ENABLED");
				createServerAndStartDiscoveryService();
				break;
			case WiFiAP.WIFI_AP_STATE_DISABLED:
				Log.d(TAG, "WIFI_AP_STATE_DISABLED");
				stopServerAndStopDiscoveryService();
				break;
			case WiFiAP.WIFI_AP_STATE_FAILED:
				Log.d(TAG, "WIFI_AP_STATE_FAILED");
				break;
			default:
				Log.d(TAG, "handleWifiApchanged, unkown state: " + wifiApState);
				break;
			}
		}

		private void stopServerAndStopDiscoveryService() {
			if (mServerCreateAndDiscovery != null) {
				mServerCreateAndDiscovery.stopServerAndDiscovery();
				mServerCreateAndDiscovery = null;
			}
		}

		private void createServerAndStartDiscoveryService() {
			if (mServerCreateAndDiscovery == null) {
				mServerCreateAndDiscovery = new ServerCreateAndDiscovery(
						mContext);
				mServerCreateAndDiscovery.start();
			}
		}

	}
}
