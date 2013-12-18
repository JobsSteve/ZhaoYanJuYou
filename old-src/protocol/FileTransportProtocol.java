package com.zhaoyan.communication.protocol;

import java.util.Arrays;

import com.dreamlink.communication.lib.util.ArrayUtil;

public class FileTransportProtocol {

	public static byte[] encodeSendFile(int sendUserID, int receiveUserID,
			int appID, byte[] inetAddressData, int serverPort, FileTransferInfo fileInfo) {
		byte[] result = null;
		byte[] headData = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_SEND_FILE);
		byte[] sendUserData = ArrayUtil.int2ByteArray(sendUserID);
		byte[] receiveUserData = ArrayUtil.int2ByteArray(receiveUserID);
		byte[] appIDData = ArrayUtil.int2ByteArray(appID);
		byte[] serverPortData = ArrayUtil.int2ByteArray(serverPort);
		byte[] fileInfoData = ArrayUtil.objectToByteArray(fileInfo);

		result = ArrayUtil.join(headData, sendUserData, receiveUserData,
				appIDData, inetAddressData, serverPortData, fileInfoData);
		return result;
	}

	public static void decodeSendFile(byte[] data,
			OnReceiveFileCallback callback) {
		int start = 0;
		int end = Protocol.SEND_USER_ID_HEADER_SIZE;
		// get send user ID.
		byte[] sendUserIDData = Arrays.copyOfRange(data, start, end);
		int sendUserID = ArrayUtil.byteArray2Int(sendUserIDData);

		// get receive user ID.
		start = end;
		end += Protocol.SEND_USER_ID_HEADER_SIZE;
		byte[] receiveUserIDData = Arrays.copyOfRange(data, start, end);
		int receiveUserID = ArrayUtil.byteArray2Int(receiveUserIDData);

		// get appID.
		start = end;
		end += Protocol.SEND_APP_ID_HEADER_SIZE;
		byte[] appIDData = Arrays.copyOfRange(data, start, end);
		int appID = ArrayUtil.byteArray2Int(appIDData);
		
		// get server address
		start = end;
		end += Protocol.SEND_FILE_SERVER_ADDRESS_HEAD_SIZE;
		byte[] serverAddress = Arrays.copyOfRange(data, start, end);
		
		// get server port.
		start = end;
		end += Protocol.SEND_FILE_SERVER_PORT_HEAD_SIZE;
		byte[] serverPortData = Arrays.copyOfRange(data, start, end);
		int serverPort = ArrayUtil.byteArray2Int(serverPortData);

		// get file info.
		start = end;
		end = data.length;
		byte[] fileInfoData = Arrays.copyOfRange(data, start, end);
		FileTransferInfo fileInfo = (FileTransferInfo) ArrayUtil
				.byteArrayToObject(fileInfoData);

		if (callback != null) {
			callback.onReceiveFile(sendUserID, receiveUserID, appID,
					serverAddress, serverPort, fileInfo);
		}
	}

	/**
	 * This interface is used by {@code ProtocolDecoder}.
	 *
	 */
	public interface OnReceiveFileCallback {
		/**
		 * Received a file which is sent by the user with id of sendUserID.
		 * 
		 * @param sendUserID
		 * @param receiveUserID
		 * @param appID
		 * @param serverAddress
		 * @param serverPort
		 * @param fileInfo
		 */
		void onReceiveFile(int sendUserID, int receiveUserID, int appID,
				byte[] serverAddress, int serverPort, FileTransferInfo fileInfo);
	}
}
