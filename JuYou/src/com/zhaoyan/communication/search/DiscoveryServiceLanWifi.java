package com.zhaoyan.communication.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;

/**
 * This is a discovery service and WiFi AP is not Android AP.</br>
 * 
 * Send multicast socket to all clients, and wait for client connection.</br>
 */
public class DiscoveryServiceLanWifi implements Runnable {
	private static final String TAG = "DiscoveryServiceLanWifi";
	// Socket for send muticast message.
	private DatagramSocket mSendMessageSocket;

	private boolean mStopped = false;
	private boolean mStarted = false;

	@Override
	public void run() {
		// multicast our message.
		try {
			mSendMessageSocket = new DatagramSocket(Search.MULTICAST_SEND_PORT);
		} catch (SocketException e) {
			Log.e(TAG, "Create multicast packet error. " + e);
		}

		String ip = NetWorkUtil.getLocalIpAddress();
		DatagramPacket packet = getSearchPacket();

		while (!mStopped) {
			if (ip == null || ip.equals("")) {
				// illegal ip, ignore.
			} else if (ip.endsWith(NetWorkUtil.getLocalIpAddress())) {
				// ip is not changed.
				sendDataToClient(packet);
			} else {
				// ip is changed.
				ip = NetWorkUtil.getLocalIpAddress();
				packet = getSearchPacket();
				sendDataToClient(packet);
			}

			try {
				Thread.sleep(Search.MULTICAST_DELAY_TIME);
			} catch (InterruptedException e) {
				Log.e(TAG, "InterruptedException " + e);
			}
		}
	}

	/**
	 * Search packet protocol:</br>
	 * 
	 * [server ip][server name size][server name]
	 * 
	 * @return
	 */
	private DatagramPacket getSearchPacket() {
		byte[] searchMessage = SearchProtocol.encodeSearchLan();

		DatagramPacket packet = null;
		try {
			packet = new DatagramPacket(searchMessage, searchMessage.length,
					InetAddress.getByName(Search.MULTICAST_IP),
					Search.MULTICAST_RECEIVE_PORT);
		} catch (UnknownHostException e) {
			Log.e(TAG, "getSearchPacket error." + e);
		}
		return packet;
	}

	private void sendDataToClient(DatagramPacket packet) {
		if (mSendMessageSocket == null) {
			Log.e(TAG, "startBroadcastData() fail, mSocket is null");
			return;
		}
		try {
			mSendMessageSocket.send(packet);
			Log.d(TAG,
					"Send broadcast ok, data = " + new String(packet.getData()));
		} catch (IOException e) {
			Log.e(TAG,
					"Send broadcast fail, data = "
							+ new String(packet.getData()) + " " + e);
		}
	}

	public void startSearch() {
		if (mStarted) {
			Log.d(TAG, "startSearch() igonre, search is already started.");
			return;
		}
		Log.d(TAG, "Start search");
		mStarted = true;
		mStopped = false;

		Thread searchThread = new Thread(this);
		searchThread.start();
	}

	public void stopSearch() {
		if (!mStarted) {
			Log.d(TAG, "stopSearch() igonre, search is not started.");
			return;
		}
		Log.d(TAG, "Stop search.");
		mStarted = false;
		mStopped = true;
		closeSocket();
	}

	private void closeSocket() {
		if (mSendMessageSocket != null) {
			mSendMessageSocket.close();
		}
	}
}
