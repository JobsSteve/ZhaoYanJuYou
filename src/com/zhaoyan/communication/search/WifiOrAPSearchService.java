package com.zhaoyan.communication.search;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;

/**
 * this start wifi or wifi-AP server or search ,this server method can not run
 * with {@link DirectSearchService} at same time,if you want run this for
 * server,please remember unbind another one
 */
public class WifiOrAPSearchService extends Service {
	private final String TAG = "CreateAPServer";

	private static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	private static final String EXTRA_WIFI_AP_STATE = "wifi_state";
	private static final int WIFI_AP_STATE_ENABLING = 12;
	private static final int WIFI_AP_STATE_ENABLED = 13;
	private static final int WIFI_AP_STATE_DISABLING = 10;
	private static final int WIFI_AP_STATE_DISABLED = 11;
	private static final int WIFI_AP_STATE_FAILED = 14;

	private SearchClient mSearchClient;
	private WifiOrAPBinder mBinder;
	private WifiManager mWifiManager;
	private IntentFilter mWiFiFilter;
	private SearchSever mSearchServer;
	private OnSearchListener onSearchListener;
	private boolean server_register = false;
	private boolean client_register = false;
	private boolean flag = false;
	private boolean scann_flag = true;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		super.onCreate();
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		mBinder = null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (mBinder == null) {
			mBinder = new WifiOrAPBinder();
		}
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (mSearchClient != null) {
			mSearchClient.stopSearch();
			mSearchClient.setOnSearchListener(null);
			flag = false;
			mSearchClient = null;
		}
		if (mSearchServer != null) {
			mSearchServer.stopSearch();
			mSearchServer.setOnSearchListener(null);
			mSearchServer = null;
		}
		if (server_register) {
			unregisterReceiver(mBroadcastReceiver);
			server_register = false;
		}
		if (client_register) {
			unregisterReceiver(mWifiBroadcastReceiver);
			client_register = false;
		}
		stopSelf();
		return super.onUnbind(intent);
	}

	public class WifiOrAPBinder extends Binder {
		public WifiOrAPSearchService getService() {
			return WifiOrAPSearchService.this;
		}
	}

	/**
	 * create a wifi/wifi-direct server</br>
	 * 
	 * @param serverType
	 *            maybe WIFI/Wifi-Direct,if null or anything else it mean create
	 *            wifi-direct
	 * @param searchListener
	 *            {@link OnSearchListener} the interface for search result
	 */
	public void startServer(String serverType, OnSearchListener searchListener) {
		if (mSearchClient != null) {
			mSearchClient.stopSearch();
			mSearchClient.setOnSearchListener(null);
			flag = false;
			mSearchClient = null;
		}
		if (mSearchServer != null) {
			mSearchServer.stopSearch();
			mSearchServer.setOnSearchListener(null);
			mSearchServer = null;
		}
		if (server_register) {
			unregisterReceiver(mBroadcastReceiver);
			server_register = false;
		}
		mSearchClient = SearchClient.getInstance(this);
		if (searchListener != null) {
			mSearchClient.setOnSearchListener(searchListener);
		} else {
			mSearchClient.setOnSearchListener(null);
		}
		if (serverType != null
				&& ConnectHelper.SERVER_TYPE_WIFI.equals(serverType)) {
			if (NetWorkUtil.isWifiApEnabled(this)) {
				NetWorkUtil.setWifiAPEnabled(this, null, false);
			}
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			if (mWifiManager.isWifiEnabled()
					&& cm.getActiveNetworkInfo() != null) {
				createServerAndStartSearch();
			} else {
				setWifiEnabled(true);
				/**
				 * need enable wifi ,or connect one network,maybe need user do
				 * that
				 */
				IntentFilter filter = new IntentFilter();
				filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
				registerReceiver(mBroadcastReceiver, filter);
				server_register = true;
			}
		} else {
			/** start wifi-ap server ,and it is default server */
			setWifiEnabled(false);
			if (!NetWorkUtil.isWifiApEnabled(getApplicationContext())) {
				String wifiNameSuffix = getWifiNameSuffixFromSharedPreferences();
				String wifiAPName = null;
				if (TextUtils.isEmpty(wifiNameSuffix)) {
					wifiAPName = WiFiNameEncryption.generateWiFiName(UserHelper
							.getUserName(this));
				} else {
					wifiAPName = WiFiNameEncryption.generateWiFiName(
							UserHelper.getUserName(this), wifiNameSuffix);
				}

				String wifiAPPassword = WiFiNameEncryption
						.getWiFiPassword(wifiAPName);
				NetWorkUtil.setWifiAPEnabled(this, wifiAPName, wifiAPPassword,
						true);
				IntentFilter filter = new IntentFilter();
				filter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
				registerReceiver(mBroadcastReceiver, filter);
				server_register = true;
			} else {
				createServerAndStartSearch();
			}
		}
	}
	
	/**
	 * This is designed for release listener.
	 * 
	 * @param listener
	 */
	public void setSearchClientListener(OnSearchListener listener) {
		if (mSearchClient != null) {
			mSearchClient.setOnSearchListener(listener);
		}
		onSearchListener = null;
	}

	private String getWifiNameSuffixFromSharedPreferences() {
		return WifiNameSuffixLoader.getWifiNameSuffix(getApplicationContext());
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive, action: " + action);
			if (WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
				handleWifiApchanged(intent.getIntExtra(EXTRA_WIFI_AP_STATE,
						WIFI_AP_STATE_FAILED));
			} else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED) {
					createServerAndStartSearch();
				}
			}
		}

		private void handleWifiApchanged(int wifiApState) {
			switch (wifiApState) {
			case WIFI_AP_STATE_ENABLING:
				Log.d(TAG, "WIFI_AP_STATE_ENABLING");
				break;
			case WIFI_AP_STATE_ENABLED:
				Log.d(TAG, "WIFI_AP_STATE_ENABLED");
				flag = true;
				createServerAndStartSearch();
				break;
			case WIFI_AP_STATE_DISABLING:
				Log.d(TAG, "WIFI_AP_STATE_DISABLING");
				break;
			case WIFI_AP_STATE_DISABLED:
				Log.d(TAG, "WIFI_AP_STATE_DISABLED");
				if (mSearchClient != null) {
					mSearchClient.stopSearch();
					mSearchClient.setOnSearchListener(null);
					mSearchClient = null;
					flag = false;
				}
				break;
			case WIFI_AP_STATE_FAILED:
				Log.d(TAG, "WIFI_AP_STATE_FAILED");
				break;
			default:
				Log.d(TAG, "handleWifiApchanged, unkown state: " + wifiApState);
				if (NetWorkUtil.isWifiApEnabled(getApplicationContext())) {
					if (flag) {
						break;
					} else {
						createServerAndStartSearch();
					}
				}
				break;
			}
		}
	};

	/**
	 * start search server
	 * 
	 * @param searchListener
	 *            {@link OnSearchListener} the result notify interface
	 * */
	public void startSearch(OnSearchListener searchListener) {
		scann_flag = true;
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.startScan();
		} else {
			setWifiEnabled(true);
		}
		this.onSearchListener = searchListener;
		if (mSearchServer != null) {
			mSearchServer.stopSearch();
			mSearchServer.setOnSearchListener(null);
		}
		if (mSearchClient != null) {
			mSearchClient.stopSearch();
			mSearchClient.setOnSearchListener(null);
			flag = false;
			mSearchClient = null;
		}
		if (client_register) {
			unregisterReceiver(mWifiBroadcastReceiver);
			client_register = false;
		}

		// If Wifi is connected, start search server.
		if (NetWorkUtil.isWifiConnected(getApplicationContext())) {
			mSearchServer = SearchSever.getInstance(this);
			mSearchServer.setOnSearchListener(searchListener);
			mSearchServer.startSearch();
		}

		mWiFiFilter = new IntentFilter();
		mWiFiFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mWiFiFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mWiFiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(mWifiBroadcastReceiver, mWiFiFilter);
		client_register = true;
	}
	
	/**
	 * This is designed for release listener.
	 * 
	 * @param listener
	 */
	public void setSearchServerListener(OnSearchListener listener) {
		if (mSearchServer != null) {
			Log.d(TAG, "setSearchServerListener ok.");
			mSearchServer.setOnSearchListener(listener);
		} else {
			Log.d(TAG, "setSearchServerListener fail.");
		}
		onSearchListener = listener;
	}

	private void setWifiEnabled(boolean enable) {
		if (enable) {
			// If Android AP is enabled, disable AP.
			if (SearchUtil.isAndroidAPNetwork(this)) {
				NetWorkUtil.setWifiAPEnabled(this,
						null + UserHelper.getUserName(this), false);
			}
			// Enable WiFi.
			if (!mWifiManager.isWifiEnabled()) {
				mWifiManager.setWifiEnabled(true);
			}
		} else {
			// Disable WiFi.
			if (mWifiManager.isWifiEnabled()) {
				mWifiManager.setWifiEnabled(false);
			}
		}
	}

	private BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				handleWifiStateChanged(intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN));
			} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				handleScanReuslt();
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
				handleNetworkSate((NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
			}

		}
	};

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

	private void handleScanReuslt() {
		Log.d(TAG, "handleScanReuslt()");
		final List<ScanResult> results = mWifiManager.getScanResults();
		if (results != null) {
			for (ScanResult result : results) {
				Log.d(TAG, "handleScanReuslt, found wifi: " + result.SSID);
				if (WiFiNameEncryption.checkWiFiName(result.SSID)) {
					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
					if (wifiInfo != null) {
						String connectedSSID = wifiInfo.getSSID();
						if (connectedSSID != null) {
							Log.d(TAG, connectedSSID + "-------------- "
									+ result.SSID);
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
					ServerInfo info = new ServerInfo();
					info.setServerName(WiFiNameEncryption
							.getUserName(result.SSID));
					info.setServerType(ConnectHelper.SERVER_TYPE_WIFI_AP);
					info.setServerSsid(result.SSID);
					onSearchListener.onSearchSuccess(info);
				}
			}
		}
		if (scann_flag)
			mWifiManager.startScan();
	}

	private void handleNetworkSate(NetworkInfo networkInfo) {
		if (networkInfo.isConnected()) {
			// Network is connected.
			if (mSearchServer != null) {
				mSearchServer.stopSearch();
				mSearchServer.setOnSearchListener(null);
				mSearchServer = null;
			}
			mSearchServer = SearchSever.getInstance(getApplicationContext());
			mSearchServer.setOnSearchListener(onSearchListener);
			mSearchServer.startSearch();
		} else {
			// Network is not connected.
			if (mSearchServer != null) {
				mSearchServer.stopSearch();
				mSearchServer.setOnSearchListener(null);
				mSearchServer = null;
			}
		}
	}

	public boolean connectToServer(ServerInfo info) {
		if (info == null) {
			return false;
		} else if (info.getServerType().equals(ConnectHelper.SERVER_TYPE_WIFI)) {
			SocketCommunicationManager.getInstance(getApplicationContext())
					.connectServer(this.getApplicationContext(),
							info.getServerIp());
			return true;
		} else if (info.getServerType().equals(
				ConnectHelper.SERVER_TYPE_WIFI_AP)) {
			connetAP(info.getServerSsid());
			return true;
		}
		return false;
	}

	public boolean connectToServer(String ip) {
		if (ip == null) {
			return false;
		}
		SocketCommunicationManager.getInstance(getApplicationContext())
				.connectServer(this, ip);
		return true;
	}

	private void connetAP(String SSID) {
		Log.d(TAG, "connetAP: " + SSID);
		WifiInfo info = mWifiManager.getConnectionInfo();
		if (info != null) {
			String connectedSSID = info.getSSID();
			if (connectedSSID != null) {
				if (connectedSSID.equals("\"" + SSID + "\"")) {
					// Already connected to the ssid ignore.
					Log.d(TAG, "Already connected to the ssid ignore. " + SSID);
					return;
				}
			}
		}

		WifiConfiguration configuration = new WifiConfiguration();
		configuration.SSID = "\"" + SSID + "\"";
		configuration.preSharedKey = "\""
				+ WiFiNameEncryption.getWiFiPassword(SSID) + "\"";
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

	private void createServerAndStartSearch() {
		SocketCommunicationManager.getInstance(getApplicationContext())
				.startServer(getApplicationContext());
		UserManager.getInstance().addLocalServerUser();

		// Let server thread start first.
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// ignore
		}
		if (mSearchClient == null) {
			mSearchClient = SearchClient.getInstance(getApplicationContext());
		}
		mSearchClient.setOnSearchListener(onSearchListener);
		mSearchClient.startSearch();

		this.sendBroadcast(new Intent(ConnectHelper.ACTION_SERVER_CREATED));
	}

	public void stopSearch() {
		scann_flag = false;
		if (client_register) {
			unregisterReceiver(mWifiBroadcastReceiver);
			client_register = false;
		}
		if (server_register) {
			unregisterReceiver(mBroadcastReceiver);
			server_register = false;
		}
		if (mSearchClient != null) {
			mSearchClient.stopSearch();
			mSearchClient.setOnSearchListener(null);
		}
		if (mSearchServer != null) {
			mSearchServer.stopSearch();
			mSearchServer.setOnSearchListener(null);
		}
	}
}
