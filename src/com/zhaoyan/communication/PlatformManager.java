package com.zhaoyan.communication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.dreamlink.communication.aidl.HostInfo;
import com.dreamlink.communication.aidl.OnCommunicationListenerExternal;
import com.dreamlink.communication.aidl.PlatformManagerCallback;
import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.util.Log;

@SuppressWarnings("unused")
public class PlatformManager implements OnCommunicationListenerExternal {
	private int incId = 0;
	private ConcurrentHashMap<Integer, HostInfo> allHostList;
	private Map<Integer, HostInfo> createHost;
	private PlatformProtocol platformProtocol;
	private Map<Integer, ArrayList<Integer>> groupMember;
	private UserManager mUserManager;
	private SocketCommunicationManager mSocketCommunicationManager;
	private int appId = 115;;
	private String TAG = "ArbiterLiu-PlatformManagerService";
	private ConcurrentHashMap<Integer, PlatformManagerCallback> callbackList;
	private ConcurrentHashMap<Integer, HostInfo> joinedGroup;
	private Context mContext;
	private static PlatformManager mPlatformManager;
	private HostNumberInterface mHostNumberInterface;

	public interface HostNumberInterface {
		public void returnHostInfo(List<HostInfo> hostList);
	}

	public void registerHostNumberInterface(
			HostNumberInterface hostNumberInterface) {
		mHostNumberInterface = hostNumberInterface;
	}

	private PlatformManager(Context context) {
		mContext = context;
		onCreate();
	}

	/**
	 * one app_id just allow register only one,if the new one come in ,replace
	 * old one
	 */
	public void register(PlatformManagerCallback callback, int app_id) {
		callbackList.put(app_id, callback);
	}

	public void unregister(int app_id) {
		callbackList.remove(app_id);
	}

	public static PlatformManager getInstance(Context context) {
		if (mPlatformManager == null) {
			mPlatformManager = new PlatformManager(context);
		}
		return mPlatformManager;
	}

	public void release() {
		mPlatformManager = null;
	}

	@SuppressLint("UseSparseArrays")
	public void onCreate() {
		mUserManager = UserManager.getInstance();
		mSocketCommunicationManager = SocketCommunicationManager.getInstance();
		joinedGroup = new ConcurrentHashMap<Integer, HostInfo>();
		allHostList = new ConcurrentHashMap<Integer, HostInfo>();
		mSocketCommunicationManager.registerOnCommunicationListenerExternal(
				this, appId);
		platformProtocol = new PlatformProtocol(this);
		createHost = new HashMap<Integer, HostInfo>();

		callbackList = new ConcurrentHashMap<Integer, PlatformManagerCallback>();
	}

	/**
	 * @param appName
	 *            创建包名下属的名字，一个程序可能会创建很多个不同的主机，需要用此加以标示
	 * @param pakcageName
	 *            创建的包名,目前这个项作用不大
	 * @param numberLimit
	 *            创建人数限制
	 * @param app_id
	 *            创建程序的appId,通信区分和创建区分
	 * */
	public void createHost(String appName, String pakcageName, int numberLimit,
			int app_id) {
		HostInfo hostInfo = new HostInfo();
		hostInfo.ownerID = mUserManager.getLocalUser().getUserID();
		hostInfo.ownerName = mUserManager.getLocalUser().getUserName();
		hostInfo.personLimit = numberLimit;
		hostInfo.packageName = pakcageName;
		hostInfo.appName = appName;
		hostInfo.app_id = app_id;
		hostInfo.personNumber = 1;
		if (!UserManager.isManagerServer(mUserManager.getLocalUser())) {
			User temUser = mUserManager.getAllUser().get(-1);
			if (temUser != null) {
				byte[] tartgetData = platformProtocol.encodePlatformProtocol(
						platformProtocol.CREATE_HOST_CMD_CODE,
						ArrayUtil.objectToByteArray(hostInfo));
				mSocketCommunicationManager.sendMessageToSingle(tartgetData,
						temUser, appId);
			}
		} else {
			requestCreateHost(hostInfo);
		}
	}

	public void requestCreateHost(HostInfo hostInfo) {
		boolean isexsit = false;
		if (!UserManager.isManagerServer(mUserManager.getLocalUser())) {
			/** this is no possible */
			return;
		}
		for (Entry<Integer, HostInfo> entry : allHostList.entrySet()) {
			HostInfo temp = entry.getValue();
			if (temp.ownerID == hostInfo.ownerID
					&& temp.app_id == hostInfo.app_id
					&& temp.appName.equals(hostInfo.appName)
					&& temp.packageName.equals(hostInfo.packageName)) {
				/* do nothing or return the old hostInfo */
				isexsit = true;
				if (hostInfo.ownerID == mUserManager.getLocalUser().getUserID()) {
					createHostAck(temp);
				} else {
					User tem = mUserManager.getAllUser().get(hostInfo.ownerID);
					if (tem != null) {
						byte[] target = platformProtocol
								.encodePlatformProtocol(
										platformProtocol.CREATE_HOST_ACK_CMD_CODE,
										ArrayUtil.objectToByteArray(temp));
						mSocketCommunicationManager.sendMessageToSingle(target,
								tem, appId);
					}
				}
			}
		}
		if (!isexsit) {
			hostInfo.hostId = incId;
			incId++;
			hostInfo.isAlive = 1;
			if (UserManager.isManagerServer(mUserManager.getAllUser().get(
					hostInfo.ownerID))) {
				createHostAck(hostInfo);
			} else {
				User tem = mUserManager.getAllUser().get(hostInfo.ownerID);
				if (tem != null) {
					byte[] target = platformProtocol.encodePlatformProtocol(
							platformProtocol.CREATE_HOST_ACK_CMD_CODE,
							ArrayUtil.objectToByteArray(hostInfo));
					mSocketCommunicationManager.sendMessageToSingle(target,
							tem, appId);
				} else {
					/** this is mean the communication has lost,do nothing */
				}
			}
		}
		updateHostInfo(hostInfo);
	}

	private void groupUpdateForAllUser() {
		byte[] target = platformProtocol.encodePlatformProtocol(
				platformProtocol.GROUP_INFO_CHANGE_CMD_CODE,
				ArrayUtil.objectToByteArray(allHostList));
		mSocketCommunicationManager.sendMessageToAll(target, appId);
	}

	/** 本地收到创建的ACK */
	@SuppressLint("UseSparseArrays")
	public void createHostAck(HostInfo hostInfo) {
		createHost.put(hostInfo.hostId, hostInfo);
		if (groupMember == null) {
			groupMember = new HashMap<Integer, ArrayList<Integer>>();
		}
		joinedGroup.put(hostInfo.hostId, hostInfo);
		ArrayList<Integer> temList = new ArrayList<Integer>();
		temList.add(mUserManager.getLocalUser().getUserID());
		groupMember.put(hostInfo.hostId, temList);
		notifyGroupCreated(hostInfo);
	}

	private void notifyGroupCreated(HostInfo hostIo) {
		int app_id = hostIo.app_id;
		PlatformManagerCallback callback = callbackList.get(app_id);
		if (callback != null) {
			try {
				callback.hostHasCreated(hostIo);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void requestAllHost(int appID, User user) {
		ConcurrentHashMap<Integer, HostInfo> temMap;
		if (appID == 0) {
			temMap = allHostList;
		} else {
			temMap = new ConcurrentHashMap<Integer, HostInfo>();
			for (Entry<Integer, HostInfo> entry : allHostList.entrySet()) {
				if (appID == entry.getValue().app_id) {
					temMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		if (UserManager.isManagerServer(user)) {
			receiverAllHostInfo(temMap);
			return;
		}
		byte[] target = platformProtocol.encodePlatformProtocol(
				platformProtocol.GROUP_INFO_CHANGE_CMD_CODE,
				ArrayUtil.objectToByteArray(temMap));
		mSocketCommunicationManager.sendMessageToSingle(target, user, appId);
	}

	public void getAllHost(int appID) {
		if (UserManager.isManagerServer(mUserManager.getLocalUser())) {
			requestAllHost(appID, mUserManager.getLocalUser());
			return;
		}
		User user = mUserManager.getAllUser().get(-1);
		if (user == null) {
			/** this mean no main server ,no network */
			return;
		}
		byte[] tem_target = null;
		tem_target = ArrayUtil.int2ByteArray(appID);
		byte[] target = platformProtocol.encodePlatformProtocol(
				platformProtocol.GET_ALL_HOST_INFO_CMD_CODE, tem_target);
		mSocketCommunicationManager.sendMessageToSingle(target, user, appId);
	}

	public void receiverAllHostInfo(
			ConcurrentHashMap<Integer, HostInfo> allHostInfo) {
		ArrayList<HostInfo> tem = new ArrayList<HostInfo>();
		for (java.util.Map.Entry<Integer, HostInfo> entry : allHostInfo
				.entrySet()) {
			HostInfo hostInfo = entry.getValue();
			tem.add(hostInfo);
		}
		for (Entry<Integer, PlatformManagerCallback> entry : callbackList
				.entrySet()) {
			try {
				entry.getValue().hostInfoChange(
						ArrayUtil.objectToByteArray(tem));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/** for platform get all host callback */
		if (mHostNumberInterface != null)
			mHostNumberInterface.returnHostInfo(tem);
	}

	/**
	 * local invoke
	 * 
	 * if the host person limit is 1 ,can not join it
	 * */
	public void joinGroup(HostInfo hostInfo) {
		if (hostInfo.ownerID == mUserManager.getLocalUser().getUserID()
				|| joinedGroup.containsKey(hostInfo.hostId)) {
			return;
		}
		if (hostInfo.personLimit == 1) {
			// this should notify user join status,refuse
			receiverJoinAck(false, hostInfo);
			return;
		}
		User temUser = mUserManager.getAllUser().get(hostInfo.ownerID);
		if (temUser != null) {
			byte[] target = platformProtocol.encodePlatformProtocol(
					platformProtocol.JOIN_GROUP_CMD_CODE,
					ArrayUtil.int2ByteArray(hostInfo.hostId));
			mSocketCommunicationManager.sendMessageToSingle(target, temUser,
					appId);
		}
	}

	@SuppressLint("UseSparseArrays")
	public void requestJoinGroup(int hostId, User applyUser) {
		if (createHost != null && createHost.containsKey(hostId)) {
			HostInfo hostInfo = createHost.get(hostId);
			if (groupMember == null) {
				groupMember = new HashMap<Integer, ArrayList<Integer>>();
			}
			if (!groupMember.containsKey(hostId)) {
				groupMember.put(hostId, new ArrayList<Integer>());
			}
			if (hostInfo.personLimit != 1) {
				if (hostInfo.personNumber + 1 <= hostInfo.personLimit
						|| hostInfo.personLimit == 0) {
					if (!groupMember.get(hostId)
							.contains(applyUser.getUserID())) {
						groupMember.get(hostId).add(applyUser.getUserID());
						hostInfo.personNumber++;
						sendJoinAck(true, applyUser, hostInfo);
						notifyGroupInfoChangeForMember(hostInfo.hostId);
						prepareUpdateHostInfo(hostInfo);
					}
				} else {
					sendJoinAck(false, applyUser, hostInfo);
				}
			} else {
				sendJoinAck(false, applyUser, hostInfo);
			}
		}
	}

	private void prepareUpdateHostInfo(HostInfo hostInfo) {
		if (UserManager.isManagerServer(mUserManager.getLocalUser())) {
			updateHostInfo(hostInfo);
		} else {
			byte[] target = platformProtocol.encodePlatformProtocol(
					platformProtocol.GROUP_INFO_CHANGE_CMD_CODE,
					ArrayUtil.objectToByteArray(hostInfo));
			User tem = mUserManager.getAllUser().get(-1);
			if (tem == null) {
				Log.e(TAG, "There is no main server,check the netwok");
			} else {
				mSocketCommunicationManager.sendMessageToSingle(target, tem,
						appId);
			}
		}
	}

	public void updateHostInfo(HostInfo hostInfo) {
		if (UserManager.isManagerServer(mUserManager.getLocalUser())) {
			if (hostInfo.isAlive == 1) {
				allHostList.put(hostInfo.hostId, hostInfo);
			} else {
				allHostList.remove(hostInfo.hostId);
			}
			groupUpdateForAllUser();
			receiverAllHostInfo(allHostList);
		} else {
			Log.e(TAG, "Check the process ,wrong case!!!");
		}
	}

	/**
	 * group owner notify apply user,just for group person limit 0 ,or refuse
	 * join,in network
	 * 
	 * @param flag
	 *            is true ,agreed ,else refuse
	 * */
	private void sendJoinAck(boolean flag, User user, HostInfo hostInfo) {
		byte[] temp;
		if (flag) {
			temp = new byte[] { 1 };
		} else {
			temp = new byte[] { 0 };
		}
		temp = ArrayUtil.join(temp, ArrayUtil.objectToByteArray(hostInfo));
		byte[] target = platformProtocol.encodePlatformProtocol(
				platformProtocol.JOIN_GROUP_ACK_CMD_CODE, temp);
		mSocketCommunicationManager.sendMessageToSingle(target, user, appId);

	}

	public void receiverJoinAck(boolean flag, HostInfo hostInfo) {
		if (flag) {
			// join OK,save joined info
			joinedGroup.put(hostInfo.hostId, hostInfo);
		}
		PlatformManagerCallback callback = callbackList.get(hostInfo.app_id);
		if (callback != null) {
			try {
				callback.joinGroupResult(hostInfo, flag);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * /** notify main server and all group member when has user join or exit
	 * ,in network
	 */
	private void notifyGroupInfoChangeForMember(int hostId) {
		ArrayList<Integer> temList = groupMember.get(hostId);
		if (temList.size() > 1) {
			byte[] temp = ArrayUtil.int2ByteArray(hostId);
			temp = ArrayUtil.join(temp, ArrayUtil.objectToByteArray(temList));
			byte[] target = platformProtocol.encodePlatformProtocol(
					platformProtocol.GROUP_MEMBER_UPDATE_CMD_CODE, temp);
			for (int id : temList) {
				if (id != mUserManager.getLocalUser().getUserID())
					mSocketCommunicationManager.sendMessageToSingle(target,
							mUserManager.getAllUser().get(id), appId);
			}
		}
		receiverGroupMemberUpdat(hostId, temList);
	}

	@SuppressLint("UseSparseArrays")
	public void receiverGroupMemberUpdat(int hostId, ArrayList<Integer> userList) {
		/** when group member update ,will receiver here */
		if (!joinedGroup.containsKey(hostId)) {
			return;
		}
		if (groupMember == null) {
			groupMember = new HashMap<Integer, ArrayList<Integer>>();
		}
		groupMember.put(hostId, userList);
		ArrayList<User> tem = new ArrayList<User>();
		Map<Integer, User> userMap = mUserManager.getAllUser();
		for (int id : userList) {
			tem.add(userMap.get(id));
		}
		HostInfo hostInfo = joinedGroup.get(hostId);
		PlatformManagerCallback callback = callbackList.get(hostInfo.app_id);
		if (callback != null) {
			try {
				callback.groupMemberUpdate(hostId,
						ArrayUtil.objectToByteArray(tem));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void exitGroup(HostInfo hostInfo) {
		if (createHost != null && createHost.containsKey(hostInfo.hostId)) {
			/** this mean cancel the host */
			cancelHost(hostInfo.hostId);
			return;
		}
		if (joinedGroup.containsKey(hostInfo.hostId)) {
			byte[] target = platformProtocol.encodePlatformProtocol(
					platformProtocol.EXIT_GROUP_CMD_CODE,
					ArrayUtil.int2ByteArray(hostInfo.hostId));
			User tem = mUserManager.getAllUser().get(hostInfo.ownerID);
			groupMember.remove(hostInfo.hostId);
			joinedGroup.remove(hostInfo.hostId);
			if (tem == null) {
				/** this mean the owner has disconnect */
			} else {
				mSocketCommunicationManager.sendMessageToSingle(target, tem,
						appId);
			}
		}

	}

	public void requestExitGroup(int hostId, User applyUser) {
		int index = -1;
		if (!createHost.containsKey(hostId)) {
			Log.e(TAG, "this is not i create ,the hostId is " + hostId);
			return;
		}
		HostInfo hostInfo = createHost.get(hostId);
		List<Integer> userList = groupMember.get(hostId);
		if (userList.contains(applyUser.getUserID())) {
			index = userList.indexOf(applyUser.getUserID());
			if (index != -1) {
				groupMember.get(hostId).remove(index);
				hostInfo.personNumber--;
			}
			byte[] target = platformProtocol.encodePlatformProtocol(
					platformProtocol.EXIT_GROUP_ACK_CMD_CODE,
					ArrayUtil.objectToByteArray(hostInfo));
			mSocketCommunicationManager.sendMessageToSingle(target, applyUser,
					appId);
			notifyGroupInfoChangeForMember(hostId);
			prepareUpdateHostInfo(hostInfo);
		}

	}

	/**
	 * 第三方調用 专供group创建者使用，踢出某人。
	 * 创建者调用之后，平台从当前保存的列表中移除成员，并向该成员通讯告知其已经被踢出，并向主server更新数目变化 ，和向其他成员更新成员列表变化
	 * */
	public void removeGroupMember(int hostId, int userId) {
		if (mUserManager.getLocalUser().getUserID() == userId) {
			Log.e(TAG, "You can not remove yourself");
			return;
		}
		int index = -1;
		if (createHost.containsKey(hostId)) {
			HostInfo hostInfo = createHost.get(hostId);
			ArrayList<Integer> temUserList = groupMember.get(hostId);
			if (temUserList == null) {
				return;
			}
			index = temUserList.indexOf(userId);
			if (index != -1) {
				temUserList.remove(index);
				byte[] target = platformProtocol.encodePlatformProtocol(
						platformProtocol.REMOVE_USER_CMD_CODE,
						ArrayUtil.objectToByteArray(hostInfo));
				User tem = mUserManager.getAllUser().get(userId);
				if (tem != null)
					mSocketCommunicationManager.sendMessageToSingle(target,
							tem, appId);
				hostInfo.personNumber--;
				notifyGroupInfoChangeForMember(hostId);
				prepareUpdateHostInfo(hostInfo);
			}
		} else
			Log.e(TAG, "you are not the host owner ,the host id is " + hostId
					+ " or the user is not in the group. the user id is "
					+ userId);
	}

	public void receiverRemoveUser(HostInfo hostInfo) {
		if (!joinedGroup.containsKey(hostInfo.hostId)) {
			return;
		}
		joinedGroup.remove(hostInfo.hostId);
		groupMember.remove(hostInfo.hostId);
		PlatformManagerCallback callback = callbackList.get(hostInfo.app_id);
		if (callback != null) {
			try {
				callback.hasExitGroup(hostInfo.hostId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.e(TAG, "you are removed by form " + hostInfo.hostId);
	}

	/**
	 * 专供group创建者使用,取消当前group。
	 * 如果其中有成员，向所有成员发送group取消，向主server发送group取消，删除本地保存的创建的这个group信息。
	 * 主server删除此group并向网域的所有成员发送group更新信息。
	 * */
	private void cancelHost(int hostId) {
		if (createHost.containsKey(hostId)) {
			HostInfo hostInfo = createHost.get(hostId);
			ArrayList<Integer> userList = groupMember.get(hostId);
			byte[] target = platformProtocol.encodePlatformProtocol(
					platformProtocol.CANCEL_HOST_CMD_CODE,
					ArrayUtil.objectToByteArray(hostInfo));
			if (userList.size() > 1) {
				for (int n : userList) {
					if (n == hostInfo.ownerID || n == -1) {
						continue;
					}
					User u = mUserManager.getAllUser().get(n);
					mSocketCommunicationManager.sendMessageToSingle(target, u,
							appId);
				}
			}
			mSocketCommunicationManager.sendMessageToSingle(target,
					mUserManager.getAllUser().get(-1), appId);
		}

	}

	private void requestCancelHost(HostInfo hostInfo, User user) {
		/* run in the host manager */
		if (allHostList.containsKey(hostInfo.hostId)
				&& hostInfo.ownerID == user.getUserID()) {
			hostInfo.isAlive = 0;
			prepareUpdateHostInfo(hostInfo);
		} else {

			Log.e(TAG, "you are not the owner or there is no the host ");
		}
	}

	public void receiverCancelHost(HostInfo hostInfo, User user) {
		if (joinedGroup.containsKey(hostInfo.hostId)) {
			joinedGroup.remove(hostInfo.hostId);
			groupMember.remove(hostInfo.hostId);
			PlatformManagerCallback callback = callbackList
					.get(hostInfo.app_id);
			if (callback != null) {
				try {
					callback.hasExitGroup(hostInfo.hostId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (mUserManager.getLocalUser().getUserID() == -1) {
			requestCancelHost(hostInfo, user);
		}
	}

	public void getGroupUser(HostInfo hostInfo) {
		if (!joinedGroup.containsKey(hostInfo.hostId)) {
			return;
		}
		if (mUserManager.getLocalUser().getUserID() == hostInfo.ownerID) {
			receiverGroupMemberUpdat(hostInfo.hostId,
					groupMember.get(hostInfo.hostId));
			return;
		}
		User tempUser = mUserManager.getAllUser().get(hostInfo.ownerID);
		byte[] target = platformProtocol.encodePlatformProtocol(
				platformProtocol.GET_ALL_GROUP_MEMBER_CMD_CODE,
				ArrayUtil.int2ByteArray(hostInfo.hostId));
		if (tempUser != null) {
			mSocketCommunicationManager.sendMessageToSingle(target, tempUser,
					appId);
		} else {
			Log.e(TAG, "the owner can not reachable");
		}

	}

	public void requetsGetGroupMember(int hostId, User user) {
		if (createHost.containsKey(hostId)) {
			ArrayList<Integer> userList = groupMember.get(hostId);
			if (userList.contains(user.getUserID())) {
				byte[] tem = ArrayUtil.int2ByteArray(hostId);
				tem = ArrayUtil
						.join(tem, ArrayUtil.objectToByteArray(userList));
				byte[] target = platformProtocol.encodePlatformProtocol(
						platformProtocol.GROUP_MEMBER_UPDATE_CMD_CODE, tem);
				mSocketCommunicationManager.sendMessageToSingle(target, user,
						appId);
			} else {
				Log.e(TAG, "the apply user " + user.getUserID()
						+ " is not in the group " + hostId);
			}
		} else {
			Log.e(TAG, "I am not the owner of the id " + hostId);
		}
	}

	@Override
	public void onReceiveMessage(byte[] arg0, User arg1) throws RemoteException {
		/** if join or exit ,remove .. that will know who want to do */
		platformProtocol.decodePlatformProtocol(arg0, arg1);
	}

	@Override
	public void onUserConnected(User arg0) throws RemoteException {
		/* do not care this case */

	}

	@Override
	public void onUserDisconnected(User arg0) throws RemoteException {
		int hostId = -1;
		if (!mSocketCommunicationManager.isConnected()) {
			// this mean local lost connect
			if (createHost != null) {
				createHost.clear();
			}
			if (joinedGroup != null) {
				for (Entry<Integer, HostInfo> entry : joinedGroup.entrySet()) {
					receiverRemoveUser(entry.getValue());
				}
				joinedGroup.clear();
				if (groupMember != null)
					groupMember.clear();
			}
			if (allHostList != null)
				allHostList.clear();
			receiverAllHostInfo(allHostList);
		} else {
			for (Entry<Integer, HostInfo> entry : joinedGroup.entrySet()) {
				HostInfo hostInfo = entry.getValue();
				if (hostInfo != null && hostInfo.ownerID == arg0.getUserID()) {
					Log.e(TAG, "hostInfo.ownerID == arg0.getUserID()");
					hostId = hostInfo.hostId;
					receiverCancelHost(hostInfo, arg0);
				}
			}
			for (Entry<Integer, HostInfo> entry : createHost.entrySet()) {
				HostInfo hostInfo = entry.getValue();
				if (hostInfo != null)
					removeGroupMember(hostInfo.hostId, arg0.getUserID());
			}
			if (mUserManager.getLocalUser().getUserID() == -1) {
				for (Entry<Integer, HostInfo> entry : allHostList.entrySet()) {
					HostInfo hostInfo = entry.getValue();
					if (hostInfo != null
							&& hostInfo.ownerID == arg0.getUserID()) {
						hostId = hostInfo.hostId;
						receiverCancelHost(allHostList.get(hostId), arg0);
					}
				}
			}
		}
	}

	public void startGroupBusiness(int hostId) {
		if (createHost.containsKey(hostId)) {
			byte[] target = platformProtocol.encodePlatformProtocol(
					platformProtocol.Start_GROUP_CMD_CODE,
					ArrayUtil.int2ByteArray(hostId));
			ArrayList<Integer> userList = groupMember.get(hostId);
			Map<Integer, User> userMap = mUserManager.getAllUser();
			for (int id : userList) {
				if (id == mUserManager.getLocalUser().getUserID()) {
					continue;
				} else {
					mSocketCommunicationManager.sendMessageToSingle(target,
							userMap.get(id), appId);
				}
			}
		}
	}

	public void receiverStartGroupBusiness(int hostId) {
		if (joinedGroup.containsKey(hostId)) {
			PlatformManagerCallback callback = callbackList.get(joinedGroup
					.get(hostId).app_id);
			if (callback != null) {
				try {
					callback.startGroupBusiness(joinedGroup.get(hostId));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendDataAll(byte[] data, HostInfo info) {
		if (!joinedGroup.containsKey(info.hostId)) {
			return;
		}
		Map<Integer, User> userMap = mUserManager.getAllUser();
		ArrayList<Integer> userList = groupMember.get(info.hostId);
		for (int id : userList) {
			if (id == mUserManager.getLocalUser().getUserID()) {
				continue;
			} else {
				sendData(data, userMap.get(id), true, info);
			}
		}

	}

	public void sendDataSingle(byte[] data, HostInfo info, User targetUser) {
		if (!joinedGroup.containsKey(info.hostId)) {
			return;
		}
		ArrayList<Integer> userList = groupMember.get(info.hostId);
		if (userList.contains(targetUser.getUserID())) {
			sendData(data, targetUser, false, info);
		}
	}

	private void sendData(byte[] data, User user, boolean flag,
			HostInfo hostInfo) {
		byte[] allFlag;
		if (flag)
			allFlag = new byte[] { 1 };
		else
			allFlag = new byte[] { 0 };
		byte[] tempData = ArrayUtil.join(allFlag,
				ArrayUtil.int2ByteArray(hostInfo.hostId));
		data = ArrayUtil.join(tempData, data);
		byte[] target = platformProtocol.encodePlatformProtocol(
				platformProtocol.MESSAGE_CMD_CODE, data);
		mSocketCommunicationManager.sendMessageToSingle(target, user, appId);
	}

	public void receiverData(byte[] data, User sendUser, boolean allFlag,
			int hostId) {
		if (joinedGroup.containsKey(hostId)) {
			PlatformManagerCallback callback = callbackList.get(joinedGroup
					.get(hostId).app_id);
			if (callback != null) {
				try {
					callback.receiverMessage(data, sendUser, allFlag,
							joinedGroup.get(hostId));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/** this is for application ,when they do not save the joined host */
	public ArrayList<HostInfo> getJoinedHost(int appId) {
		ArrayList<HostInfo> temp = new ArrayList<HostInfo>();
		if (appId == 0) {
			for (Entry<Integer, HostInfo> entry : joinedGroup.entrySet()) {
				temp.add(entry.getValue());
			}
		} else {
			for (Entry<Integer, HostInfo> entry : joinedGroup.entrySet()) {
				if (entry.getValue().app_id == appId)
					temp.add(entry.getValue());
			}
		}
		return temp;
	}

	@Override
	public IBinder asBinder() {
		return null;
	}

}
