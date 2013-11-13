package com.zhaoyan.communication;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.dreamlink.communication.aidl.OnCommunicationListenerExternal;
import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.CallBacks.ILoginRequestCallBack;
import com.zhaoyan.communication.CallBacks.ILoginRespondCallback;
import com.zhaoyan.communication.FileSender.OnFileSendListener;
import com.zhaoyan.communication.SocketClientTask.OnConnectedToServerListener;
import com.zhaoyan.communication.SocketCommunication.OnCommunicationChangedListener;
import com.zhaoyan.communication.SocketCommunication.OnReceiveMessageListener;
import com.zhaoyan.communication.SocketServerTask.OnClientConnectedListener;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.communication.protocol.FileTransferInfo;
import com.zhaoyan.communication.protocol.ProtocolDecoder;
import com.zhaoyan.communication.protocol.ProtocolEncoder;

/**
 * This class is used for providing communication operations for activity.</br>
 * 
 * This class is single instance, so use {@link #getInstance(Context)} to get
 * object.
 * 
 */
public class SocketCommunicationManager implements OnClientConnectedListener,
		OnConnectedToServerListener, OnCommunicationChangedListener,
		OnReceiveMessageListener, OnUserChangedListener, ILoginRequestCallBack,
		ILoginRespondCallback {
	private static final String TAG = "SocketCommunicationManager";

	/**
	 * Interface of SocketCommunication. </br>
	 * 
	 * Notice: </br>
	 * 
	 * 1. Message in this interface is not encoded with Protocol.</br>
	 * 
	 * 2. This is only used before Login operation. After Login success, use
	 * OnCommunicationListenerExternal instead of this.</br>
	 * 
	 */
	public interface OnCommunicationListener {
		/**
		 * Received a message from communication.</br>
		 * 
		 * Be careful, this method is not run in UI thread. If do UI operation,
		 * we can use {@link android.os.Handler}.</br>
		 * 
		 * Message in this method is not encoded with Protocol.</br>
		 * 
		 * @param msg
		 *            the message.
		 * @param communication
		 *            the message from.
		 * 
		 */
		void onReceiveMessage(byte[] msg, SocketCommunication communication);

		void onSendResult(byte[] msg);

		/**
		 * There is new communication established or a communication lost.
		 */
		void notifyConnectChanged();

	}

	public interface OnFileTransportListener {
		void onReceiveFile(FileReceiver fileReceiver);
	}

	private static SocketCommunicationManager mInstance;

	private Context mContext;
	private Notice mNotice;

	private Vector<SocketCommunication> mCommunications;
	/** Thread pool */
	private ExecutorService mExecutorService = null;

	private Vector<OnCommunicationListener> mOnCommunicationListeners;

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
	private ConcurrentHashMap<OnFileTransportListener, Integer> mOnFileTransportListener = new ConcurrentHashMap<SocketCommunicationManager.OnFileTransportListener, Integer>();
	private UserManager mUserManager = UserManager.getInstance();
	private ProtocolDecoder mProtocolDecoder;

	/** Used for Login confirm UI */
	private ILoginRequestCallBack mLoginRequestCallBack;
	private ILoginRespondCallback mLoginRespondCallback;

	private SocketCommunicationManager() {

	}

	private SocketCommunicationManager(Context context) {
		mContext = context;
		mOnCommunicationListeners = new Vector<OnCommunicationListener>();
		mNotice = new Notice(context);
		mCommunications = new Vector<SocketCommunication>();

		mUserManager.setLocalUser(UserHelper.loadUser(context));
		mUserManager.registerOnUserChangedListener(this);
		mProtocolDecoder = new ProtocolDecoder(this);
		mProtocolDecoder.setLoginRequestCallBack(this);
		mProtocolDecoder.setLoginRespondCallback(this);
	}

	public static synchronized SocketCommunicationManager getInstance(
			Context context) {
		if (mInstance == null) {
			mInstance = new SocketCommunicationManager(
					context.getApplicationContext());
		}
		return mInstance;
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

	public void setLoginRequestCallBack(ILoginRequestCallBack callback) {
		mLoginRequestCallBack = callback;
	}

	public void setLoginRespondCallback(ILoginRespondCallback callback) {
		mLoginRespondCallback = callback;
	}

	/**
	 * Send message to communication.</br>
	 * 
	 * for internal use.
	 * 
	 * @param communication
	 * @param message
	 */
	public void sendMessage(SocketCommunication communication, byte[] message) {
		if (message.length == 0) {
			return;
		}
		if (communication != null) {
			communication.sendMessage(message);
		} else {
			mNotice.showToast("Connection lost.");
		}
	}

	public void sendFile(File file, OnFileSendListener listener,
			User receiveUser, int appID) {
		sendFile(file, listener, receiveUser, appID, null);
	}

	public void sendFile(File file, OnFileSendListener listener,
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
			return;
		}
		InetAddress inetAddress = NetWorkUtil.getLocalInetAddress();
		if (inetAddress == null) {
			Log.e(TAG,
					"sendFile error, get inet address fail. file = "
							+ file.getName());
			return;
		}
		int userID = receiveUser.getUserID();
		byte[] inetAddressData = inetAddress.getAddress();
		byte[] data = ProtocolEncoder.encodeSendFile(mUserManager
				.getLocalUser().getUserID(), receiveUser.getUserID(), appID,
				inetAddressData, serverPort, new FileTransferInfo(file,
						mContext));
		SocketCommunication communication = mUserManager
				.getSocketCommunication(userID);
		if (communication != null) {
			communication.sendMessage(data);
		} else {
			Log.e(TAG, "sendFile fail. can not connect with the receiver: "
					+ receiveUser);
		}
	}

	/**
	 * 
	 * This is used by ProtocolDecoder.
	 * 
	 * @param sendUserID
	 * @param appID
	 * @param serverAddress
	 * @param serverPort
	 * @param fileInfo
	 */
	public void notfiyFileReceiveListeners(int sendUserID, int appID,
			byte[] serverAddress, int serverPort,
			FileTransferInfo fileTransferInfo) {
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
						serverAddress, serverPort, fileTransferInfo);
				entry.getKey().onReceiveFile(fileReceiver);
			}
		}
	}

	/**
	 * Stop all communications. </br>
	 * 
	 * Notice, this method should not be called by apps.</br>
	 */
	public void closeAllCommunication() {
		if (mCommunications != null) {
			synchronized (mCommunications) {
				for (final SocketCommunication communication : mCommunications) {
					new Thread() {
						@Override
						public void run() {
							communication.stopComunication();
						}
					}.start();
				}
			}
		}
		mCommunications.clear();
		if (SocketServer.getInstance() != null) {
			SocketServer.getInstance().stopServer();
		}
		if (mExecutorService != null) {
			mExecutorService.shutdown();
		}
	}

	/**
	 * Start a communication.
	 * 
	 * @param socket
	 */
	public void startCommunication(Socket socket) {
		if (mExecutorService == null) {
			mExecutorService = Executors.newCachedThreadPool();
		}
		SocketCommunication communication = new SocketCommunication(socket,
				this);
		communication.setOnCommunicationChangedListener(this);
		try {
			mExecutorService.execute(communication);
		} catch (RejectedExecutionException e) {
			Log.e(TAG, "addCommunication fail." + e.toString());
		}

	}

	/**
	 * Get all communications
	 * 
	 * @return
	 */
	public Vector<SocketCommunication> getCommunications() {
		return mCommunications;
	}

	@Override
	public void OnCommunicationEstablished(SocketCommunication communication) {
		synchronized (mCommunications) {
			mCommunications.add(communication);
			if (!SocketServer.getInstance().isServerStarted()) {
				sendLoginRequest();
			}
			if (!mCommunications.isEmpty()) {
				for (SocketCommunication comm : mCommunications) {
					if ((comm.getConnectedAddress().equals(communication
							.getConnectedAddress()))
							&& (comm.getId() != communication.getId())) {
						comm.stopComunication();
					}
				}
			}
			notifyComunicationChange();
		}
	}

	@Override
	public void OnCommunicationLost(SocketCommunication communication) {
		mCommunications.remove(communication);
		notifyComunicationChange();
		if (mCommunications.isEmpty()) {
			if (mExecutorService != null) {
				mExecutorService.shutdown();
				mExecutorService = null;
			}
		}
		mUserManager.removeUser(communication);
		mUserManager.removeLocalCommunication(communication);
		sendMessageToUpdateAllUser();
	}

	public void registered(OnCommunicationListener iSubscribe) {
		mOnCommunicationListeners.add(iSubscribe);
	}

	public void unregistered(OnCommunicationListener iSubscribe) {
		mOnCommunicationListeners.remove(iSubscribe);
	}

	@Override
	public void onReceiveMessage(byte[] msg,
			SocketCommunication socketCommunication) {
		// decode;
		long start = System.currentTimeMillis();
		mProtocolDecoder.decode(msg, socketCommunication);
		long end = System.currentTimeMillis();
		Log.i(TAG, "onReceiveMessage() decode takes time: " + (end - start));
	}

	/**
	 * In the WiFi Direct network. record the communications which connect us as
	 * clients.</br>
	 * 
	 * Key means user ID assigned by us(Note, the user ID will reassigned by the
	 * Server we connected.). Value is the SocketCommunication.</br>
	 */
	private ConcurrentHashMap<Integer, SocketCommunication> mLocalCommunications = new ConcurrentHashMap<Integer, SocketCommunication>();
	private int mLastLocalID = 0;

	public int addLocalCommunicaiton(SocketCommunication communication) {
		if (!mLocalCommunications.contains(communication)) {
			mLastLocalID++;
			mLocalCommunications.put(mLastLocalID, communication);
			return mLastLocalID;
		} else {
			int id = 0;
			for (Map.Entry<Integer, SocketCommunication> entry : mLocalCommunications
					.entrySet()) {
				if (communication == entry.getValue()) {
					id = entry.getKey();
					break;
				}
			}
			return id;
		}
	}

	public void removeLocalCommunicaiton(int id) {
		mLocalCommunications.remove(id);
	}

	public SocketCommunication getLocalCommunicaiton(int id) {
		return mLocalCommunications.get(id);
	}

	/**
	 * client login server directly.
	 */
	public void sendLoginRequest() {
		mUserManager.setLocalUser(UserHelper.loadUser(mContext));
		byte[] loginRequest = ProtocolEncoder.encodeLoginRequest(mUserManager
				.getLocalUser());
		sendMessageToAllWithoutEncode(loginRequest);
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
		byte[] data = ProtocolEncoder.encodeSendMessageToSingle(msg,
				localUserID, receiveUserID, appID);
		sendMessageToAllWithoutEncode(data);
	}

	/**
	 * Send message to all users in the network.
	 * 
	 * @param msg
	 */
	public void sendMessageToAll(byte[] msg, int appID) {
		Log.d(TAG, "sendMessageToAll.msg.=" + new String(msg));
		int localUserID = mUserManager.getLocalUser().getUserID();
		byte[] data = ProtocolEncoder.encodeSendMessageToAll(msg, localUserID,
				appID);
		sendMessageToAllWithoutEncode(data);
	}

	/**
	 * send the message to all users in the network.
	 * 
	 * @param msg
	 */
	public void sendMessageToAllWithoutEncode(byte[] msg) {
		synchronized (mCommunications) {
			for (SocketCommunication communication : mCommunications) {
				sendMessage(communication, msg);
			}
		}

	}

	/**
	 * @param addFlag
	 *            ,if true ,connect add ,else connect remove
	 * */
	private void notifyComunicationChange() {
		// if need notify someone ,doing here
		if (!mOnCommunicationListeners.isEmpty()) {
			for (OnCommunicationListener listener : mOnCommunicationListeners) {
				listener.notifyConnectChanged();
			}
		}
	}

	public boolean isConnected() {
		User localUser = mUserManager.getLocalUser();
		if (localUser == null) {
			return false;
		}

		if (localUser.getUserID() == 0) {
			return false;
		} else if (UserManager.isManagerServer(localUser)) {
			if (mCommunications.isEmpty()
					&& !SocketServer.getInstance().isServerStarted()) {
				return false;
			} else {
				return true;
			}
		} else {
			if (mCommunications.isEmpty()) {
				return false;
			}
		}

		return true;
	}

	public boolean isServerAndCreated() {
		User localUser = mUserManager.getLocalUser();
		if (localUser != null && UserManager.isManagerServer(localUser)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Start Server.
	 * 
	 * @param context
	 */
	public void startServer(Context context) {
		SocketServerTask serverTask = new SocketServerTask(context,
				SocketCommunication.PORT);
		serverTask.setOnClientConnectedListener(this);
		serverTask.start();
		PlatformManager platformManager = PlatformManager.getInstance(mContext);

		Intent intent = new Intent();
		intent.setClass(mContext, LoginService.class);
		mContext.startService(intent);
	}

	/**
	 * Stop server.
	 */
	public void stopServer() {
		SocketServer server = SocketServer.getInstance();
		server.stopServer();

		Intent intent = new Intent();
		intent.setClass(mContext, LoginService.class);
		mContext.stopService(intent);
	}

	/**
	 * Update user when user connect and disconnect.
	 */
	private void sendMessageToUpdateAllUser() {
		byte[] allUserData = ProtocolEncoder.encodeUpdateAllUser(mUserManager);
		sendMessageToAllWithoutEncode(allUserData);
	}

	/**
	 * Connect to server.
	 * 
	 * @param context
	 *            Activity context.
	 * @param serverIp
	 */
	public void connectServer(Context context, String serverIp) {
		SocketClientTask clientTask = new SocketClientTask(context);
		clientTask.setOnConnectedToServerListener(this);
		clientTask.execute(new String[] { serverIp,
				String.valueOf(SocketCommunication.PORT) });
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
	public void notifyReceiveListeners(int sendUserID, int appID, byte[] data) {
		// int index =
		// SocketCommunicationService.mCallBackList.beginBroadcast();
		// for (int i = 0; i < index; i++) {
		// int app_ip = (Integer) SocketCommunicationService.mCallBackList
		// .getBroadcastCookie(i);
		// if (app_ip == appID) {
		// OnCommunicationListenerExternal l = (OnCommunicationListenerExternal)
		// SocketCommunicationService.mCallBackList
		// .getBroadcastItem(i);
		// try {
		// l.onReceiveMessage(data,
		// mUserManager.getAllUser().get(sendUserID));
		// } catch (RemoteException e) {
		// Log.e(TAG,
		// "notifyReceiveListeners SocketCommunicationService listener error."
		// + e);
		// }
		// }
		// }
		// SocketCommunicationService.mCallBackList.finishBroadcast();
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

	@Override
	public void onUserConnected(User user) {

		// int index =
		// SocketCommunicationService.mCallBackList.beginBroadcast();
		// for (int i = 0; i < index; i++) {
		// OnCommunicationListenerExternal l = (OnCommunicationListenerExternal)
		// SocketCommunicationService.mCallBackList
		// .getBroadcastItem(i);
		// try {
		// l.onUserConnected(user);
		// } catch (RemoteException e) {
		// Log.e(TAG, "onUserConnected SocketCommunicationService error."
		// + e);
		// }
		// }
		// SocketCommunicationService.mCallBackList.finishBroadcast();

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

		// int index =
		// SocketCommunicationService.mCallBackList.beginBroadcast();
		// for (int i = 0; i < index; i++) {
		// OnCommunicationListenerExternal l = (OnCommunicationListenerExternal)
		// SocketCommunicationService.mCallBackList
		// .getBroadcastItem(i);
		// try {
		// l.onUserDisconnected(user);
		// } catch (RemoteException e) {
		// Log.e(TAG,
		// "onUserDisconnected SocketCommunicationService error."
		// + e);
		// }
		// }
		// SocketCommunicationService.mCallBackList.finishBroadcast();

		for (Map.Entry<OnCommunicationListenerExternal, Integer> entry : mOnCommunicationListenerExternals
				.entrySet()) {
			try {
				entry.getKey().onUserDisconnected(user);
			} catch (RemoteException e) {
				Log.e(TAG, "onUserDisconnected error." + e);
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
	public void onLoginRequest(User user, SocketCommunication communication) {
		Log.d(TAG, "onLoginRequest()");
		if (mLoginRequestCallBack != null) {
			mLoginRequestCallBack.onLoginRequest(user, communication);
		}

	}

	/**
	 * Respond to the login request.
	 * 
	 * @param user
	 * @param communication
	 * @param isAllow
	 */
	public void respondLoginRequest(User user,
			SocketCommunication communication, boolean isAllow) {
		mProtocolDecoder.respondLoginRequest(user, communication, isAllow);
	}

	@Override
	public void onClientConnected(Socket clientSocket) {
		Log.d(TAG, "onClientConnected ip = "
				+ clientSocket.getInetAddress().getHostAddress());
		startCommunication(clientSocket);
	}

	@Override
	public void onConnectedToServer(Socket socket) {
		startCommunication(socket);
	}

	// For debug begin.
	/**
	 * This method is used for debug.
	 * 
	 * @return
	 */
	public String getOnCommunicationListenerExternalStatus() {
		StringBuffer status = new StringBuffer();
		status.append("Total size: " + mOnCommunicationListenerExternals.size()
				+ "\n");
		status.append(mOnCommunicationListenerExternals.toString() + "\n");
		return status.toString();
	}

	public String getOnCommunicationListenerStatus() {
		StringBuffer status = new StringBuffer();
		status.append("Total size: " + mOnCommunicationListeners.size() + "\n");
		status.append(mOnCommunicationListeners.toString() + "\n");
		return status.toString();
	}

	public String getOnFileTransportListenerStatus() {
		StringBuffer status = new StringBuffer();
		status.append("Total size: " + mOnFileTransportListener.size() + "\n");
		status.append(mOnFileTransportListener.toString() + "\n");
		return status.toString();
	}
	// For debug end.
}
