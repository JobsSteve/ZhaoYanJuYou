package com.zhaoyan.communication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;

import com.dreamlink.communication.lib.util.ArrayUtil;
import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.BitmapUtilities;
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
		UserInfo userInfo = loadLocalUser(context);
		if (userInfo != null) {
			name = userInfo.getUser().getUserName();
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
		UserInfo userInfo = loadLocalUser(context);
		if (userInfo == null) {
			User user = new User();
			user.setUserName(KEY_NAME_DEFAULT);
			return user;
		}
		return userInfo.getUser();
	}

	private static UserInfo getUserFromCursor(Cursor cursor) {
		// get user.
		User user = new User();
		int id = cursor.getInt(cursor.getColumnIndex(JuyouData.User.USER_ID));
		String name = cursor.getString(cursor
				.getColumnIndex(JuyouData.User.USER_NAME));
		user.setUserID(id);
		user.setUserName(name);

		// get user info
		UserInfo userInfo = new UserInfo();
		userInfo.setUser(user);

		int headID = cursor.getInt(cursor
				.getColumnIndex(JuyouData.User.HEAD_ID));
		byte[] headData = cursor.getBlob(cursor
				.getColumnIndex(JuyouData.User.HEAD_DATA));
		int type = cursor.getInt(cursor.getColumnIndex(JuyouData.User.TYPE));

		userInfo.setHeadId(headID);
		userInfo.setHeadBitmap(BitmapUtilities.byteArrayToBitmap(headData));
		if (type == JuyouData.User.TYPE_LOCAL) {
			userInfo.setIsLocal(true);
		} else {
			userInfo.setIsLocal(false);
		}
		return userInfo;
	}

	/**
	 * Load local user from database. If there is no local user, return null.
	 * 
	 * @param context
	 * @return
	 */
	public static UserInfo loadLocalUser(Context context) {
		UserInfo userInfo = null;

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
						userInfo = getUserFromCursor(cursor);
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

		return userInfo;
	}

	/**
	 * Save the user as local user.
	 * 
	 * @param context
	 * @param userInfo
	 */
	public static void saveLocalUser(Context context, UserInfo userInfo) {
		if (!userInfo.isLocal()) {
			throw new IllegalArgumentException(
					"saveLocalUser, this user is not local user.");
		}
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
					addUserToDatabase(context, userInfo);
				} else if (count == 1) {
					if (cursor.moveToFirst()) {
						int id = cursor.getInt(cursor
								.getColumnIndex(JuyouData.User._ID));
						updateUserToDatabase(context, userInfo, id);
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
	}

	private static void updateUserToDatabase(Context context,
			UserInfo userInfo, int id) {
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.User._ID + "=" + id;
		contentResolver.update(JuyouData.User.CONTENT_URI,
				getContentValuesFromUserInfo(userInfo), selection, null);

	}

	public static void addUserToDatabase(Context context, UserInfo userInfo) {
		ContentResolver contentResolver = context.getContentResolver();
		contentResolver.insert(JuyouData.User.CONTENT_URI,
				getContentValuesFromUserInfo(userInfo));

	}

	private static ContentValues getContentValuesFromUserInfo(UserInfo userInfo) {
		ContentValues values = new ContentValues();
		values.put(JuyouData.User.USER_ID, userInfo.getUser().getUserID());
		values.put(JuyouData.User.USER_NAME, userInfo.getUser().getUserName());
		values.put(JuyouData.User.HEAD_ID, userInfo.getHeadId());

		Bitmap headBitmap = userInfo.getHeadBitmap();
		if (headBitmap == null) {
			values.put(JuyouData.User.HEAD_DATA, new byte[] {});
		} else {
			values.put(JuyouData.User.HEAD_DATA, BitmapUtilities
					.bitmapToByteArray(userInfo.getHeadBitmap(),
							Bitmap.CompressFormat.JPEG));
		}

		int type = userInfo.isLocal() ? JuyouData.User.TYPE_LOCAL
				: JuyouData.User.TYPE_REMOTE;
		values.put(JuyouData.User.TYPE, type);
		return values;
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
