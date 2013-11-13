package com.zhaoyan.communication.protocol;

import java.util.Arrays;

import android.util.Log;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.ArrayUtil;

/**
 * This class is used for encode and decode send message.
 * 
 */
public class SendProtocol {
	private static final String TAG = "SendProtocol";

	public interface ISendProtocolTypeSingleCallBack {
		/**
		 * Received a message.
		 * 
		 * @param sendUserID
		 * @param receiveUserID
		 * @param appID
		 * @param data
		 */
		public void onReceiveMessageSingleType(int sendUserID,
				int receiveUserID, int appID, byte[] data);
	}

	public interface ISendProtocolTypeAllCallBack {
		/**
		 * Received a message.
		 * 
		 * @param data
		 * @param appID
		 * @param sendUserID
		 */
		public void onReceiveMessageAllType(int sendUserID, int appID,
				byte[] data);
	}

	/**
	 * Send message to single protocol: </br>
	 * 
	 * [DATA_TYPE_HEADER_SEND_SINGLE][sendUser][receiveUser][appID][data]
	 * 
	 * @param msg
	 * @param sendUser
	 * @param receiveUser
	 * @param appID
	 * @return
	 */
	public static byte[] encodeSendMessageToSingle(byte[] msg, int sendUserID,
			int receiveUserID, int appID) {
		byte[] dataTypeData = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_SEND_SINGLE);
		byte[] sendUserData = ArrayUtil.int2ByteArray(sendUserID);
		byte[] receiveUserData = ArrayUtil.int2ByteArray(receiveUserID);
		byte[] appIDData = ArrayUtil.int2ByteArray(appID);
		msg = ArrayUtil.join(dataTypeData, sendUserData, receiveUserData,
				appIDData, msg);
		return msg;
	}

	/**
	 * see {@link #encodeSendMessageToSingle(byte[], User, User, int)};
	 * 
	 * @param msg
	 * @param callBack
	 */
	public static void decodeSendMessageToSingle(byte[] msg,
			ISendProtocolTypeSingleCallBack callBack) {
		int start = 0;
		int end = Protocol.SEND_USER_ID_HEADER_SIZE;
		byte[] sendUserIDData = Arrays.copyOfRange(msg, start, end);
		int sendUserID = ArrayUtil.byteArray2Int(sendUserIDData);
		Log.d(TAG, "decodeSendMessageToSingle: sendUserID = " + sendUserID);

		start = end;
		end += Protocol.SEND_USER_ID_HEADER_SIZE;
		byte[] receiveUserIDData = Arrays.copyOfRange(msg, start, end);
		int receiveUserID = ArrayUtil.byteArray2Int(receiveUserIDData);
		Log.d(TAG, "decodeSendMessageToSingle: receiveUserID = "
				+ receiveUserID);

		start = end;
		end += Protocol.SEND_APP_ID_HEADER_SIZE;
		byte[] appIDData = Arrays.copyOfRange(msg, start, end);
		int appID = ArrayUtil.byteArray2Int(appIDData);
		Log.d(TAG, "decodeSendMessageToSingle: appID = " + appID);

		start = end;
		end = msg.length;
		byte[] data = Arrays.copyOfRange(msg, start, end);
		if (callBack != null) {
			callBack.onReceiveMessageSingleType(sendUserID, receiveUserID,
					appID, data);
		} else {
			Log.e(TAG, "ISendProtocolTypeSingleCallBack is null");
		}
	}

	/**
	 * Send message to all protocol: </br>
	 * 
	 * [DATA_TYPE_HEADER_SEND_ALL][sendUser][appID][data]
	 * 
	 * @param msg
	 * @param sendUser
	 * @param appID
	 * @return
	 */
	public static byte[] encodeSendMessageToAll(byte[] msg, int sendUserID,
			int appID) {
		byte[] dataTypeData = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_SEND_ALL);
		byte[] sendUserData = ArrayUtil.int2ByteArray(sendUserID);
		byte[] appIDData = ArrayUtil.int2ByteArray(appID);
		msg = ArrayUtil.join(dataTypeData, sendUserData, appIDData, msg);
		return msg;
	}

	/**
	 * see {@link #encodeSendMessageToAll(byte[], User, int)}.
	 * 
	 * @param msg
	 * @param callBack
	 */
	public static void decodeSendMessageToAll(byte[] msg,
			ISendProtocolTypeAllCallBack callBack) {
		int start = 0;
		int end = Protocol.SEND_USER_ID_HEADER_SIZE;
		byte[] sendUserIDData = Arrays.copyOfRange(msg, start, end);
		int sendUserID = ArrayUtil.byteArray2Int(sendUserIDData);
		Log.d(TAG, "decodeSendMessageToAll: sendUserID = " + sendUserID);

		start = end;
		end += Protocol.SEND_APP_ID_HEADER_SIZE;
		byte[] appIDData = Arrays.copyOfRange(msg, start, end);
		int appID = ArrayUtil.byteArray2Int(appIDData);
		Log.d(TAG, "decodeSendMessageToAll: appID = " + appID);

		start = end;
		end = msg.length;
		byte[] data = Arrays.copyOfRange(msg, start, end);
		if (callBack != null) {
			callBack.onReceiveMessageAllType(sendUserID, appID, data);
		} else {
			Log.e(TAG, "ISendProtocolTypeAllCallBack is null");
		}
	}

}
