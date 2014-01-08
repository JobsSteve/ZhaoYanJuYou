package com.zhaoyan.juyou.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class JuyouData {
	public static final String DATABASE_NAME = "juyou.db";
	public static final int DATABASE_VERSION = 6;

	public static final String AUTHORITY = "com.zhaoyan.juyou.provider.JuyouProvider";

	/**
	 * table history
	 */
	public static final class History implements BaseColumns {
		public static final String TABLE_NAME = "history";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/history");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://"
				+ AUTHORITY + "/history_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/history";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/history";

		// items
		/**
		 * file path, type:String
		 */
		public static final String FILE_PATH = "file_path";
		/**
		 * file name, type:String
		 */
		public static final String FILE_NAME = "file_name";
		/**
		 * file size, type:long
		 */
		public static final String FILE_SIZE = "file_size";
		/**
		 * send user name,type:String
		 */
		public static final String SEND_USERNAME = "send_username";
		/**
		 * send user head icon id,type:int
		 */
		public static final String SEND_USER_HEADID = "send_user_headid";
		/**
		 * send user head icon,type:blob
		 */
		public static final String SEND_USER_ICON = "send_user_icon";
		/**
		 * receive user name,type:String
		 */
		public static final String RECEIVE_USERNAME = "receive_username";
		/**
		 * current file transfer bytes,type:double
		 */
		public static final String PROGRESS = "progress";
		/**
		 * file transfer time,mills,type:long
		 */
		public static final String DATE = "date";
		/**
		 * file transfer status, type:int
		 */
		public static final String STATUS = "status";
		/**
		 * message type,send or receive,Type:int
		 */
		public static final String MSG_TYPE = "msg_type";
		/***/
		public static final String FILE_TYPE = "file_type";
		/** File icon bitmap. type blob. */
		public static final String FILE_ICON = "file_icon";

		/** order by _id DESC */
		public static final String SORT_ORDER_DEFAULT = DATE + " DESC";
	}

	/**
	 * table TrafficStaticsRX
	 */
	public static final class TrafficStaticsRX implements BaseColumns {
		public static final String TABLE_NAME = "trafficstatics_rx";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/trafficstatics_rx");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/trafficstatics_rx";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/trafficstatics_rx";

		/**
		 * traffic statics date, type:string
		 */
		public static final String DATE = "date";

		/**
		 * total rx bytes, type:long
		 */
		public static final String TOTAL_RX_BYTES = "total_rx_bytes";

		/** order by DATE DESC */
		public static final String SORT_ORDER_DEFAULT = DATE + " DESC";
	}

	/**
	 * table TrafficStaticsTX
	 */
	public static final class TrafficStaticsTX implements BaseColumns {
		public static final String TABLE_NAME = "trafficstatics_tx";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/trafficstatics_tx");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/trafficstatics_tx";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/trafficstatics_tx";

		/**
		 * traffic statics date, type:string
		 */
		public static final String DATE = "date";
		/**
		 * total tx bytes, type:long
		 */
		public static final String TOTAL_TX_BYTES = "total_tx_bytes";

		/** order by DATE DESC */
		public static final String SORT_ORDER_DEFAULT = DATE + " DESC";
	}

	/**
	 * table user
	 */
	public static final class User implements BaseColumns {
		public static final String TABLE_NAME = "user";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/user");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/user";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/user";

		/**
		 * user name, type:string
		 */
		public static final String USER_NAME = "name";
		/**
		 * user id, type:int
		 */
		public static final String USER_ID = "user_id";
		/**
		 * preinstall head picture ID, type:int
		 */
		public static final String HEAD_ID = "head_id";
		/**
		 * Third Login, eg: Sina WeiBo or QQ, type:int
		 */
		public static final String THIRD_LOGIN = "third_login";
		/**
		 * head picture, type:blob
		 */
		public static final String HEAD_DATA = "head";
		/**
		 * IP address, type:string
		 */
		public static final String IP_ADDR = "ip_addr";
		/**
		 * user status, type:int
		 */
		public static final String STATUS = "status";
		public static final int STATUS_UNKOWN = 0;
		public static final int STATUS_SERVER_CREATED = 1;
		public static final int STATUS_CONNECTED = 2;
		public static final int STATUS_DISCONNECT = 3;

		/**
		 * Local user or remote user, type:int. {@link #TYPE_LOCAL},
		 * {@link #TYPE_REMOTE}.
		 */
		public static final String TYPE = "type";
		public static final int TYPE_LOCAL = 1;
		public static final int TYPE_REMOTE = 2;
		public static final int TYPE_REMOTE_SEARCH_AP = 3;
		public static final int TYPE_REMOTE_SEARCH_LAN = 4;
		/**
		 * User wifi SSID, this is only used in the situation that the use is an
		 * Android AP Server, type:string
		 */
		public static final String SSID = "ssid";

		public static final String NETWORK = "network_type";
		public static final int NETWORK_AP = 1;
		public static final int NETWORK_WIFI = 2;
		
		/**
		 * Signature, type:string
		 */
		public static final String SIGNATURE = "signature";

		/** order by DATE DESC */
		public static final String SORT_ORDER_DEFAULT = USER_ID + " ASC";
	}

	/**
	 * table account
	 */
	public static final class Account implements BaseColumns {
		public static final String TABLE_NAME = "account";
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/account");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/account";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/account";

		/**
		 * user name, type:string
		 */
		public static final String USER_NAME = "name";
		/**
		 * preinstall head picture ID, type:int
		 */
		public static final String HEAD_ID = "head_id";
		/**
		 * head picture, type:blob
		 */
		public static final String HEAD_DATA = "head";

		/**
		 * Account of zhaoyan, type:string
		 */
		public static final String ACCOUNT_ZHAOYAN = "account_zhaoyan";

		/**
		 * phone number, type:string
		 */
		public static final String PHONE_NUMBER = "phone";

		/**
		 * Email, type:string
		 */
		public static final String EMAIL = "email";
		/**
		 * Account of qq, type:string
		 */
		public static final String ACCOUNT_QQ = "account_qq";
		/**
		 * Account of renren, type:string
		 */
		public static final String ACCOUNT_RENREN = "account_renren";
		/**
		 * Account of sina weibo, type:string
		 */
		public static final String ACCOUNT_SINA_WEIBO = "account_sina_weibo";
		/**
		 * Account of tencent weibo, type:string
		 */
		public static final String ACCOUNT_TENCENT_WEIBO = "account_tencent_weibo";

		/**
		 * Signature, type:string
		 */
		public static final String SIGNATURE = "signature";
		/**
		 * Login status, type:int
		 */
		public static final String LOGIN_STATUS = "login_status";
		public static final int LOGIN_STATUS_NOT_LOGIN = 0;
		public static final int LOGIN_STATUS_LOGIN = 1;

		/**
		 * Last login time, type:long
		 */
		public static final String LAST_LOGIN_TIME = "last_login_time";

		/**
		 * Tourist account or not, type:int
		 */
		public static final String TOURIST_ACCOUNT = "tourist_account";
		public static final int TOURIST_ACCOUNT_TRUE = 1;
		public static final int TOURIST_ACCOUNT_FALSE = 0;

		/** order by _id ASC */
		public static final String SORT_ORDER_DEFAULT = _ID + " ASC";
		/** order by last_login_time DESC */
		public static final String SORT_ORDER_LOGIN_TIME = LAST_LOGIN_TIME
				+ " DESC";
	}
}
