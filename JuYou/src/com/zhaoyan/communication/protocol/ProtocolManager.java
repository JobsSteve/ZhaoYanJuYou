package com.zhaoyan.communication.protocol;

import android.content.Context;
import android.util.Log;

import com.zhaoyan.communication.SocketCommunication;

/**
 * Manage all protocols.
 * 
 */
public class ProtocolManager {
	private static final String TAG = "ProtocolManager";
	private BaseProtocol mBaseProtocol;
	private Context mContext;

	public ProtocolManager(Context context) {
		mContext = context;
	}

	public void init() {
		Log.d(TAG, "init()");
		mBaseProtocol = new BaseProtocol();
		mBaseProtocol.addProtocol(new LoginProtocol(mContext));
		mBaseProtocol.addProtocol(new UserUpdateProtocol(mContext));
		mBaseProtocol.addProtocol(new MessageSendProtocol(mContext));
		mBaseProtocol.addProtocol(new FileTransportProtocol(mContext));
		mBaseProtocol.addProtocol(new LogoutProtocol());
	}

	/**
	 * Decode the message.
	 * 
	 * @param msgData
	 * @param communication
	 * @return decode result whether the message is decoded.
	 */
	public boolean decode(byte[] msgData, SocketCommunication communication) {
		boolean result = mBaseProtocol.decode(msgData, communication);
		return result;
	}
}
