package com.zhaoyan.communication;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;

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

	private User mLocalUser = new User();

	public User getLocalUser() {
		return mLocalUser;
	}

	public void setLocalUser(User localUser) {
		mLocalUser = localUser;
	}

	public void resetLocalUserID() {
		if (mLocalUser == null) {
			mLocalUser = new User();
		}
		mLocalUser.setUserID(DEFAULT_USER_ID);
		clear();
	}

	private UserManager() {

	}

	public synchronized static UserManager getInstance() {
		if (mInstance == null) {
			mInstance = new UserManager();
		}
		return mInstance;
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
	 * Add a user.
	 * 
	 * @param user
	 * @param communication
	 * @return
	 */
	public synchronized boolean addUser(User user,
			SocketCommunication communication) {
		if (isUserExist(user)) {
			Log.d(TAG, "addUser ignore, user is already exist. " + user);
			return false;
		}

		if (user.getUserID() != 0) {
			// This is a client, The user is already assigned a id by server.
		} else {
			// This is the server, and here a client connected. So assign it a
			// user id.
			mLastUserId++;
			user.setUserID(mLastUserId);
		}
		if (mLocalUser.getUserID() == -1) {
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
		}
		mUsers.put(user.getUserID(), user);
		if (isLocalUser(user.getUserID())) {
			mCommunications.put(user.getUserID(),
					mLocalCommunications.get(user.getUserID()));
		} else {
			mCommunications.put(user.getUserID(), communication);
		}
		for (OnUserChangedListener listener : mOnUserChangedListeners) {
			listener.onUserConnected(user);
		}
		Log.d(TAG, "addUser " + user);
		return true;
	}

	/**
	 * Check the user is exist or not.
	 * 
	 * @param user
	 * @return
	 */
	public synchronized boolean isUserExist(User user) {
		for (Map.Entry<Integer, User> entry : mUsers.entrySet()) {
			if (user.getUserID() == (int) entry.getKey()) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean addLocalServerUser() {
		mLocalUser.setUserID(SERVER_USER_ID);
		UserTree.getInstance().setHead(mLocalUser);
		// Create new network, clear old users.
		mUsers.clear();
		mUsers.put(mLocalUser.getUserID(), mLocalUser);
		SocketCommunication communication = new SocketCommunication(null, null);
		mCommunications.put(mLocalUser.getUserID(), communication);
		for(OnUserChangedListener listener : mOnUserChangedListeners) {
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
		mUsers.clear();
		mCommunications.clear();
		for(OnUserChangedListener listener : mOnUserChangedListeners) {
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

}
