package com.zhaoyan.communication.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.util.Log;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.communication.search.ServerSearcherLan.OnSearchListenerLan;

/**
 * This class is use for search server in WiFi network in which the AP is
 * android WiFi hot access point.</br>
 * 
 * This class is single instance, So use {@link #getInstance(Context)} to get
 * object.</br>
 * 
 * After started, we send UDP packet to android WiFi hot AP which IP address is
 * 192.168.43.1 to confirm it is server or not. If the android WiFi hot AP
 * responds that it is a server, we found a server. </br>
 * 
 */
public class ServerSearcherLanAndroidAP implements Runnable {

	private static final String TAG = "SearchSeverLanAndroidAP";

	// Socket for receive server respond.
	private DatagramSocket mReceiveRespondSocket;

	// Address for send search request.
	private InetAddress mSendRequestAddress;
	// Socket for send search request.
	private DatagramSocket mSendRequestSocket;
	private DatagramPacket mSendRequestPacket;
	private GetPacket mGetPacket;

	private OnSearchListenerLan mListener;

	private boolean mStopped = false;
	private boolean mStarted = false;

	private Context mContext;

	public ServerSearcherLanAndroidAP(Context context) {
		mContext = context;
	}

	public void setOnSearchListener(OnSearchListenerLan listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		try {
			mReceiveRespondSocket = new DatagramSocket(
					Search.ANDROID_AP_RECEIVE_PORT);
			mReceiveRespondSocket.setSoTimeout(Search.TIME_OUT);
			// request ip
			mSendRequestAddress = InetAddress
					.getByName(Search.ANDROID_AP_ADDRESS);
			mSendRequestSocket = new DatagramSocket();
			// request data
			byte[] request = Search.ANDROID_AP_CLIENT_REQUEST.getBytes();
			mSendRequestPacket = new DatagramPacket(request, request.length,
					mSendRequestAddress, Search.ANDROID_AP_RECEIVE_PORT);
		} catch (Exception e) {
			Log.e(TAG, "SearchSeverAPMode error" + e);
		}

		startListenServerMessage();

		// Send search request to find Android AP server.
		if (!NetWorkUtil.isWifiApEnabled(mContext)) {
			while (!mStopped) {
				sendSearchRequest();
				try {
					Thread.sleep(Search.ANDROID_AP_SEARCH_DELAY);
				} catch (InterruptedException e) {
					Log.e(TAG, "InterruptedException " + e);
				}
			}
		}
	}

	private void sendSearchRequest() {
		if (mSendRequestSocket != null) {
			try {
				mSendRequestSocket.send(mSendRequestPacket);
				Log.d(TAG, "Send broadcast ok, data = "
						+ new String(mSendRequestPacket.getData()));
			} catch (IOException e) {
				Log.e(TAG, "Send broadcast fail, data = "
						+ new String(mSendRequestPacket.getData()) + " " + e);
			}
		} else {
			Log.e(TAG, "sendSearchRequest() fail, mSendRequestSocket is null");
		}
	}

	public void startSearch() {
		if (mStarted) {
			Log.d(TAG, "startSearch() ignore, search is already started.");
			return;
		}
		Log.d(TAG, "Start search.");
		mStarted = true;
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
		mStopped = true;

		if (mGetPacket != null) {
			mGetPacket.stop();
			mGetPacket = null;
		}

		closeSocket();
	}

	private void closeSocket() {
		if (mReceiveRespondSocket != null) {
			mReceiveRespondSocket.close();
			mReceiveRespondSocket = null;
		}
		if (mSendRequestSocket != null) {
			mSendRequestSocket.close();
			mSendRequestSocket = null;
		}
	}

	/**
	 * Listen server message.
	 */
	private void startListenServerMessage() {
		if (mGetPacket != null) {
			mGetPacket.stop();
		}
		mGetPacket = new GetPacket();
		new Thread(mGetPacket).start();
	}

	/**
	 * Get message from server.
	 * 
	 */
	class GetPacket implements Runnable {
		private boolean mStop;

		public void stop() {
			mStop = true;
		}

		@Override
		public void run() {
			DatagramPacket inPacket;

			String message;
			while (!mStop) {
				try {
					inPacket = new DatagramPacket(new byte[1024], 1024);
					mReceiveRespondSocket.receive(inPacket);
					message = new String(inPacket.getData(), 0,
							inPacket.getLength());
					Log.d(TAG, "Received broadcast, message: " + message);

					if (message.startsWith(Search.ANDROID_AP_SERVER_RESPOND)) {
						// Android AP is server.
						Log.d(TAG, "Android AP is server.");
						if (mListener != null) {
							mListener.onFoundLanServer(inPacket.getAddress()
									.getHostAddress());
						}
					} else if (message
							.startsWith(Search.ANDROID_AP_SERVER_REQUEST)) {
						Log.d(TAG, "This client is an AP. Found a server.");
						if (mListener != null) {
							mListener.onFoundLanServer(inPacket.getAddress()
									.getHostAddress());
						}
					}
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
