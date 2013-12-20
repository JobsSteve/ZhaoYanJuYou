package com.zhaoyan.communication;

import java.net.Socket;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import android.content.Context;
import android.content.Intent;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketClientTask.OnConnectedToServerListener;
import com.zhaoyan.communication.SocketCommunication.OnCommunicationChangedListener;
import com.zhaoyan.communication.SocketCommunication.OnReceiveMessageListener;
import com.zhaoyan.communication.SocketServerTask.OnClientConnectedListener;
import com.zhaoyan.communication.recovery.CommunicationRecovery;

/**
 * This class is used for providing socket communication operations.</br>
 * 
 * This class is single instance, so use {@link #getInstance(Context)} to get
 * object.
 * 
 */
public class SocketCommunicationManager implements OnClientConnectedListener,
		OnConnectedToServerListener, OnCommunicationChangedListener,
		OnReceiveMessageListener {
	private static final String TAG = "SocketCommunicationManager";

	private static SocketCommunicationManager mInstance;
	private Context mContext;
	private Notice mNotice;
	private Vector<SocketCommunication> mCommunications;
	private ExecutorService mExecutorService = null;
	private UserManager mUserManager = UserManager.getInstance();

	private SocketCommunicationManager() {

	}

	/**
	 * Get instance without context. If this is first called, must initialize
	 * context by {@link #init(Context)}.
	 * 
	 * @return
	 */
	public static synchronized SocketCommunicationManager getInstance() {
		if (mInstance == null) {
			mInstance = new SocketCommunicationManager();
		}
		return mInstance;
	}

	/**
	 * Initialize with application context.
	 * 
	 * @param context
	 */
	public void init(Context context) {
		mContext = context;
		mNotice = new Notice(context);
		mCommunications = new Vector<SocketCommunication>();
		mUserManager.init(context);
	}

	/**
	 * Release resource.
	 */
	public void release() {
		mInstance = null;
		UserManager.getInstance().release();

		SocketServer.getInstance().release();
		PlatformManager.getInstance(mContext).release();
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
					communication.stopComunication();
				}
			}
			mCommunications.clear();
		}

		if (SocketServer.getInstance() != null) {
			SocketServer.getInstance().stopServer();
		}
		if (mExecutorService != null) {
			mExecutorService.shutdown();
			mExecutorService = null;

		}

		stopScreenMonitor();
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

	public void stopCommunication(SocketCommunication socketCommunication) {
		socketCommunication.stopComunication();
		mCommunications.remove(socketCommunication);

		if (mCommunications.isEmpty()) {
			stopScreenMonitor();
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
		}
		startScreenMonitor();
	}

	@Override
	public void OnCommunicationLost(SocketCommunication communication) {
		Log.d(TAG, "OnCommunicationLost " + communication);
		CommunicationRecovery recovery = new CommunicationRecovery(mContext);
		recovery.getLastSatus();
		
		mCommunications.remove(communication);
		if (mCommunications.isEmpty()) {
			if (mExecutorService != null) {
				mExecutorService.shutdown();
				mExecutorService = null;
			}
		}
		mUserManager.removeUser(communication);
		mUserManager.removeLocalCommunication(communication);

		if (!mCommunications.isEmpty()) {
			sendMessageToUpdateAllUser();
		}

		if (mCommunications.isEmpty()) {
			stopScreenMonitor();
		}
		
		recovery.attemptRecovery();
	}

	private void startScreenMonitor() {
		Intent intent = new Intent();
		intent.setClass(mContext, ScreenMonitor.class);
		mContext.startService(intent);
	}

	private void stopScreenMonitor() {
		Intent intent = new Intent();
		intent.setClass(mContext, ScreenMonitor.class);
		mContext.stopService(intent);
	}

	@Override
	public void onReceiveMessage(byte[] msg,
			SocketCommunication socketCommunication) {
		ProtocolCommunication protocolCommunication = ProtocolCommunication
				.getInstance();
		protocolCommunication.decodeMessage(msg, socketCommunication);
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
		ProtocolCommunication protocolCommunication = ProtocolCommunication
				.getInstance();
		protocolCommunication.sendLoginRequest();
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

	public void sendMessageToSingleWithoutEncode(byte[] data, int receiveUserId) {
		SocketCommunication communication = mUserManager.getAllCommmunication()
				.get(receiveUserId);
		if (communication != null) {
			communication.sendMessage(data);
		} else {
			Log.e(TAG, "sendMessageToSingleWithoutEncode cannot find receiver "
					+ receiveUserId);
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
			if (mCommunications.isEmpty()
					|| mUserManager.getAllUser().size() == 0) {
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

	public boolean isServerSocketStarted() {
		return SocketServer.getInstance().isServerStarted();
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
		ProtocolCommunication protocolCommunication = ProtocolCommunication
				.getInstance();
		protocolCommunication.sendMessageToUpdateAllUser();
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
}
