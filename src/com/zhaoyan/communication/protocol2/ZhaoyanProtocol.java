package com.zhaoyan.communication.protocol2;

import android.content.Context;

import com.zhaoyan.communication.SocketCommunication;

public class ZhaoyanProtocol {
	private BaseProtocol mBaseProtocol;
	private Context mContext;

	public ZhaoyanProtocol(Context context) {
		mContext = context;
		init();
	}

	public void init() {
		mBaseProtocol = new BaseProtocol();
		mBaseProtocol.addProtocol(new LoginProtocol(mContext));
		mBaseProtocol.addProtocol(new UserUpdateProtocol(mContext));
		mBaseProtocol.addProtocol(new FileTransportProtocol(mContext));
	}

	public void decode(byte[] msgData, SocketCommunication communication) {
		mBaseProtocol.decode(msgData, communication);
	}
}
