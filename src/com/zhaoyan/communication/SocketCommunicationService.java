package com.zhaoyan.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.dreamlink.communication.aidl.Communication;
import com.dreamlink.communication.aidl.HostInfo;
import com.dreamlink.communication.aidl.OnCommunicationListenerExternal;
import com.dreamlink.communication.aidl.PlatformManagerCallback;
import com.dreamlink.communication.aidl.User;

public class SocketCommunicationService extends Service {
	private SocketCommunicationManager mSocketCommunicationManager;
	private SocketCommunicationMananerRemote mRemote;
	public static RemoteCallbackList<OnCommunicationListenerExternal> mCallBackList = new RemoteCallbackList<OnCommunicationListenerExternal>();
	private PlatformManager mPlatformManager;

	private class SocketCommunicationMananerRemote extends Communication.Stub {

		/**
		 * set the callback method,must use. the NotifyListener implements on
		 * client
		 * */
		@Override
		public void registListener(OnCommunicationListenerExternal lis,
				int appid) throws RemoteException {
			mSocketCommunicationManager
					.registerOnCommunicationListenerExternal(lis, appid);
		}

		/** if user is null ,mean send all */
		@Override
		public void sendMessage(byte[] msg, int appID, User user)
				throws RemoteException {
			if (user == null) {
				mSocketCommunicationManager.sendMessageToAll(msg, appID);
			} else {
				mSocketCommunicationManager.sendMessageToSingle(msg, user,
						appID);
			}
		}

		@Override
		public List<User> getAllUser() throws RemoteException {
			UserManager userManager = UserManager.getInstance();
			ArrayList<User> list = new ArrayList<User>();
			Map<Integer, User> map = userManager.getAllUser();
			for (Map.Entry<Integer, User> entry : map.entrySet()) {
				list.add(entry.getValue());
			}
			return list;
		}

		@Override
		public void unRegistListener(int appId) throws RemoteException {
			mSocketCommunicationManager
					.unregisterOnCommunicationListenerExternal(appId);
			mPlatformManager.unregister(appId);
		}

		@Override
		public User getLocalUser() throws RemoteException {
			return UserManager.getInstance().getLocalUser();
		}

		@Override
		public void sendMessageToAll(byte[] msg, int appID)
				throws RemoteException {
			mSocketCommunicationManager.sendMessageToAll(msg, appID);
		}

		@Override
		public void createHost(String arg0, String arg1, int arg2, int arg3)
				throws RemoteException {
			mPlatformManager.createHost(arg0, arg1, arg2, arg3);

		}

		@Override
		public void exitGroup(HostInfo arg0) throws RemoteException {
			mPlatformManager.exitGroup(arg0);
		}

		@Override
		public void getAllHost(int arg0) throws RemoteException {
			mPlatformManager.getAllHost(arg0);
		}

		@Override
		public void getGroupUser(HostInfo arg0) throws RemoteException {
			mPlatformManager.getGroupUser(arg0);
		}

		@Override
		public void joinGroup(HostInfo arg0) throws RemoteException {
			mPlatformManager.joinGroup(arg0);
		}

		@Override
		public void removeGroupMember(int arg0, int arg1)
				throws RemoteException {
			mPlatformManager.removeGroupMember(arg0, arg1);
		}

		@Override
		public void sendDataAll(byte[] arg0, HostInfo arg1)
				throws RemoteException {
			mPlatformManager.sendDataAll(arg0, arg1);
		}

		@Override
		public void sendDataSingle(byte[] arg0, HostInfo arg1, User arg2)
				throws RemoteException {
			mPlatformManager.sendDataSingle(arg0, arg1, arg2);
		}

		@Override
		public void startGroupBusiness(int arg0) throws RemoteException {
			mPlatformManager.startGroupBusiness(arg0);
		}

		@Override
		public void regitserPlatformCallback(PlatformManagerCallback arg0,
				int arg1) throws RemoteException {
			if (arg0 != null)
				mPlatformManager.register(arg0, arg1);
		}

		@Override
		public void unregitserPlatformCallback(int arg0) throws RemoteException {
			mPlatformManager.unregister(arg0);
		}

		@Override
		public List<HostInfo> getJoinedHostInfo(int arg0)
				throws RemoteException {
			return mPlatformManager.getJoinedHost(arg0);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		mSocketCommunicationManager = SocketCommunicationManager.getInstance();
		mPlatformManager = PlatformManager.getInstance(getApplicationContext());
		if (mRemote == null) {
			mRemote = new SocketCommunicationMananerRemote();
		}
		return mRemote;
	}

	@Override
	public boolean onUnbind(Intent intent) {

		return super.onUnbind(intent);
	}
}
