package com.zhaoyan.communication;

import com.dreamlink.communication.aidl.User;

public interface CallBacks {
	/**
	 * Call back interface for login activity.
	 * 
	 */
	public interface ILoginRequestCallBack {
		/**
		 * When a client requests login, this method will notify the server.
		 * 
		 * @param user
		 * @param communication
		 */
		void onLoginRequest(User user, SocketCommunication communication);
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
