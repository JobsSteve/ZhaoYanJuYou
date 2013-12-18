package com.zhaoyan.communication.search;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
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
import android.util.Log;

import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.search.WifiDirectReciver.WifiDirectDeviceNotify;


/**
 * Manage WiFi direct to search peers and connect peers.
 * 
 */
@TargetApi(14)
public class WifiDirectManager implements WifiDirectDeviceNotify {
	private static final String TAG = "WifiDirectManager";
	@SuppressLint("NewApi")
	private WifiP2pManager mWifiP2pManager;
	private WifiManager mWifiManager;
	private boolean serverFlag = false;
	public Channel channel;
	private Context context;
	private ArrayList<ManagerP2pDeivce> observerList;

	public interface ManagerP2pDeivce {
		public void deviceChange(ArrayList<WifiP2pDevice> list);

		public void hasConnect(WifiP2pInfo info);
	};

	public WifiDirectManager(Context context) {
		this.context = context;
		mWifiP2pManager = (WifiP2pManager) context
				.getSystemService(Context.WIFI_P2P_SERVICE);
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		setDeviceName(mWifiP2pManager, channel,
				UserHelper.getUserName(context), null);
		if (mWifiManager.isWifiEnabled()) {
			channel = mWifiP2pManager.initialize(context,
					context.getMainLooper(), null);
			observerList = new ArrayList<WifiDirectManager.ManagerP2pDeivce>();
		}
	}

	public WifiDirectManager(Context context, boolean flag) {
		this.serverFlag = flag;
		this.context = context;
		mWifiP2pManager = (WifiP2pManager) context
				.getSystemService(Context.WIFI_P2P_SERVICE);
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		if (mWifiManager.isWifiEnabled()) {
			channel = mWifiP2pManager.initialize(context,
					context.getMainLooper(), null);
			observerList = new ArrayList<WifiDirectManager.ManagerP2pDeivce>();
		}
		if (flag) {
			setDeviceName(
					mWifiP2pManager,
					channel,
					WiFiNameEncryption.generateWiFiName("DreamLink"
							+ UserHelper.getUserName(context)), null);
		}
	}

	@SuppressLint({ "NewApi", "NewApi" })
	public void discover() {
		mWifiP2pManager.discoverPeers(channel, null);
	}

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

	/**
	 * @param serverFlag
	 *            ,if true as the group owner,else as the client
	 * */
	public void searchDirect(Context context, boolean server) {

		serverFlag = server;
		discover();
	}

	@SuppressLint({ "NewApi", "NewApi" })
	public void stopSearch() {
		mWifiP2pManager.stopPeerDiscovery(channel, null);
	}

	public void stopConnect() {
		try {
			mWifiP2pManager.cancelConnect(channel, null);
			mWifiP2pManager.removeGroup(channel, null);
		} catch (Exception e) {
			Log.e(TAG, "stopConnect()" + e);
		}
	}

	public void registerObserver(ManagerP2pDeivce notify) {
		observerList.add(notify);
	}

	public void unRegisterObserver(ManagerP2pDeivce notify) {
		observerList.remove(notify);
	}

	public void connect(WifiP2pConfig config) {
		mWifiP2pManager.connect(channel, config, null);
	}

	@Override
	public void wifiP2pConnected() {
		notifyConnect();
	}

	private void getPeerDevice(final boolean flag) {
		final ArrayList<WifiP2pDevice> serverList = new ArrayList<WifiP2pDevice>();
		mWifiP2pManager.requestPeers(channel, new PeerListListener() {
			@SuppressLint({ "NewApi", "NewApi", "NewApi", "NewApi", "NewApi",
					"NewApi", "NewApi", "NewApi", "NewApi", "NewApi", "NewApi",
					"NewApi", "NewApi", "NewApi" })
			public void onPeersAvailable(WifiP2pDeviceList peers) {
				if (!flag) {
					for (WifiP2pDevice device : peers.getDeviceList()) {
						if (WiFiNameEncryption.checkWiFiName(device.deviceName)) {
							serverList.add(device);
						}
					}
				}
				if (observerList != null) {
					for (ManagerP2pDeivce f : observerList) {
						f.deviceChange(serverList);
					}
				}
			}
		});
	}

	private void notifyConnect() {
		mWifiP2pManager.requestConnectionInfo(this.channel,
				new ConnectionInfoListener() {
					public void onConnectionInfoAvailable(WifiP2pInfo info) {
						if (observerList != null) {
							for (ManagerP2pDeivce f : observerList) {
								f.hasConnect(info);
							}
						}
					}
				});
	}

	@Override
	public void notifyDeviceChange() {
		getPeerDevice(serverFlag);
	}

	public void setServerFlag(boolean flag) {
		if (serverFlag == flag) {
			return;
		}
		this.serverFlag = flag;
		this.stopSearch();
		if (serverFlag) {
			setDeviceName(mWifiP2pManager, channel,
					WiFiNameEncryption.generateWiFiName(UserHelper
							.getUserName(context)), null);
		} else {
			setDeviceName(mWifiP2pManager, channel,
					UserHelper.getUserName(context), null);
		}
		this.discover();
		getPeerDevice(serverFlag);
	}

}
