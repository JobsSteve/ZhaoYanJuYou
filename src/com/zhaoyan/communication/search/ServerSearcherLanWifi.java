package com.zhaoyan.communication.search;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.search.ServerSearcherLan.OnSearchListenerLan;

/**
 * This class is use for search server in Wifi network in which the AP is not
 * android WiFi hot access point.</br>
 * 
 * This class is single instance, So use {@link #getInstance(Context)} to get
 * object.</br>
 * 
 * After started, we listen the mulitcast message in the network. The message is
 * the server IP, so when get the message, found a server. </br>
 * 
 */
public class ServerSearcherLanWifi implements Runnable {

	private static final String TAG = "SearchServerLanWifi";

	// multicast address.
	private InetAddress mMulticastAddress;
	// Socket for receive multicast message.
	private MulticastSocket mReceiveSocket;
	// Read socket packet thread
	private GetPacket mGetPacketRunnable;

	private OnSearchListenerLan mListener;

	private boolean mStarted = false;

	private Context mContext;

	public ServerSearcherLanWifi(Context context) {
		mContext = context;
	}

	public void setOnSearchListener(OnSearchListenerLan listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		try {
			mReceiveSocket = new MulticastSocket(Search.MULTICAST_RECEIVE_PORT);
			mReceiveSocket.setSoTimeout(Search.TIME_OUT);
			mMulticastAddress = InetAddress.getByName(Search.MULTICAST_IP);
		} catch (Exception e) {
			Log.e(TAG, "Create mReceiveSocket fail." + e);
		}

		join(mMulticastAddress);
		startListenServerMessage();
	}

	public void startSearch() {
		if (mStarted) {
			Log.d(TAG, "startSearch() ignore, search is already started.");
			return;
		}
		Log.d(TAG, "Start search.");
		mStarted = true;
		NetWorkUtil.acquireWifiMultiCastLock(mContext);
		Thread searchThread = new Thread(this);
		searchThread.start();
	}

	public void stopSearch() {
		if (!mStarted) {
			Log.d(TAG, "stopSearch() ignore, search is not started.");
			return;
		}
		Log.d(TAG, "Stop search");
		mStarted = false;
		if (mGetPacketRunnable != null) {
			mGetPacketRunnable.stop();
			mGetPacketRunnable = null;
		}
		leaveGroup(mMulticastAddress);
		NetWorkUtil.releaseWifiMultiCastLock();

		closeSocket();
	}

	private void closeSocket() {
		if (mReceiveSocket != null) {
			mReceiveSocket.close();
			mReceiveSocket = null;
		}
	}

	/**
	 * Join broadcast group.
	 */
	private void join(InetAddress groupAddr) {
		try {
			// Join broadcast group.
			mReceiveSocket.joinGroup(groupAddr);
		} catch (Exception e) {
			Log.e(TAG, "Join group fail. " + e.toString());
		}
	}

	/**
	 * Leave broadcast group.
	 */
	private void leaveGroup(InetAddress groupAddr) {
		try {
			// leave broadcast group.
			mReceiveSocket.leaveGroup(groupAddr);
		} catch (Exception e) {
			Log.e(TAG, "leave group fail. " + e.toString());
		}
	}

	/**
	 * Listen server message.
	 */
	private void startListenServerMessage() {
		if (mGetPacketRunnable != null) {
			mGetPacketRunnable.stop();
		}
		mGetPacketRunnable = new GetPacket();
		new Thread(mGetPacketRunnable).start();
	}

	/**
	 * Get message from server.
	 * 
	 */
	class GetPacket implements Runnable {
		private boolean mStop = false;

		public void stop() {
			mStop = true;
		}

		@Override
		public void run() {
			DatagramPacket inPacket;

			while (!mStop) {
				try {
					inPacket = new DatagramPacket(new byte[1024], 1024);
					mReceiveSocket.receive(inPacket);
					byte[] data = inPacket.getData();

					SearchProtocol.decodeSearchLan(data, mListener);
				} catch (Exception e) {
					if (e instanceof SocketTimeoutException) {
						// time out, search again.
					} else {
						if (!mStop) {
							Log.e(TAG, "GetPacket error," + e.toString());
						}
						if (mListener != null) {
							mListener.onSearchLanStop();
						}
					}
				}
			}
		}
	}

}
