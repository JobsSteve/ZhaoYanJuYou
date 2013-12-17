package com.zhaoyan.communication.protocol2;

import java.util.Map;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBBase;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;
import com.zhaoyan.communication.protocol.pb.PBSendMessageProtos.PBSendMessageToAll;
import com.zhaoyan.communication.protocol.pb.PBSendMessageProtos.PBSendMessageToSingle;

/**
 * This class is used for encode and decode send message.
 * 
 * @see PBSendMessageProtos
 */
public class MessageSendProtocol implements IProtocol {
	private static final String TAG = "SendProtocol";

	public MessageSendProtocol(Context context) {
	}

	@Override
	public PBType[] getMessageTypes() {
		return new PBType[] { PBType.SEND_MESSAGE_TO_SINGLE,
				PBType.SEND_MESSAGE_TO_ALL };
	}

	@Override
	public boolean decode(PBType type, byte[] msgData,
			SocketCommunication communication) {
		boolean result = true;
		if (type == PBType.SEND_MESSAGE_TO_SINGLE) {
			decodeSendMessageToSingle(msgData, communication);
		} else if (type == PBType.SEND_MESSAGE_TO_ALL) {
			decodeSendMessageToAll(msgData, communication);
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Encode and send the message to the receiver user.
	 * 
	 * @param msg
	 * @param sendUserID
	 * @param receiveUserID
	 * @param appID
	 * @see PBSendMessageToSingle
	 */
	public static void encodeSendMessageToSingle(byte[] msg, int sendUserID,
			int receiveUserID, int appID) {
		PBSendMessageToSingle.Builder builder = PBSendMessageToSingle
				.newBuilder();
		builder.setSendUserId(sendUserID);
		builder.setReceiveUserId(receiveUserID);
		builder.setAppId(appID);
		builder.setData(ByteString.copyFrom(msg));
		PBSendMessageToSingle message = builder.build();
		PBBase pbBase = BaseProtocol.createBaseMessage(
				PBType.SEND_MESSAGE_TO_SINGLE, message);

		UserManager userManager = UserManager.getInstance();
		SocketCommunication communication = userManager.getAllCommmunication()
				.get(receiveUserID);
		if (communication != null) {
			communication.sendMessage(pbBase.toByteArray());
		} else {
			Log.e(TAG,
					"encodeSendMessageToSingle fail. communication not found. receive user id = "
							+ receiveUserID);
		}
	}

	/**
	 * 
	 * @param msgData
	 * @param communication
	 * @see #encodeSendMessageToSingle(byte[], int, int, int)
	 */
	private void decodeSendMessageToSingle(byte[] msgData,
			SocketCommunication communication) {
		PBSendMessageToSingle message = null;
		try {
			message = PBSendMessageToSingle.parseFrom(msgData);
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "decodeSendMessageToSingle " + e);
		}

		if (message != null) {
			int sendUserId = message.getSendUserId();
			int receiveUserId = message.getReceiveUserId();
			int appId = message.getAppId();
			byte[] data = message.getData().toByteArray();
			Log.d(TAG, "onReceiveMessageSingleType senUserId = " + sendUserId
					+ ", receiveUserId = " + receiveUserId + ", appId = "
					+ appId);

			SocketCommunicationManager communicationManager = SocketCommunicationManager
					.getInstance();
			UserManager userManager = UserManager.getInstance();
			User localUser = userManager.getLocalUser();

			if (receiveUserId == localUser.getUserID()) {
				Log.d(TAG, "This message is for me");
				communicationManager.notifyReceiveListeners(sendUserId, appId,
						data);
			} else {
				Log.d(TAG, "This message is not for me");
				SocketCommunication destinationCommunication = userManager
						.getSocketCommunication(receiveUserId);
				if (destinationCommunication != null) {
					PBBase pbBase = BaseProtocol.createBaseMessage(
							PBType.SEND_MESSAGE_TO_SINGLE, message);
					communication.sendMessage(pbBase.toByteArray());
				} else {
					Log.e(TAG,
							"decodeSendMessageToSingle error, destination communication not found.");
				}
			}
		}
	}

	/**
	 * Encode and send the message to all user in the network.
	 * 
	 * @param msg
	 * @param sendUserID
	 * @param appID
	 * @see PBSendMessageToAll
	 */
	public static void encodeSendMessageToAll(byte[] msg, int sendUserID,
			int appID) {
		PBSendMessageToAll.Builder builder = PBSendMessageToAll.newBuilder();
		builder.setSendUserId(sendUserID);
		builder.setAppId(appID);
		builder.setData(ByteString.copyFrom(msg));
		PBSendMessageToAll message = builder.build();
		PBBase baseMessage = BaseProtocol.createBaseMessage(
				PBType.SEND_MESSAGE_TO_ALL, message);

		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		communicationManager.sendMessageToAllWithoutEncode(baseMessage
				.toByteArray());
	}

	/**
	 * 
	 * @param msg
	 * @param communication
	 * @see #encodeSendMessageToAll(byte[], int, int)
	 */
	private void decodeSendMessageToAll(byte[] msg,
			SocketCommunication communication) {
		PBSendMessageToAll message = null;
		try {
			message = PBSendMessageToAll.parseFrom(msg);
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "decodeSendMessageToAll " + e);
		}

		if (message != null) {
			int sendUserID = message.getSendUserId();
			int appID = message.getAppId();
			byte[] data = message.getData().toByteArray();
			Log.d(TAG, "decodeSendMessageToAll sendUserID = " + sendUserID
					+ ", appID = " + appID);

			SocketCommunicationManager communicationManager = SocketCommunicationManager
					.getInstance();
			UserManager userManager = UserManager.getInstance();
			if (sendUserID == userManager.getLocalUser().getUserID()) {
				// When the client connected to the server but is not login
				// success, the client can receive the message, and the client
				// will send the message to other communications which user ID
				// is not the same as the message from. So the client will send
				// the message back to the server. So this message should
				// ignore.
				// TODO This condition should avoid in the future.
				Log.d(TAG,
						"onReceiveMessageAllType, This message is sent by me, ignore.");
				return;
			}
			// Notify me.
			communicationManager
					.notifyReceiveListeners(sendUserID, appID, data);
			// Send this message to others.
			PBBase baseMessage = BaseProtocol.createBaseMessage(
					PBType.SEND_MESSAGE_TO_ALL, message);
			byte[] baseMessageData = baseMessage.toByteArray();
			Map<Integer, SocketCommunication> communications = userManager
					.getAllCommmunication();
			for (int id : communications.keySet()) {
				if (id != sendUserID) {
					communications.get(id).sendMessage(baseMessageData);
				} else {
					// Ignore, the communication is the message comes from.
				}
			}
		}
	}
}
