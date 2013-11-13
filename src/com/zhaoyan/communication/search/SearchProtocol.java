package com.zhaoyan.communication.search;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import android.annotation.TargetApi;
import android.os.Build;

import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.UserManager;

public class SearchProtocol {
	private static final String TAG = "SearchProtocol";

	public interface OnSearchListener {
		/**
		 * Search server success and found a server</br>
		 * 
		 * Be careful:</br>
		 * 
		 * This method is not in activity main thread.</br>
		 * 
		 * @param serverIP
		 *            The server IP address.
		 */
		void onSearchSuccess(String serverIP, String name);

		void onSearchSuccess(ServerInfo serverInfo);

		/**
		 * Search server stop</br>
		 * 
		 * Be careful:</br>
		 * 
		 * This method is not in activity main thread.</br>
		 * 
		 */
		void onSearchStop();
	}

	// [4 bytes ][4 bytes][n bytes ]
	// [server ip][server name size][server name]
	public static final int IP_ADDRESS_HEADER_SIZE = 4;
	public static final int SERVER_NAME_HEADER_SIZE = 4;

	/**
	 * Search packet protocol:</br>
	 * 
	 * [server ip][server name size][server name]
	 * 
	 * @return
	 */
	public static byte[] encodeSearchLan() {
		byte[] localIPAddresss = NetWorkUtil.getLocalIpAddressBytes();
		byte[] localUserName = UserManager.getInstance().getLocalUser()
				.getUserName().getBytes();
		byte[] localUserNameSize = ArrayUtil
				.int2ByteArray(localUserName.length);
		byte[] searchMessage = ArrayUtil.join(localIPAddresss,
				localUserNameSize, localUserName);

		return searchMessage;
	}

	/**
	 * see {@link #encodeSearchLan()}
	 * 
	 * @param data
	 * @throws UnknownHostException
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static void decodeSearchLan(byte[] data, OnSearchListener listener)
			throws UnknownHostException {

		if (data.length < SearchProtocol.IP_ADDRESS_HEADER_SIZE
				+ SearchProtocol.SERVER_NAME_HEADER_SIZE) {
			// Data format error.
			Log.e(TAG,
					"GetMulticastPacket, Data format error, received data length = "
							+ data.length);
			return;
		}
		// server ip.
		int start = 0;
		int end = SearchProtocol.IP_ADDRESS_HEADER_SIZE;
		byte[] serverIpData = Arrays.copyOfRange(data, start, end);
		String serverIP = "";
		serverIP = InetAddress.getByAddress(serverIpData).getHostAddress();
		// server name size.
		start = end;
		end += SearchProtocol.SERVER_NAME_HEADER_SIZE;
		byte[] serveraNameSizeData = Arrays.copyOfRange(data, start, end);
		int serverNameSize = ArrayUtil.byteArray2Int(serveraNameSizeData);

		// server name.
		if (serverNameSize < 0
				|| data.length < SearchProtocol.IP_ADDRESS_HEADER_SIZE
						+ SearchProtocol.SERVER_NAME_HEADER_SIZE
						+ serverNameSize) {
			// Data format error.
			Log.e(TAG,
					"GetMulticastPacket, Data format error, received data length = "
							+ data.length + ", server name length = "
							+ serverNameSize);
			start = 0;
			end = 16;
			serverIpData = Arrays.copyOfRange(data, start, end);
			serverIP = "";
			serverIP = InetAddress.getByAddress(serverIpData).getHostAddress();
			// server name size.
			start = end;
			end += SearchProtocol.SERVER_NAME_HEADER_SIZE;
			serveraNameSizeData = Arrays.copyOfRange(data, start, end);
			serverNameSize = ArrayUtil.byteArray2Int(serveraNameSizeData);
			Log.e("ArbiterLiu", serverNameSize + "");
			if (serverNameSize < 0
					|| data.length < SearchProtocol.IP_ADDRESS_HEADER_SIZE
							+ SearchProtocol.SERVER_NAME_HEADER_SIZE
							+ serverNameSize) {
				// Data format error.
				Log.e(TAG,
						"GetMulticastPacket, Data format error, received data length = "
								+ data.length + ", server name length = "
								+ serverNameSize);
				return;
			}
		}
		start = end;
		end += serverNameSize;
		byte[] serverNameData = Arrays.copyOfRange(data, start, end);
		String serverName = new String(serverNameData);
		Log.d(TAG, "Found server ip = " + serverIP + ", name = " + serverName);
		if (serverIP.equals(NetWorkUtil.getLocalIpAddress())) {
			// ignore.
		} else {
			// Got another server. because client will not broadcast
			// message.
			if (listener != null) {
				listener.onSearchSuccess(serverIP, serverName);
			}
		}
	}

}
