package com.zhaoyan.communication.search;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;

/**
 * This class is used by server for search clients.</br>
 * 
 * There are two kind of lan network:</br>
 * 
 * 1. No Android AP Lan.</br>
 * 
 * 2. Has Android AP Lan.</br>
 * 
 * In the situation 1, we use lan multicast to find clients.</br>
 * 
 * In the situation 2, we use lan mulitcast and UDP communication to search
 * clients</br>
 * 
 * This is because AP can not send or receive multicast in Android AP lan
 * network.</br>
 * 
 * Notice: SearchClient do not get clint IP, Only client can get server IP, and
 * client connect server.</br>
 */
public class DiscoveryService {
	private static final String TAG = "DiscoveryService";
	private boolean mStarted = false;
	private static DiscoveryService mInstance;

	private Context mContext;
	private DiscoveryServiceLanAP mSearchClientLanAndroidAP;
	private DiscoveryServiceLanWifi mSearchClientLan;
	private SendServerInfoSocket mSendServerInfoSocket;

	public static DiscoveryService getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DiscoveryService(context);
		}
		return mInstance;
	}

	private DiscoveryService(Context context) {
		mContext = context;
	}

	/**
	 * start deamon client.
	 */
	public void startDiscoveryService() {
		if (mStarted) {
			Log.d(TAG,
					"startDiscoveryService() igonre, search is already started.");
			return;
		}

		Log.d(TAG, "startDiscoveryService.");
		mStarted = true;

		NetWorkUtil.acquireWifiMultiCastLock(mContext);
		Log.d(TAG, "The ip is " + NetWorkUtil.getLocalIpAddress());
		if (SearchUtil.isAndroidAPNetwork(mContext)) {
			Log.d(TAG, "Android AP network.");
			if (!NetWorkUtil.isWifiApEnabled(mContext)) {
				Log.d(TAG, "This is not Android AP");
				mSearchClientLan = new DiscoveryServiceLanWifi();
				mSearchClientLan.startSearch();
			}

			mSearchClientLanAndroidAP = DiscoveryServiceLanAP
					.getInstance(mContext);
			mSearchClientLanAndroidAP.startSearch();
		} else {
			Log.d(TAG, "not Android AP network.");
			mSearchClientLan = new DiscoveryServiceLanWifi();
			mSearchClientLan.startSearch();
		}
		
		mSendServerInfoSocket = SendServerInfoSocket.getInstance();
		mSendServerInfoSocket.startServer(mContext);
	}

	public void stopSearch() {
		Log.d(TAG, "Stop search.");
		StackTraceElement st[] = Thread.currentThread().getStackTrace();
		for (int i = 0; i < st.length; i++) {
			Log.d(TAG, "trace: " + st[i].toString());
		}
		mStarted = false;
		NetWorkUtil.releaseWifiMultiCastLock();

		if (mSearchClientLan != null) {
			mSearchClientLan.stopSearch();
			mSearchClientLan = null;
		}

		if (mSearchClientLanAndroidAP != null) {
			mSearchClientLanAndroidAP.stopSearch();
			mSearchClientLanAndroidAP = null;
		}
		
		if (mSendServerInfoSocket != null) {
			mSendServerInfoSocket.stopServer();
		}
		mInstance = null;
	}
}
