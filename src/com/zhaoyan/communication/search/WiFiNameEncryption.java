package com.zhaoyan.communication.search;

import java.util.Random;

import com.zhaoyan.communication.UserInfo;

/**
 * This class is used for generate encrypted WiFi name and check WiFi name</br>
 * 
 * WiFi name naming rule: userName@wifiName.</br>
 * 
 * userName is the user name.</br>
 * 
 * wifiName is the encrypted WiFi name.</br>
 * 
 */
public class WiFiNameEncryption {
	public static final String WIFI_NAME_SUFFIX_KEY = "WLAN";
	public static final int WIFI_NAME_SUFFIX_LENGTH = 10;

	public static final int USER_HEAD_ID_LENGHT = 2;

	/**
	 * Generate a encrypted name.
	 * 
	 * @param userName
	 * @return
	 */
	public static String generateWiFiName(String userName) {
		int headId = UserInfo.HEAD_ID_NOT_PRE_INSTALL;
		return generateWiFiName(userName, headId);
	}

	/**
	 * Generate a encrypted name.
	 * 
	 * @param userName
	 * @return
	 */
	public static String generateWiFiName(String userName, int headId) {
		return generateWiFiName(userName, headId, generateWiFiNameSuffix());
	}

	/**
	 * Generate a WiFi name suffix.
	 * 
	 * @return
	 */
	public static String generateWiFiNameSuffix() {
		Random random = new Random();
		// Get a random position for WIFI_NAME_SUFFIX_KEY.
		int wifiNameKeyPosition = random.nextInt(WIFI_NAME_SUFFIX_LENGTH
				- WIFI_NAME_SUFFIX_KEY.length() - 1);
		// Get the WiFi name suffix.
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(generateRandomUperCaseChar(wifiNameKeyPosition));
		stringBuilder.append(WIFI_NAME_SUFFIX_KEY);
		stringBuilder.append(generateRandomUperCaseChar(WIFI_NAME_SUFFIX_LENGTH
				- wifiNameKeyPosition - WIFI_NAME_SUFFIX_KEY.length()));
		// Get the encrypted WiFi name suffix.
		return Encryption.encrypt(stringBuilder.toString());
	}

	/**
	 * Generate WiFi name with user name and an exist suffix.
	 * 
	 * @param userName
	 * @param suffix
	 * @return
	 */
	public static String generateWiFiName(String userName, String suffix) {
		int headId = UserInfo.HEAD_ID_NOT_PRE_INSTALL;
		return generateWiFiName(userName, headId, suffix);
	}

	/**
	 * Generate WiFi name with user name and an exist suffix.
	 * 
	 * @param userName
	 * @param suffix
	 * @return
	 */
	public static String generateWiFiName(String userName, int headId,
			String suffix) {
		String headIdString = String.valueOf(headId);
		if (headIdString.length() < USER_HEAD_ID_LENGHT) {
			while (headIdString.length() < USER_HEAD_ID_LENGHT) {
				headIdString = "0" + headIdString;
			}
		} else if (headIdString.length() > USER_HEAD_ID_LENGHT) {
			throw new IllegalArgumentException(
					"WiFiNameEncryption generateWiFiName() headId is too large");
		}
		return userName + headIdString + "@" + suffix;
	}

	/**
	 * Get user name from WiFi name.
	 * 
	 * @param wifiName
	 * @return
	 */
	public static String getUserName(String wifiName) {
		return wifiName.substring(0, wifiName.length()
				- WIFI_NAME_SUFFIX_LENGTH - 1 - USER_HEAD_ID_LENGHT);
	}

	public static int getUserHeadId(String wifiName) {
		int start = wifiName.length() - WIFI_NAME_SUFFIX_LENGTH - 1
				- USER_HEAD_ID_LENGHT;
		int end = wifiName.length() - WIFI_NAME_SUFFIX_LENGTH - 1;
		int headId = UserInfo.HEAD_ID_NOT_PRE_INSTALL;
		if (start >= 0) {
			String headIdString = wifiName.substring(start, end);
			try {
				headId = Integer.valueOf(headIdString);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return headId;
	}

	/**
	 * Get suffix from WiFi name.
	 * 
	 * @return
	 */
	public static String getSuffix(String wifiName) {
		return wifiName.substring(wifiName.length() - WIFI_NAME_SUFFIX_LENGTH,
				wifiName.length());
	}

	/**
	 * Check WiFi name whether is our WiFi AP or WiFi direct.
	 * 
	 * @param wifiName
	 * @return
	 */
	public static boolean checkWiFiName(String wifiName) {
		// Check total length.
		if (wifiName == null || wifiName.length() < WIFI_NAME_SUFFIX_LENGTH) {
			return false;
		}
		// Check WiFi name suffix whether all chars are from 'A' to 'Z'.
		String wifiNameSuffix = wifiName.substring(wifiName.length()
				- WIFI_NAME_SUFFIX_LENGTH, wifiName.length());
		for (int i = 0; i < wifiNameSuffix.length(); i++) {
			if (wifiNameSuffix.charAt(i) < 'A'
					|| wifiNameSuffix.charAt(i) > 'Z') {
				return false;
			}
		}
		// Decrypt the WiFi name and check it whether contains
		// WIFI_NAME_SUFFIX_KEY.
		String wifiNameSuffixDecrypted = Encryption.decrypt(wifiNameSuffix);
		if (wifiNameSuffixDecrypted.contains(WIFI_NAME_SUFFIX_KEY)) {
			return true;
		} else {
			return false;
		}
	}

	private static char[] generateRandomUperCaseChar(int n) {
		Random random = new Random();
		char[] array = new char[n];
		for (int i = 0; i < n; i++) {
			array[i] = (char) ('A' + random.nextInt(25));
		}
		return array;
	}

	/**
	 * Get a WiFi password base on WiFi name.
	 * 
	 * @param wifiName
	 *            Name of wiFi AP, and it must be checked by
	 *            {@link #checkWiFiName(String)}.
	 * @return
	 */
	// Get the last WIFI_PASSWORD_LENGTH string.
	public static String getWiFiPassword(String wifiName) {
		return Encryption.encrypt(getSuffix(wifiName));
	}
}
