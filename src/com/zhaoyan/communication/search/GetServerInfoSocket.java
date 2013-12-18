package com.zhaoyan.communication.search;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;

import com.zhaoyan.common.util.Log;

public class GetServerInfoSocket {
	private static final String TAG = "GetServerInfoSocket";
	private Socket mSocket;
	private String mServerIp;
	private int mServerPort;

	public GetServerInfoSocket(String serverIp, int serverPort) {
		mServerIp = serverIp;
		mServerPort = serverPort;
	}

	public void getServerInfo(Context context) {
		try {
			mSocket = new Socket(mServerIp, mServerPort);
		} catch (UnknownHostException e) {
			Log.e(TAG, "getServerInfo " + e);
		} catch (IOException e) {
			Log.e(TAG, "getServerInfo " + e);
		}
		if (mSocket != null) {
			try {
				DataInputStream in = new DataInputStream(
						mSocket.getInputStream());
				processServerInfoData(context, in);
				in.close();
				mSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "getServerInfo " + e);
			}

		}
	}

	private void processServerInfoData(Context context, DataInputStream in) {
		SearchProtocol.decodeLanServerInfo(context, in, mServerIp);
	}

}
