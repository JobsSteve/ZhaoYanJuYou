package com.zhaoyan.communication.protocol;

public class Protocol {
	/**
	 *  Heart beat message. Note this message is only used by {@link #HeartBeat}
	 */
	public static final int DATA_TYPE_HEADER_HEART_BEAT = 1;

	public static final int DATA_SIZE_HEADER_SIZE = 4;
	public static final int DATA_TYPE_HEADER_SIZE = 4;

	// Login.
	public static final int DATA_TYPE_HEADER_LOGIN_REQUEST = 100;
	public static final int DATA_TYPE_HEADER_LOGIN_RESPOND = 101;
	public static final int LOGIN_RESPOND_RESULT_HEADER_SIZE = 1;
	// Login success.
	public static final byte LOGIN_RESPOND_RESULT_SUCCESS = 1;
	public static final int LOGIN_RESPOND_USERID_HEADER_SIZE = 4;
	// Login fail
	public static final byte LOGIN_RESPOND_RESULT_FAIL = 0;
	public static final int LOGIN_RESPOND_RESULT_FAIL_REASON_HEADER_SIZE = 4;
	public static final int LOGIN_RESPOND_RESULT_FAIL_UNKOWN = 0;
	public static final int LOGIN_RESPOND_RESULT_FAIL_SERVER_DISALLOW = 1;
	// Update all user info when user login or logout.
	public static final int DATA_TYPE_HEADER_UPDATE_ALL_USER = 102;
	public static final int UPDATE_ALL_USER_USER_TOTAL_NUMBER_HEADER_SIZE = 4;
	// Login forward
	public static final int DATA_TYPE_HEADER_LOGIN_REQUEST_FORWARD = 103;
	public static final int DATA_TYPE_HEADER_LOGIN_RESPOND_FORWARD = 104;
	public static final int LOGIN_FORWARD_USER_ID_SIZE = 4;

	// Send.
	public static final int SEND_USER_ID_HEADER_SIZE = 4;
	public static final int SEND_APP_ID_HEADER_SIZE = 4;
	public static final int DATA_TYPE_HEADER_SEND_SINGLE = 200;
	// Send to single
	public static final int DATA_TYPE_HEADER_SEND_ALL = 201;

	// File transport
	public static final int DATA_TYPE_HEADER_SEND_FILE = 300;
	public static final int SEND_FILE_SERVER_PORT_HEAD_SIZE = 4;
	public static final int SEND_FILE_SERVER_ADDRESS_HEAD_SIZE = 4;
}
