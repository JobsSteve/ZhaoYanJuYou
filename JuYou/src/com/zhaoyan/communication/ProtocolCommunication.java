package com.zhaoyan.communication;

import java.io.File;
import java.net.InetAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.dreamlink.communication.aidl.OnCommunicationListenerExternal;
import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.FileSender.OnFileSendListener;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.communication.protocol.FileTransportProtocol;
import com.zhaoyan.communication.protocol.LoginProtocol;
import com.zhaoyan.communication.protocol.LogoutProtocol;
import com.zhaoyan.communication.protocol.MessageSendProtocol;
import com.zhaoyan.communication.protocol.ProtocolManager;
import com.zhaoyan.communication.protocol.UserUpdateProtocol;
import com.zhaoyan.communication.protocol.FileTransportProtocol.FileInfo;
import com.zhaoyan.juyou.R;

import android.content.Context;
import android.os.RemoteException;

/**
 * This class provide common interface for protocols.
 * 
 * 1. use protocols to communicate.</br>
 * 
 * 2. notify protocols' events to listeners.
 * 
 */
public class ProtocolCommunication implements OnUserChangedListener {
	private static final String TAG = "ProtocolCommunication";
	private static ProtocolCommunication mInstance;
	private Context mContext;
	/** Used for Login confirm UI */
	private ILoginRequestCallBack mLoginRequestCallBack;
	private ILoginRespondCallback mLoginRespondCallback;

	private UserManager mUserManager;
	private Notice mNotice;
	private ProtocolManager mProtocolManager;

	/**
	 * Map for OnFileTransportListener and appID management. When an application
	 * register to SocketCommunicationManager, record it in this map. When
	 * received a message, notify the related applications base on the
	 * appID.</br>
	 * 
	 * Map structure</br>
	 * 
	 * key: listener, value: app ID.
	 */
	private ConcurrentHashMap<OnFileTransportListener, Integer> mOnFileTransportListener = new ConcurrentHashMap<OnFileTransportListener, Integer>();
	/**
	 * Map for OnCommunicationListenerExternal and appID management. When an
	 * application register to SocketCommunicationManager, record it in this
	 * map. When received a message, notify the related applications base on the
	 * appID.</br>
	 * 
	 * Map structure</br>
	 * 
	 * key: listener, value: app ID.
	 */
	private ConcurrentHashMap<OnCommunicationListenerExternal, Integer> mOnCommunicationListenerExternals = new ConcurrentHashMap<OnCommunicationListenerExternal, Integer>();

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
		mUserManager.registerOnUserChangedListener(this);
		mNotice = new Notice(mContext);
		mProtocolManager = new ProtocolManager(mContext);
		mProtocolManager.init();
	}

	public void release() {
		mInstance = null;
		if (mUserManager != null) {
			mUserManager.unregisterOnUserChangedListener(this);
		}
	}

	public void decodeMessage(byte[] msg,
			SocketCommunication socketCommunication) {
		long start = System.currentTimeMillis();
		mProtocolManager.decode(msg, socketCommunication);
		long end = System.currentTimeMillis();
		Log.i(TAG, "decodeMessage() takes time: " + (end - start));
	}

	public void registerOnCommunicationListenerExternal(
			OnCommunicationListenerExternal listener, int appID) {
		Log.d(TAG, "registerOnCommunicationListenerExternal() appID = " + appID);
		mOnCommunicationListenerExternals.put(listener, appID);
	}

	public void unregisterOnCommunicationListenerExternal(
			OnCommunicationListenerExternal listener) {
		if (listener == null) {
			Log.e(TAG, "the params listener is null");
		} else {
			if (mOnCommunicationListenerExternals.containsKey(listener)) {
				int appID = mOnCommunicationListenerExternals.remove(listener);
				Log.d(TAG, "registerOnCommunicationListenerExternal() appID = "
						+ appID);
			} else {
				Log.e(TAG, "there is no this listener in the map");
			}
		}
	}

	public void unregisterOnCommunicationListenerExternal(int appId) {
		for (Entry<OnCommunicationListenerExternal, Integer> entry : mOnCommunicationListenerExternals
				.entrySet()) {
			if (entry.getValue() == appId) {
				mOnCommunicationListenerExternals.remove(entry.getKey());
			}
		}
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
	 * client login server directly.
	 */
	public void sendLoginRequest() {
		LoginProtocol.encodeLoginRequest(mContext);
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

	public void logout() {
		Log.d(TAG, "logout()");
		SocketCommunicationManager socketCommunicationManager = SocketCommunicationManager
				.getInstance();
		boolean isServer = socketCommunicationManager.isServerAndCreated();
		boolean isConnectedToServer = socketCommunicationManager.isConnected();
		if (isServer) {
			LogoutProtocol.encodeLogoutSever(mContext);
		} else if (isConnectedToServer) {
			LogoutProtocol.encodeLogoutClient(mContext);
		}

		UserManager userManager = UserManager.getInstance();
		userManager.resetLocalUser();
	}

	public void registerOnFileTransportListener(
			OnFileTransportListener listener, int appID) {
		Log.d(TAG, "registerOnFileTransportListener() appID = " + appID);
		mOnFileTransportListener.put(listener, appID);
	}

	public void unregisterOnFileTransportListener(
			OnFileTransportListener listener) {
		if (null == listener) {
			Log.e(TAG, "the params listener is null");
		} else {
			if (mOnFileTransportListener.containsKey(listener)) {
				int appID = mOnFileTransportListener.remove(listener);
				Log.d(TAG, "mOnFileTransportListener() appID = " + appID);
			} else {
				Log.e(TAG, "there is no this listener in the map");
			}
		}
	}

	// @Snow.Tian, Cancel Send File
	public void cancelSendFile(User receiveUser, int appID) {
		Log.d(TAG, "cancelSendFile: " + receiveUser.getUserName()
				+ ", appID = " + appID);
		boolean result = FileTransportProtocol.encodeCancelSend(receiveUser,
				appID);
		if (result) {
			mNotice.showToast(R.string.cancel_send);
		} else {
			mNotice.showToast("cancelSendFile: Communcation Null!");
		}
	}

	// @Snow.Tian, Cancel Receive File
	public void cancelReceiveFile(User sendUser, int appID) {
		Log.d(TAG, "cancelReceiveFile: " + sendUser.getUserName()
				+ ", appID = " + appID);
		boolean result = FileTransportProtocol.encodeCancelReceive(sendUser,
				appID);
		if (result) {
			mNotice.showToast(R.string.cancel_receive);
		} else {
			mNotice.showToast("cancelReceiveFile: Communcation Null!");
		}
	}

	/**
	 * Send file to the receive user.
	 * 
	 * @param file
	 * @param listener
	 * @param receiveUser
	 * @param appID
	 */
	public void sendFile(File file, OnFileSendListener listener,
			User receiveUser, int appID) {
		sendFile(file, listener, receiveUser, appID, null);
	}

	/**
	 * Send file to the receive user.
	 * 
	 * @param file
	 * @param listener
	 * @param receiveUser
	 * @param appID
	 * @param key
	 *            Key is used for marking different FileSenders.
	 * @return
	 */
	public FileSender sendFile(File file, OnFileSendListener listener,
			User receiveUser, int appID, Object key) {
		Log.d(TAG, "sendFile() file = " + file.getName() + ", receive user = "
				+ receiveUser.getUserName() + ", appID = " + appID);
		FileSender fileSender = null;
		if (key == null) {
			fileSender = new FileSender();
		} else {
			fileSender = new FileSender(key);
		}

		int serverPort = fileSender.sendFile(file, listener);
		if (serverPort == -1) {
			Log.e(TAG, "sendFile error, create socket server fail. file = "
					+ file.getName());
			return fileSender;
		}
		InetAddress inetAddress = NetWorkUtil.getLocalInetAddress();
		if (inetAddress == null) {
			Log.e(TAG,
					"sendFile error, get inet address fail. file = "
							+ file.getName());
			return fileSender;
		}

		FileTransportProtocol.encodeSendFile(receiveUser, appID, serverPort,
				file, mContext);
		return fileSender;
	}

	/**
	 * Notify all registered file receive listener.</br>
	 * 
	 * This is used by {@link FileTransportProtocol}. When receive a file from
	 * {@link FileTransportProtocol}, this method will be called.
	 * 
	 * @param sendUserID
	 * @param appID
	 * @param serverAddress
	 * @param serverPort
	 * @param fileInfo
	 */
	public void notfiyFileReceiveListeners(int sendUserID, int appID,
			byte[] serverAddress, int serverPort, FileInfo fileInfo) {
		for (Map.Entry<OnFileTransportListener, Integer> entry : mOnFileTransportListener
				.entrySet()) {
			if (entry.getValue() == appID) {
				User sendUser = mUserManager.getAllUser().get(sendUserID);
				if (sendUser == null) {
					Log.e(TAG,
							"notfiyFileReceiveListeners cannot find send user, send user id = "
									+ sendUserID);
					return;
				}
				FileReceiver fileReceiver = new FileReceiver(sendUser,
						serverAddress, serverPort, fileInfo);
				entry.getKey().onReceiveFile(fileReceiver);
			}
		}
	}

	/**
	 * Notify all listeners that we received a message sent by the user with the
	 * ID sendUserID for us.
	 * 
	 * This is used by ProtocolDecoder.
	 * 
	 * @param sendUserID
	 * @param appID
	 * @param data
	 */
	public void notifyMessageReceiveListeners(int sendUserID, int appID,
			byte[] data) {
		for (Map.Entry<OnCommunicationListenerExternal, Integer> entry : mOnCommunicationListenerExternals
				.entrySet()) {
			if (entry.getValue() == appID) {
				try {
					entry.getKey().onReceiveMessage(data,
							mUserManager.getAllUser().get(sendUserID));
				} catch (RemoteException e) {
					Log.e(TAG, "notifyReceiveListeners error." + e);
				}
			}
		}
	}

	/**
	 * Send message to the receiver.
	 * 
	 * @param msg
	 * @param receiveUser
	 * @param appID
	 */
	public void sendMessageToSingle(byte[] msg, User receiveUser, int appID) {
		int localUserID = mUserManager.getLocalUser().getUserID();
		int receiveUserID = receiveUser.getUserID();
		MessageSendProtocol.encodeSendMessageToSingle(msg, localUserID,
				receiveUserID, appID);
	}

	/**
	 * Send message to all users in the network.
	 * 
	 * @param msg
	 */
	public void sendMessageToAll(byte[] msg, int appID) {
		Log.d(TAG, "sendMessageToAll.msg.=" + new String(msg));
		int localUserID = mUserManager.getLocalUser().getUserID();
		MessageSendProtocol.encodeSendMessageToAll(msg, localUserID, appID);
	}

	/**
	 * Update user when user connect and disconnect.
	 */
	public void sendMessageToUpdateAllUser() {
		UserUpdateProtocol.encodeUpdateAllUser(mContext);
	}

	@Override
	public void onUserConnected(User user) {
		for (Map.Entry<OnCommunicationListenerExternal, Integer> entry : mOnCommunicationListenerExternals
				.entrySet()) {
			try {
				entry.getKey().onUserConnected(user);
			} catch (RemoteException e) {
				Log.e(TAG, "onUserConnected error." + e);
			}
		}
	}

	@Override
	public void onUserDisconnected(User user) {
		for (Map.Entry<OnCommunicationListenerExternal, Integer> entry : mOnCommunicationListenerExternals
				.entrySet()) {
			try {
				entry.getKey().onUserDisconnected(user);
			} catch (RemoteException e) {
				Log.e(TAG, "onUserDisconnected error." + e);
			}
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

	public interface OnFileTransportListener {
		void onReceiveFile(FileReceiver fileReceiver);
	}

	public String getOnFileTransportListenerStatus() {
		StringBuffer status = new StringBuffer();
		status.append("Total size: " + mOnFileTransportListener.size() + "\n");
		status.append(mOnFileTransportListener.toString() + "\n");
		return status.toString();
	}

	public String getOnCommunicationListenerExternalStatus() {
		StringBuffer status = new StringBuffer();
		status.append("Total size: " + mOnCommunicationListenerExternals.size()
				+ "\n");
		status.append(mOnCommunicationListenerExternals.toString() + "\n");
		return status.toString();
	}

}
