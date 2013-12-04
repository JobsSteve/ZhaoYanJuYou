package com.zhaoyan.communication.search2;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.SocketPort;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.search.WiFiNameEncryption;
import com.zhaoyan.communication.search2.ServerSearcherAndroidAP.OnSearchListenerAP;
import com.zhaoyan.communication.search2.ServerSearcherLan.OnSearchListenerLan;
import com.zhaoyan.juyou.provider.JuyouData;

/**
 * When found a server, get server information and write to database.
 * 
 */
public class ServerInfoProcesor implements OnSearchListenerLan,
		OnSearchListenerAP {
	private Context mContext;

	public ServerInfoProcesor(Context context) {
		mContext = context;
	}

	@Override
	public void onFoundLanServer(String serverIP) {
		// Check whether the serverIp is already added to database. If the
		// serverIp is already added, ignore it, else add it to database.
		if (!isLanServerAlreadyAdded(serverIP)) {
			getLanServerInfo(serverIP);
		}
	}

	@Override
	public void onSearchLanStop() {
		// no process.
	}

	@Override
	public void onFoundAPServer(String ssid) {
		// Check whether the ssid is already added to database. If the
		// ssid is already added, ignore it, else add it to database.
		if (!isApServerAlreadyAdded(ssid)) {
			getApServerInfo(ssid);
		}
	}

	private void getApServerInfo(String ssid) {
		String userName = WiFiNameEncryption.getUserName(ssid);
		int userHeadId = WiFiNameEncryption.getUserHeadId(ssid);

		UserInfo userInfo = new UserInfo();
		User user = new User();
		user.setUserName(userName);
		userInfo.setUser(user);
		userInfo.setHeadId(userHeadId);
		userInfo.setSsid(ssid);
		userInfo.setType(JuyouData.User.TYPE_REMOTE_SEARCH_AP);
		UserHelper.addRemoteUserToDatabase(mContext, userInfo);
	}

	private boolean isApServerAlreadyAdded(String ssid) {
		boolean result = false;
		ContentResolver contentResolver = mContext.getContentResolver();
		String selection = JuyouData.User.SSID + "='" + ssid + "'";
		Cursor cursor = contentResolver.query(JuyouData.User.CONTENT_URI, null,
				selection, null, JuyouData.User.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			try {
				if (cursor.getCount() > 0) {
					result = true;
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}
		return result;
	}

	private boolean isLanServerAlreadyAdded(String serverIp) {
		boolean result = false;
		ContentResolver contentResolver = mContext.getContentResolver();
		String selection = JuyouData.User.IP_ADDR + "='" + serverIp + "'";
		Cursor cursor = contentResolver.query(JuyouData.User.CONTENT_URI, null,
				selection, null, JuyouData.User.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			try {
				if (cursor.getCount() > 0) {
					result = true;
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}
		return result;
	}

	private void getLanServerInfo(String serverIp) {
		GetServerInfoSocket socket = new GetServerInfoSocket(serverIp,
				SocketPort.SEARCH_SERVER_INFO_PORT);
		socket.getServerInfo(mContext);
	}

	public void clearServerInfo(int serverType) {
		if (serverType == ServerSearcher.SERVER_TYPE_NONE) {
			return;
		}

		boolean deleteAp = false;
		boolean deleteLan = false;
		if (ServerSearcher.SERVER_TYPE_AP == (serverType & ServerSearcher.SERVER_TYPE_AP)) {
			deleteAp = true;
		}
		if (ServerSearcher.SERVER_TYPE_LAN == (serverType & ServerSearcher.SERVER_TYPE_LAN)) {
			deleteLan = true;
		}

		String selection = null;
		if (deleteAp && deleteLan) {
			selection = JuyouData.User.TYPE + "="
					+ JuyouData.User.TYPE_REMOTE_SEARCH_AP + " or "
					+ JuyouData.User.TYPE + "="
					+ JuyouData.User.TYPE_REMOTE_SEARCH_LAN + ";";
		} else if (deleteAp) {
			selection = JuyouData.User.TYPE + "="
					+ JuyouData.User.TYPE_REMOTE_SEARCH_AP + ";";
		} else if (deleteLan) {
			selection = JuyouData.User.TYPE + "="
					+ JuyouData.User.TYPE_REMOTE_SEARCH_LAN + ";";
		}

		ContentResolver contentResolver = mContext.getContentResolver();
		contentResolver.delete(JuyouData.User.CONTENT_URI, selection, null);
	}
}
