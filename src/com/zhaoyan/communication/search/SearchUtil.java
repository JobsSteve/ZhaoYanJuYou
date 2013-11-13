package com.zhaoyan.communication.search;

import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;

public class SearchUtil {
	private static final String TAG = "SearchUtil";

	public static void clearWifiConnectHistory(Context context) {
		Log.d(TAG, "clearWifiConnectHistory");
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		List<WifiConfiguration> configurations = wifiManager
				.getConfiguredNetworks();
		if (configurations == null) {
			return;
		}
		for (WifiConfiguration configuration : configurations) {
			// SSID format is "ABC@PKQDJTTRJOATQMVX", so remove the "".
			String SSID = configuration.SSID.substring(1,
					configuration.SSID.length() - 1);
			if (WiFiNameEncryption.checkWiFiName(SSID)) {
				wifiManager.removeNetwork(configuration.networkId);
			}
		}
	}

	public static boolean isAndroidAPNetwork(Context context) {
		String ipAddress = NetWorkUtil.getLocalIpAddress();
		if (!TextUtils.isEmpty(ipAddress)
				&& ipAddress.startsWith(Search.ANDROID_STA_ADDRESS_START)) {
			return true;
		} else {
			return false;
		}
	}
}
