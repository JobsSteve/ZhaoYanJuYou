package com.zhaoyan.communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is use for creating server socket, and getting all connected
 * client sockets
 * 
 */
public class SocketServer {

	/**
	 * Call back to get client socket.
	 * 
	 */
	public interface OnClientConnectedListener {
		/**
		 * A new client socket is connected.
		 * 
		 * @param socket
		 * @return
		 */
		public Socket onClientConnected(Socket socket);
	}

	/** Never time out */
	private final int TIME_OUT = 0;
	private Socket socket;
	private ServerSocket server;
	private boolean mIsServerStarted = false;
	private static SocketServer mInstance;

	private SocketServer() {

	}

	public static synchronized SocketServer getInstance() {
		if (mInstance == null) {
			mInstance = new SocketServer();
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
	public Socket startServer(int port, OnClientConnectedListener listener) {
		if (mIsServerStarted) {
			return null;
		}
		if (server == null) {
			try {
				server = new ServerSocket(port);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		while (true) {
			mIsServerStarted = true;
			try {
				if (server != null) {
					server.setSoTimeout(TIME_OUT);
					socket = server.accept();
					if (socket != null) {
						listener.onClientConnected(socket);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}
		mIsServerStarted = false;
		return null;
	}

	public boolean isServerStarted() {
		return mIsServerStarted;
	}

	/** branch liucheng_1 */
	public void stopServer() {
		if (server != null) {
			try {
				server.close();
				server = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void release() {
		mInstance = null;
	}
}