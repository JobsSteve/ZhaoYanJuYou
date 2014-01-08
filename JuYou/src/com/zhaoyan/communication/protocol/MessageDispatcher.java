package com.zhaoyan.communication.protocol;

import java.util.HashMap;

import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;

/**
 * Dispatch messages.
 * 
 */
public class MessageDispatcher {
	private HashMap<PBType, IProtocol> mProtocolMap = new HashMap<PBType, IProtocol>();

	public void addProtocol(IProtocol protocol) {
		for (PBType type : protocol.getMessageTypes()) {
			mProtocolMap.put(type, protocol);
		}
	}

	public IProtocol getProtocol(PBType type) {
		return mProtocolMap.get(type);
	}

	/**
	 * Dispatch message based on types.
	 * 
	 * @param type
	 * @param msgData
	 * @param communication
	 * @return
	 */
	public boolean dispatchMessage(PBType type, byte[] msgData,
			SocketCommunication communication) {
		IProtocol protocol = mProtocolMap.get(type);
		if (protocol != null) {
			protocol.decode(type, msgData, communication);
			return true;
		} else {
			return false;
		}
	}

	public PBType[] getDispatchTypes() {
		return mProtocolMap.keySet().toArray(new PBType[mProtocolMap.size()]);
	}
}
