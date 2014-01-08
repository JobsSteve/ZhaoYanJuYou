package com.zhaoyan.communication.search;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketPort;

public class SendServerInfoSocket {
	private static final String TAG = "ServerInfoSocket";
	/** Never time out */
	private final int TIME_OUT = 0;
	private boolean mIsServerStarted = false;
	private WaitForClientThread mWaitForClientThread;
	private static SendServerInfoSocket mInstance;

	private SendServerInfoSocket() {

	}

	public static synchronized SendServerInfoSocket getInstance() {
		if (mInstance == null) {
			mInstance = new SendServerInfoSocket();
		}
		return mInstance;
	}

	/**
	 * start server. return the client socket by callback interface
	 * #OnClientConnectedListener.
	 * 
	 * @param port
	 *            server port number.
	 * @param callback
	 *            interface.
	 * @return
	 */
	public void startServer(Context context) {
		if (mIsServerStarted) {
			Log.d(TAG, "startServer ignore, server is already started.");
			return;
		}
		Log.d(TAG, "startServer()");
		mIsServerStarted = true;
		if (mWaitForClientThread != null) {
			mWaitForClientThread.stopServer();
		}

		mWaitForClientThread = new WaitForClientThread(context);
		mWaitForClientThread.start();
	}

	private void sendServerInfo(Context context, Socket socket) {
		Log.d(TAG, "sendServerInfo to "
				+ socket.getInetAddress().getHostAddress());
		Thread sendThread = new SendServerInfoToClientThread(context, socket);
		sendThread.start();
	}

	public boolean isServerStarted() {
		return mIsServerStarted;
	}

	public void stopServer() {
		if (!mIsServerStarted) {
			Log.d(TAG, "stopServer ignore, server is not started.");
			return;
		}
		Log.d(TAG, "stopServer()");
		mIsServerStarted = false;
		if (mWaitForClientThread != null) {
			mWaitForClientThread.stopServer();
			mWaitForClientThread = null;
		}
	}

	public void release() {
		mInstance = null;
	}

	private class WaitForClientThread extends Thread {
		private Context mContext;
		private ServerSocket mServerSocket;
		private boolean mStop = false;

		public WaitForClientThread(Context context) {
			mContext = context;
		}

		public void stopServer() {
			mStop = true;
			if (mServerSocket != null) {
				try {
					mServerSocket.close();
					mServerSocket = null;
				} catch (IOException e) {
					Log.e(TAG, "WaitForClientThread stop server" + e);
				}
			}
		}

		@Override
		public void run() {
			if (mServerSocket == null) {
				try {
					mServerSocket = new ServerSocket(
							SocketPort.SEARCH_SERVER_INFO_PORT);
				} catch (IOException e) {
					Log.e(TAG, "WaitForClientThread startServer " + e);
					return;
				}
			}
			while (!mStop) {
				mIsServerStarted = true;
				try {
					mServerSocket.setSoTimeout(TIME_OUT);
					Log.d(TAG,
							"WaitForClientThread waiting for client connect.");
					Socket socket = mServerSocket.accept();
					sendServerInfo(mContext, socket);
				} catch (IOException e) {
					if (!mStop) {
						Log.e(TAG, "WaitForClientThread startServer " + e);
					}
					break;
				}
			}
		}

	}

	private class SendServerInfoToClientThread extends Thread {
		private Context mContext;
		private Socket mSocket;

		public SendServerInfoToClientThread(Context context, Socket socket) {
			mSocket = socket;
			mContext = context;
		}

		@Override
		public void run() {
			try {
				DataOutputStream out = new DataOutputStream(
						mSocket.getOutputStream());
				sendServerInfoData(mContext, out);
				out.close();
				mSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "SendServerInfoThread " + e);
			}

		}

	}

	private void sendServerInfoData(Context context, DataOutputStream out) {
		SearchProtocol.encodeLanServerInfo(context, out);
	}
}
