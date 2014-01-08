package com.zhaoyan.communication.search;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;


/**
 * This class is used for search the server in current WiFi network
 * connection.</br>
 * 
 * Before {@link #startSearch()}, make sure the device is already connected wifi
 * network.</br>
 * 
 * It can find STA server, Android WiFi AP server in current network. For more
 * detail, see {@link SearchSeverLan} and {@link SearchSeverLanAndroidAP}.</br>
 * 
 */
public class SearchSever {

	private static final String TAG = "SearchSever";

	private OnSearchListener mListener;

	private boolean mStarted = false;

	private static SearchSever mInstance;
	private Context mContext;

	private SearchSeverLan mSearchSeverLan;
	private SearchSeverLanAndroidAP mSearchSeverLanAndroidAP;

	public static SearchSever getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SearchSever(context);
		}
		return mInstance;
	}

	private SearchSever(Context context) {
		mContext = context;
	}

	public void setOnSearchListener(OnSearchListener listener) {
		mListener = listener;
		if (mSearchSeverLan != null) {
			mSearchSeverLan.setOnSearchListener(listener);
		}
		if (mSearchSeverLanAndroidAP != null) {
			mSearchSeverLanAndroidAP.setOnSearchListener(listener);
		}
	}

	public void startSearch() {
		Log.d(TAG, "Start search.");
		if (mStarted) {
			Log.d(TAG, "startSearch() ignore, search is already started.");
			return;
		}
		mStarted = true;

		if (SearchUtil.isAndroidAPNetwork(mContext)) {
			// Android AP network.
			Log.d(TAG, "Android AP network.");
			mSearchSeverLanAndroidAP = SearchSeverLanAndroidAP
					.getInstance(mContext);
			mSearchSeverLanAndroidAP.setOnSearchListener(mListener);
			mSearchSeverLanAndroidAP.startSearch();
		} else {
			Log.d(TAG, "not Android AP network.");
		}

		if (!NetWorkUtil.isWifiApEnabled(mContext)) {
			Log.d(TAG, "This is not Android AP");
			mSearchSeverLan = SearchSeverLan.getInstance(mContext);
			mSearchSeverLan.setOnSearchListener(mListener);
			mSearchSeverLan.startSearch();
		} else {
			Log.d(TAG, "This is AP");
			// Android AP is enabled
			// Because Android AP can not send or receive Lan
			// multicast/broadcast,So it does not need to listen multicast.
		}
	}

	public void stopSearch() {
		Log.d(TAG, "Stop search");
		mStarted = false;

		if (mSearchSeverLan != null) {
			mSearchSeverLan.stopSearch();
		}
		if (mSearchSeverLanAndroidAP != null) {
			mSearchSeverLanAndroidAP.stopSearch();
		}
		mInstance = null;
	}
}
