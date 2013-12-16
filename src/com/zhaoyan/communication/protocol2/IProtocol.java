package com.zhaoyan.communication.protocol2;

import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.protocol.gen.PBBaseProtos.PBType;

public interface IProtocol {
	/**
	 * Get the message types which this protocol supported.
	 * 
	 * @return
	 */
	PBType[] getMessageTypes();

	/**
	 * Decode the message.
	 * 
	 * @param type
	 * @param msgData
	 * @param communication
	 */
	void decode(PBType type, byte[] msgData, SocketCommunication communication);
}
