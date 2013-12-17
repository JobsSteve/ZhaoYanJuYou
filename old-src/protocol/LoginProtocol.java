package com.zhaoyan.communication.protocol;

import java.util.Map;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.util.ArraysCompat;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.CallBacks.ILoginRespondCallback;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.juyou.provider.JuyouData;

/**
 * This class is used for encode and decode login message.
 * 
 */
public class LoginProtocol {
	private static final String TAG = "LoginProtocol";

	/**
	 * login request Protocol:
	 * [DATA_TYPE_HEADER_LOGIN_REQUEST][nameLength][name]
	 * [headImageId][headImage]
	 * 
	 * @param user
	 * @return
	 */
	public static byte[] encodeLoginRequest(Context context) {
		byte[] loginMsg;
		byte[] loginHeader = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_LOGIN_REQUEST);
		UserInfo userInfo = UserHelper.loadLocalUser(context);
		byte[] name = userInfo.getUser().getUserName().getBytes();
		byte[] nameLength = ArrayUtil.int2ByteArray(name.length);
		byte[] headImageId = ArrayUtil.int2ByteArray(userInfo.getHeadId());

		// If head is user customized, send head image.
		if (userInfo.getHeadId() == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
			byte[] headImage = userInfo.getHeadBitmapData();
			byte[] headImageLength = ArrayUtil.int2ByteArray(headImage.length);
			loginMsg = ArrayUtil.join(loginHeader, nameLength, name,
					headImageId, headImageLength, headImage);
		} else {
			loginMsg = ArrayUtil.join(loginHeader, nameLength, name,
					headImageId);
		}
		return loginMsg;
	}

	/**
	 * see {@link #encodeLoginRequest(Context)}.
	 * 
	 * @param data
	 * @return
	 */
	public static UserInfo decodeLoginRequest(byte[] data) {
		int start = 0;
		int end = Protocol.LENGTH_INT;
		// name
		byte[] nameLengthData = ArraysCompat.copyOfRange(data, start, end);
		int nameLength = ArrayUtil.byteArray2Int(nameLengthData);
		start = end;
		end += nameLength;
		byte[] nameData = ArraysCompat.copyOfRange(data, start, end);
		String name = new String(nameData);

		// head image id
		start = end;
		end += Protocol.LENGTH_INT;
		byte[] headImageIdData = ArraysCompat.copyOfRange(data, start, end);
		int headImageId = ArrayUtil.byteArray2Int(headImageIdData);

		// head image
		byte[] headImageData = null;
		if (headImageId == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
			start = end;
			end += Protocol.LENGTH_INT;
			byte[] headImageLengthData = ArraysCompat.copyOfRange(data, start,
					end);
			int headImageLength = ArrayUtil.byteArray2Int(headImageLengthData);
			start = end;
			end += headImageLength;
			headImageData = ArraysCompat.copyOfRange(data, start, end);
		}

		// Get user info
		UserInfo userInfo = new UserInfo();
		User user = new User();
		user.setUserName(name);
		userInfo.setUser(user);
		userInfo.setHeadId(headImageId);
		if (headImageId == UserInfo.HEAD_ID_NOT_PRE_INSTALL
				&& headImageData != null) {
			userInfo.setHeadBitmapData(headImageData);
		}
		userInfo.setType(JuyouData.User.TYPE_REMOTE);
		return userInfo;
	}

	/**
	 * login respond Protocol: </br>
	 * 
	 * sucess:
	 * [DATA_TYPE_HEADER_LOGIN_RESPOND][LOGIN_RESPOND_RESULT_SUCCESS][user
	 * id]</br>
	 * 
	 * fail:[DATA_TYPE_HEADER_LOGIN_RESPOND][LOGIN_RESPOND_RESULT_FAIL][reason]<
	 * /br>
	 * 
	 */
	public static byte[] encodeLoginRespond(boolean isAdded, int userID) {
		byte[] respond = null;
		byte[] respondHeader = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_LOGIN_RESPOND);
		byte[] respondResult;
		byte[] respondData;
		if (isAdded) {
			// login result.
			respondResult = new byte[] { Protocol.LOGIN_RESPOND_RESULT_SUCCESS };
			// user id.
			respondData = ArrayUtil.int2ByteArray(userID);
		} else {
			// login result.
			respondResult = new byte[] { Protocol.LOGIN_RESPOND_RESULT_FAIL };
			// login fail reason. the server disallow the login request.
			// TODO Maybe there are other fail reasons.
			respondData = ArrayUtil
					.int2ByteArray(Protocol.LOGIN_RESPOND_RESULT_FAIL_SERVER_DISALLOW);
		}
		respond = ArrayUtil.join(respondHeader, respondResult, respondData);
		return respond;
	}

	/**
	 * Get the login result and update user id.</br>
	 * 
	 * Protocol see {@link #encodeLoginRespond(boolean, User, UserManager)}
	 * 
	 * @param data
	 * @param userManager
	 * @param communication
	 * @return
	 */
	public static boolean decodeLoginRespond(byte[] data, Context context,
			SocketCommunication communication, ILoginRespondCallback callback) {
		boolean loginResult = false;
		int start = 0;
		int end = Protocol.LOGIN_RESPOND_RESULT_HEADER_SIZE;
		// Login result.
		byte loginResultData = data[0];

		switch (loginResultData) {
		case Protocol.LOGIN_RESPOND_RESULT_SUCCESS:
			loginResult = true;
			// user id.
			start = end;
			end += Protocol.LOGIN_RESPOND_USERID_HEADER_SIZE;
			byte[] userIDData = ArraysCompat.copyOfRange(data, start, end);
			int userId = ArrayUtil.byteArray2Int(userIDData);
			Log.d(TAG, "login success, user id = " + userId);

			// Update local user.
			UserInfo userInfo = UserHelper.loadLocalUser(context);
			userInfo.getUser().setUserID(userId);
			userInfo.setStatus(JuyouData.User.STATUS_CONNECTED);
			UserManager.getInstance().setLocalUserInfo(userInfo);
			UserManager.getInstance().setLocalUserConnected(communication);

			callback.onLoginSuccess(userInfo.getUser(), communication);
			break;
		case Protocol.LOGIN_RESPOND_RESULT_FAIL:
			loginResult = false;
			// fail reason.
			start = end;
			end += Protocol.LOGIN_RESPOND_RESULT_FAIL_REASON_HEADER_SIZE;
			byte[] failReasonData = ArraysCompat.copyOfRange(data, start, end);
			int failReason = ArrayUtil.byteArray2Int(failReasonData);
			callback.onLoginFail(failReason, communication);
			break;

		default:
			loginResult = false;
			break;
		}
		Log.d(TAG, "Login result: " + loginResult);
		return loginResult;
	}

	/**
	 * login request Protocol: </br>
	 * 
	 * [DATA_TYPE_HEADER_LOGIN_REQUEST_FORWARD][user id][user data]
	 * 
	 * @param user
	 * @return
	 */
	public static byte[] encodeLoginRequestForward(User user, int userLocalID) {
		byte[] loginMsg;
		byte[] loginHeader = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_LOGIN_REQUEST_FORWARD);
		byte[] userLocalIDData = ArrayUtil.int2ByteArray(userLocalID);
		byte[] userData = UserHelper.encodeUser(user);
		loginMsg = ArrayUtil.join(loginHeader, userLocalIDData, userData);
		return loginMsg;
	}

	/**
	 * see {@link #encodeLoginRequestForward(User, int)}
	 * 
	 * @param data
	 * @return
	 */
	public static DecodeLoginRequestForwardResult decodeLoginRequestForward(
			byte[] data) {
		int start = 0;
		int end = Protocol.LOGIN_FORWARD_USER_ID_SIZE;
		byte[] userIDData = ArraysCompat.copyOfRange(data, start, end);
		int userID = ArrayUtil.byteArray2Int(userIDData);

		start = end;
		end = data.length;
		byte[] userData = ArraysCompat.copyOfRange(data, start, end);
		User user = UserHelper.decodeUser(userData);

		DecodeLoginRequestForwardResult result = new DecodeLoginRequestForwardResult();
		result.setUser(user);
		result.setUserID(userID);

		return result;
	}

	/**
	 * login respond Protocol: </br>
	 * 
	 * sucess: [DATA_TYPE_HEADER_LOGIN_RESPOND_FORWARD][user local
	 * id][LOGIN_RESPOND_RESULT_SUCCESS][user id]</br>
	 * 
	 * fail:[DATA_TYPE_HEADER_LOGIN_RESPOND_FORWARD][user local
	 * id][LOGIN_RESPOND_RESULT_FAIL][reason]< /br>
	 * 
	 */
	public static byte[] encodeLoginRespondForward(boolean isAdded, User user,
			int userLocalID) {
		byte[] respond = null;
		byte[] respondHeader = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_LOGIN_RESPOND_FORWARD);
		byte[] userLocalIDData = ArrayUtil.int2ByteArray(userLocalID);
		byte[] respondResult;
		byte[] respondData;
		if (isAdded) {
			// login result.
			respondResult = new byte[] { Protocol.LOGIN_RESPOND_RESULT_SUCCESS };
			// user id.
			respondData = ArrayUtil.int2ByteArray(user.getUserID());
		} else {
			// login result.
			respondResult = new byte[] { Protocol.LOGIN_RESPOND_RESULT_FAIL };
			// login fail reason.
			respondData = new byte[] { Protocol.LOGIN_RESPOND_RESULT_FAIL_UNKOWN };
		}
		respond = ArrayUtil.join(respondHeader, userLocalIDData, respondResult,
				respondData);
		return respond;
	}

	/**
	 * Get the login result and update user id.</br>
	 * 
	 * Protocol see {@link #encodeLoginRespondForward(boolean, User, int)}
	 * 
	 * @param data
	 * @param userManager
	 * @param communication
	 * @return
	 */
	public static DecodeLoginRespondForwardResult decodeLoginRespondForward(
			byte[] data) {
		int start = 0;
		int end = Protocol.LOGIN_FORWARD_USER_ID_SIZE;
		byte[] userLocalIDData = ArraysCompat.copyOfRange(data, start, end);
		int userLocalID = ArrayUtil.byteArray2Int(userLocalIDData);

		// Login result.
		start = end;
		end += Protocol.LOGIN_RESPOND_RESULT_HEADER_SIZE;
		byte loginResultData = data[start];

		DecodeLoginRespondForwardResult result = new DecodeLoginRespondForwardResult();
		result.setUserLocalID(userLocalID);

		switch (loginResultData) {
		case Protocol.LOGIN_RESPOND_RESULT_SUCCESS:

			// user id.
			start = end;
			end += Protocol.LOGIN_RESPOND_USERID_HEADER_SIZE;
			byte[] userIDData = ArraysCompat.copyOfRange(data, start, end);
			int userId = ArrayUtil.byteArray2Int(userIDData);
			Log.d(TAG, "login success, user id = " + userId);
			result.setLoginResult(true);
			result.setUserID(userId);
			break;
		case Protocol.LOGIN_RESPOND_RESULT_FAIL:
			result.setLoginResult(false);
			result.setFailReson(Protocol.LOGIN_RESPOND_RESULT_FAIL_UNKOWN);
			break;

		default:
			break;
		}

		return result;
	}

	/**
	 * Update all user protocol:</br>
	 * 
	 * 1. Server send all user to all users.</br>
	 * 
	 * 2. Send one use in one time.</br>
	 * 
	 * 3. When receive the first user, clear all original users.</br>
	 * 
	 * [DATA_TYPE_HEADER_UPDATE_ALL_USER][currentUserNumber][userTotalNumber][
	 * user data]</br>
	 * 
	 * [DATA_TYPE_HEADER_UPDATE_ALL_USER][currentUserNumber][userTotalNumber][
	 * user data]</br>
	 * 
	 * [DATA_TYPE_HEADER_UPDATE_ALL_USER][currentUserNumber][userTotalNumber
	 * ][user data]
	 * 
	 * @param userManager
	 * @return
	 */
	public static void encodeUpdateAllUser(Context context) {
		UserManager userManager = UserManager.getInstance();
		SocketCommunicationManager communicationManager = SocketCommunicationManager
				.getInstance();
		// All user total number;
		byte[] updateAllUserHeader = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_UPDATE_ALL_USER);
		Map<Integer, User> users = userManager.getAllUser();
		int userTotalNumber = users.size();
		if (userTotalNumber == 0) {
			return;
		}
		// 0 / total
		byte[] allUserIdData = null;
		int currentNumber = 0;
		for (int id : users.keySet()) {
			if (allUserIdData == null) {
				allUserIdData = ArrayUtil.int2ByteArray(id);
			} else {
				allUserIdData = ArrayUtil.join(allUserIdData,
						ArrayUtil.int2ByteArray(id));
			}
		}
		byte[] updateAllUserIdData = ArrayUtil.join(updateAllUserHeader,
				ArrayUtil.int2ByteArray(currentNumber),
				ArrayUtil.int2ByteArray(userTotalNumber), allUserIdData);
		communicationManager.sendMessageToAllWithoutEncode(updateAllUserIdData);

		// 1 / total - total / total.
		currentNumber = 1;
		for (Map.Entry<Integer, User> entry : users.entrySet()) {
			User user = entry.getValue();
			UserInfo userInfo = UserHelper.getUserInfo(context, user);
			if (userInfo != null) {
				byte[] data = encodeUpdateOneUser(updateAllUserHeader,
						userInfo, currentNumber, userTotalNumber);
				communicationManager.sendMessageToAllWithoutEncode(data);
			} else {
				Log.e(TAG, "encodeUpdateAllUser getUserInfo fail. user = "
						+ user);
			}
			currentNumber++;
		}
	}

	/**
	 * [DATA_TYPE_HEADER_UPDATE_ALL_USER][currentNumber][totalNumber][userInfo]
	 * 
	 * @param header
	 * @param userInfo
	 * @param currentNumber
	 * @param totalNumber
	 * @return
	 */
	private static byte[] encodeUpdateOneUser(byte[] header, UserInfo userInfo,
			int currentNumber, int totalNumber) {
		byte[] currentNumberData = ArrayUtil.int2ByteArray(currentNumber);
		byte[] totalNumberData = ArrayUtil.int2ByteArray(totalNumber);
		// Set type as TYPE_REMOTE.
		userInfo.setType(JuyouData.User.TYPE_REMOTE);
		byte[] userInfoData = ArrayUtil.objectToByteArray(userInfo);
		byte[] data = ArrayUtil.join(header, currentNumberData,
				totalNumberData, userInfoData);
		return data;
	}

	/**
	 * Update all user.
	 * 
	 * Protocol see {@link #encodeUpdateAllUser(UserManager)}
	 * 
	 * @param data
	 * @param communication
	 */
	public static void decodeUpdateAllUser(byte[] data,
			SocketCommunication communication) {
		UserManager userManager = UserManager.getInstance();
		// User current number.
		int start = 0;
		int end = Protocol.LENGTH_INT;
		byte[] userCurrentNumberData = ArraysCompat.copyOfRange(data, start,
				end);
		int userCurrentNumber = ArrayUtil.byteArray2Int(userCurrentNumberData);
		// User total number.
		start = end;
		end += Protocol.LENGTH_INT;
		byte[] userTotalNumberData = ArraysCompat.copyOfRange(data, start, end);
		int userTotalNumber = ArrayUtil.byteArray2Int(userTotalNumberData);

		// User info.
		if (userCurrentNumber == 0) {
			// Clear the user not exist.
			int[] allUserId = new int[userTotalNumber];
			for (int i = 0; i < userTotalNumber; i++) {
				start = end;
				end += Protocol.LENGTH_INT;
				byte[] userIdData = ArraysCompat.copyOfRange(data, start, end);
				int userId = ArrayUtil.byteArray2Int(userIdData);
				allUserId[i] = userId;
			}
			for (int originalId : userManager.getAllUser().keySet()) {
				boolean originalUserDisconnect = true;
				for (int updateId : allUserId) {
					if (updateId == originalId) {
						originalUserDisconnect = false;
					}
				}
				if (originalUserDisconnect) {
					userManager.removeUser(originalId);
				}
			}
		} else {
			start = end;
			end = data.length;
			byte[] userInfoData = ArraysCompat.copyOfRange(data, start, end);
			UserInfo userInfo = (UserInfo) ArrayUtil
					.byteArrayToObject(userInfoData);
			userManager.addUpdateUser(userInfo, communication);
		}
	}

	public static class DecodeLoginRequestForwardResult {
		private User user;
		private int userID;

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}

		public int getUserID() {
			return userID;
		}

		public void setUserID(int userID) {
			this.userID = userID;
		}
	}

	public static class DecodeLoginRespondForwardResult {
		private int userID;
		private boolean loginResult;
		private int userLocalID;
		private int failReson;

		public int getUserID() {
			return userID;
		}

		public void setUserID(int userID) {
			this.userID = userID;
		}

		public int getUserLocalID() {
			return userLocalID;
		}

		public void setUserLocalID(int userLocalID) {
			this.userLocalID = userLocalID;
		}

		public boolean isLoginResult() {
			return loginResult;
		}

		public void setLoginResult(boolean loginResult) {
			this.loginResult = loginResult;
		}

		public int getFailReson() {
			return failReson;
		}

		public void setFailReson(int failReson) {
			this.failReson = failReson;
		}
	}
}
