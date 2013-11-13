package com.zhaoyan.communication.search;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.search.DirectSearchService.DirectBinder;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;
import com.zhaoyan.communication.search.WifiOrAPSearchService.WifiOrAPBinder;

public class ConnectHelper {
	private final String TAG = "ConnectHelper";
	public final static String SERVER_TYPE_WIFI = "wifi";
	public final static String SERVER_TYPE_WIFI_AP = "wifi-ap";
	public final static String SERVER_TYPE_WIFI_DIRECT = "wifi-direct";
	private final String[] SERVER_TYPE = { SERVER_TYPE_WIFI,
			SERVER_TYPE_WIFI_AP, SERVER_TYPE_WIFI_DIRECT };

	/** Intent action for server created broadcast. */
	public static final String ACTION_SERVER_CREATED = "com.dreamlink.communication.server.created";

	private Context mContext;

	private WifiOrAPSearchService mWifiOrAPService;
	private WifiOrAPServiceConnection mWifiOrAPServiceConnection;
	private DirectSearchService mDirectService;
	private DirectServiceConnection mDirectServiceConnection;

	private static ConnectHelper mInstance;

	private ConnectHelper(Context context) {
		this.mContext = context;
	}

	public synchronized static ConnectHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ConnectHelper(context.getApplicationContext());
		}
		return mInstance;
	}

	public String[] getServerType() {
		return SERVER_TYPE;
	}

	/**
	 * create server
	 * 
	 * @param type
	 *            just like: "wifi", "wifi-ap", "wifi-direct" case insensitive
	 * @param searchListener
	 *            {@link OnSearchListener}
	 * */
	public void createServer(String type, OnSearchListener searchListener) {
		if (type == null) {
			// default ap server
			type = SERVER_TYPE_WIFI_AP;
		}
		if (type.equals(SERVER_TYPE_WIFI)) {
			startWifiServer(searchListener);
		} else if (type.equals(SERVER_TYPE_WIFI_AP)) {
			startWifiAPserver(searchListener);
		} else if (type.equals(SERVER_TYPE_WIFI_DIRECT)) {
			startWifiDirectServer(null, searchListener);
		} else {
			Log.e(TAG, "Unknow type!");
		}
	}

	/**
	 * Start as a client to search WiFi and WiFi AP server.
	 * 
	 * @param user
	 *            which user do you want to search,just for direct ,in wifi
	 *            &wifi-ap case,this param is must be null
	 */
	public void searchServer(OnSearchListener searchListener) {
		Log.d(TAG, "searchServer");
		// Close WiFi AP if AP is enabled.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}

		if (mWifiOrAPService != null) {
			mWifiOrAPService.startSearch(searchListener);
		} else {
			Intent intent = new Intent();
			intent.setClass(mContext, WifiOrAPSearchService.class);
			bindService(intent, new WifiOrAPServiceConnection(false, null,
					searchListener));
		}
	}

	/**
	 * Start as a client to search WiFi Direct server.
	 * 
	 * @param searchListener
	 * @param user
	 */
	public void searchDirectServer(OnSearchListener searchListener, User user) {
		// TODO This method is not be called
		if (!NetWorkUtil.isWifiDirectSupport(mContext)) {
			Toast.makeText(mContext, "Not support wifi-direct",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// Disable WiFi AP if WiFi AP is enabled.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}

		if (mDirectService != null) {
			mDirectService.startDirect(false, user, searchListener);
		} else {
			Intent intent = new Intent();
			intent.setClass(mContext, DirectSearchService.class);
			bindService(intent, new DirectServiceConnection(false, user,
					searchListener));
		}
	}

	private void startWifiDirectServer(User user,
			OnSearchListener searchListener) {
		if (!NetWorkUtil.isWifiDirectSupport(mContext)) {
			Toast.makeText(mContext, "Not support wifi-direct",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// Unbind wifiOrAPService
		if (mWifiOrAPServiceConnection != null) {
			unbindService(mWifiOrAPServiceConnection);
		}

		// Disable WiFi AP if AP is enabled.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}

		if (mDirectService != null) {
			mDirectService.startDirect(true, user, searchListener);
		}

		Intent intent = new Intent();
		intent.setClass(mContext, DirectSearchService.class);
		bindService(intent, new DirectServiceConnection(true, user,
				searchListener));
	}

	private void startWifiAPserver(OnSearchListener searchListener) {
		// Unbind WiFi direct service.
		if (mDirectServiceConnection != null) {
			unbindService(mDirectServiceConnection);
		}

		if (mWifiOrAPService != null) {
			mWifiOrAPService.startServer(SERVER_TYPE_WIFI_AP, searchListener);
		} else {
			Intent intent = new Intent();
			intent.setClass(mContext, WifiOrAPSearchService.class);
			bindService(intent, new WifiOrAPServiceConnection(true,
					SERVER_TYPE_WIFI_AP, searchListener));
		}
	}

	private void startWifiServer(OnSearchListener searchListener) {
		// Unbind WiFi direct service.
		if (mDirectServiceConnection != null) {
			unbindService(mDirectServiceConnection);
		}

		// Close WiFi AP if AP is enabled.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}

		if (mWifiOrAPService != null) {
			mWifiOrAPService.startServer(SERVER_TYPE_WIFI, searchListener);
		} else {
			Intent intent = new Intent();
			intent.setClass(mContext, WifiOrAPSearchService.class);
			bindService(intent, new WifiOrAPServiceConnection(true,
					SERVER_TYPE_WIFI, searchListener));
		}
	}

	/**
	 * Stop all search and unbind search service.
	 */
	public void stopSearch() {
		stopSearch(true);
	}

	/**
	 * Stop all search.
	 * 
	 * @param unbindService
	 *            If you want start search later, set unbindService false. If
	 *            you don't
	 */
	public void stopSearch(boolean unbindService) {
		if (mWifiOrAPService != null) {
			mWifiOrAPService.stopSearch();
		}
		if (mDirectService != null) {
			mDirectService.stopSearch();
		}

		if (unbindService) {
			if (mWifiOrAPServiceConnection != null) {
				unbindService(mWifiOrAPServiceConnection);
			}

			if (mDirectServiceConnection != null) {
				unbindService(mDirectServiceConnection);
			}
		}
	}

	public void connenctToServer(ServerInfo info) {
		Log.d(TAG, "connenctToServer");
		if (info.getServerType().equals("wifi-direct")) {
			if (mDirectService != null)
				mDirectService.connectToServer(info);
		} else if (info.getServerType().equals("wifi")
				|| info.getServerType().equals("wifi-ap")) {
			if (mWifiOrAPService != null)
				mWifiOrAPService.connectToServer(info);
		}
	}

	private void bindService(Intent intent, ServiceConnection connection) {
		mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
	}

	private void unbindService(ServiceConnection connection) {
		try {
			mContext.unbindService(connection);
		} catch (Exception e) {
			Log.e(TAG, "unbindServer error. " + e);
		}
		connection = null;
	}


	/**
	 * Service connection for {@link #WifiOrAPService}.
	 */
	private class DirectServiceConnection implements ServiceConnection {
		private boolean mIsStartServer = false;
		private User mUser;
		private OnSearchListener mListener;

		/**
		 * 
		 * @param isStartServer
		 *            true means start a server to search clients, false means
		 *            start a client to search servers.
		 * @param user
		 */
		public DirectServiceConnection(boolean isStartServer, User user,
				OnSearchListener listener) {
			mIsStartServer = isStartServer;
			mUser = user;
			mListener = listener;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDirectServiceConnection = this;

			DirectBinder binder = (DirectBinder) service;
			mDirectService = binder.getService();
			mDirectService.startDirect(mIsStartServer, mUser, mListener);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mWifiOrAPService = null;
			mDirectServiceConnection = null;
		}

	}

	/**
	 * Service connection for {@link #WifiOrAPService}.
	 */
	private class WifiOrAPServiceConnection implements ServiceConnection {
		private boolean mIsStartServer = false;
		private String mServerType;
		private OnSearchListener mListener;

		/**
		 * 
		 * @param isStartServer
		 *            true means start a server to search clients, false means
		 *            start a client to search servers.
		 * @param type
		 */
		public WifiOrAPServiceConnection(boolean isStartServer, String type,
				OnSearchListener onSearchListener) {
			mIsStartServer = isStartServer;
			mServerType = type;
			mListener = onSearchListener;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mWifiOrAPServiceConnection = this;
			WifiOrAPBinder binder = (WifiOrAPBinder) service;
			mWifiOrAPService = binder.getService();
			if (mIsStartServer) {
				mWifiOrAPService.startServer(mServerType, mListener);
			} else {
				mWifiOrAPService.startSearch(mListener);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mWifiOrAPService = null;
			mWifiOrAPServiceConnection = null;
		}

	}
}
