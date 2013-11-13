package com.zhaoyan.communication.search;

import java.util.Random;

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
	public static final int WIFI_NAME_SUFFIX_LENGTH = 16;
	public static final int WIFI_PASSWORD_LENGTH = 10;

	/**
	 * Generate a encrypted name.
	 * 
	 * @param userName
	 * @return
	 */
	public static String generateWiFiName(String userName) {
		return userName + "@" + generateWiFiNameSuffix();
	}

	/**
	 * Generate a WiFi name suffix.
	 * 
	 * @return
	 */
	public static String generateWiFiNameSuffix() {
		Random random = new Random();
		// Get a random position from 0 to 11.
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
		return userName + "@" + suffix;
	}

	/**
	 * Get user name from WiFi name.
	 * 
	 * @param wifiName
	 * @return
	 */
	public static String getUserName(String wifiName) {
		return wifiName.substring(0, wifiName.length()
				- WIFI_NAME_SUFFIX_LENGTH - 1);
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
	public static String getWiFiPassword(String wifiName) {
		// Get the last WIFI_PASSWORD_LENGTH string.
		return Encryption.encrypt(getSuffix(wifiName));
	}
}
