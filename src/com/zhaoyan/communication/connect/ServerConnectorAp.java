package com.zhaoyan.communication.connect;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.search.Search;
import com.zhaoyan.communication.search.WiFiNameEncryption;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class ServerConnectorAp {
	private static final String TAG = "ServerConnectorAp";
	private Context mContext;
	private WifiManager mWifiManager;
	private NetworkBroadcastReceiver mNetworkBroadcastReceiver;
	ConnectivityManager connectivityManager;

	public ServerConnectorAp(Context context) {
		mContext = context;
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public void connectServer(String ssidAp) {
		Log.d(TAG, ssidAp);
		if (isConnectedAp(ssidAp)) {
			connectCurrentApServer();
		} else {
			connectAp(ssidAp);
			mNetworkBroadcastReceiver = new NetworkBroadcastReceiver(ssidAp);
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			mContext.registerReceiver(mNetworkBroadcastReceiver, intentFilter);
		}
	}

	private boolean isConnectedAp(String ssidAp) {

		NetworkInfo.State state = connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
		boolean result = false;
		if (state == NetworkInfo.State.CONNECTED) {
			WifiInfo info = mWifiManager.getConnectionInfo();
			if (info != null) {
				String connectedSSID = info.getSSID();
				if (("\"" + ssidAp + "\"").equals(connectedSSID)) {
					// Already connected to the ssid ignore.
					Log.d(TAG, "Already connected to the ssid ignore. "
							+ ssidAp);
					result = true;
				}
			}
		}
		return result;
	}

	private void connectCurrentApServer() {
		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		communicationManager.connectServer(mContext, Search.ANDROID_AP_ADDRESS);
	}

	private void connectAp(String ssidAp) {
		Log.d(TAG, "connetAP: " + ssidAp);
		WifiConfiguration configuration = new WifiConfiguration();
		configuration.SSID = "\"" + ssidAp + "\"";
		configuration.preSharedKey = "\""
				+ WiFiNameEncryption.getWiFiPassword(ssidAp) + "\"";
		configuration.hiddenSSID = true;
		configuration.allowedAuthAlgorithms
				.set(WifiConfiguration.AuthAlgorithm.OPEN);
		configuration.allowedKeyManagement
				.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		configuration.allowedGroupCiphers
				.set(WifiConfiguration.GroupCipher.TKIP);
		configuration.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		configuration.allowedGroupCiphers
				.set(WifiConfiguration.GroupCipher.CCMP);
		configuration.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.CCMP);
		configuration.status = WifiConfiguration.Status.ENABLED;

		int netId = mWifiManager.addNetwork(configuration);
		mWifiManager.saveConfiguration();
		boolean result = mWifiManager.enableNetwork(netId, true);
		Log.d(TAG, "enable network result: " + result);
	}

	private class NetworkBroadcastReceiver extends BroadcastReceiver {
		private String mSsidAp;

		public NetworkBroadcastReceiver(String ssidAp) {
			mSsidAp = ssidAp;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
				handleNetworkSate(context.getApplicationContext());
			}
		}

		private void handleNetworkSate(Context context) {
			if (isConnectedAp(mSsidAp)) {
				connectCurrentApServer();
				try {
					context.unregisterReceiver(mNetworkBroadcastReceiver);
				} catch (Exception e) {
				}
			}

		}
	}
}
