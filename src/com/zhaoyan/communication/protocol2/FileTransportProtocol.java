package com.zhaoyan.communication.protocol2;

import java.io.File;
import java.net.InetAddress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.dreamlink.communication.aidl.User;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.zhaoyan.common.file.APKUtil;
import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.BitmapUtilities;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBBase;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;
import com.zhaoyan.communication.protocol.pb.PBFileTransportProtos.PBCancelReceiveFile;
import com.zhaoyan.communication.protocol.pb.PBFileTransportProtos.PBCancelSendFile;
import com.zhaoyan.communication.protocol.pb.PBFileTransportProtos.PBFileInfo;
import com.zhaoyan.communication.protocol.pb.PBFileTransportProtos.PBSendFile;

public class FileTransportProtocol implements IProtocol {
	private static final String TAG = "FileTransportProtocol";

	public FileTransportProtocol(Context context) {
	}

	@Override
	public PBType[] getMessageTypes() {
		PBType[] types = new PBType[] { PBType.FILE_TRANSPORT_SEND,
				PBType.FILE_TRANSPORT_CANCEL_SEND,
				PBType.FILE_TRANSPORT_CANCEL_RECEIVE };
		return types;
	}

	@Override
	public void decode(PBType type, byte[] msgData,
			SocketCommunication communication) {
		Log.d(TAG, "decode type = " + type);
		if (type == PBType.FILE_TRANSPORT_SEND) {
			decodeSend(msgData, communication);
		} else if (type == PBType.FILE_TRANSPORT_CANCEL_SEND) {
			decodeCancelSend(msgData, communication);
		} else if (type == PBType.FILE_TRANSPORT_CANCEL_RECEIVE) {
			decodeCancelReceive(msgData, communication);
		}
	}

	public static boolean encodeCancelReceive(User sendUser, int appID) {
		int userID = sendUser.getUserID();
		UserManager userManager = UserManager.getInstance();
		SocketCommunication communication = userManager
				.getSocketCommunication(userID);
		if (communication != null) {
			PBCancelReceiveFile.Builder builder = PBCancelReceiveFile
					.newBuilder();
			builder.setSendUserId(sendUser.getUserID());
			PBCancelReceiveFile pbCancelReceiveFile = builder.build();

			PBBase pbBase = BaseProtocol.createBaseMessage(
					PBType.FILE_TRANSPORT_CANCEL_RECEIVE, pbCancelReceiveFile);
			communication.sendMessage(pbBase.toByteArray());
			return true;
		} else {
			Log.e(TAG,
					"cancelReceiveFile fail. can not connect with the sender: "
							+ sendUser);
			return false;
		}
	}

	private void decodeCancelReceive(byte[] msgData,
			SocketCommunication communication) {
		// TODO implement later

	}

	public static boolean encodeCancelSend(User receiveUser, int appID) {
		int userID = receiveUser.getUserID();
		UserManager userManager = UserManager.getInstance();
		SocketCommunication communication = userManager
				.getSocketCommunication(userID);
		if (communication != null) {
			PBCancelSendFile.Builder builder = PBCancelSendFile.newBuilder();
			builder.setReceiveUserId(userID);
			PBCancelSendFile pbCancelSendFile = builder.build();

			PBBase pbBase = BaseProtocol.createBaseMessage(
					PBType.FILE_TRANSPORT_CANCEL_SEND, pbCancelSendFile);
			communication.sendMessage(pbBase.toByteArray());
			return true;
		} else {
			Log.e(TAG,
					"cancelSendFile fail. can not connect with the receiver: "
							+ receiveUser);
			return false;
		}
	}

	private void decodeCancelSend(byte[] msgData,
			SocketCommunication communication) {
		// TODO implement later

	}

	public static void encodeSendFile(User receiveUser, int appID,
			int serverPort, File file, Context context) {
		Log.d(TAG, "encodeSendFile");
		UserManager userManager = UserManager.getInstance();
		User localUser = userManager.getLocalUser();

		InetAddress inetAddress = NetWorkUtil.getLocalInetAddress();
		if (inetAddress == null) {
			Log.e(TAG,
					"sendFile error, get inet address fail. file = "
							+ file.getName());
			return;
		}
		byte[] inetAddressBytes = inetAddress.getAddress();

		PBSendFile.Builder sendFileBuilder = PBSendFile.newBuilder();
		sendFileBuilder.setSendUserId(localUser.getUserID());
		sendFileBuilder.setRecieveUserId(receiveUser.getUserID());
		sendFileBuilder.setAppId(appID);
		sendFileBuilder.setServerAddress(ByteString.copyFrom(inetAddressBytes));
		sendFileBuilder.setServerPort(serverPort);

		FileInfo fileInfo = new FileInfo(file, context);
		PBFileInfo pbFileInfo = fileInfo2PBFileInfo(fileInfo);
		sendFileBuilder.setFileInfo(pbFileInfo);
		PBSendFile pbSendFile = sendFileBuilder.build();

		PBBase pbBase = BaseProtocol.createBaseMessage(
				PBType.FILE_TRANSPORT_SEND, pbSendFile);

		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		communicationManager.sendMessageToSingleWithoutEncode(
				pbBase.toByteArray(), receiveUser.getUserID());
	}

	private void decodeSend(byte[] msgData, SocketCommunication communication) {
		PBSendFile pbSendFile = null;
		try {
			pbSendFile = PBSendFile.parseFrom(msgData);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		if (pbSendFile != null) {
			handleReceivedFile(pbSendFile, communication);
		}
	}

	private void handleReceivedFile(PBSendFile pbSendFile,
			SocketCommunication communication) {
		Log.d(TAG, "handleReceivedFile pbSendFile = " + pbSendFile);
		UserManager userManager = UserManager.getInstance();
		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		User localUser = userManager.getLocalUser();
		if (pbSendFile.getRecieveUserId() == localUser.getUserID()) {
			Log.d(TAG, "onReceiveFile This file is for me");
			int sendUserID = pbSendFile.getSendUserId();
			int appID = pbSendFile.getAppId();
			byte[] serverAddress = pbSendFile.getServerAddress().toByteArray();
			int serverPort = pbSendFile.getServerPort();

			FileInfo fileInfo = pbFileInfo2FileInfo(pbSendFile.getFileInfo());
			communicationManager.notfiyFileReceiveListeners(sendUserID, appID,
					serverAddress, serverPort, fileInfo);
		} else {
			Log.d(TAG, "onReceiveFile This file is not for me");
			PBBase pbBase = BaseProtocol.createBaseMessage(
					PBType.FILE_TRANSPORT_SEND, pbSendFile);
			communicationManager.sendMessageToSingleWithoutEncode(
					pbBase.toByteArray(), pbSendFile.getRecieveUserId());
		}
	}

	public static PBFileInfo fileInfo2PBFileInfo(FileInfo fileInfo) {
		PBFileInfo.Builder builder = PBFileInfo.newBuilder();
		builder.setFileName(fileInfo.mFileName);
		builder.setFilePath(fileInfo.mFilePath);
		builder.setFileSize(fileInfo.mFileSize);
		if (fileInfo.mFileIcon != null) {
			builder.setFileIcon(ByteString.copyFrom(fileInfo.mFileIcon));
		}
		return builder.build();
	}

	public static FileInfo pbFileInfo2FileInfo(PBFileInfo pbFileInfo) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.mFilePath = pbFileInfo.getFilePath();
		fileInfo.mFileName = pbFileInfo.getFileName();
		fileInfo.mFileSize = pbFileInfo.getFileSize();
		fileInfo.mFileIcon = pbFileInfo.getFileIcon().toByteArray();
		return fileInfo;
	}

	public static final class FileInfo {
		public String mFilePath;
		public String mFileName;
		public long mFileSize;
		public byte[] mFileIcon;

		public FileInfo() {

		}

		public FileInfo(File file, Context context) {
			getFileInfo(file, context);
		}

		private void getFileInfo(File file, Context context) {
			int fileType = FileManager.getFileType(context, file);
			Log.d(TAG, "getFileInfo file = " + file.getName() + ", type = "
					+ fileType);
			switch (fileType) {
			case FileManager.APK:
				getApkFileInfo(file, context);
				break;

			default:
				getFileInfoDefault(file);
				break;
			}
		}

		private void getFileInfoDefault(File file) {
			mFilePath = file.getAbsolutePath();
			mFileName = file.getName();
			mFileSize = file.length();
			mFileIcon = null;
		}

		private void getApkFileInfo(File apk, Context context) {
			mFilePath = apk.getAbsolutePath();
			mFileName = APKUtil.getApkLabel(context, apk.getAbsolutePath())
					+ ".apk";
			mFileSize = apk.length();

			Drawable icon = APKUtil.getApkIcon2(context, apk.getAbsolutePath());
			Bitmap bitmap = BitmapUtilities.drawableToBitmap(icon);
			mFileIcon = BitmapUtilities.bitmapToByteArray(bitmap);
			bitmap.recycle();
		}

		public String getFileName() {
			return mFileName;
		}

		public long getFileSize() {
			return mFileSize;
		}

		public byte[] getFileIcon() {
			return mFileIcon;
		}
	}
}
