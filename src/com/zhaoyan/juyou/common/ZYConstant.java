package com.zhaoyan.juyou.common;

import java.io.File;

import com.zhaoyan.communication.FileTransferService;

import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

public class ZYConstant {
	public static final int REQUEST_FOR_MODIFY_NAME = 0x12;

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
	public static final String SEND_FILE_ACTION = FileTransferService.ACTION_SEND_FILE;
	public static final String RECEIVE_FILE_ACTION = "com.zhaoyan.juyou.receivefile";
	public static final String CANCEL_SEND_ACTION = FileTransferService.ACTION_CANCEL_SEND;
	public static final String CANCEL_RECEIVE_ACTION = FileTransferService.ACTION_CANCEL_RECEIVE;

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
	public static final String PACKAGE_NAME = "com.zhaoyan.juyou";

	public static final String EXIT_ACTION = "intent.exit.aciton";
	public static final String APP_ACTION = "com.dreamlink.communication.action.app";
	public static final String APP_CATEGORY = "com.dreamlink.communication.category.app";

	/** test for yuri,only for yuri */
	public static final boolean UREY_TEST = true;
	
	public static final String NEW_FOLDER = "new folder";

	public static final String DEFAULT_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	/** the default folder that save the receive files */
	public static final String JUYOU_FOLDER = DEFAULT_SDCARD + File.separator + "JuYou";
	public static final String JUYOU_BACKUP_FOLDER = JUYOU_FOLDER + File.separator + "应用备份";
	public static final String JUYOU_IMAGE_FOLDER = JUYOU_FOLDER + File.separator + "图片";
	public static final String JUYOU_APP_FOLDER = JUYOU_FOLDER + File.separator + "应用";
	public static final String JUYOU_MUSIC_FOLDER = JUYOU_FOLDER + File.separator + "音乐";
	public static final String JUYOU_VIDEO_FOLDER = JUYOU_FOLDER + File.separator + "视频";
	public static final String JUYOU_OTHER_FOLDER = JUYOU_FOLDER + File.separator + "其他";
	
	public static void initJuyouFolder(){
		mkdirs(JUYOU_FOLDER);
		mkdirs(JUYOU_APP_FOLDER);
		mkdirs(JUYOU_IMAGE_FOLDER);
		mkdirs(JUYOU_MUSIC_FOLDER);
		mkdirs(JUYOU_VIDEO_FOLDER);
		mkdirs(JUYOU_OTHER_FOLDER);
		mkdirs(JUYOU_BACKUP_FOLDER);
	}
	
	private static void mkdirs(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}
}
