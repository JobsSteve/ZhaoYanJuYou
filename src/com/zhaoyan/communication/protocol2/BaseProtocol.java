package com.zhaoyan.communication.protocol2;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.protocol.gen.PBBaseProtos.PBBase;
import com.zhaoyan.communication.protocol.gen.PBBaseProtos.PBType;

public class BaseProtocol extends MessageDispatcher {
	private static final String TAG = "BaseProtocol";

	public void decode(byte[] msgData, SocketCommunication communication) {
		try {
			PBBase pbBase = PBBase.parseFrom(msgData);
			PBType type = pbBase.getType();
			byte[] msgDataInner = pbBase.getMessage().toByteArray();

			if (dispatchMessage(type, msgDataInner, communication)) {
				Log.d(TAG, "dispatchMessage success.");
			} else {
				Log.d(TAG, "dispatchMessage fail.");
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	public static PBBase createBaseMessage(PBType type, Message message) {
		PBBase.Builder pbBaseBuilder = PBBase.newBuilder();
		pbBaseBuilder.setType(type);
		pbBaseBuilder.setMessage(message.toByteString());
		return pbBaseBuilder.build();
	}

}
