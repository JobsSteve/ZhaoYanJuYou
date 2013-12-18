package com.zhaoyan.communication.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;


/**
 * This class is use for search client not in Android AP network.</br>
 * 
 * Send multicast socket to all clients, and wait for client connection.</br>
 */
public class SearchClientLan implements Runnable {

	private static final String TAG = "SearchClientLan";

	// Socket for receive multicast message.
	private MulticastSocket mMulticastReceiveSocket;
	// Socket for send broadcast message.
	private DatagramSocket mSendSocket;

	private OnSearchListener mListener;
	private boolean mStopped = false;
	private boolean mStarted = false;

	private static SearchClientLan mInstance;

	private Context mContext;

	public static SearchClientLan getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SearchClientLan(context);
		}
		return mInstance;
	}

	private SearchClientLan(Context context) {
		mContext = context;
	}

	public void setOnSearchListener(OnSearchListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		try {
			mMulticastReceiveSocket = new MulticastSocket(
					Search.MULTICAST_RECEIVE_PORT);
			mMulticastReceiveSocket.setSoTimeout(Search.TIME_OUT);
		} catch (Exception e) {
			Log.e(TAG, "Create multicast socket error. " + e);
		}

		// Listen other server messages.
		joinGroup(Search.MULTICAST_IP);
		new Thread(new GetMulticastPacket()).start();

		// multicast our message.
		try {
			mSendSocket = new DatagramSocket(Search.MULTICAST_SEND_PORT);
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
		if (mSendSocket == null) {
			Log.e(TAG, "startBroadcastData() fail, mSocket is null");
			return;
		}
		try {
			mSendSocket.send(packet);
			Log.d(TAG,
					"Send broadcast ok, data = " + new String(packet.getData()));
		} catch (IOException e) {
			Log.e(TAG,
					"Send broadcast fail, data = "
							+ new String(packet.getData()) + " " + e);
		}
	}

	public void startSearch() {
		Log.d(TAG, "Start search");
		if (mStarted) {
			Log.d(TAG, "startSearch() igonre, search is already started.");
			return;
		}
		mStarted = true;

		Thread searchThread = new Thread(this);
		searchThread.start();
	}

	public void stopSearch() {
		Log.d(TAG, "Stop search.");
		mStarted = false;
		mStopped = true;
		closeSocket();

		mInstance = null;
	}

	private void closeSocket() {
		if (mSendSocket != null) {
			mSendSocket.close();
		}

		if (mMulticastReceiveSocket != null) {
			leaveGroup(Search.MULTICAST_IP);
			mMulticastReceiveSocket.close();
		}
	}

	/**
	 * Join multicast group.
	 */
	private void joinGroup(String broadcastIP) {
		try {
			InetAddress groupAddr = InetAddress.getByName(broadcastIP);
			// Join broadcast group.
			mMulticastReceiveSocket.joinGroup(groupAddr);
		} catch (Exception e) {
			Log.e(TAG, "Join group fail");
		}
	}

	/**
	 * Leave multicast group.
	 */
	private void leaveGroup(String broadcastIP) {
		try {
			InetAddress groupAddr = InetAddress.getByName(broadcastIP);
			// leave broadcast group.
			mMulticastReceiveSocket.leaveGroup(groupAddr);
		} catch (Exception e) {
			Log.e(TAG, "leave group fail");
		}
	}

	/**
	 * Get message from other server.
	 * 
	 */
	class GetMulticastPacket implements Runnable {

		public void run() {
			DatagramPacket inPacket;

			while (!mStopped) {
				inPacket = new DatagramPacket(new byte[1024], 1024);
				try {
					mMulticastReceiveSocket.receive(inPacket);
					byte[] data = inPacket.getData();
					SearchProtocol.decodeSearchLan(data, mListener);
				} catch (Exception e) {
					if (e instanceof SocketTimeoutException) {
						// time out, search again.
						Log.d(TAG, "GetPacket time out. search again.");
					} else {
						Log.e(TAG, "GetPacket error," + e.toString());
						if (mListener != null) {
							mListener.onSearchStop();
						}
					}
				}
			}
		}
	}
}
