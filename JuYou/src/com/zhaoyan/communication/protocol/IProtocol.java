package com.zhaoyan.communication.protocol;

import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;

public interface IProtocol {
	/**
	 * Get the message types which the protocol support.
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
	 * @return decode result. If decode success, return true, else false should
	 *         be returned.
	 */
	boolean decode(PBType type, byte[] msgData,
			SocketCommunication communication);
}
