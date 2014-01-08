package com.zhaoyan.communication.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.util.Log;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;


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
public class SearchSeverLanAndroidAP implements Runnable {

	private static final String TAG = "SearchSeverLanAndroidAP";

	// Socket for receive server respond.
	private DatagramSocket mReceiveRespondSocket;

	// Address for send search request.
	private InetAddress mSendRequestAddress;
	// Socket for send search request.
	private DatagramSocket mSendRequestSocket;
	private DatagramPacket mSendRequestPacket;

	private OnSearchListener mListener;

	private boolean mStopped = false;
	private boolean mStarted = false;

	private static SearchSeverLanAndroidAP mInstance;

	private Context mContext;

	public static SearchSeverLanAndroidAP getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SearchSeverLanAndroidAP(context);
		}
		return mInstance;
	}

	private SearchSeverLanAndroidAP(Context context) {
		mContext = context;
	}

	public void setOnSearchListener(OnSearchListener listener) {
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
		Log.d(TAG, "Start search.");
		if (mStarted) {
			Log.d(TAG, "startSearch() ignore, search is already started.");
			return;
		}
		mStarted = true;
		Thread searchThread = new Thread(this);
		searchThread.start();
	}

	public void stopSearch() {
		Log.d(TAG, "Stop search");
		mStarted = false;
		mStopped = true;

		closeSocket();

		mInstance = null;
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
		new Thread(new GetPacket()).start();
	}

	/**
	 * Get message from server.
	 * 
	 */
	class GetPacket implements Runnable {
		public void run() {
			DatagramPacket inPacket;

			String message;
			while (!mStopped) {
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
							mListener.onSearchSuccess(inPacket.getAddress()
									.getHostAddress(), message
									.substring(Search.ANDROID_AP_SERVER_RESPOND
											.length()));
						}
					} else if (message
							.startsWith(Search.ANDROID_AP_SERVER_REQUEST)) {
						Log.d(TAG, "This client is an AP. Found a server.");
						if (mListener != null) {
							mListener.onSearchSuccess(inPacket.getAddress()
									.getHostAddress(), message
									.substring(Search.ANDROID_AP_SERVER_REQUEST
											.length()));
						}
					}
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
