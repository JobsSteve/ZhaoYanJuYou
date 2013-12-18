package com.zhaoyan.communication.search;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;

/**
 * This class is used for search the server in current WiFi network
 * connection.</br>
 * 
 * Before {@link #startSearch()}, make sure the device is already connected wifi
 * network.</br>
 * 
 * It can find STA server, Android WiFi AP server in current network. For more
 * detail, see {@link ServerSearcherLanWifi} and
 * {@link ServerSearcherLanAndroidAP} .</br>
 * 
 */
public class ServerSearcherLan {
	private static final String TAG = "ServerSearcherLan";
	private Context mContext;
	private OnSearchListenerLan mListener;
	private boolean mStarted = false;

	private ServerSearcherLanWifi mServerSearcherLanWifi;
	private ServerSearcherLanAndroidAP mServerSearcherLanAndroidAP;

	public ServerSearcherLan(Context context) {
		mContext = context.getApplicationContext();
	}

	public void setOnSearchListener(OnSearchListenerLan listener) {
		mListener = listener;
		if (mServerSearcherLanWifi != null) {
			mServerSearcherLanWifi.setOnSearchListener(listener);
		}
		if (mServerSearcherLanAndroidAP != null) {
			mServerSearcherLanAndroidAP.setOnSearchListener(listener);
		}
	}

	public void startSearch() {
		if (mStarted) {
			Log.d(TAG, "startSearch() ignore, search is already started.");
			return;
		}
		Log.d(TAG, "Start search.");
		mStarted = true;

		if (SearchUtil.isAndroidAPNetwork(mContext)) {
			// Android AP network.
			Log.d(TAG, "Android AP network.");
			mServerSearcherLanAndroidAP = new ServerSearcherLanAndroidAP(
					mContext);
			mServerSearcherLanAndroidAP.setOnSearchListener(mListener);
			mServerSearcherLanAndroidAP.startSearch();
		} else {
			Log.d(TAG, "not Android AP network.");
		}

		if (!NetWorkUtil.isWifiApEnabled(mContext)) {
			Log.d(TAG, "This is not Android AP");
			mServerSearcherLanWifi = new ServerSearcherLanWifi(mContext);
			mServerSearcherLanWifi.setOnSearchListener(mListener);
			mServerSearcherLanWifi.startSearch();
		} else {
			Log.d(TAG, "This is AP");
			// Android AP is enabled
			// Because Android AP can not send or receive Lan
			// multicast/broadcast,So it does not need to listen multicast.
		}
	}

	public void stopSearch() {
		if (!mStarted) {
			Log.d(TAG, "stopSearch igonre, not started.");
			return;
		}
		Log.d(TAG, "Stop search");
		mStarted = false;

		if (mServerSearcherLanWifi != null) {
			mServerSearcherLanWifi.setOnSearchListener(null);
			mServerSearcherLanWifi.stopSearch();
		}
		if (mServerSearcherLanAndroidAP != null) {
			mServerSearcherLanAndroidAP.setOnSearchListener(null);
			mServerSearcherLanAndroidAP.stopSearch();
		}
	}

	public interface OnSearchListenerLan {
		/**
		 * Search server success and found a server</br>
		 * 
		 * @param serverIP
		 *            The server IP address.
		 */
		void onFoundLanServer(String serverIP);

		/**
		 * Search server stop</br>
		 */
		void onSearchLanStop();
	}
}
