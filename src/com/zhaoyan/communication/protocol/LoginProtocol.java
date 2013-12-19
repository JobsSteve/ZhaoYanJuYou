package com.zhaoyan.communication.protocol;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.google.protobuf.InvalidProtocolBufferException;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.ProtocolCommunication;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBBase;
import com.zhaoyan.communication.protocol.pb.PBBaseProtos.PBType;
import com.zhaoyan.communication.protocol.pb.PBLoginProtos;
import com.zhaoyan.communication.protocol.pb.PBLoginProtos.PBLoginFailReason;
import com.zhaoyan.communication.protocol.pb.PBLoginProtos.PBLoginRequest;
import com.zhaoyan.communication.protocol.pb.PBLoginProtos.PBLoginRespond;
import com.zhaoyan.communication.protocol.pb.PBLoginProtos.PBLoginResult;
import com.zhaoyan.juyou.provider.JuyouData;

/**
 * This class is used for encode and decode login message.
 * 
 * @see PBLoginProtos
 */
public class LoginProtocol implements IProtocol {
	private static final String TAG = "LoginProtocol";
	private Context mContext;

	public LoginProtocol(Context context) {
		mContext = context;
	}

	@Override
	public PBType[] getMessageTypes() {
		return new PBType[] { PBType.LOGIN_REQUEST, PBType.LOGIN_RESPOND };
	}

	@Override
	public boolean decode(PBType type, byte[] msgData,
			SocketCommunication communication) {
		boolean result = true;
		if (type == PBType.LOGIN_REQUEST) {
			decodeLoginRequest(msgData, communication);
		} else if (type == PBType.LOGIN_RESPOND) {
			decodeLoginRespond(msgData, communication);
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Encode and send login request.
	 * 
	 * @param context
	 * @see PBLoginRequest
	 */
	public static void encodeLoginRequest(Context context) {
		UserInfo userInfo = UserHelper.loadLocalUser(context);
		userInfo.setType(JuyouData.User.TYPE_REMOTE);
		PBLoginRequest.Builder requestBuilder = PBLoginRequest.newBuilder();
		requestBuilder.setUserInfo(UserInfoUtil.userInfo2PBUserInfo(userInfo));
		PBLoginRequest pbLoginRequest = requestBuilder.build();

		PBBase pbBase = BaseProtocol.createBaseMessage(PBType.LOGIN_REQUEST,
				pbLoginRequest);

		SocketCommunicationManager manager = SocketCommunicationManager
				.getInstance();
		manager.sendMessageToAllWithoutEncode(pbBase.toByteArray());
	}

	/**
	 * @see {@link #encodeLoginRequest(Context)}
	 * @param data
	 * @param communication
	 */
	private void decodeLoginRequest(byte[] data,
			SocketCommunication communication) {
		User localUser = UserManager.getInstance().getLocalUser();
		if (UserManager.isManagerServer(localUser)) {
			Log.d(TAG, "This is manager server, process login request.");
			UserInfo userInfo = getLoginRequestUserInfo(data);

			// Let ProtocolCommunication to handle the request.
			ProtocolCommunication protocolCommunication = ProtocolCommunication
					.getInstance();
			protocolCommunication.notifyLoginRequest(userInfo, communication);
		}
	}

	private UserInfo getLoginRequestUserInfo(byte[] data) {
		PBLoginRequest loginRequest = null;
		try {
			loginRequest = PBLoginRequest.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "getLoginRequestUserInfo " + e);
		}

		UserInfo userInfo = UserInfoUtil.pbUserInfo2UserInfo(loginRequest
				.getUserInfo());
		return userInfo;
	}

	/**
	 * Encode and send login respond.
	 * 
	 * @param isAdded
	 * @param userID
	 * @param communication
	 * @see PBLoginRespond
	 */
	public static void encodeLoginRespond(boolean isAdded, int userID,
			SocketCommunication communication) {
		PBLoginRespond.Builder respondBuilder = PBLoginRespond.newBuilder();
		// result
		PBLoginResult result = isAdded ? PBLoginResult.SUCCESS
				: PBLoginResult.FAIL;
		respondBuilder.setResult(result);
		if (isAdded) {
			// user id.
			respondBuilder.setUserId(userID);
		} else {
			// login fail reason.
			// TODO Not implement yet.
			respondBuilder.setFailReason(PBLoginFailReason.UNKOWN);
		}
		PBLoginRespond loginRespond = respondBuilder.build();

		PBBase pbBase = BaseProtocol.createBaseMessage(PBType.LOGIN_RESPOND,
				loginRespond);

		communication.sendMessage(pbBase.toByteArray());
	}

	/**
	 * Get the login result and update user id.</br>
	 * 
	 * @param data
	 * @param userManager
	 * @param communication
	 * @see #encodeLoginRespond(boolean, int, SocketCommunication)
	 * @return
	 */
	private void decodeLoginRespond(byte[] data,
			SocketCommunication communication) {
		PBLoginRespond loginRespond = null;
		try {
			loginRespond = PBLoginRespond.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

		ProtocolCommunication protocolCommunication = ProtocolCommunication
				.getInstance();
		if (loginRespond != null) {
			PBLoginResult result = loginRespond.getResult();
			Log.d(TAG, "Login result = " + result);
			if (result == PBLoginResult.SUCCESS) {
				// user id.
				int userId = loginRespond.getUserId();
				Log.d(TAG, "login success, user id = " + userId);

				// Update local user.
				UserInfo userInfo = UserHelper.loadLocalUser(mContext);
				userInfo.getUser().setUserID(userId);
				userInfo.setStatus(JuyouData.User.STATUS_CONNECTED);
				UserManager.getInstance().setLocalUserInfo(userInfo);
				UserManager.getInstance().setLocalUserConnected(communication);

				protocolCommunication.notifyLoginSuccess(userInfo.getUser(),
						communication);
			} else if (result == PBLoginResult.FAIL) {
				// fail reason.
				PBLoginFailReason reason = loginRespond.getFailReason();

				protocolCommunication.notifyLoginFail(reason.getNumber(),
						communication);
			}
		}
	}
}
