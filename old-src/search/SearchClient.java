package com.zhaoyan.communication.search;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;


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
public class SearchClient {

	private static final String TAG = "SearchClient";

	private OnSearchListener mListener;
	private boolean mStarted = false;

	private static SearchClient mInstance;

	private Context mContext;

	private SearchClientLanAndroidAP mSearchClientLanAndroidAP;
	private SearchClientLan mSearchClientLan;

	public static SearchClient getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SearchClient(context);
		}
		return mInstance;
	}

	private SearchClient(Context context) {
		mContext = context;
	}

	public void setOnSearchListener(OnSearchListener listener) {
		mListener = listener;
		if (mSearchClientLan != null) {
			mSearchClientLan.setOnSearchListener(listener);
		}
		if (mSearchClientLanAndroidAP != null) {
			mSearchClientLanAndroidAP.setOnSearchListener(listener);
		}
	}

	/**
	 * start search client.
	 */
	public void startSearch() {
		Log.d(TAG, "Start search");
		if (mStarted) {
			Log.d(TAG, "startSearch() igonre, search is already started.");
			return;
		}
		mStarted = true;
		NetWorkUtil.acquireWifiMultiCastLock(mContext);
		Log.d(TAG, "The ip is "+NetWorkUtil.getLocalIpAddress());
		if (SearchUtil.isAndroidAPNetwork(mContext)) {
			// Android AP network.
			Log.d(TAG, "Android AP network.");
			mSearchClientLanAndroidAP = SearchClientLanAndroidAP
					.getInstance(mContext);
			mSearchClientLanAndroidAP.setOnSearchListener(mListener);
			mSearchClientLanAndroidAP.startSearch();
		} else {
			Log.d(TAG, "not Android AP network."+"the ip is "+NetWorkUtil.getLocalIpAddress());
		}

		if (!NetWorkUtil.isWifiApEnabled(mContext)) {
			Log.d(TAG, "This is not Android AP");
			mSearchClientLan = SearchClientLan.getInstance(mContext);
			mSearchClientLan.setOnSearchListener(mListener);
			mSearchClientLan.startSearch();
		} else {
			Log.d(TAG, "This is AP");
			
			if (SearchUtil.isAndroidAPNetwork(mContext)) {
				// Android AP network.
				Log.d(TAG, "Android AP network.");
				mSearchClientLanAndroidAP = SearchClientLanAndroidAP
						.getInstance(mContext);
				mSearchClientLanAndroidAP.setOnSearchListener(mListener);
				mSearchClientLanAndroidAP.startSearch();
			}else{
				Log.e(TAG,
						"This ip is  " + NetWorkUtil.getLocalIpAddress());
			}
			// Android AP is enabled
			// Because Android AP can not send or receive Lan
			// multicast/broadcast,So it does not need to listen multicast.
		}
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
		mInstance = null;
	}
}
