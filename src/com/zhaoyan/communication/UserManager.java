package com.zhaoyan.communication;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.provider.JuyouData;

/**
 * Management user and user's communication.
 * 
 */
public class UserManager {
	public static final int SERVER_USER_ID = -1;
	private static final int DEFAULT_USER_ID = 0;

	/**
	 * Interface for monitor the user changes.
	 * 
	 */
	public interface OnUserChangedListener {
		/**
		 * There is new user connected.
		 */
		void onUserConnected(User user);

		/**
		 * There is a user disconnected.
		 */
		void onUserDisconnected(User user);
	}

	private static final String TAG = "UserManager";
	/** user id : user communication */
	private ConcurrentHashMap<Integer, SocketCommunication> mCommunications = new ConcurrentHashMap<Integer, SocketCommunication>();
	/** user id : user communication */
	private ConcurrentHashMap<Integer, SocketCommunication> mLocalCommunications = new ConcurrentHashMap<Integer, SocketCommunication>();
	/** user id : user info */
	private ConcurrentHashMap<Integer, User> mUsers = new ConcurrentHashMap<Integer, User>();
	private int mLastUserId = 0;
	private static UserManager mInstance;
	private Vector<OnUserChangedListener> mOnUserChangedListeners = new Vector<OnUserChangedListener>();
	private Context mContext;

	private User mLocalUser = new User();

	public User getLocalUser() {
		return mLocalUser;
	}

	public void setLocalUser(User localUser) {
		mLocalUser = localUser;
	}

	/**
	 * set local user info and save into database.
	 * 
	 * @param userInfo
	 */
	public void setLocalUserInfo(UserInfo userInfo) {
		mLocalUser = userInfo.getUser();
		UserHelper.saveLocalUser(mContext, userInfo);
	}

	private UserManager() {

	}

	public synchronized static UserManager getInstance() {
		if (mInstance == null) {
			mInstance = new UserManager();
		}
		return mInstance;
	}

	public void init(Context context) {
		mContext = context;
		// Reset local user.
		resetLocalUser();
	}

	public void resetLocalUser() {
		UserInfo userInfo = UserHelper.loadLocalUser(mContext);
		if (userInfo != null) {
			userInfo.getUser().setUserID(DEFAULT_USER_ID);
			userInfo.setStatus(JuyouData.User.STATUS_DISCONNECT);
			UserHelper.saveLocalUser(mContext, userInfo);

			mLocalUser = userInfo.getUser();
			clear();
		} else {
			mLocalUser = new User();
		}
	}

	public void registerOnUserChangedListener(OnUserChangedListener listener) {
		if (!mOnUserChangedListeners.contains(listener)) {
			mOnUserChangedListeners.add(listener);
		}
	}

	public void unregisterOnUserChangedListener(OnUserChangedListener listener) {
		mOnUserChangedListeners.remove(listener);
	}

	/**
	 * A Remote user has login success. Add the remote user and communication
	 * into map.
	 * 
	 * @param useInfo
	 * @param communication
	 * @return
	 */
	public synchronized boolean addNewLoginedUser(UserInfo userInfo,
			SocketCommunication communication) {
		Log.d(TAG, "addNewLoginedUser ");
		User user = userInfo.getUser();
		if (mLocalUser.getUserID() != SERVER_USER_ID) {
			Log.e(TAG, "addNewLoginedUser error, this is not server.");
			return false;
		}
		if (isUserExistInMap(user)) {
			Log.d(TAG,
					"addNewLoginedUser ignore, user is already exist in map. "
							+ user);
			return false;
		}

		// Assign user id.
		if (user.getUserID() != 0) {
			// This is for login forward but it is not in use anymore.
			// This is a client, The user is already assigned a id by server.
		} else {
			// This is the server, and here a client connected. So assign it a
			// user id.
			mLastUserId++;
			user.setUserID(mLastUserId);
		}
		// Set user status.
		userInfo.setStatus(JuyouData.User.STATUS_CONNECTED);

		// Add user into user tree
		if (!mCommunications.contains(communication)) {
			UserTree.getInstance().addUser(mLocalUser, user);
		} else {
			int n = 65535;
			for (Map.Entry<Integer, SocketCommunication> entry : mCommunications
					.entrySet()) {
				if (entry.getValue().equals(communication)
						&& n > entry.getKey()) {
					n = entry.getKey();
				}
			}
			UserTree.getInstance().addUser(mUsers.get(n), user);
		}

		addUser(userInfo, communication);
		return true;
	}

	/**
	 * When server send update all user message, add the update user into map.
	 * 
	 * @param userInfo
	 * @param communication
	 * @return
	 */
	public synchronized boolean addUpdateUser(UserInfo userInfo,
			SocketCommunication communication) {
		Log.d(TAG, "addUpdateUser " + userInfo);
		User user = userInfo.getUser();
		if (isUserExistInMap(user)) {
			Log.d(TAG, "addUpdateUser ignore, user is already exist in map. "
					+ user);
			return false;
		}
		
		if (isManagerServer(user)) {
			UserInfo localUserInfo = UserHelper.loadLocalUser(mContext);
			localUserInfo.setNetworkType(userInfo.getNetworkType());
			UserHelper.saveLocalUser(mContext, localUserInfo);
		}

		addUser(userInfo, communication);
		return true;
	}

	/**
	 * Add a remote user and communication into map and database.
	 * 
	 * @param useInfo
	 * @param communication
	 * @return
	 */
	private void addUser(UserInfo userInfo, SocketCommunication communication) {
		Log.d(TAG, "addUser " + userInfo);
		User user = userInfo.getUser();
		// Add user into map.
		mUsers.put(user.getUserID(), user);
		if (isLocalUser(user.getUserID())) {
			// TODO This is for login forward.
			mCommunications.put(user.getUserID(),
					mLocalCommunications.get(user.getUserID()));
		} else {
			mCommunications.put(user.getUserID(), communication);
		}

		// If the user is not exist in database, add it.
		if (UserHelper.getUserInfo(mContext, user) == null) {
			// The user is not exist in database
			UserHelper.addRemoteUserToDatabase(mContext, userInfo);
		}

		for (OnUserChangedListener listener : mOnUserChangedListeners) {
			listener.onUserConnected(user);
		}
	}

	/**
	 * Add a user.
	 * 
	 * @param user
	 * @param communication
	 * @return
	 * 
	 * @deprecated
	 */
	public synchronized boolean addUser(User user,
			SocketCommunication communication) {
		Log.e(TAG, "addUser is not used already.");
		return true;
	}

	/**
	 * Check the user is exist or not.
	 * 
	 * @param user
	 * @return
	 */
	public synchronized boolean isUserExistInMap(User user) {
		return mUsers.containsKey(user.getUserID());
	}

	/**
	 * When create server, add local user into communication and user map, and
	 * update database.
	 * 
	 * @return
	 */
	public synchronized boolean addLocalServerUser(int networkType) {
		mLocalUser.setUserID(SERVER_USER_ID);
		UserTree.getInstance().setHead(mLocalUser);

		// Create new network, clear old users, add this user to user map and
		// user database.
		mUsers.clear();
		mUsers.put(mLocalUser.getUserID(), mLocalUser);
		UserInfo userInfo = UserHelper.loadLocalUser(mContext);
		userInfo.getUser().setUserID(SERVER_USER_ID);
		userInfo.setStatus(JuyouData.User.STATUS_SERVER_CREATED);
		userInfo.setNetworkType(networkType);
		UserHelper.saveLocalUser(mContext, userInfo);

		// Add communication.
		mCommunications.clear();
		SocketCommunication communication = new SocketCommunication(null, null);
		mCommunications.put(mLocalUser.getUserID(), communication);

		// Notify changes.
		for (OnUserChangedListener listener : mOnUserChangedListeners) {
			listener.onUserConnected(mLocalUser);
		}
		return true;
	}

	public static synchronized boolean isManagerServer(User user) {
		if (SERVER_USER_ID == user.getUserID()) {
			return true;
		}
		return false;
	}

	public synchronized void removeUser(int id) {
		UserHelper.removeRemoteConnectedUser(mContext, id);
		User tempUser = mUsers.get(id);
		mUsers.remove(id);
		mCommunications.remove(id);
		for (OnUserChangedListener listener : mOnUserChangedListeners) {
			listener.onUserDisconnected(tempUser);
		}
		tempUser = null;
		Log.d(TAG, "remove user id = " + id);
	}

	/**
	 * Remove the user and user's communication.
	 * 
	 * @param user
	 */
	public synchronized void removeUser(User user) {
		for (Map.Entry<Integer, User> entry : mUsers.entrySet()) {
			if (user.getUserID() == (int) entry.getKey()) {
				int userId = entry.getKey();
				removeUser(userId);
			}
		}
	}

	/**
	 * Remove the user and user's communication.
	 * 
	 * @param communication
	 */
	public synchronized void removeUser(SocketCommunication communication) {
		for (Map.Entry<Integer, SocketCommunication> entry : mCommunications
				.entrySet()) {
			if (communication == entry.getValue()) {
				int userId = entry.getKey();
				removeUser(userId);
			}
		}
	}

	/**
	 * Get the SocketCommunication for the user.
	 * 
	 * @param user
	 * @return
	 */
	public synchronized SocketCommunication getSocketCommunication(User user) {
		SocketCommunication result = null;
		for (Map.Entry<Integer, User> entry : mUsers.entrySet()) {
			if (user.getUserID() == (int) entry.getKey()) {
				result = mCommunications.get(user.getUserID());
			}
		}
		return result;
	}

	/**
	 * Get the SocketCommunication for the user.
	 * 
	 * @param user
	 * @return
	 */
	public synchronized SocketCommunication getSocketCommunication(int userID) {
		return mCommunications.get(userID);
	}

	public Map<Integer, User> getAllUser() {
		return mUsers;
	}

	public Map<Integer, SocketCommunication> getAllCommmunication() {
		return mCommunications;
	}

	public void clear() {
		UserHelper.removeAllRemoteConnectedUser(mContext);
		mUsers.clear();
		mCommunications.clear();
		for (OnUserChangedListener listener : mOnUserChangedListeners) {
			listener.onUserDisconnected(mLocalUser);
		}
	}

	@Override
	public String toString() {
		return "UserManager [mCommunications=" + mCommunications + ", mUsers="
				+ mUsers + ", mLastUserId=" + mLastUserId + ", mLocalUser="
				+ mLocalUser + "]";
	}

	public void addLocalCommunication(int id, SocketCommunication communication) {
		mLocalCommunications.put(id, communication);
	}

	public void removeLocalCommunication(SocketCommunication communication) {
		if (communication != null) {
			mLocalCommunications.remove(getLocalCommunication(communication));
			return;
		}
	}

	private int getLocalCommunication(SocketCommunication communication) {
		for (Map.Entry<Integer, SocketCommunication> entry : mLocalCommunications
				.entrySet()) {
			if (entry.getValue() == communication) {
				return entry.getKey();
			}
		}
		return 0;
	}

	private boolean isLocalUser(int userID) {
		for (Map.Entry<Integer, SocketCommunication> entry : mLocalCommunications
				.entrySet()) {
			if (entry.getKey() == userID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get connected all user
	 * 
	 * @return the user list
	 */
	public ArrayList<String> getAllUserNameList() {
		ArrayList<String> allUsers = new ArrayList<String>();
		User localUser = getLocalUser();
		for (Map.Entry<Integer, User> entry : getAllUser().entrySet()) {
			if (localUser.getUserID() != (int) entry.getKey()) {
				allUsers.add(entry.getValue().getUserName());
			}
		}
		return allUsers;
	}

	/**
	 * accord the username to get the user info
	 * 
	 * @param userName
	 * @return user
	 */
	public User getUser(String userName) {
		for (Map.Entry<Integer, User> entry : getAllUser().entrySet()) {
			if (entry.getValue().getUserName().equals(userName)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Release resource.
	 */
	public void release() {
		mInstance = null;
	}

	public void setLocalUserConnected(SocketCommunication communication) {
		mUsers.clear();
		mCommunications.clear();
		mUsers.put(mLocalUser.getUserID(), mLocalUser);
		mCommunications.put(mLocalUser.getUserID(), communication);
		if (mOnUserChangedListeners != null) {
			for (OnUserChangedListener listener : mOnUserChangedListeners) {
				listener.onUserConnected(mLocalUser);
			}
		}
	}

	public User getServer() {
		return mUsers.get(SERVER_USER_ID);
	}

}
