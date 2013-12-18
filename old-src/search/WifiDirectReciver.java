package com.zhaoyan.communication.search;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Receive WiFi Direct sate and WiFi Direct connection info.
 * 
 */
@TargetApi(14)
public class WifiDirectReciver extends BroadcastReceiver {
	private static final String TAG = "WifiDirectReciver";
	private boolean connectFlag = false;
	private ArrayList<WifiDirectDeviceNotify> observerList;

	public interface WifiDirectDeviceNotify {
		public void notifyDeviceChange();

		public void wifiP2pConnected();
	}

	public void registerObserver(WifiDirectDeviceNotify notify) {
		observerList.add(notify);
		notify.notifyDeviceChange();
	}

	public void unRegisterObserver(WifiDirectDeviceNotify notify) {
		observerList.remove(notify);
	}

	public WifiDirectReciver() {
		observerList = new ArrayList<WifiDirectReciver.WifiDirectDeviceNotify>();
	}

	@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi" })
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
			int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN);
			Log.d(TAG, "WIFI_STATE_CHANGED_ACTION. state = " + wifiState);
		} else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			// Check to see if Wi-Fi is enabled and notify appropriate activity
			int p2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,
					0);
			Log.d(TAG, "WIFI_P2P_STATE_CHANGED_ACTION. state = " + p2pState);
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			// Call WifiP2pManager.requestPeers() to get a list of current peers
			Log.d(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION.");

			for (WifiDirectDeviceNotify f : observerList) {
				f.notifyDeviceChange();
			}
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
				.equals(action)) {
			NetworkInfo networkInfo = intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			WifiP2pInfo wifiP2pInfo = intent
					.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
			Log.d(TAG,
					"WIFI_P2P_CONNECTION_CHANGED_ACTION, networkInfo-connect = "
							+ networkInfo.isConnected()
							+ ", wifiP2pInfo-groupFormed = "
							+ wifiP2pInfo.groupFormed);

			if (networkInfo.isConnected()) {
				if (!connectFlag) {
					connectFlag = true;
					if (observerList != null) {
						for (WifiDirectDeviceNotify f : observerList) {
							f.wifiP2pConnected();
						}
					}
				}
			} else {
				connectFlag = false;
			}
		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
				.equals(action)) {
		}
	}

}
