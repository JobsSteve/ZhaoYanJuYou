package com.zhaoyan.juyou.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class AppData{
	public static final String DATABASE_NAME = "Appinfos.db";
	public static final int DATABASE_VERSION = 1;

	public static final String AUTHORITY = "com.zhaoyan.juyou.provider.AppProvider";
	
	/**
	 * table App
	 */
	public static final class App implements BaseColumns{
		public static final String TABLE_NAME = "app";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/app");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/app_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/app";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/app";
		
		//items
		/**
		 * package name, type:String
		 */
		public static final String PKG_NAME = "pkgName";
		/**
		 * app label, type:String
		 */
		public static final String LABEL = "label";
		/**
		 * app size, type:long
		 */
		public static final String APP_SIZE = "AppSize";
		/**
		 * Version,type:String
		 */
		public static final String VERSION = "version";
		/**
		 * last modify time,mills,type:long
		 */
		public static final String DATE = "date";
		/**
		 * app type,my app or normal app,Type:int
		 */
		public static final String TYPE  = "AppType";
		/**app icon,Type:blob*/
		public static final String ICON = "icon";
		/**app path*/
		public static final String PATH = "path";
		
		/**order by label ASC*/
		public static final String SORT_ORDER_LABEL = LABEL + " ASC"; 
	}
	
	/**game table*/
	public static final class AppGame implements BaseColumns{
		public static final String TABLE_NAME = "game";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/game");
		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/game_filter");
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/game";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/game";
	}
}
