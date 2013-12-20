package com.zhaoyan.communication.protocol;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.google.protobuf.InvalidProtocolBufferException;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.ProtocolCommunication;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBBase;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;
import com.zhaoyan.communication.protocol.pb.PBLogoutProtos.PBLogoutClient;
import com.zhaoyan.communication.protocol.pb.PBLogoutProtos.PBLogoutServer;

public class LogoutProtocol implements IProtocol {
	private static final String TAG = "LogoutProtocol";

	@Override
	public PBType[] getMessageTypes() {
		return new PBType[] { PBType.LOGOUT_CLIENT, PBType.LOGOUT_SERVER };
	}

	@Override
	public boolean decode(PBType type, byte[] msgData,
			SocketCommunication communication) {
		boolean result = false;
		if (type == PBType.LOGOUT_CLIENT) {
			decodeLogoutClient(msgData, communication);
			result = true;
		} else if (type == PBType.LOGOUT_SERVER) {
			decodeLogoutServer(msgData, communication);
			result = true;
		}
		return result;
	}

	public static void encodeLogoutSever(Context context) {
		UserManager userManager = UserManager.getInstance();
		User localUser = userManager.getLocalUser();

		PBLogoutServer.Builder builder = PBLogoutServer.newBuilder();
		builder.setUserId(localUser.getUserID());
		PBLogoutServer pbLogoutServer = builder.build();

		PBBase pbBase = BaseProtocol.createBaseMessage(PBType.LOGOUT_SERVER,
				pbLogoutServer);
		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		communicationManager
				.sendMessageToAllWithoutEncode(pbBase.toByteArray());
	}

	private void decodeLogoutServer(byte[] msgData,
			SocketCommunication communication) {
		Log.d(TAG, "decodeLogoutServer");
		UserManager userManager = UserManager.getInstance();
		userManager.resetLocalUser();

		SocketCommunicationManager socketCommunicationManager = SocketCommunicationManager
				.getInstance();
		socketCommunicationManager.closeAllCommunication();
	}

	public static void encodeLogoutClient(Context context) {
		UserManager userManager = UserManager.getInstance();
		User localUser = userManager.getLocalUser();

		PBLogoutClient.Builder builder = PBLogoutClient.newBuilder();
		builder.setUserId(localUser.getUserID());
		PBLogoutClient pbLogoutClient = builder.build();

		PBBase pbBase = BaseProtocol.createBaseMessage(PBType.LOGOUT_CLIENT,
				pbLogoutClient);
		try {
			User serverUser = userManager.getServer();
			SocketCommunication communication = userManager
					.getAllCommmunication().get(serverUser.getUserID());
			communication.sendMessage(pbBase.toByteArray());
		} catch (NullPointerException e) {
			Log.e(TAG, "encodeLogoutClient" + e);
		}
	}

	private void decodeLogoutClient(byte[] msgData,
			SocketCommunication communication) {
		Log.d(TAG, "decodeLogoutClient");
		UserManager userManager = UserManager.getInstance();
		boolean isLocalUserServer = UserManager.isManagerServer(userManager
				.getLocalUser());
		if (!isLocalUserServer) {
			Log.w(TAG,
					"decodeLogoutClient, only server process logout of client.");
			return;
		}

		PBLogoutClient pbLogoutClient = null;
		try {
			pbLogoutClient = PBLogoutClient.parseFrom(msgData);
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "decodeLogoutClient " + e);
		}
		if (pbLogoutClient != null) {
			int userId = pbLogoutClient.getUserId();
			Log.d(TAG, "decodeLogoutClient userId = " + userId);
			// Stop SocketCommunicaton
			SocketCommunicationManager socketCommunicationManager = SocketCommunicationManager
					.getInstance();
			SocketCommunication socketCommunication = userManager
					.getSocketCommunication(userId);
			if (socketCommunication != null) {
				socketCommunicationManager
						.stopCommunication(socketCommunication);
			}
			// Remove user from user manager.
			userManager.removeUser(userId);
			// Update user list.
			ProtocolCommunication protocolCommunication = ProtocolCommunication
					.getInstance();
			protocolCommunication.sendMessageToUpdateAllUser();
		}
	}
}
