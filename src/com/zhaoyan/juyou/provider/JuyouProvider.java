package com.zhaoyan.juyou.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.zhaoyan.common.util.Log;

public class JuyouProvider extends ContentProvider {
	private static final String TAG = "ZhaoyanProvider";

	private SQLiteDatabase mSqLiteDatabase;
	private DatabaseHelper mDatabaseHelper;

	public static final int HISTORY_COLLECTION = 10;
	public static final int HISTORY_SINGLE = 11;
	public static final int HISTORY_FILTER = 12;

	public static final int TRAFFIC_STATICS_RX_COLLECTION = 20;
	public static final int TRAFFIC_STATICS_RX_SINGLE = 21;
	public static final int TRAFFIC_STATICS_TX_COLLECTION = 22;
	public static final int TRAFFIC_STATICS_TX_SINGLE = 23;

	public static final int USER_COLLECTION = 30;
	public static final int USER_SINGLE = 31;

	public static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(JuyouData.AUTHORITY, "history", HISTORY_COLLECTION);
		uriMatcher.addURI(JuyouData.AUTHORITY, "history/*", HISTORY_SINGLE);
		uriMatcher.addURI(JuyouData.AUTHORITY, "history_filter/*",
				HISTORY_FILTER);

		uriMatcher.addURI(JuyouData.AUTHORITY, "trafficstatics_rx",
				TRAFFIC_STATICS_RX_COLLECTION);
		uriMatcher.addURI(JuyouData.AUTHORITY, "trafficstatics_rx/#",
				TRAFFIC_STATICS_RX_SINGLE);
		uriMatcher.addURI(JuyouData.AUTHORITY, "trafficstatics_tx",
				TRAFFIC_STATICS_TX_COLLECTION);
		uriMatcher.addURI(JuyouData.AUTHORITY, "trafficstatics_tx/#",
				TRAFFIC_STATICS_TX_SINGLE);

		uriMatcher.addURI(JuyouData.AUTHORITY, "user", USER_COLLECTION);
		uriMatcher.addURI(JuyouData.AUTHORITY, "user/#", USER_SINGLE);
	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return (mDatabaseHelper == null) ? false : true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		String table;

		switch (uriMatcher.match(uri)) {
		case HISTORY_COLLECTION:
			table = JuyouData.History.TABLE_NAME;
			break;
		case HISTORY_SINGLE:
			table = JuyouData.History.TABLE_NAME;
			String segment2 = uri.getPathSegments().get(1);
			if (selection != null && segment2.length() > 0) {
				selection = "_id=" + segment2 + " AND (" + selection + ")";
			} else {
				// 由于segment是个string，那么需要给他加个'',如果是int型的就不需要了
				// selection = "pkg_name='" + segment + "'";
			}
			break;

		case TRAFFIC_STATICS_RX_SINGLE:
			table = JuyouData.TrafficStaticsRX.TABLE_NAME;
			selection = "_id=" + uri.getPathSegments().get(1);
			selectionArgs = null;
			break;
		case TRAFFIC_STATICS_RX_COLLECTION:
			table = JuyouData.TrafficStaticsRX.TABLE_NAME;
			break;
		case TRAFFIC_STATICS_TX_SINGLE:
			table = JuyouData.TrafficStaticsTX.TABLE_NAME;
			selection = "_id=" + uri.getPathSegments().get(1);
			selectionArgs = null;
			break;
		case TRAFFIC_STATICS_TX_COLLECTION:
			table = JuyouData.TrafficStaticsTX.TABLE_NAME;
			break;
		case USER_SINGLE:
			table = JuyouData.User.TABLE_NAME;
			selection = "_id=" + uri.getPathSegments().get(1);
			selectionArgs = null;
			break;
		case USER_COLLECTION:
			table = JuyouData.User.TABLE_NAME;
			break;
		default:
			throw new IllegalArgumentException("UnKnow Uri:" + uri);
		}

		int count = mSqLiteDatabase.delete(table, selection, selectionArgs);
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case HISTORY_COLLECTION:
			return JuyouData.History.CONTENT_TYPE;
		case HISTORY_SINGLE:
			return JuyouData.History.CONTENT_TYPE_ITEM;

		case TRAFFIC_STATICS_TX_COLLECTION:
			return JuyouData.TrafficStaticsRX.CONTENT_TYPE;
		case TRAFFIC_STATICS_RX_SINGLE:
			return JuyouData.TrafficStaticsRX.CONTENT_TYPE_ITEM;
		case USER_COLLECTION:
			return JuyouData.User.CONTENT_TYPE;
		case USER_SINGLE:
			return JuyouData.User.CONTENT_TYPE_ITEM;
		default:
			throw new IllegalArgumentException("Unkonw uri:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.v(TAG, "insert db");
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		String table;
		Uri contentUri;

		switch (uriMatcher.match(uri)) {
		case HISTORY_COLLECTION:
		case HISTORY_SINGLE:
			table = JuyouData.History.TABLE_NAME;
			contentUri = JuyouData.History.CONTENT_URI;
			break;

		case TRAFFIC_STATICS_RX_SINGLE:
		case TRAFFIC_STATICS_RX_COLLECTION:
			table = JuyouData.TrafficStaticsRX.TABLE_NAME;
			contentUri = JuyouData.TrafficStaticsRX.CONTENT_URI;
			break;

		case TRAFFIC_STATICS_TX_SINGLE:
		case TRAFFIC_STATICS_TX_COLLECTION:
			table = JuyouData.TrafficStaticsTX.TABLE_NAME;
			contentUri = JuyouData.TrafficStaticsTX.CONTENT_URI;
			break;

		case USER_SINGLE:
		case USER_COLLECTION:
			table = JuyouData.User.TABLE_NAME;
			contentUri = JuyouData.User.CONTENT_URI;
			break;
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}

		long rowId = mSqLiteDatabase.insertWithOnConflict(table, "", values,
				SQLiteDatabase.CONFLICT_REPLACE);
		if (rowId > 0) {
			Uri rowUri = ContentUris.withAppendedId(contentUri, rowId);
			getContext().getContentResolver().notifyChange(uri, null);
			Log.v(TAG, "insertDb.rowId=" + rowId);
			return rowUri;
		}
		throw new IllegalArgumentException("Cannot insert into uri:" + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (uriMatcher.match(uri)) {
		case HISTORY_COLLECTION:
			qb.setTables(JuyouData.History.TABLE_NAME);
			break;
		case HISTORY_SINGLE:
			qb.setTables(JuyouData.History.TABLE_NAME);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case HISTORY_FILTER:
			break;

		case TRAFFIC_STATICS_RX_SINGLE:
			qb.setTables(JuyouData.TrafficStaticsRX.TABLE_NAME);
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		case TRAFFIC_STATICS_RX_COLLECTION:
			qb.setTables(JuyouData.TrafficStaticsRX.TABLE_NAME);
			break;

		case TRAFFIC_STATICS_TX_SINGLE:
			qb.setTables(JuyouData.TrafficStaticsTX.TABLE_NAME);
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		case TRAFFIC_STATICS_TX_COLLECTION:
			qb.setTables(JuyouData.TrafficStaticsTX.TABLE_NAME);
			break;
		case USER_SINGLE:
			qb.setTables(JuyouData.User.TABLE_NAME);
			qb.appendWhere("_id=" + uri.getPathSegments().get(1));
			break;
		case USER_COLLECTION:
			qb.setTables(JuyouData.User.TABLE_NAME);
			break;
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}

		mSqLiteDatabase = mDatabaseHelper.getReadableDatabase();
		Cursor ret = qb.query(mSqLiteDatabase, projection, selection,
				selectionArgs, null, null, sortOrder);

		if (ret != null) {
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		}

		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int match = uriMatcher.match(uri);
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		String table;

		switch (match) {
		case HISTORY_SINGLE:
			table = JuyouData.History.TABLE_NAME;
			selection = "_id=" + uri.getPathSegments().get(1);
			selectionArgs = null;
			break;
		case HISTORY_COLLECTION:
			table = JuyouData.History.TABLE_NAME;
			break;

		case TRAFFIC_STATICS_RX_SINGLE:
			table = JuyouData.TrafficStaticsRX.TABLE_NAME;
			selection = "_id=" + uri.getPathSegments().get(1);
			selectionArgs = null;
			break;
		case TRAFFIC_STATICS_RX_COLLECTION:
			table = JuyouData.TrafficStaticsRX.TABLE_NAME;
			break;
		case TRAFFIC_STATICS_TX_SINGLE:
			table = JuyouData.TrafficStaticsTX.TABLE_NAME;
			selection = "_id=" + uri.getPathSegments().get(1);
			selectionArgs = null;
			break;
		case TRAFFIC_STATICS_TX_COLLECTION:
			table = JuyouData.TrafficStaticsTX.TABLE_NAME;
			break;
		case USER_SINGLE:
			table = JuyouData.User.TABLE_NAME;
			selection = "_id=" + uri.getPathSegments().get(1);
			selectionArgs = null;
			break;
		case USER_COLLECTION:
			table = JuyouData.User.TABLE_NAME;
			break;

		default:
			throw new UnsupportedOperationException("Cannot update uri:" + uri);
		}
		int count = mSqLiteDatabase.update(table, values, selection, null);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, JuyouData.DATABASE_NAME, null,
					JuyouData.DATABASE_VERSION);
			Log.d(TAG, "DatabaseHelper");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "DatabaseHelper.onCreate");

			// create history table
			db.execSQL("create table " + JuyouData.History.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ JuyouData.History.FILE_PATH + " TEXT, "
					+ JuyouData.History.FILE_NAME + " TEXT, "
					+ JuyouData.History.FILE_SIZE + " LONG, "
					+ JuyouData.History.SEND_USERNAME + " TEXT, "
					+ JuyouData.History.RECEIVE_USERNAME + " TEXT, "
					+ JuyouData.History.PROGRESS + " LONG, "
					+ JuyouData.History.DATE + " LONG, "
					+ JuyouData.History.STATUS + " INTEGER, "
					+ JuyouData.History.MSG_TYPE + " INTEGER, "
					+ JuyouData.History.FILE_TYPE + " INTEGER, "
					+ JuyouData.History.FILE_ICON + " BLOB);");

			// create traffic statics rx table.
			db.execSQL("create table " + JuyouData.TrafficStaticsRX.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ JuyouData.TrafficStaticsRX.DATE + " TEXT UNIQUE, "
					+ JuyouData.TrafficStaticsRX.TOTAL_RX_BYTES + " LONG);");

			// create traffic statics tx table.
			db.execSQL("create table " + JuyouData.TrafficStaticsTX.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ JuyouData.TrafficStaticsTX.DATE + " TEXT UNIQUE, "
					+ JuyouData.TrafficStaticsTX.TOTAL_TX_BYTES + " LONG);");

			// create user table
			db.execSQL("create table " + JuyouData.User.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ JuyouData.User.USER_NAME + " TEXT, "
					+ JuyouData.User.USER_ID + " INTEGER, "
					+ JuyouData.User.HEAD_ID + " INTEGER, "
					+ JuyouData.User.HEAD_DATA + " BLOB, "
					+ JuyouData.User.IP_ADDR + " TEXT, "
					+ JuyouData.User.STATUS + " INTEGER, "
					+ JuyouData.User.TYPE + " INTEGER, " + JuyouData.User.SSID
					+ " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + JuyouData.History.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "
					+ JuyouData.TrafficStaticsRX.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS "
					+ JuyouData.TrafficStaticsTX.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + JuyouData.User.TABLE_NAME);
			onCreate(db);
		}

	}

}
