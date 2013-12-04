package com.zhaoyan.communication.protocol;

import android.content.Context;


/**
 * This class is used for encode the message based on protocol.
 * 
 */
public class ProtocolEncoder {

	public static byte[] encodeLoginRequest(Context context) {
		return LoginProtocol.encodeLoginRequest(context);
	}

	public static byte[] encodeSendMessageToSingle(byte[] msg, int sendUserID,
			int receiveUserID, int appID) {
		return SendProtocol.encodeSendMessageToSingle(msg, sendUserID,
				receiveUserID, appID);
	}

	public static byte[] encodeSendMessageToAll(byte[] msg, int sendUserID,
			int appID) {
		return SendProtocol.encodeSendMessageToAll(msg, sendUserID, appID);
	}

	public static void encodeUpdateAllUser(Context context) {
		LoginProtocol.encodeUpdateAllUser(context);
	}

	public static byte[] encodeSendFile(int sendUserID, int receiveUserID,
			int appID, byte[] inetAddressData, int serverPort,
			FileTransferInfo fileInfo) {
		return FileTransportProtocol.encodeSendFile(sendUserID, receiveUserID,
				appID, inetAddressData, serverPort, fileInfo);

	}
}
