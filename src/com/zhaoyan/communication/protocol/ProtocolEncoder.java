package com.zhaoyan.communication.protocol;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.UserManager;

/**
 * This class is used for encode the message based on protocol.
 * 
 */
public class ProtocolEncoder {

	public static byte[] encodeLoginRequest(User localUser) {
		return LoginProtocol.encodeLoginRequest(localUser);
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

	public static byte[] encodeUpdateAllUser(UserManager userManager) {
		return LoginProtocol.encodeUpdateAllUser(userManager);
	}

	public static byte[] encodeSendFile(int sendUserID, int receiveUserID,
			int appID, byte[] inetAddressData, int serverPort, FileTransferInfo fileInfo) {
		return FileTransportProtocol.encodeSendFile(sendUserID, receiveUserID,
				appID, inetAddressData, serverPort, fileInfo);

	}
}
