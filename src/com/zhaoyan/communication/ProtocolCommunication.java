package com.zhaoyan.communication;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.protocol2.LoginProtocol;
import com.zhaoyan.communication.protocol2.UserUpdateProtocol;

import android.content.Context;

/**
 * This class provide common interface for protocols.
 * 
 */
public class ProtocolCommunication {
	private static final String TAG = "ProtocolCommunication";
	private static ProtocolCommunication mInstance;
	private Context mContext;
	/** Used for Login confirm UI */
	private ILoginRequestCallBack mLoginRequestCallBack;
	private ILoginRespondCallback mLoginRespondCallback;

	private UserManager mUserManager;
	private SocketCommunicationManager mSocketComManager;

	private ProtocolCommunication() {

	}

	public static synchronized ProtocolCommunication getInstance() {
		if (mInstance == null) {
			mInstance = new ProtocolCommunication();
		}
		return mInstance;
	}

	public void init(Context context) {
		mContext = context;
		mUserManager = UserManager.getInstance();
	}

	public void release() {
		mInstance = null;
	}

	public void setLoginRequestCallBack(ILoginRequestCallBack callback) {
		mLoginRequestCallBack = callback;
	}

	public void setLoginRespondCallback(ILoginRespondCallback callback) {
		mLoginRespondCallback = callback;
	}

	public void notifyLoginSuccess(User localUser,
			SocketCommunication communication) {
		if (mLoginRespondCallback != null) {
			mLoginRespondCallback.onLoginSuccess(localUser, communication);
		}
	}

	public void notifyLoginFail(int failReason,
			SocketCommunication communication) {
		if (mLoginRespondCallback != null) {
			mLoginRespondCallback.onLoginFail(failReason, communication);
		}
	}

	public void notifyLoginRequest(UserInfo user,
			SocketCommunication communication) {
		Log.d(TAG, "onLoginRequest()");
		if (mLoginRequestCallBack != null) {
			mLoginRequestCallBack.onLoginRequest(user, communication);
		}
	}

	/**
	 * Respond to the login request. If login is allowed, send message to update
	 * user list.
	 * 
	 * @param userInfo
	 * @param communication
	 * @param isAllow
	 */
	public void respondLoginRequest(UserInfo userInfo,
			SocketCommunication communication, boolean isAllow) {
		// TODO If the server disallow the login request, may be stop the socket
		// communication. But we should check the login request is from the WiFi
		// direct server or a client. Let this be done in the future.
		boolean loginResult = false;
		if (isAllow) {
			loginResult = mUserManager.addNewLoginedUser(userInfo,
					communication);
		} else {
			loginResult = false;
		}
		LoginProtocol.encodeLoginRespond(loginResult, userInfo.getUser()
				.getUserID(), communication);
		Log.d(TAG, "longin result = " + loginResult + ", userInfo = "
				+ userInfo);

		if (loginResult) {
			UserUpdateProtocol.encodeUpdateAllUser(mContext);
		}
	}

	/**
	 * Call back interface for login activity.
	 * 
	 */
	public interface ILoginRequestCallBack {
		/**
		 * When a client requests login, this method will notify the server.
		 * 
		 * @param userInfo
		 * @param communication
		 */
		void onLoginRequest(UserInfo userInfo, SocketCommunication communication);
	}

	/**
	 * Call back interface for login activity.
	 * 
	 */
	public interface ILoginRespondCallback {
		/**
		 * When the server responds the login request and allows login, this
		 * method will notify the request client.
		 * 
		 * @param localUser
		 * @param communication
		 */
		void onLoginSuccess(User localUser, SocketCommunication communication);

		/**
		 * When the server responds the login request and disallow login, this
		 * method will notify the request client.
		 * 
		 * @param failReason
		 * @param communication
		 */
		void onLoginFail(int failReason, SocketCommunication communication);
	}
}
