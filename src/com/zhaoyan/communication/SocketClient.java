package com.zhaoyan.communication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.zhaoyan.common.util.Log;


/**
 * This class is used for creating client socket to connect server socket.
 * 
 */
public class SocketClient {
	private static final String TAG = "SocketClient";
	private Socket socket;

	public Socket startClient(String host, int port) {
		try {
			socket = new Socket(host, port);

			return socket;
		} catch (UnknownHostException e) {
			Log.e(TAG, "Connect server fail. server: " + host + ", port: " + port + ". " + e);
		} catch (IOException e) {
			Log.e(TAG, "Connect server fail. server: " + host + ", port: " + port + ". " + e);
		}
		return null;
	}

}