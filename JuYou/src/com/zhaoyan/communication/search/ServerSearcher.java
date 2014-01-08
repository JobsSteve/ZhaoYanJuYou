package com.zhaoyan.communication.search;

import android.content.Context;

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
public class ServerSearcher {
	private static final String TAG = "ServerSearcher";
	private static ServerSearcher mInstance;
	private Context mContext;
	private boolean mStarted = false;
	public static final int SERVER_TYPE_NONE = 0x0;
	public static final int SERVER_TYPE_LAN = 0x1;
	public static final int SERVER_TYPE_AP = 0x2;
	public static final int SERVER_TYPE_ALL = SERVER_TYPE_LAN | SERVER_TYPE_AP;
	private int mCurrentServerType = SERVER_TYPE_NONE;

	private ServerSearcherLan mServerSearcherLan;
	private ServerSearcherAndroidAP mServerSearcherAndroidAP;
	private ServerInfoProcesor mServerInfoProcesor;

	public static ServerSearcher getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ServerSearcher(context);
		}
		return mInstance;
	}

	private ServerSearcher(Context context) {
		mContext = context.getApplicationContext();
		mServerInfoProcesor = new ServerInfoProcesor(mContext);
	}

	public void startSearch(int serverType) {
		if (mStarted && mCurrentServerType == serverType) {
			Log.d(TAG,
					"startSearch() ignore, search is already started. serverType = "
							+ serverType);
			return;
		}
		Log.d(TAG, "startSearch() serverType = " + serverType);
		mStarted = true;

		if (SERVER_TYPE_AP != (mCurrentServerType & SERVER_TYPE_AP)
				&& SERVER_TYPE_AP == (serverType & SERVER_TYPE_AP)) {
			// start AP search.
			mServerSearcherAndroidAP = new ServerSearcherAndroidAP(mContext);
			mServerSearcherAndroidAP.setOnSearchListener(mServerInfoProcesor);
			mServerSearcherAndroidAP.startSearch();
			mCurrentServerType = mCurrentServerType | SERVER_TYPE_AP;
		}

		if (SERVER_TYPE_LAN != (mCurrentServerType & SERVER_TYPE_LAN)
				&& SERVER_TYPE_LAN == (serverType & SERVER_TYPE_LAN)) {
			// start LAN search.
			mServerSearcherLan = new ServerSearcherLan(mContext);
			mServerSearcherLan.setOnSearchListener(mServerInfoProcesor);
			mServerSearcherLan.startSearch();
			mCurrentServerType = mCurrentServerType | SERVER_TYPE_LAN;
		}
	}

	public void stopSearch(int serverType) {
		if (!mStarted && (mCurrentServerType & serverType) == serverType) {
			Log.d(TAG, "stopSearch() ignore, Not started. serverType = "
					+ serverType);
			return;
		}
		Log.d(TAG, "Stop search. serverType = " + serverType);

		if (SERVER_TYPE_AP == (mCurrentServerType & SERVER_TYPE_AP)
				&& SERVER_TYPE_AP == (serverType & SERVER_TYPE_AP)) {
			if (mServerSearcherAndroidAP != null) {
				mServerSearcherAndroidAP.stopSearch();
				mServerSearcherAndroidAP = null;
			}
			mCurrentServerType = mCurrentServerType & (~SERVER_TYPE_AP);
		}
		if (SERVER_TYPE_LAN == (mCurrentServerType & SERVER_TYPE_LAN)
				&& SERVER_TYPE_LAN == (serverType & SERVER_TYPE_LAN)) {
			if (mServerSearcherLan != null) {
				mServerSearcherLan.stopSearch();
				mServerSearcherLan = null;
			}
			mCurrentServerType = mCurrentServerType & (~SERVER_TYPE_LAN);
		}

		if (mCurrentServerType == SERVER_TYPE_NONE) {
			Log.d(TAG, "All search is stoped.");
			mStarted = false;
		}
	}

	public void clearServerInfo(int serverType) {
		mServerInfoProcesor.clearServerInfo(serverType);
	}

	public void release() {
		mInstance = null;
	}
}
