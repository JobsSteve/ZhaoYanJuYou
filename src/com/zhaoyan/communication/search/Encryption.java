package com.zhaoyan.communication.search;

/**
 * This class is used for encrypt and decrypt.
 * 
 */
public class Encryption {

	private static final char[][] KEY_ARRAY = generateKeyArray();
	private static final char[] KEY = "INFINITEINFINITEINFINI".toCharArray();

	private static char[][] generateKeyArray() {
		char[][] array = new char[26][26];
		for (int i = 0; i < 26; i++) {
			for (int j = 0; j < 26; j++) {
				if ('A' + i + j > 'Z') {
					array[i][j] = (char) ('A' + i + j - 'Z' + 'A' - 1);
				} else {
					array[i][j] = (char) ('A' + i + j);
				}
			}
		}
		return array;
	}

	/**
	 * Encrypt the string.
	 * 
	 * Note: The chars of the string must be from 'A' to 'Z';
	 * 
	 * @param str
	 * @return
	 */
	public static String encrypt(String str) {
		char[] strCharArray = str.toCharArray();
		StringBuilder stringBuilder = new StringBuilder();
		for (int k = 0; k < strCharArray.length; k++) {
			// i is row number, j is column number.
			int i, j;
			i = KEY[k] - 'A';
			j = strCharArray[k] - 'A';
			stringBuilder.append(KEY_ARRAY[i][j]);
		}
		return stringBuilder.toString();
	}

	/**
	 * Decrypt the string.
	 * 
	 * @param str
	 * @return
	 */
	public static String decrypt(String str) {
		char[] strCharArray = str.toCharArray();
		StringBuilder stringBuilder = new StringBuilder();
		for (int k = 0; k < strCharArray.length; k++) {
			// i is row number, j is column number.
			int i, j;
			i = KEY[k] - 'A';
			if (strCharArray[k] >= KEY_ARRAY[i][0]) {
				j = strCharArray[k] - KEY_ARRAY[i][0];
			} else {
				j = 'Z' - KEY_ARRAY[i][0] + 1 + strCharArray[k] - 'A';
			}
			stringBuilder.append((char) ('A' + j));
		}
		return stringBuilder.toString();
	}

	/**
	 * For debug.
	 * 
	 * @return
	 */
	public static String printKeyArray() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 26; i++) {
			stringBuilder.append("[");
			for (int j = 0; j < 26; j++) {
				stringBuilder.append(KEY_ARRAY[i][j] + " ");
			}
			stringBuilder.append("]\n");
		}
		return stringBuilder.toString();
	}
}
