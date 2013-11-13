package com.zhaoyan.juyou.common;

import android.R.integer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class ZYConstant {

	// context action menu id
	public static final int ACTION_MENU_OPEN = 0x01;
	public static final int ACTION_MENU_SEND = 0x02;
	public static final int ACTION_MENU_DELETE = 0x03;
	public static final int ACTION_MENU_INFO = 0x04;
	public static final int ACTION_MENU_MORE = 0x05;
	public static final int ACTION_MENU_PLAY = 0x06;
	public static final int ACTION_MENU_RENAME = 0x07;

	public static final int REQUEST_FOR_MODIFY_NAME = 0x12;
	
	//action menu mode,edit is menu mode
	public static final int MENU_MODE_NORMAL = 0;
    public static final int MENU_MODE_EDIT = 1;

	// context action menu id

	public static class Extra {
		public static final String IMAGE_POSITION = "image_position";
		public static final String IMAGE_INFO = "image_info";

		public static final String AUDIO_SIZE = "audio_size";
		public static final String VIDEO_SIZE = "video_size";

		public static final String CAMERA_SIZE = "camera_size";
		public static final String GALLERY_SIZE = "gallery_size";

		public static final String IS_SERVER = "is_server";

		public static final String IS_FIRST_START = "is_first_start";

		public static final String COPY_PATH = "copy_path";

		public static final String SEND_FILE = "send_file";
		public static final String SEND_FILES = "send_files";
		public static final String RECEIVE_FILE = "receive_file";
		public static final String SEND_USERS = "send_users";

		public static final String SHARED_PERFERENCE_NAME = "my_shared";
		public static final String DEFAULT_SAVE_PATH = "DEFAULT_SAVE_PATH";
		public static final String SDCARD_PATH = "sdcard_path";
		public static final String INTERNAL_PATH = "internal_path";

		public static final String APP_ID = "app_id";

	}

	public static class Cmd {
		/** List files of current directory */
		public static final int LS = 100;
		/** copy command */
		public static final int COPY = 101;
		/** stop send file command */
		public static final int STOP_SEND_FILE = 102;
		/** LRETN */
		public static final int LSRETN = 103;
		/** stop return */
		public static final int STOP_RETN = 104;
		/** end flag */
		public static final int END_FLAG = 105;
		/** ask for get remote file share service */
		public static final int GET_REMOTE_SHARE_SERVICE = 106;
		/** return for get remote file share service */
		public static final int RETURN_REMOTE_SHARE_SERVICE = 107;
		/** send file command */
		public static final int SEND_FILE = 108;
		/** ask for get remote file share service */
		public static final int REMOTE_SHARE_SERVICE_STOPPED = 109;
	}

	public static final String MEDIA_AUDIO_ACTION = "intent.media.audio.action";
	public static final String MEDIA_VIDEO_ACTION = "intent.media.video.action";
	public static final String SEND_FILE_ACTION = "com.dreamlink.communication.sendfile";
	public static final String RECEIVE_FILE_ACTION = "com.dreamlink.communication.receivefile";

	public static final String FILE_EX = "file://";
	public static final String ENTER = "\n";

	public static final boolean CACHE = false;

	// SD卡中的图片保存数据库Uri
	public static final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	// SD卡中的Audio保存数据库Uri
	public static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	// SD卡中的Video保存数据库Uri
	public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

	/** package name for this app */
	public static final String PACKAGE_NAME = "com.dreamlink.communication";

	public static final String EXIT_ACTION = "intent.exit.aciton";
	public static final String APP_ACTION = "com.dreamlink.communication.action.app";
	public static final String APP_CATEGORY = "com.dreamlink.communication.category.app";

	/** test for yuri,only for yuri */
	public static final boolean UREY_TEST = true;

	public static final String DEFAULT_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	/** the default folder that save the receive files */
	public static final String DREAMLINK_FOLDER = DEFAULT_SDCARD + "/Dreamlink";
	public static final String BACKUP_FOLDER = DREAMLINK_FOLDER + "/backup_apps";
}
