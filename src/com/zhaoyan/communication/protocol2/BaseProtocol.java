package com.zhaoyan.communication.protocol2;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBBase;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;

/**
 * Decode and encode {@link PBBase}. Dispatch messages based on message type.
 * 
 */
public class BaseProtocol extends MessageDispatcher {
	private static final String TAG = "BaseProtocol";

	public boolean decode(byte[] msgData, SocketCommunication communication) {
		boolean result = false;
		try {
			PBBase pbBase = PBBase.parseFrom(msgData);
			PBType type = pbBase.getType();
			byte[] msgDataInner = pbBase.getMessage().toByteArray();

			if (dispatchMessage(type, msgDataInner, communication)) {
				Log.d(TAG, "dispatchMessage success. type = " + type);
				result = true;
			} else {
				Log.d(TAG, "dispatchMessage fail. type = " + type);
				result = false;
			}
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "decode " + e);
		}
		return result;
	}

	/**
	 * Wrap message with message type.
	 * 
	 * @param type
	 * @param message
	 * @return
	 */
	public static PBBase createBaseMessage(PBType type, Message message) {
		PBBase.Builder pbBaseBuilder = PBBase.newBuilder();
		pbBaseBuilder.setType(type);
		pbBaseBuilder.setMessage(message.toByteString());
		return pbBaseBuilder.build();
	}

}
