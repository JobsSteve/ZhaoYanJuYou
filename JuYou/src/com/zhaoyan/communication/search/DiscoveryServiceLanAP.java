package com.zhaoyan.communication.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.content.Context;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.UserManager;

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
 * case of there are other servers<br>
 * 
 */
public class DiscoveryServiceLanAP {
	private static final String TAG = "SearchClientLanAndroidAP";

	private SendMessageToAPThread mSendMessageToAPThread;
	private TellClientThisIsServerThread mTellClientThisIsServerThread;

	private boolean mStarted = false;
	private static DiscoveryServiceLanAP mInstance;
	private Context mContext;

	public static DiscoveryServiceLanAP getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DiscoveryServiceLanAP(context);
		}
		return mInstance;
	}

	private DiscoveryServiceLanAP(Context context) {
		mContext = context;
	}

	public void startSearch() {
		if (mStarted) {
			Log.d(TAG, "startSearch() igonre, search is already started.");
			return;
		}
		Log.d(TAG, "Start search");
		mStarted = true;

		if (!NetWorkUtil.isWifiApEnabled(mContext)) {
			// In Android soft AP network. And this server is a STA.
			Log.d(TAG, "In Android soft AP network. And this server is a STA");
			if (mSendMessageToAPThread != null) {
				mSendMessageToAPThread.quit();
			}
			mSendMessageToAPThread = new SendMessageToAPThread();
			mSendMessageToAPThread.start();
		} else {
			// In Android soft AP network. And this server is a AP.
			if (mTellClientThisIsServerThread != null) {
				mTellClientThisIsServerThread.quit();
			}

			mTellClientThisIsServerThread = new TellClientThisIsServerThread();
			mTellClientThisIsServerThread.start();
		}
	}

	public void stopSearch() {
		if (!mStarted) {
			Log.d(TAG, "stopSearch() igonre, search is not started.");
			return;
		}
		Log.d(TAG, "Stop search.");
		mStarted = false;
		closeSocket();

		mInstance = null;
	}

	private void closeSocket() {
		if (mTellClientThisIsServerThread != null) {
			mTellClientThisIsServerThread.quit();
			mTellClientThisIsServerThread = null;
		}
		if (mSendMessageToAPThread != null) {
			mSendMessageToAPThread.quit();
			mSendMessageToAPThread = null;
		}
	}

	/**
	 * Respond client search request, and tell it that "Yes, I am the server you
	 * find."
	 */
	class TellClientThisIsServerThread extends Thread {
		private boolean mStop = false;
		private DatagramSocket mSocket;

		public TellClientThisIsServerThread() {
			try {
				mSocket = new DatagramSocket(Search.ANDROID_AP_RECEIVE_PORT);
			} catch (SocketException e) {
				Log.e(TAG, "TellClientThisIsServerThread create socket fail. "
						+ e);
			}
		}

		public void run() {
			DatagramPacket inPacket;
			String message;
			Log.d(TAG, "GetClientPacket started. Waiting for client...");
			while (!mStop) {
				if (mSocket == null) {
					break;
				}
				try {
					inPacket = new DatagramPacket(new byte[1024], 1024);
					mSocket.receive(inPacket);
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
						Log.d(TAG, "Got another server.");
					}
				} catch (Exception e) {
					if (e instanceof SocketTimeoutException) {
						// time out, search again.
						Log.d(TAG, "GetPacket time out. search again.");
					} else {
						Log.e(TAG, "GetPacket error," + e.toString());
						break;
					}
				}
			}
		}

		public void quit() {
			mStop = true;
			if (mSocket != null) {
				mSocket.close();
				mSocket = null;
			}
		}

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
	}

	/**
	 * Send packet to Android AP to tell "I am server."; This is only used when
	 * this server is a WiFi STA.
	 */
	class SendMessageToAPThread extends Thread {
		// Socket for send packet to tell Android soft AP "I am server.";
		private DatagramSocket mSendToClientSocket;
		private DatagramPacket mSearchPacket = null;
		private boolean mStop = false;

		public SendMessageToAPThread() {
			try {
				mSendToClientSocket = new DatagramSocket();

				InetAddress androidAPAddress = InetAddress
						.getByName(Search.ANDROID_AP_ADDRESS);
				// search data like: "I am server. IP: 192.168.43.169"
				byte[] searchData = (Search.ANDROID_AP_SERVER_REQUEST + UserManager
						.getInstance().getLocalUser().getUserName()).getBytes();

				mSearchPacket = new DatagramPacket(searchData,
						searchData.length, androidAPAddress,
						Search.ANDROID_AP_RECEIVE_PORT);
			} catch (UnknownHostException e) {
				Log.e(TAG, "SendMessageToAPThread." + e);
			} catch (SocketException e) {
				Log.e(TAG, "SendMessageToAPThread." + e);
			}
		}

		@Override
		public void run() {
			while (!mStop) {
				if (mSendToClientSocket != null) {
					try {
						mSendToClientSocket.send(mSearchPacket);
					} catch (IOException e) {
						Log.e(TAG, "sendSearchRequest, data = "
								+ new String(mSearchPacket.getData()) + " " + e);
					}
				}
				try {
					Thread.sleep(Search.ANDROID_AP_SEARCH_DELAY);
				} catch (InterruptedException e) {
				}
			}
		}

		public void quit() {
			mStop = true;
			if (mSendToClientSocket != null) {
				mSendToClientSocket.close();
				mSendToClientSocket = null;
			}
		}
	}
}
