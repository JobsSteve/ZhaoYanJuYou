package com.zhaoyan.communication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.dreamlink.communication.lib.util.ArrayUtil;
import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.provider.JuyouData;

public class UserHelper {
	private static final String TAG = "UserHelper";
	public static final String KEY_NAME = "name";
	public static final String KEY_NAME_DEFAULT = "Unkown name";

	public static final int[] HEAD_IMAGES = { R.drawable.head1,
			R.drawable.head2, R.drawable.head3, R.drawable.head4,
			R.drawable.head5, R.drawable.head6, R.drawable.head7,
			R.drawable.head8, R.drawable.head9 };

	private static final String[] PROJECTION = { JuyouData.User._ID,
			JuyouData.User.USER_NAME, JuyouData.User.USER_ID,
			JuyouData.User.HEAD_ID, JuyouData.User.HEAD_DATA,
			JuyouData.User.IP_ADDR, JuyouData.User.STATUS, JuyouData.User.TYPE };

	public static final int getHeadImageResource(int headId) {
		return HEAD_IMAGES[headId];
	}

	/**
	 * Get the set name, if name is not set, return null
	 * 
	 * @param context
	 * @return
	 */
	public static String getUserName(Context context) {
		String name = null;
		User user = loadLocalUser(context);
		if (user != null) {
			name = user.getUserName();
		}
		return name;
	}

	/**
	 * Load use, if no local user, use default user.
	 * 
	 * Use {@link #loadLocalUser(Context)}.
	 * 
	 * @param user
	 */
	@Deprecated
	public static User loadUser(Context context) {
		User user = loadLocalUser(context);
		if (user == null) {
			user = new User();
			user.setUserName(KEY_NAME_DEFAULT);
		}
		return user;
	}

	private static User getUserFromCursor(Cursor cursor) {
		User user = new User();
		int id = cursor.getInt(cursor.getColumnIndex(JuyouData.User.USER_ID));
		String name = cursor.getString(cursor
				.getColumnIndex(JuyouData.User.USER_NAME));
		int headID = cursor.getInt(cursor
				.getColumnIndex(JuyouData.User.HEAD_ID));
		int type = cursor.getInt(cursor.getColumnIndex(JuyouData.User.TYPE));
		user.setUserID(id);
		user.setUserName(name);
		user.setHeadId(headID);
		if (type == JuyouData.User.TYPE_LOCAL) {
			user.setIsLocal(true);
		} else {
			user.setIsLocal(false);
		}
		return user;
	}

	/**
	 * Load local user from database. If there is no local user, return null.
	 * 
	 * @param context
	 * @return
	 */
	public static User loadLocalUser(Context context) {
		User user = null;

		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.User.TYPE + "="
				+ JuyouData.User.TYPE_LOCAL;
		Cursor cursor = contentResolver.query(JuyouData.User.CONTENT_URI,
				PROJECTION, selection, null, JuyouData.User.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count == 0) {
					Log.d(TAG, "No Local user");
				} else if (count == 1) {
					if (cursor.moveToFirst()) {
						user = getUserFromCursor(cursor);
					}
				} else {
					throw new IllegalStateException(TAG
							+ ", There must be one local user at most!");
				}
			} catch (Exception e) {

			} finally {
				cursor.close();
			}
		}

		return user;
	}

	/**
	 * Save use info to database
	 * 
	 * @param user
	 */
	public static void saveUser(Context context, User user) {
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.User.TYPE + "="
				+ JuyouData.User.TYPE_LOCAL;
		Cursor cursor = contentResolver.query(JuyouData.User.CONTENT_URI,
				PROJECTION, selection, null, JuyouData.User.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count == 0) {
					Log.d(TAG, "No Local user");
					addUserToDatabase(context, user);
				} else if (count == 1) {
					if (cursor.moveToFirst()) {
						int id = cursor.getInt(cursor
								.getColumnIndex(JuyouData.User._ID));
						updateUserToDatabase(context, user, id);
					} else {
						throw new IllegalStateException(TAG
								+ " saveUser moveToFirst() error.");
					}
				} else {
					throw new IllegalStateException(TAG
							+ ",saveUser There must be one local user at most!");
				}
			} catch (Exception e) {

			} finally {
				cursor.close();
			}
		}

		User originalUser = loadLocalUser(context);
		if (originalUser == null) {

		}
		if (!TextUtils.isEmpty(user.getUserName())) {
			SharedPreferences sharedPreferences = SharedPreferenceUtil
					.getSharedPreference(context);
			Editor editor = sharedPreferences.edit();
			editor.putString(KEY_NAME, user.getUserName());
			editor.commit();
		} else {
			Log.d(TAG, "saveUser: user name is empty, abort.");
		}
	}

	private static void updateUserToDatabase(Context context, User user, int id) {
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.User._ID + "=" + id;
		contentResolver.update(JuyouData.User.CONTENT_URI,
				getContentValuesFromUser(user), selection, null);
	}

	private static ContentValues getContentValuesFromUser(User user) {
		ContentValues values = new ContentValues();
		values.put(JuyouData.User.USER_ID, user.getHeadId());
		values.put(JuyouData.User.USER_NAME, user.getUserName());
		values.put(JuyouData.User.HEAD_ID, user.getHeadId());
		int type = user.isLocal() ? JuyouData.User.TYPE_LOCAL
				: JuyouData.User.TYPE_REMOTE;
		values.put(JuyouData.User.TYPE, type);
		return values;
	}

	private static void addUserToDatabase(Context context, User user) {
		ContentResolver contentResolver = context.getContentResolver();
		contentResolver.insert(JuyouData.User.CONTENT_URI,
				getContentValuesFromUser(user));
	}

	public static byte[] encodeUser(User user) {
		byte[] data = ArrayUtil.objectToByteArray(user);
		return data;
	}

	public static User decodeUser(byte[] data) {
		User user = (User) ArrayUtil.byteArrayToObject(data);
		return user;
	}
}
