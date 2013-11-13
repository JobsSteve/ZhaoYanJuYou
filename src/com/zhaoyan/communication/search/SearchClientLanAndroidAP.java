package com.zhaoyan.communication.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.content.Context;
import android.util.Log;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;


/**
 * This class is use for search client in Android AP network.</br>
 * 
 * There are two situation:</br>
 * 
 * 1. This server is AP.</br>
 * 
 * 2. This server is STA.</br>
 * 
 * In situation 1, just wait and receive request from client, and tell them
 * "Yes, I am server."</br>
 * 
 * In situation 2, send message to AP, tell him
 * "I am server, IP: 192.168.43.xxx". And also listen message from client in
 * cast of there are other servers<br>
 * 
 */
public class SearchClientLanAndroidAP implements Runnable {

	private static final String TAG = "SearchClientLanAndroidAP";

	// Socket for receive packet from client.
	private DatagramSocket mReceiveClientSocket;
	// Socket for send packet to tell Android soft AP "I am server.";
	private DatagramSocket mSendToClientSocket;

	private OnSearchListener mListener;
	private boolean mStopped = false;
	private boolean mStarted = false;

	private static SearchClientLanAndroidAP mInstance;

	private Context mContext;

	public static SearchClientLanAndroidAP getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SearchClientLanAndroidAP(context);
		}
		return mInstance;
	}

	private SearchClientLanAndroidAP(Context context) {
		mContext = context;
	}

	public void setOnSearchListener(OnSearchListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		if (SearchUtil.isAndroidAPNetwork(mContext)
				&& !NetWorkUtil.isWifiApEnabled(mContext)) {
			// In Android soft AP network. And this server is a STA.
			Log.d(TAG, "In Android soft AP network. And this server is a STA");
			try {
				mSendToClientSocket = new DatagramSocket();
			} catch (SocketException e) {
				Log.e(TAG, "Create SendToClientSocket error." + e);
			}

			new Thread(new SendSearchPacket()).start();
		}

		try {
			mReceiveClientSocket = new DatagramSocket(
					Search.ANDROID_AP_RECEIVE_PORT);
		} catch (SocketException e) {
			Log.e(TAG, "SearchClientAPMode create socket fail. " + e);
		}
		// Listen client request messages.
		new Thread(new GetClientPacket()).start();
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
		if (mReceiveClientSocket != null) {
			mReceiveClientSocket.close();
		}
		if (mSendToClientSocket != null) {
			mSendToClientSocket.close();
		}
	}

	/**
	 * Get packet from client request or other server packet.
	 * 
	 */
	class GetClientPacket implements Runnable {
		/**
		 * Send "Yes, I am the server you find." respond to client.
		 * 
		 * @param clientAddress
		 */
		private void sendRespond(InetAddress clientAddress) {
			Log.d(TAG, "sendRespond to " + clientAddress.getHostAddress());
			try {
				DatagramSocket socket = new DatagramSocket();
				byte[] respond = (Search.ANDROID_AP_SERVER_RESPOND + UserManager
						.getInstance().getLocalUser().getUserName()).getBytes();
				DatagramPacket inPacket = new DatagramPacket(respond,
						respond.length, clientAddress,
						Search.ANDROID_AP_RECEIVE_PORT);
				socket.send(inPacket);
				socket.close();
				Log.d(TAG, "Send respond ok.");
			} catch (SocketException e) {
				Log.e(TAG, "Send respond fail." + e);
			} catch (IOException e) {
				Log.e(TAG, "Send respond fail." + e);
			}
		}

		public void run() {
			DatagramPacket inPacket;
			String message;
			Log.d(TAG, "GetClientPacket started. Waiting for client...");
			while (!mStopped) {
				try {
					inPacket = new DatagramPacket(new byte[1024], 1024);
					mReceiveClientSocket.receive(inPacket);
					if (mReceiveClientSocket == null) {
						Log.e(TAG, "mSocket is null");
						break;
					}
					message = new String(inPacket.getData(), 0,
							inPacket.getLength());
					Log.d(TAG, "Received message: " + message);

					if (message.equals(Search.ANDROID_AP_CLIENT_REQUEST)) {
						// Got a client search request.
						Log.d(TAG, "Got a client search request");
						sendRespond(inPacket.getAddress());
					} else if (message
							.startsWith(Search.ANDROID_AP_SERVER_REQUEST)) {
						// Got another server.
						Log.d(TAG, "Go another server.");
						if (mListener != null) {
							mListener.onSearchSuccess(inPacket.getAddress()
									.getHostAddress(), "");
						}
					} else {
						// ignore
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

	/**
	 * Send packet to Android AP to tell "I am server."; This is only used when
	 * this server is a WiFi STA.
	 */
	class SendSearchPacket implements Runnable {
		DatagramPacket mSearchPacket = null;

		private void init() {
			// init search packet.
			try {
				InetAddress androidAPAddress = InetAddress
						.getByName(Search.ANDROID_AP_ADDRESS);
				// search data like: "I am server. IP: 192.168.43.169"
				byte[] searchData = (Search.ANDROID_AP_SERVER_REQUEST + UserManager
						.getInstance().getLocalUser().getUserName()).getBytes();

				mSearchPacket = new DatagramPacket(searchData,
						searchData.length, androidAPAddress,
						Search.ANDROID_AP_RECEIVE_PORT);
			} catch (UnknownHostException e) {
				Log.e(TAG, "SendSearchPacket init() fail." + e);
			}
		}

		private void sendSearchRequest() {
			if (mSendToClientSocket != null) {
				try {
					mSendToClientSocket.send(mSearchPacket);
					Log.d(TAG, "sendSearchRequest ok, data = "
							+ new String(mSearchPacket.getData()));
				} catch (IOException e) {
					Log.e(TAG, "sendSearchRequest, data = "
							+ new String(mSearchPacket.getData()) + " " + e);
				}
			} else {
				Log.e(TAG,
						"sendSearchRequest() fail, mSendRequestSocket is null");
			}
		}

		public void run() {
			init();

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
}
