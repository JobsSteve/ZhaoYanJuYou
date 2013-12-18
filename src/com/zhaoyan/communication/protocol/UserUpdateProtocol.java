package com.zhaoyan.communication.protocol;

import java.util.List;
import java.util.Map;

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
import com.zhaoyan.communication.protocol.pb.PBUserUpdateProtos.PBUpdateUserId;
import com.zhaoyan.communication.protocol.pb.PBUserUpdateProtos.PBUserInfo;
import com.zhaoyan.juyou.provider.JuyouData;

/**
 * Update all user after user login. Rules is below:</br>
 * 
 * 1. Server send all user to all users.</br>
 * 
 * 2. Send one use in one time.</br>
 * 
 * 3. Send all user id first, then send all user info.</br>
 * 
 * @see PBUserUpdateProtos
 * 
 */
public class UserUpdateProtocol implements IProtocol {
	private static final String TAG = "UserUpdateProtocol";

	public UserUpdateProtocol(Context context) {

	}

	@Override
	public PBType[] getMessageTypes() {
		return new PBType[] { PBType.UPDATE_USER_ID, PBType.UPDATE_USER_INFO };
	}

	@Override
	public boolean decode(PBType type, byte[] msgData,
			SocketCommunication communication) {
		boolean result = true;
		if (type == PBType.UPDATE_USER_ID) {
			decodeUpdateUserId(msgData, communication);
		} else if (type == PBType.UPDATE_USER_INFO) {
			decodeUpdateUserInfo(msgData, communication);
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Add new users.
	 * 
	 * @param msgData
	 * @param communication
	 * @see #encodeUpdateUserInfo(Context);
	 */
	private void decodeUpdateUserInfo(byte[] msgData,
			SocketCommunication communication) {
		UserManager userManager = UserManager.getInstance();
		PBUserInfo pbUserInfo = null;
		try {
			pbUserInfo = PBUserInfo.parseFrom(msgData);
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "decodeUpdateUserInfo " + e);
		}
		if (pbUserInfo != null) {
			UserInfo userInfo = pbUserInfo2UserInfo(pbUserInfo);
			userManager.addUpdateUser(userInfo, communication);
		}
	}

	/**
	 * Clear the user not exist.
	 * 
	 * @param msgData
	 * @param communication
	 * @see #encodeUpdateUserId(Context)
	 */
	private void decodeUpdateUserId(byte[] msgData,
			SocketCommunication communication) {
		PBUpdateUserId updateUserId = null;
		try {
			updateUserId = PBUpdateUserId.parseFrom(msgData);
		} catch (InvalidProtocolBufferException e) {
			Log.e(TAG, "decodeUpdateUserId " + e);
		}

		if (updateUserId != null) {
			List<Integer> userIds = updateUserId.getUserIdList();
			UserManager userManager = UserManager.getInstance();
			for (int originalId : userManager.getAllUser().keySet()) {
				boolean originalUserDisconnect = true;
				for (int updateId : userIds) {
					if (updateId == originalId) {
						originalUserDisconnect = false;
						break;
					}
				}
				if (originalUserDisconnect) {
					userManager.removeUser(originalId);
				}
			}
		}
	}

	/**
	 * Encode and send update user message.
	 * 
	 * @param context
	 * @see PBUpdateUserId
	 * @see PBUserInfo
	 */
	public static void encodeUpdateAllUser(Context context) {
		encodeUpdateUserId(context);
		encodeUpdateUserInfo(context);
	}

	private static void encodeUpdateUserId(Context context) {
		PBUpdateUserId.Builder updateUserIdBuilder = PBUpdateUserId
				.newBuilder();
		UserManager userManager = UserManager.getInstance();
		Map<Integer, User> users = userManager.getAllUser();
		updateUserIdBuilder.addAllUserId(users.keySet());
		PBUpdateUserId updateUserId = updateUserIdBuilder.build();

		PBBase pbBase = BaseProtocol.createBaseMessage(PBType.UPDATE_USER_ID,
				updateUserId);
		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		communicationManager
				.sendMessageToAllWithoutEncode(pbBase.toByteArray());
	}

	private static void encodeUpdateUserInfo(Context context) {
		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		UserManager userManager = UserManager.getInstance();
		Map<Integer, User> users = userManager.getAllUser();
		for (Map.Entry<Integer, User> entry : users.entrySet()) {
			User user = entry.getValue();
			UserInfo userInfo = UserHelper.getUserInfo(context, user);
			if (userInfo != null) {
				// Set type as TYPE_REMOTE.
				userInfo.setType(JuyouData.User.TYPE_REMOTE);
				PBUserInfo pbUserInfo = userInfo2PBUserInfo(userInfo);
				PBBase pbBase = BaseProtocol.createBaseMessage(
						PBType.UPDATE_USER_INFO, pbUserInfo);
				communicationManager.sendMessageToAllWithoutEncode(pbBase
						.toByteArray());
			} else {
				Log.e(TAG, "encodeUpdateAllUser getUserInfo fail. user = "
						+ user);
			}
		}
	}

	public static PBUserInfo userInfo2PBUserInfo(UserInfo userInfo) {
		PBUserInfo.Builder userInfoBuilder = PBUserInfo.newBuilder();
		userInfoBuilder.setUserId(userInfo.getUser().getUserID());
		userInfoBuilder.setUserName(userInfo.getUser().getUserName());
		userInfoBuilder.setHeadImageId(userInfo.getHeadId());
		if (userInfo.getHeadBitmapData() != null) {
			userInfoBuilder.setHeadImageData(ByteString.copyFrom(userInfo
					.getHeadBitmapData()));
		}
		if (userInfo.getIpAddress() != null) {
			userInfoBuilder.setIpAddress(userInfo.getIpAddress());
		}
		userInfoBuilder.setType(userInfo.getType());
		if (userInfo.getSsid() != null) {
			userInfoBuilder.setSsid(userInfo.getSsid());
		}
		userInfoBuilder.setStatus(userInfo.getStatus());
		return userInfoBuilder.build();
	}

	public static UserInfo pbUserInfo2UserInfo(PBUserInfo pbUserInfo) {
		UserInfo userInfo = new UserInfo();
		User user = new User();
		user.setUserID(pbUserInfo.getUserId());
		user.setUserName(pbUserInfo.getUserName());
		userInfo.setUser(user);
		userInfo.setHeadId(pbUserInfo.getHeadImageId());
		userInfo.setHeadBitmapData(pbUserInfo.getHeadImageData().toByteArray());
		userInfo.setIpAddress(pbUserInfo.getIpAddress());
		userInfo.setType(userInfo.getType());
		userInfo.setSsid(pbUserInfo.getSsid());
		userInfo.setStatus(pbUserInfo.getStatus());
		return userInfo;
	}
}
