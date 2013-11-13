package com.zhaoyan.communication.search;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.app.Service;
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
import com.zhaoyan.communication.search.WifiDirectReciver.WifiDirectDeviceNotify;

@SuppressLint({ "NewApi", "NewApi" })
public class DirectService extends Service {

	private WifiP2pManager wifiP2pManager;
	private WifiDirectReciver wifiDirectReciver;
	private Channel channel;
	private boolean flag;
	private IntentFilter mWifiDirectIntentFilter;
	private String TAG = "Direct Service";
	private User user;

	@Override
	public IBinder onBind(Intent arg0) {
		if (myBinder == null) {
			myBinder = new MyBinder();
		}
		return myBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		myBinder = null;
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		this.flag = intent.getBooleanExtra("flag", false);
		this.user = (User) intent.getBundleExtra("info").get("user");
		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				startDirectServer(flag, user);
			}
		}.start();

		return super.onStartCommand(intent, flags, startId);
	}

	public class MyBinder extends Binder {

		public DirectService getService() {
			return DirectService.this;
		}
	}

	private MyBinder myBinder;

	/**
	 * @param flag
	 *            true,start server;else false
	 * @param user
	 *            null,the server start,else the client will connect to
	 */
	@SuppressLint("NewApi")
	public void startDirectServer(boolean flag, User user) {
		this.flag = flag;
		this.user = user;
		wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = wifiP2pManager.initialize(getApplicationContext(),
				getMainLooper(), null);
		initReceiver();
		this.registerReceiver(wifiDirectReciver, mWifiDirectIntentFilter);
		if (flag) {
			setDeviceName(wifiP2pManager, channel,
					WiFiNameEncryption.generateWiFiName(UserHelper
							.getUserName(getApplicationContext())), null);
		} else {
			setDeviceName(wifiP2pManager, channel,
					UserHelper.getUserName(getApplicationContext()), null);
		}
		wifiP2pManager.discoverPeers(channel, null);
		wifiDirectReciver.registerObserver(new DeviceNotify());
	}

	private class DeviceNotify implements WifiDirectDeviceNotify {

		@SuppressLint({ "NewApi", "NewApi" })
		@Override
		public void notifyDeviceChange() {
			if (!flag) {
				wifiP2pManager.requestPeers(channel, new PeerListListener() {

					@SuppressLint("NewApi")
					@Override
					public void onPeersAvailable(WifiP2pDeviceList peers) {
						// TODO client auto connect to server
						if (user == null) {
							/** no possible */
							return;
						}
						for (WifiP2pDevice device : peers.getDeviceList()) {
							if (WiFiNameEncryption
									.checkWiFiName(device.deviceName)) {
								WifiP2pConfig config = new WifiP2pConfig();
								config.deviceAddress = device.deviceAddress;
								config.groupOwnerIntent = 0;
								wifiP2pManager.connect(channel, config, null);
								return;
							}
						}
					}
				});
			}
		}

		@Override
		public void wifiP2pConnected() {
			// TODO
			wifiP2pManager.requestConnectionInfo(channel,
					new ConnectionInfoListener() {
						public void onConnectionInfoAvailable(WifiP2pInfo info) {
							if (flag) {
								SocketCommunicationManager.getInstance(
										getApplicationContext()).startServer(
										getApplicationContext());
							} else {
								SocketCommunicationManager.getInstance(
										getApplicationContext())
										.connectServer(
												getApplicationContext(),
												info.groupOwnerAddress
														.getHostAddress());
							}
							wifiP2pManager.stopPeerDiscovery(channel, null);
							DirectService.this.getApplicationContext()
									.unregisterReceiver(wifiDirectReciver);
							wifiDirectReciver = null;
							channel = null;
							wifiP2pManager = null;
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
		wifiDirectReciver = new WifiDirectReciver();
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
}
