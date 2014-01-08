package com.zhaoyan.communication;

import java.net.Socket;

import android.annotation.SuppressLint;
import android.content.Context;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketServer.OnClientConnectedListener;

/**
 * This class is a AsyncTask, used for creating server socket and accept client
 * socket connection.</br>
 * 
 * After connected server, start communication with client socket.</br>
 * 
 */
@SuppressLint("UseValueOf")
public class SocketServerTask extends Thread implements
		OnClientConnectedListener {
	private static final String TAG = "SocketServerTask";

	public interface OnClientConnectedListener {
		/**
		 * A client connected.
		 * 
		 * @param clientSocket
		 */
		void onClientConnected(Socket clientSocket);
	}

	private OnClientConnectedListener mOnClientConnectedListener;

	private Notice notice;
	private SocketServer server;
	private int mSocketPort;

	public SocketServerTask(Context context, int port) {
		server = SocketServer.getInstance();
		notice = new Notice(context);
		mSocketPort = port;
	}

	public void setOnClientConnectedListener(OnClientConnectedListener listener) {
		mOnClientConnectedListener = listener;
	}

	@Override
	public void run() {
		Log.d(TAG, "run started.");
		if (server.isServerStarted()) {
			notice.showToast("Server is already started");
		}

		server.startServer(mSocketPort, this);
		Log.d(TAG, "run finished.");
	}

	@Override
	public Socket onClientConnected(Socket socket) {
		if (socket != null) {
			notice.showToast("Client connected.");
			if (mOnClientConnectedListener != null) {
				mOnClientConnectedListener.onClientConnected(socket);
			}
		}
		return socket;
	}

}
