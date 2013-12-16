package com.zhaoyan.communication.protocol2;

import java.io.File;
import java.net.InetAddress;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.protocol.FileTransferInfo;
import com.zhaoyan.communication.protocol.gen.PBBaseProtos.PBBase;
import com.zhaoyan.communication.protocol.gen.PBBaseProtos.PBType;
import com.zhaoyan.communication.protocol.gen.PBFileTransportProtos.PBSendFile;

public class FileTransportProtocol implements IProtocol {
	private static final String TAG = "FileTransportProtocol";
	private Context mContext;

	public FileTransportProtocol(Context context) {
		mContext = context;
	}

	@Override
	public PBType[] getMessageTypes() {
		PBType[] types = new PBType[] { PBType.FILE_TRANSPORT };
		return types;
	}

	@Override
	public void decode(PBType type, byte[] msgData,
			SocketCommunication communication) {
		Log.d(TAG, "decode type = " + type);
		if (type == PBType.FILE_TRANSPORT) {
			decodeFileTransport(msgData, communication);
		}
	}

	public static void encodeSendFile(User receiveUser, int appID,
			int serverPort, File file, Context context) {
//		Log.d(TAG, "encodeSendFile");
//		SocketCommunicationManager communicationManager = SocketCommunicationManager
//				.getInstance();
//		UserManager userManager = UserManager.getInstance();
//		User localUser = userManager.getLocalUser();
//
//		InetAddress inetAddress = NetWorkUtil.getLocalInetAddress();
//		if (inetAddress == null) {
//			Log.e(TAG,
//					"sendFile error, get inet address fail. file = "
//							+ file.getName());
//			return;
//		}
//		byte[] inetAddressBytes = inetAddress.getAddress();
//
//		PBSendFile.Builder sendFileBuilder = PBSendFile.newBuilder();
//		sendFileBuilder.setSendUserId(localUser.getUserID());
//		sendFileBuilder.setRecieveUserId(receiveUser.getUserID());
//		sendFileBuilder.setAppId(appID);
//		sendFileBuilder.setServerAddress(ByteString.copyFrom(inetAddressBytes));
//		sendFileBuilder.setServerPort(serverPort);
//
//		FileTransferInfo fileTransferInfo = new FileTransferInfo(file, context);
//		sendFileBuilder.setFileInfo(fileTransferInfo.getPBFileInfo());
//		PBSendFile pbSendFile = sendFileBuilder.build();
//
//		PBBase pbBase = BaseProtocol.createBaseMessage(PBType.FILE_TRANSPORT,
//				pbSendFile);
//		communicationManager.sendMessageToSingleWithoutEncode(
//				pbBase.toByteArray(), receiveUser);
	}

	private void decodeFileTransport(byte[] msgData,
			SocketCommunication communication) {
		PBSendFile pbSendFile = null;
		try {
			pbSendFile = PBSendFile.parseFrom(msgData);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		if (pbSendFile == null) {
			return;
		}

		handleReceivedFile(pbSendFile, communication);
	}

	private void handleReceivedFile(PBSendFile pbSendFile,
			SocketCommunication communication) {
//		Log.d(TAG, "onReceiveFile pbSendFile = " + pbSendFile);
//		UserManager userManager = UserManager.getInstance();
//		SocketCommunicationManager communicationManager = SocketCommunicationManager
//				.getInstance();
//		User localUser = userManager.getLocalUser();
//		int sendUserID = pbSendFile.getSendUserId();
//		int receiveUserID = pbSendFile.getRecieveUserId();
//		int appID = pbSendFile.getAppId();
//		byte[] serverAddress = pbSendFile.getServerAddress().toByteArray();
//		int serverPort = pbSendFile.getServerPort();
//		FileTransferInfo fileInfo = new FileTransferInfo(
//				pbSendFile.getFileInfo(), mContext);
//		if (pbSendFile.getRecieveUserId() == localUser.getUserID()) {
//			Log.d(TAG, "onReceiveFile This file is for me");
//			communicationManager.notfiyFileReceiveListeners(sendUserID, appID,
//					serverAddress, serverPort, fileInfo);
//		} else {
//			Log.d(TAG, "onReceiveFile This file is not for me");
//			PBBase pbBase = BaseProtocol.createBaseMessage(
//					PBType.FILE_TRANSPORT, pbSendFile);
//			communicationManager.sendMessageToSingleWithoutEncode(
//					pbBase.toByteArray(),
//					userManager.getAllUser().get(receiveUserID));
//		}
	}
}
