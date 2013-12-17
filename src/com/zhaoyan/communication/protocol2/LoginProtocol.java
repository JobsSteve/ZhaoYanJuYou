package com.zhaoyan.communication.protocol2;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.zhaoyan.common.util.Log;
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
		PBLoginRequest.Builder requestBuilder = PBLoginRequest.newBuilder();
		// name
		UserInfo userInfo = UserHelper.loadLocalUser(context);
		String name = userInfo.getUser().getUserName();
		requestBuilder.setName(name);
		// head image id
		int headImageId = userInfo.getHeadId();
		requestBuilder.setHeadImageId(headImageId);
		// If head is user customized, send head image data.
		if (userInfo.getHeadId() == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
			byte[] headImage = userInfo.getHeadBitmapData();
			requestBuilder.setHeadImageData(ByteString.copyFrom(headImage));
		}
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

			// Let SocketCommunicationManager to handle the request.
			SocketCommunicationManager manager = SocketCommunicationManager
					.getInstance();
			manager.onLoginRequest(userInfo, communication);
		}
	}

	private UserInfo getLoginRequestUserInfo(byte[] data) {
		PBLoginRequest loginRequest = null;
		try {
			loginRequest = PBLoginRequest.parseFrom(data);
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "getLoginRequestUserInfo " + e);
		}

		// Get user info
		UserInfo userInfo = null;
		if (loginRequest != null) {
			userInfo = new UserInfo();
			// name
			User user = new User();
			user.setUserName(loginRequest.getName());
			userInfo.setUser(user);
			// head image id.
			int headImageId = loginRequest.getHeadImageId();
			userInfo.setHeadId(headImageId);
			if (headImageId == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
				userInfo.setHeadBitmapData(loginRequest.getHeadImageData()
						.toByteArray());
			}
			userInfo.setType(JuyouData.User.TYPE_REMOTE);
		}
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
		if (loginRespond != null) {
			SocketCommunicationManager manager = SocketCommunicationManager
					.getInstance();
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

				manager.onLoginSuccess(userInfo.getUser(), communication);
			} else if (result == PBLoginResult.FAIL) {
				// fail reason.
				PBLoginFailReason reason = loginRespond.getFailReason();
				manager.onLoginFail(reason.getNumber(), communication);
			}
		}
	}
}
