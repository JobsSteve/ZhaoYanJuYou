package com.zhaoyan.communication.search;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;
import com.zhaoyan.communication.search.WifiDirectReciver.WifiDirectDeviceNotify;

/**
 * use this create wifi-direct server and find wifi-direct server to connect.
 * </br> you should implements the {@link DeviceNotify} interface to know the
 * device change and connect status.</br> normally , application start this
 * service only once at the same time,so please remember unbind it.
 * */
@SuppressLint({ "NewApi", "NewApi" })
public class DirectSearchService extends Service {

	private WifiP2pManager wifiP2pManager;
	private WifiDirectReciver wifiDirectReceiver;
	private Channel channel;
	private boolean flag;
	private IntentFilter mWifiDirectIntentFilter;
	private String TAG = "Direct Service";
	private User user;
	private ArrayList<DeviceNotify> list;
	private IntentFilter filter;
	private OnSearchListener onSearchListener;
	private boolean register_flag = false;

	/**
	 * this interface will notify you that the wifi-direct device update and
	 * connect info
	 */
	public interface DeviceNotify {
		public void deviceChange(ArrayList<WifiP2pDevice> list);

		public void hasConnect(WifiP2pInfo info);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (myBinder == null) {
			myBinder = new DirectBinder();
		}
		return myBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		wifiP2pManager.stopPeerDiscovery(channel, null);
		unregisterReceiver(wifiDirectReceiver);
		if (register_flag) {
			unregisterReceiver(server_reciver);
			register_flag = false;
		}
		stopSelf();
		return super.onUnbind(intent);
	}

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		super.onCreate();
		initReceiver();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		myBinder = null;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}

	public class DirectBinder extends Binder {

		public DirectSearchService getService() {
			return DirectSearchService.this;
		}
	}

	public void stopSearch() {
		if (wifiP2pManager != null) {
			wifiP2pManager.stopPeerDiscovery(channel, null);
		}
	}

	private DirectBinder myBinder;

	public void regitserListener(DeviceNotify deviceNotify) {
		if (list == null) {
			list = new ArrayList<DirectSearchService.DeviceNotify>();
		}
		list.add(deviceNotify);
	}

	public void unregitserListener(DeviceNotify deviceNotify) {
		if (list != null && list.contains(deviceNotify)) {
			list.remove(deviceNotify);
			if (list.size() == 0) {
				if (wifiP2pManager != null) {
					stopServer();
				}
			}
		}
	}

	/**
	 * start wifi-direct server or search direct device
	 * 
	 * @param flag
	 *            true,start server;else search device
	 * @param user
	 *            the client will connect to,if null ,show all devices which
	 *            name conform the rule
	 */
	@SuppressLint("NewApi")
	public void startDirect(boolean flag, User user,
			OnSearchListener onSearchListener) {
		this.flag = flag;
		this.user = user;
		this.onSearchListener = onSearchListener;
		final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			startDirectServer();
		} else {
			wifiManager.setWifiEnabled(true);
			registerReceiver(server_reciver, filter);
			register_flag = true;
		}
	}

	private void startDirectServer() {
		wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = wifiP2pManager.initialize(getApplicationContext(),
				getMainLooper(), null);
		this.registerReceiver(wifiDirectReceiver, mWifiDirectIntentFilter);
		if (flag) {
			setDeviceName(wifiP2pManager, channel,
					WiFiNameEncryption.generateWiFiName(UserHelper
							.getUserName(getApplicationContext())), null);
		} else {
			setDeviceName(wifiP2pManager, channel,
					UserHelper.getUserName(getApplicationContext()), null);
		}
		wifiP2pManager.discoverPeers(channel, null);
		wifiDirectReceiver.registerObserver(new WifiDeviceNotify());
		notifyServerCreated();
	}

	private class WifiDeviceNotify implements WifiDirectDeviceNotify {

		@SuppressLint({ "NewApi", "NewApi" })
		@Override
		public void notifyDeviceChange() {
			if (!flag) {
				/* if server need not to know the device change */
				wifiP2pManager.requestPeers(channel, new PeerListListener() {

					@SuppressLint("NewApi")
					@Override
					public void onPeersAvailable(WifiP2pDeviceList peers) {
						ArrayList<WifiP2pDevice> serverList = new ArrayList<WifiP2pDevice>();
						for (WifiP2pDevice device : peers.getDeviceList()) {
							Log.e("ArbiterLiu", "just test ,not error : "
									+ device.deviceName);
							if (WiFiNameEncryption
									.checkWiFiName(device.deviceName)) {
								if (user != null) {
									/**
									 * this mean connect to the user which the
									 * server ask
									 */
									connectToDevice(device);
									return;
								} else {
									/**
									 * this mean search all that name conform
									 * the rule
									 */
									serverList.add(device);
									ServerInfo info = new ServerInfo();
									info.setServerType(ConnectHelper.SERVER_TYPE_WIFI_DIRECT);
									info.setServerDevice(device);
									info.setServerName(device.deviceName);
									onSearchListener.onSearchSuccess(info);
								}
							}
						}
					}
				});
			}
		}

		@Override
		public void wifiP2pConnected() {
			wifiP2pManager.requestConnectionInfo(channel,
					new ConnectionInfoListener() {
						public void onConnectionInfoAvailable(WifiP2pInfo info) {
							if (info.isGroupOwner) {
								SocketCommunicationManager.getInstance().startServer(
										getApplicationContext());
							} else {
								SocketCommunicationManager.getInstance()
										.connectServer(
												getApplicationContext(),
												info.groupOwnerAddress
														.getHostAddress());
							}
							if (list != null) {
								for (DeviceNotify f : list) {
									f.hasConnect(info);
								}
							}
							if (!info.isGroupOwner) {
								stopServer();
							}
						}
					});
		}

	}

	private void initReceiver() {
		mWifiDirectIntentFilter = new IntentFilter();
		mWifiDirectIntentFilter
				.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mWifiDirectIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mWifiDirectIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mWifiDirectIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mWifiDirectIntentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		wifiDirectReceiver = new WifiDirectReciver();
		filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	}

	@SuppressLint("NewApi")
	private void setDeviceName(WifiP2pManager manager, Channel channel,
			String name, ActionListener listener) {
		try {
			Method method = manager.getClass().getMethod("setDeviceName",
					Channel.class, String.class, ActionListener.class);
			method.invoke(manager, channel, name, listener);
		} catch (Exception e) {
			Log.e(TAG, "setDeviceName fail: " + e);
		}
	}

	private void connectToDevice(WifiP2pDevice device) {
		String userName = device.deviceName.substring(0,
				device.deviceName.indexOf("@"));
		if (userName.equals(user.getUserName())) {
			WifiP2pConfig config = new WifiP2pConfig();
			config.deviceAddress = device.deviceAddress;
			config.groupOwnerIntent = 0;
			wifiP2pManager.connect(channel, config, null);
		}
	}

	public void stopServer() {
		wifiP2pManager.stopPeerDiscovery(channel, null);
		DirectSearchService.this.getApplicationContext().unregisterReceiver(
				wifiDirectReceiver);
		wifiDirectReceiver = null;
		channel = null;
		wifiP2pManager = null;
	}

	private BroadcastReceiver server_reciver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				int wifiState = intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN);
				if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
					startDirectServer();
				}
			}
		}

	};

	public boolean connectToServer(ServerInfo info) {
		if (info == null
				|| !info.getServerType().equals(
						ConnectHelper.SERVER_TYPE_WIFI_DIRECT)) {
			return false;
		}
		WifiP2pDevice device = info.getServerDevice();
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.groupOwnerIntent = 0;
		wifiP2pManager.connect(channel, config, null);
		return true;

	}

	public void notifyServerCreated() {
		this.sendBroadcast(new Intent(ConnectHelper.ACTION_SERVER_CREATED));
	}
}
