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

public class AppProvider extends ContentProvider {
	private static final String TAG = "AppProvider";
	
	private SQLiteDatabase mSqLiteDatabase;
	private DatabaseHelper mDatabaseHelper;
	
	public static final int GAME_COLLECTION = 1;
	public static final int GAME_SINGLE = 2;
	public static final int GAME_FILTER = 5;
	
	public static final int APP_COLLECTION = 10;
	public static final int APP_SINGLE = 11;
	public static final int APP_FILTER = 12;
	
	public static final UriMatcher uriMatcher;
	
	static{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AppData.AUTHORITY, "game", GAME_COLLECTION);
		uriMatcher.addURI(AppData.AUTHORITY, "game/*", GAME_SINGLE);
		uriMatcher.addURI(AppData.AUTHORITY, "game_filter/*", GAME_FILTER);
		
		uriMatcher.addURI(AppData.AUTHORITY, "app", APP_COLLECTION);
		uriMatcher.addURI(AppData.AUTHORITY, "app/*", APP_SINGLE);
		uriMatcher.addURI(AppData.AUTHORITY, "app_filter/*", APP_FILTER);
	}
	
	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return (mDatabaseHelper == null) ? false : true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case GAME_COLLECTION:
			count = mSqLiteDatabase.delete(AppData.AppGame.TABLE_NAME, selection, selectionArgs);
			break;
		case GAME_SINGLE:
			String segment = uri.getPathSegments().get(1);
			if (selection != null && segment.length() > 0) {
				//根据ID删除
				selection = "_id=" + segment + " AND (" + selection + ")";
			}else {
				//由于segment是个string，那么需要给他加个'',如果是int型的就不需要了
				//根据包名删除
				selection = AppData.App.PKG_NAME + "='" + segment + "'";
			}
			count = mSqLiteDatabase.delete(AppData.AppGame.TABLE_NAME, selection, selectionArgs);
			break;
			
		case APP_COLLECTION:
			count = mSqLiteDatabase.delete(AppData.App.TABLE_NAME, selection, selectionArgs);
			break;
		case APP_SINGLE:
			String segment2 = uri.getPathSegments().get(1);
			if (selection != null && segment2.length() > 0) {
				selection = "_id=" + segment2 + " AND (" + selection + ")";
			}else {
				//由于segment是个string，那么需要给他加个'',如果是int型的就不需要了
				Log.d(TAG, "delete packagename=" + segment2);
				selection = AppData.App.PKG_NAME + "='" +  segment2 + "'";
			}
			count = mSqLiteDatabase.delete(AppData.App.TABLE_NAME, selection, selectionArgs);
			break;	
		default:
			throw new IllegalArgumentException("UnKnow Uri:" + uri);
		}
		
		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case GAME_COLLECTION:
			return AppData.AppGame.CONTENT_TYPE;
		case GAME_SINGLE:
			return AppData.AppGame.CONTENT_TYPE_ITEM;
		default:
			throw new IllegalArgumentException("Unkonw uri:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
//		Log.d(TAG, "insert db");
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		long rowId = 0;
		switch (uriMatcher.match(uri)) {
		case GAME_COLLECTION:
		case GAME_SINGLE:
			rowId = mSqLiteDatabase.insertWithOnConflict(AppData.AppGame.TABLE_NAME, "", 
					values, SQLiteDatabase.CONFLICT_REPLACE);
			if (rowId > 0) {
				Uri rowUri = ContentUris.withAppendedId(AppData.AppGame.CONTENT_URI, rowId);
				getContext().getContentResolver().notifyChange(uri, null);
				return rowUri;
			}
			throw new IllegalArgumentException("Cannot insert into uri:" + uri);
		case APP_COLLECTION:
		case APP_SINGLE:
			rowId = mSqLiteDatabase.insertWithOnConflict(AppData.App.TABLE_NAME, "", 
					values, SQLiteDatabase.CONFLICT_REPLACE);
			if (rowId > 0) {
				Uri rowUri = ContentUris.withAppendedId(uri, rowId);
				getContext().getContentResolver().notifyChange(uri, null);
//				Log.i(TAG, "insertDb.rowId=" + rowId);
				return rowUri;
			}
			throw new IllegalArgumentException("Cannot insert into uri:" + uri);
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		// TODO Auto-generated method stub
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		int numValues = 0;
		mSqLiteDatabase.beginTransaction();
		try {
			numValues = values.length;
			for (int i = 0; i < values.length; i++) {
				insert(uri, values[i]);
			}
		} catch (Exception e) {
			Log.e(TAG, "insert error:" + e.toString());
		} finally{
			mSqLiteDatabase.setTransactionSuccessful();
			mSqLiteDatabase.endTransaction();
		}
		return numValues;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (uriMatcher.match(uri)) {
		case GAME_COLLECTION:
			qb.setTables(AppData.AppGame.TABLE_NAME);
			break;
		case GAME_SINGLE:
			qb.setTables(AppData.AppGame.TABLE_NAME);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case GAME_FILTER:
			qb.setTables(AppData.AppGame.TABLE_NAME);
			qb.appendWhere(AppData.App.PKG_NAME + " like \'%"
					+ uri.getPathSegments().get(1) + "%\'");
			break;
			
		case APP_COLLECTION:
			qb.setTables(AppData.App.TABLE_NAME);
			break;
		case APP_SINGLE:
			qb.setTables(AppData.App.TABLE_NAME);
			qb.appendWhere("_id=");
			qb.appendWhere(uri.getPathSegments().get(1));
			break;
		case APP_FILTER:
			break;
		default:
			throw new IllegalArgumentException("Unknow uri:" + uri);
		}
		
		mSqLiteDatabase = mDatabaseHelper.getReadableDatabase();
//		Log.i(TAG, "selection:" + selection + ",args:" + selectionArgs.toString());
		Cursor ret = qb.query(mSqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);
		
		if (ret != null) {
			ret.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return ret;
	}
	
	

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count;
		long rowId = 0;
		int match = uriMatcher.match(uri);
		mSqLiteDatabase = mDatabaseHelper.getWritableDatabase();
		
		
		switch (match) {
		case GAME_SINGLE:
			String segment1 = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment1);
			count = mSqLiteDatabase.update(AppData.AppGame.TABLE_NAME, values, "_id=" + rowId, null);
			break;
		case GAME_COLLECTION:
			count = mSqLiteDatabase.update(AppData.AppGame.TABLE_NAME, values, selection, null);
			break;
			
		case APP_SINGLE:
			String segment2 = uri.getPathSegments().get(1);
			rowId = Long.parseLong(segment2);
			count = mSqLiteDatabase.update(AppData.App.TABLE_NAME, values, "_id=" + rowId, null);
			break;
		case APP_COLLECTION:
			count = mSqLiteDatabase.update(AppData.App.TABLE_NAME, values, selection, null);
			break;
		default:
			throw new UnsupportedOperationException("Cannot update uri:" + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper{

		public DatabaseHelper(Context context) {
			super(context, AppData.DATABASE_NAME, null, AppData.DATABASE_VERSION);
			Log.d(TAG, "DatabaseHelper");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "DatabaseHelper.onCreate");
			//create game table
			db.execSQL("create table " + AppData.AppGame.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ AppData.App.PKG_NAME + " TEXT);"
					);
			
			//create APP table
			db.execSQL("create table " + AppData.App.TABLE_NAME
					+ " ( _id INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ AppData.App.PKG_NAME + " TEXT, "
					+ AppData.App.LABEL + " TEXT, "
					+ AppData.App.APP_SIZE + " LONG, "
					+ AppData.App.VERSION + " TEXT, "
					+ AppData.App.DATE + " LONG, "
					+ AppData.App.TYPE + " INTEGER, "
					+ AppData.App.ICON + " BLOB, "
					+ AppData.App.PATH + " TEXT);"
					);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + AppData.AppGame.TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + AppData.App.TABLE_NAME);
			onCreate(db);
		}
		
	}

}
