package com.zhaoyan.communication.search;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.content.Context;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.ArrayUtil;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.ArraysCompat;
import com.zhaoyan.common.util.BitmapUtilities;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.search.ServerSearcherLan.OnSearchListenerLan;
import com.zhaoyan.juyou.provider.JuyouData;

public class SearchProtocol {
	private static final String TAG = "SearchProtocol";
	private static final int LENGTH_WRITE_ONE_TIME = 1024 * 4;
	private static final int LENGTH_INT = 4;
	private static final int IP_ADDRESS_HEADER_SIZE = 4;

	/**
	 * Search packet protocol:</br>
	 * 
	 * [server ip]
	 * 
	 * @return
	 */
	public static byte[] encodeSearchLan() {
		byte[] localIPAddresss = NetWorkUtil.getLocalIpAddressBytes();
		return localIPAddresss;
	}

	/**
	 * see {@link #encodeSearchLan()}
	 * 
	 * @param data
	 * @throws UnknownHostException
	 */
	public static void decodeSearchLan(byte[] data, OnSearchListenerLan listener)
			throws UnknownHostException {
		if (data.length < SearchProtocol.IP_ADDRESS_HEADER_SIZE) {
			// Data format error.
			Log.e(TAG,
					"decodeSearchLan. Data format error, received data length = "
							+ data.length);
			return;
		}
		// server ip.
		int start = 0;
		int end = SearchProtocol.IP_ADDRESS_HEADER_SIZE;
		byte[] serverIpData = ArraysCompat.copyOfRange(data, start, end);
		String serverIP = InetAddress.getByAddress(serverIpData)
				.getHostAddress();

		Log.d(TAG, "Found server ip = " + serverIP);
		if (!serverIP.equals(NetWorkUtil.getLocalIpAddress())) {
			if (listener != null) {
				listener.onFoundLanServer(serverIP);
			}
		}
	}

	public static void encodeLanServerInfo(Context context,
			DataOutputStream outputStream) {
		UserInfo userInfo = UserHelper.loadLocalUser(context);
		try {
			byte[] name = userInfo.getUser().getUserName().getBytes();
			byte[] nameLength = ArrayUtil.int2ByteArray(name.length);
			byte[] headImageId = ArrayUtil.int2ByteArray(userInfo.getHeadId());
			outputStream.write(nameLength);
			outputStream.write(name);
			outputStream.write(headImageId);

			// If head is user customized, send head image.
			if (userInfo.getHeadId() == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
				byte[] headImage = BitmapUtilities.bitmapToByteArray(userInfo
						.getHeadBitmap());
				byte[] headImageLength = ArrayUtil
						.int2ByteArray(headImage.length);

				outputStream.write(headImageLength);
				// If the head image is too large, write it in multiple
				// times.
				if (headImage.length <= LENGTH_WRITE_ONE_TIME) {
					outputStream.write(headImage);
				} else {
					int start = 0;
					int end = start + LENGTH_WRITE_ONE_TIME;
					int sentLength = 0;
					int totalLength = headImage.length;
					while (sentLength < totalLength) {
						outputStream.write(ArraysCompat.copyOfRange(headImage,
								start, end));
						sentLength += end - start;
						start = end;
						if (totalLength - sentLength <= LENGTH_WRITE_ONE_TIME) {
							end = totalLength;
						} else {
							end += LENGTH_WRITE_ONE_TIME;
						}
					}
				}
			}

			outputStream.flush();
		} catch (Exception e) {
			Log.e(TAG, "encodeLanServerInfo " + e);
		}

	}

	public static void decodeLanServerInfo(Context context,
			DataInputStream inputStream, String serverIp) {
		try {
			// name
			byte[] nameLengthData = new byte[LENGTH_INT];
			inputStream.readFully(nameLengthData);
			byte[] nameData = new byte[ArrayUtil.byteArray2Int(nameLengthData)];
			inputStream.readFully(nameData);
			String name = new String(nameData);

			// head image id
			byte[] headImageIdData = new byte[LENGTH_INT];
			inputStream.readFully(headImageIdData);
			int headImageId = ArrayUtil.byteArray2Int(headImageIdData);

			// head image
			byte[] headImageData = null;
			if (headImageId == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
				byte[] headImageLengthData = new byte[LENGTH_INT];
				inputStream.readFully(headImageLengthData);
				headImageData = new byte[ArrayUtil
						.byteArray2Int(headImageLengthData)];
				inputStream.readFully(headImageData);
			}

			// Save server info to database.
			UserInfo userInfo = new UserInfo();
			User user = new User();
			user.setUserName(name);
			userInfo.setUser(user);
			userInfo.setHeadId(headImageId);
			if (headImageId == UserInfo.HEAD_ID_NOT_PRE_INSTALL
					&& headImageData != null) {
				userInfo.setHeadBitmap(BitmapUtilities
						.byteArrayToBitmap(headImageData));
			}
			userInfo.setType(JuyouData.User.TYPE_REMOTE_SEARCH_LAN);
			userInfo.setIpAddress(serverIp);
			UserHelper.addRemoteUserToDatabase(context, userInfo);
			Log.d(TAG, "decodeLanServerInfo add into database userInfo = " + userInfo);
		} catch (Exception e) {
			Log.e(TAG, "decodeLanServerInfo " + e);
		}
	}
}
