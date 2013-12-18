package com.zhaoyan.communication.search;

import android.net.wifi.p2p.WifiP2pDevice;

import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;


/**
 * use this to notify search result </br>
 * 
 * be used in {@link OnSearchListener} to notify use
 * */

public class ServerInfo {
	/** the server name to show */
	private String mServerName;
	/** three type : wifi,wifi-ap and wifi-direct */
	private String mServerType;
	/** if wifi ,use this to connect */
	private String mServerIp;
	/** if wifi-direct ,use this to get connect info to connect */
	private WifiP2pDevice mServerDevice;
	/** if wifi-ap ,use this to get connect info to connect */
	private String mServerSSID;

	public String getServerName() {
		return mServerName;
	}

	public void setServerName(String serverName) {
		this.mServerName = serverName;
	}

	public String getServerType() {
		return mServerType;
	}

	public void setServerType(String serverType) {
		this.mServerType = serverType;
	}

	public String getServerIp() {
		return mServerIp;
	}

	public void setServerIp(String serverIp) {
		this.mServerIp = serverIp;
	}

	public WifiP2pDevice getServerDevice() {
		return mServerDevice;
	}

	public void setServerDevice(WifiP2pDevice serverDevice) {
		this.mServerDevice = serverDevice;
	}

	public String getServerSsid() {
		return mServerSSID;
	}

	public void setServerSsid(String serverSsid) {
		this.mServerSSID = serverSsid;
	}

	@Override
	public String toString() {
		return "ServerInfo [mServerName=" + mServerName + ", mServerType="
				+ mServerType + ", mServerIp=" + mServerIp + ", mServerDevice="
				+ mServerDevice + ", mServerSSID=" + mServerSSID + "]";
	}
}
