package com.zhaoyan.communication.connect;

import com.zhaoyan.communication.SocketCommunicationManager;

import android.content.Context;

public class ServerConnectorLan {
	private Context mContext;

	public ServerConnectorLan(Context context) {
		mContext = context;
	}

	public void connectServer(String serverIp) {
		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		communicationManager.connectServer(mContext, serverIp);
	}
}
