package com.zhaoyan.communication.search2;

import java.util.List;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.search.WiFiNameEncryption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class ServerSearcherAndroidAP {
	private static final String TAG = "ServerSearcherAndroidAP";
	private Context mContext;
	private WifiManager mWifiManager;
	private OnSearchListenerAP mOnSearchListener;
	private BroadcastReceiver mWifiBroadcastReceiver;
	private boolean mIsStarted;

	public ServerSearcherAndroidAP(Context context) {
		mContext = context.getApplicationContext();
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
	}

	public void setOnSearchListener(OnSearchListenerAP listener) {
		mOnSearchListener = listener;
	}

	public void startSearch() {
		if (mIsStarted) {
			return;
		}
		Log.d(TAG, "startSearch");
		mIsStarted = true;

		// close wifi ap if needed.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}
		// open wifi or start scan.
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		} else {
			mWifiManager.startScan();
		}

		mWifiBroadcastReceiver = new WifiBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mContext.registerReceiver(mWifiBroadcastReceiver, intentFilter);
	}

	public void stopSearch() {
		if (!mIsStarted) {
			return;
		}
		Log.d(TAG, "stopSearch");
		mIsStarted = false;

		mOnSearchListener = null;
		try {
			mContext.unregisterReceiver(mWifiBroadcastReceiver);
		} catch (Exception e) {
			Log.e(TAG, "stopSearch " + e);
		}
	}

	private class WifiBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive, action = " + action);
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				handleWifiStateChanged(intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN));
			} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				handleScanReuslt();
			}
		}

		private void handleScanReuslt() {
			Log.d(TAG, "handleScanReuslt()");
			final List<ScanResult> results = mWifiManager.getScanResults();
			if (results == null) {
				return;
			}
			for (ScanResult result : results) {
				Log.d(TAG, "handleScanReuslt, wifi: " + result.SSID);
				if (WiFiNameEncryption.checkWiFiName(result.SSID)) {
					Log.d(TAG, "handleScanReuslt, Found a matched wifi: "
							+ result.SSID);
					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
					if (wifiInfo != null) {
						String connectedSSID = wifiInfo.getSSID();
						if (connectedSSID != null) {
							if (connectedSSID.equals("\"" + result.SSID + "\"")
									|| connectedSSID.equals(result.SSID)) {
								// Already connected to the ssid ignore.
								Log.d(TAG,
										"Already connected to the ssid ignore. "
												+ result.SSID);
								continue;
							}
						}
					}
					// Add this android wifi Ap server into database
					if (mOnSearchListener != null) {
						mOnSearchListener.onFoundAPServer(result.SSID);
					}
				}
			}

		}

		private void handleWifiStateChanged(int wifiState) {
			switch (wifiState) {
			case WifiManager.WIFI_STATE_ENABLING:
				Log.d(TAG, "WIFI_STATE_ENABLING");
				break;
			case WifiManager.WIFI_STATE_ENABLED:
				Log.d(TAG, "WIFI_STATE_ENABLED");
				Log.d(TAG, "Start WiFi scan.");
				mWifiManager.startScan();
				// if (mSearchServer != null)
				// mSearchServer.startSearch();
				break;
			case WifiManager.WIFI_STATE_DISABLING:
				Log.d(TAG, "WIFI_STATE_DISABLING");
				break;
			case WifiManager.WIFI_STATE_DISABLED:
				Log.d(TAG, "WIFI_STATE_DISABLED");
				break;

			default:
				break;
			}
		}
	}

	public interface OnSearchListenerAP {
		void onFoundAPServer(String ssid);
	}
}
