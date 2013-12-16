package com.zhaoyan.communication.protocol;

import java.util.Arrays;
import java.util.Map;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.CallBacks.ILoginRequestCallBack;
import com.zhaoyan.communication.CallBacks.ILoginRespondCallback;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.protocol.FileTransportProtocol.OnReceiveFileCallback;
import com.zhaoyan.communication.protocol.LoginProtocol.DecodeLoginRequestForwardResult;
import com.zhaoyan.communication.protocol.LoginProtocol.DecodeLoginRespondForwardResult;
import com.zhaoyan.communication.protocol.SendProtocol.ISendProtocolTypeAllCallBack;
import com.zhaoyan.communication.protocol.SendProtocol.ISendProtocolTypeSingleCallBack;

/**
 * This class is used for decode the message based on protocol.
 * 
 */
public class ProtocolDecoder implements ISendProtocolTypeSingleCallBack,
		ISendProtocolTypeAllCallBack, ILoginRespondCallback,
		OnReceiveFileCallback {
	private static final String TAG = "ProtocolDecoder";

	private UserManager mUserManager;
	private SocketCommunicationManager mCommunicationManager;

	/**
	 * Call back for SocketCommunicationManager
	 */
	private ILoginRequestCallBack mLoginRequestCallBack;
	/**
	 * Call back for SocketCommunicationManager
	 */
	private ILoginRespondCallback mLoginRespondCallback;
	private Context mContext;

	public ProtocolDecoder(Context context) {
		mContext = context;
		mUserManager = UserManager.getInstance();
		mCommunicationManager = SocketCommunicationManager.getInstance();
	}

	public void setLoginRequestCallBack(ILoginRequestCallBack callback) {
		mLoginRequestCallBack = callback;
	}

	public void setLoginRespondCallback(ILoginRespondCallback callback) {
		mLoginRespondCallback = callback;
	}

	/**
	 * Decode message.
	 * 
	 * @param msg
	 * @param communication
	 * @return
	 */
	public byte[] decode(byte[] msg, SocketCommunication communication) {
		int dataType = ArrayUtil.byteArray2Int(Arrays.copyOfRange(msg, 0,
				Protocol.DATA_TYPE_HEADER_SIZE));
		byte[] data = Arrays.copyOfRange(msg, Protocol.DATA_TYPE_HEADER_SIZE,
				msg.length);

		switch (dataType) {
		case Protocol.DATA_TYPE_HEADER_LOGIN_REQUEST:
			Log.d(TAG, "DATA_TYPE_HEADER_LOGIN_REQUEST.");
			handleLoginRequest(data, communication);
			break;
		case Protocol.DATA_TYPE_HEADER_LOGIN_RESPOND:
			Log.d(TAG, "DATA_TYPE_HEADER_LOGIN_RESPOND");
			handleLoginRespond(data, communication);
			break;
		case Protocol.DATA_TYPE_HEADER_UPDATE_ALL_USER:
			Log.d(TAG, "DATA_TYPE_HEADER_UPDATE_ALL_USER");
			handleLoginUpdateAllUser(data, msg, communication);
			break;
		case Protocol.DATA_TYPE_HEADER_LOGIN_REQUEST_FORWARD:
			Log.d(TAG, "DATA_TYPE_HEADER_LOGIN_REQUEST_FORWARD");
			handleLoginRequestForward(data, communication);
			break;
		case Protocol.DATA_TYPE_HEADER_LOGIN_RESPOND_FORWARD:
			Log.d(TAG, "DATA_TYPE_HEADER_LOGIN_RESPOND_FORWARD");
			handleLoginRespondForward(data, communication);
			break;
		case Protocol.DATA_TYPE_HEADER_SEND_SINGLE:
			Log.d(TAG, "DATA_TYPE_HEADER_SEND_SINGLE");
			handleSendSingle(data);
			break;
		case Protocol.DATA_TYPE_HEADER_SEND_ALL:
			Log.d(TAG, "DATA_TYPE_HEADER_SEND_ALL");
			handleSendAll(data);
			break;
		case Protocol.DATA_TYPE_HEADER_SEND_FILE:
			Log.d(TAG, "DATA_TYPE_HEADER_SEND_FILE");
			handleSendFile(data);
            break;
		case Protocol.DATA_TYPE_HEADER_CANCEL_SEND_FILE:
			Log.d(TAG, "DATA_TYPE_HEADER_CANCEL_SEND_FILE");
			// For Later
            break;
		case Protocol.DATA_TYPE_HEADER_CANCEL_RECEIVE_FILE:
			Log.d(TAG, "DATA_TYPE_HEADER_CANCEL_RECEIVE_FILE");
			// For Later
            break;
		default:
			Log.d(TAG, "Unkown data type: " + dataType);
			break;
		}
		return data;
	}

	private void handleSendFile(byte[] data) {
		FileTransportProtocol.decodeSendFile(data, this);
	}

	/**
	 * Protocol.DATA_TYPE_HEADER_SEND_ALL
	 * 
	 * @param data
	 */
	private void handleSendAll(byte[] data) {
		SendProtocol.decodeSendMessageToAll(data, this);
	}

	/**
	 * Protocol.DATA_TYPE_HEADER_SEND_SINGLE
	 * 
	 * @param data
	 */
	private void handleSendSingle(byte[] data) {
		SendProtocol.decodeSendMessageToSingle(data, this);
	}

	/**
	 * Protocol.DATA_TYPE_HEADER_LOGIN_RESPOND_FORWARD
	 * 
	 * @param data
	 * @param communication
	 */
	private void handleLoginRespondForward(byte[] data,
			SocketCommunication communication) {
		// TODO Because of wifi direct is not used, this is not used already.
		DecodeLoginRespondForwardResult decodeRespondResult = LoginProtocol
				.decodeLoginRespondForward(data);
		int userLocalIDRespond = decodeRespondResult.getUserLocalID();
		boolean loginResult = decodeRespondResult.isLoginResult();
		int userID = decodeRespondResult.getUserID();

		byte[] loginRespond = LoginProtocol.encodeLoginRespond(loginResult,
				userID);

		SocketCommunication communication2 = mCommunicationManager
				.getLocalCommunicaiton(userLocalIDRespond);
		if (communication2 != null) {
			mCommunicationManager.sendMessage(communication2, loginRespond);
			mUserManager.addLocalCommunication(userID, communication2);
			mCommunicationManager.removeLocalCommunicaiton(userLocalIDRespond);
		} else {
			Log.e(TAG, "communication2 is null");
		}

	}

	/**
	 * Protocol.DATA_TYPE_HEADER_LOGIN_REQUEST_FORWARD
	 * 
	 * @param data
	 * @param communication
	 */
	private void handleLoginRequestForward(byte[] data,
			SocketCommunication communication) {
		// TODO Because of wifi direct is not used, this is not used already.
		User localUser = mUserManager.getLocalUser();
		if (UserManager.isManagerServer(localUser)) {
			DecodeLoginRequestForwardResult decodeRequestResult = LoginProtocol
					.decodeLoginRequestForward(data);
			User user = decodeRequestResult.getUser();
			int userLocalID = decodeRequestResult.getUserID();
			Log.d(TAG, "DATA_TYPE_HEADER_LOGIN_REQUEST user = " + user);
			boolean isAdded = mUserManager.addUser(user, communication);
			Log.d(TAG,
					"DATA_TYPE_HEADER_LOGIN_REQUEST send add users' info to all, isAdded = "
							+ isAdded);
			byte[] respond = LoginProtocol.encodeLoginRespondForward(isAdded,
					user, userLocalID);
			Log.d(TAG, "DATA_TYPE_HEADER_LOGIN_REQUEST respond length = "
					+ respond.length);
			mCommunicationManager.sendMessage(communication, respond);

			if (isAdded) {
				// sendMessageToUpdateAllUser();
			}
		} else {
			// TODO This condition is not supported yet.
		}

	}

	/**
	 * Protocol.DATA_TYPE_HEADER_UPDATE_ALL_USER
	 * 
	 * @param data
	 * @param originMessage
	 * @param communication
	 */
	private void handleLoginUpdateAllUser(byte[] data, byte[] originMessage,
			SocketCommunication communication) {
		LoginProtocol.decodeUpdateAllUser(data, communication);
		// TODO Because of wifi direct is not used, code below is not used
		// already.
		// send all user data to other user in my local network.
		for (SocketCommunication communication2 : mCommunicationManager
				.getCommunications()) {
			if (communication != communication2) {
				mCommunicationManager
						.sendMessage(communication2, originMessage);
			}
		}
	}

	/**
	 * Protocol.DATA_TYPE_HEADER_LOGIN_RESPOND
	 * 
	 * @param data
	 * @param communication
	 */
	private void handleLoginRespond(byte[] data,
			SocketCommunication communication) {
		LoginProtocol.decodeLoginRespond(data, mContext, communication, this);
	}

	/**
	 * Protocol.DATA_TYPE_HEADER_LOGIN_REQUEST
	 * 
	 * @param data
	 * @param communication
	 */
	private void handleLoginRequest(byte[] data,
			SocketCommunication communication) {
		User localUser = mUserManager.getLocalUser();
		if (UserManager.isManagerServer(localUser)) {
			Log.d(TAG, "This is manager server, process login request.");
			UserInfo userInfo = LoginProtocol.decodeLoginRequest(data);

			// Let call back to handle the request.
			if (mLoginRequestCallBack != null) {
				mLoginRequestCallBack.onLoginRequest(userInfo, communication);
			}
		} else {
			// TODO Because of wifi direct is not used, this is not used
			// already.
			// Log.d(TAG, "This is not manager server, need forward.");
			// This login request need forward.
			// int id =
			// mCommunicationManager.addLocalCommunicaiton(communication);
			// User user = LoginProtocol.decodeLoginRequest(data);
			// byte[] loginReqestData = LoginProtocol.encodeLoginRequestForward(
			// user, id);
			// // Send the message to other user except the login requester.
			// for (SocketCommunication communication2 : mCommunicationManager
			// .getCommunications()) {
			// if (communication2 != communication) {
			// mCommunicationManager.sendMessage(communication2,
			// loginReqestData);
			// Log.d(TAG, "login forward success.");
			// }
			// }
		}
	}

	@Override
	public void onReceiveMessageSingleType(int sendUserID, int receiveUserID,
			int appID, byte[] data) {
		Log.d(TAG, "onReceiveMessageSingleType senUserID = " + sendUserID
				+ ", receiveUserID = " + receiveUserID + ", appID = " + appID);
		User localUser = mUserManager.getLocalUser();
		if (receiveUserID == localUser.getUserID()) {
			Log.d(TAG, "This message is for me");
			mCommunicationManager.notifyReceiveListeners(sendUserID, appID,
					data);
		} else {
			Log.d(TAG, "This message is not for me");
			SocketCommunication communication = mUserManager
					.getSocketCommunication(receiveUserID);
			byte[] originalMsg = SendProtocol.encodeSendMessageToSingle(data,
					sendUserID, receiveUserID, appID);
			communication.sendMessage(originalMsg);
		}
	}

	@Override
	public void onReceiveMessageAllType(int sendUserID, int appID, byte[] data) {
		Log.d(TAG, "onReceiveMessageAllType sendUserID = " + sendUserID
				+ ", appID = " + appID);
		if (sendUserID == mUserManager.getLocalUser().getUserID()) {
			// When the client connected to the server but is not login success,
			// the client can receive the message, and the client will send the
			// message to other communications which user ID is not the same as
			// the message from. So the client will send the message back to the
			// server. So this message should ignore.
			// TODO This condition should avoid in the future.
			Log.d(TAG,
					"onReceiveMessageAllType, This message is sent by me, ignore.");
			return;
		}
		mCommunicationManager.notifyReceiveListeners(sendUserID, appID, data);

		byte[] msg = SendProtocol.encodeSendMessageToAll(data, sendUserID,
				appID);
		Map<Integer, SocketCommunication> communications = mUserManager
				.getAllCommmunication();
		for (SocketCommunication communication : mCommunicationManager
				.getCommunications()) {
			if (communications.get(sendUserID) != communication) {
				mCommunicationManager.sendMessage(communication, msg);
				Log.d(TAG,
						"Send to communication: "
								+ communication.getConnectedAddress());
			} else {
				Log.d(TAG,
						"Ignore, the communication is the message comes from.");
			}
		}
	}

	@Override
	public void onLoginSuccess(User localUser, SocketCommunication communication) {
		if (mLoginRespondCallback != null) {
			mLoginRespondCallback.onLoginSuccess(localUser, communication);
		} else {
			Log.d(TAG, "mLoginReusltCallback is null");
		}
	}

	@Override
	public void onLoginFail(int failReason, SocketCommunication communication) {
		if (mLoginRespondCallback != null) {
			mLoginRespondCallback.onLoginFail(failReason, communication);
		} else {
			Log.d(TAG, "mLoginReusltCallback is null");
		}
	}

	@Override
	public void onReceiveFile(int sendUserID, int receiveUserID, int appID,
			byte[] serverAddress, int serverPort, FileTransferInfo fileInfo) {
		Log.d(TAG, "onReceiveFile senUserID = " + sendUserID
				+ ", receiveUserID = " + receiveUserID + ", appID = " + appID
				+ ", serverPort = " + serverPort + ", file = "
				+ fileInfo.mFileName);
		User localUser = mUserManager.getLocalUser();
		if (receiveUserID == localUser.getUserID()) {
			Log.d(TAG, "onReceiveFile This file is for me");
			mCommunicationManager.notfiyFileReceiveListeners(sendUserID, appID,
					serverAddress, serverPort, fileInfo);
		} else {
			Log.d(TAG, "onReceiveFile This file is not for me");
			SocketCommunication communication = mUserManager
					.getSocketCommunication(receiveUserID);
			byte[] originalMsg = ProtocolEncoder.encodeSendFile(sendUserID,
					receiveUserID, appID, serverAddress, serverPort, fileInfo);
			communication.sendMessage(originalMsg);
		}
	}
}
