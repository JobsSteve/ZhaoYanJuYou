package com.zhaoyan.communication.protocol;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.CallBacks.ILoginRespondCallback;
import com.zhaoyan.communication.SocketCommunication;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserManager;

/**
 * This class is used for encode and decode login message.
 * 
 */
public class LoginProtocol {
	private static final String TAG = "LoginProtocol";

	/**
	 * login request Protocol: [DATA_TYPE_HEADER_LOGIN_REQUEST][user data]
	 * 
	 * @param user
	 * @return
	 */
	public static byte[] encodeLoginRequest(User user) {
		byte[] loginMsg;
		byte[] loginHeader = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_LOGIN_REQUEST);
		byte[] userData = UserHelper.encodeUser(user);
		loginMsg = ArrayUtil.join(loginHeader, userData);
		return loginMsg;
	}

	/**
	 * login request Protocol: [DATA_TYPE_HEADER_LOGIN_REQUEST][user data]
	 * 
	 * @param data
	 * @return
	 */
	public static User decodeLoginRequest(byte[] data) {
		User user = UserHelper.decodeUser(data);
		return user;
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
	public static boolean decodeLoginRespond(byte[] data,
			UserManager userManager, SocketCommunication communication,
			ILoginRespondCallback callback) {
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
			byte[] userIDData = Arrays.copyOfRange(data, start, end);
			int userId = ArrayUtil.byteArray2Int(userIDData);
			Log.d(TAG, "login success, user id = " + userId);
			User localUser = userManager.getLocalUser();
			localUser.setUserID(userId);
			userManager.setLocalUser(localUser);

			callback.onLoginSuccess(localUser, communication);
			break;
		case Protocol.LOGIN_RESPOND_RESULT_FAIL:
			loginResult = false;
			// fail reason.
			start = end;
			end += Protocol.LOGIN_RESPOND_RESULT_FAIL_REASON_HEADER_SIZE;
			byte[] failReasonData = Arrays.copyOfRange(data, start, end);
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
		byte[] userIDData = Arrays.copyOfRange(data, start, end);
		int userID = ArrayUtil.byteArray2Int(userIDData);

		start = end;
		end = data.length;
		byte[] userData = Arrays.copyOfRange(data, start, end);
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
		byte[] userLocalIDData = Arrays.copyOfRange(data, start, end);
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
			byte[] userIDData = Arrays.copyOfRange(data, start, end);
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
	 * [DATA_TYPE_HEADER_UPDATE_ALL_USER][userTotalNumber][all user data]
	 * 
	 * @param userManager
	 * @return
	 */
	public static byte[] encodeUpdateAllUser(UserManager userManager) {
		byte[] result = null;
		// All user total number;
		byte[] updateAllUserHeader = ArrayUtil
				.int2ByteArray(Protocol.DATA_TYPE_HEADER_UPDATE_ALL_USER);
		byte[] userTotalNumber = ArrayUtil.int2ByteArray(userManager
				.getAllUser().size());
		result = ArrayUtil.join(updateAllUserHeader, userTotalNumber,
				getAllUser(userManager));
		return result;
	}

	/**
	 * get all user byte data.
	 * 
	 * @param userManager
	 * @return
	 */
	private static byte[] getAllUser(UserManager userManager) {
		byte[] result = null;
		Map<Integer, User> users = userManager.getAllUser();
		User[] users3 = new User[users.size()];
		int i = 0;
		for (Map.Entry<Integer, User> entry : users.entrySet()) {
			users3[i] = entry.getValue();
			i++;
		}
		result = ArrayUtil.objectToByteArray(users3);
		Log.d(TAG, "getAllUser, user data length = " + result.length);

		return result;
	}

	/**
	 * Update all user.
	 * 
	 * Protocol see {@link #encodeUpdateAllUser(UserManager)}
	 * 
	 * @param data
	 * @param userManager
	 * @param communication
	 */
	public static void decodeUpdateAllUser(byte[] data,
			UserManager userManager, SocketCommunication communication) {
		int start = 0;
		int end = Protocol.UPDATE_ALL_USER_USER_TOTAL_NUMBER_HEADER_SIZE;
		// User total number.
		byte[] userTotalNumberData = Arrays.copyOfRange(data, start, end);
		int userTotalNumber = ArrayUtil.byteArray2Int(userTotalNumberData);

		// Update all user data.
		start = end;
		end = data.length;
		byte[] allUserData = Arrays.copyOfRange(data, start, end);
		// userManager.clear();
		addAllUser(userTotalNumber, allUserData, userManager, communication);
	}

	private static User[] decodeAllUser(int userTotalNumber, byte[] allUserData) {
		User[] result = new User[userTotalNumber];
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					allUserData);
			ObjectInputStream objectInputStream = new ObjectInputStream(
					byteArrayInputStream);
			for (int i = 0; i < result.length; i++) {
				result[i] = (User) objectInputStream.readObject();
			}
			byteArrayInputStream.close();
			objectInputStream.close();
		} catch (Exception e) {
			Log.e(TAG, "decodeAllUser error." + e);
		}
		return result;
	}

	private static User[] addAllUser(int userCount, byte[] data,
			UserManager userManager, SocketCommunication communication) {
		User[] result = decodeAllUser(userCount, data);
		for (User user : result) {
			Log.d(TAG, "addAllUser : " + user);
			userManager.addUser(user, communication);
		}
		Map<Integer, User> userMap = userManager.getAllUser();
		for (Entry<Integer, User> entry : userMap.entrySet()) {
			boolean flag = false;
			User tem = entry.getValue();
			for (User user : result) {
				if (tem.getUserID() == user.getUserID()) {
					flag = true;
				}
			}
			if (!flag) {
				userManager.removeUser(tem.getUserID());
			}
		}
		return result;
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
