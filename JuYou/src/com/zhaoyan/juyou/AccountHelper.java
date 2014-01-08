package com.zhaoyan.juyou;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.juyou.provider.JuyouData;

public class AccountHelper {
	private static final String TAG = "AccountHelper";
	public static final int[] HEAD_IMAGES = UserHelper.HEAD_IMAGES;

	private static final String[] PROJECTION = { JuyouData.Account._ID,
			JuyouData.Account.USER_NAME, JuyouData.Account.HEAD_ID,
			JuyouData.Account.HEAD_DATA, JuyouData.Account.ACCOUNT_ZHAOYAN,
			JuyouData.Account.PHONE_NUMBER, JuyouData.Account.EMAIL,
			JuyouData.Account.ACCOUNT_QQ, JuyouData.Account.ACCOUNT_RENREN,
			JuyouData.Account.ACCOUNT_SINA_WEIBO,
			JuyouData.Account.ACCOUNT_TENCENT_WEIBO,
			JuyouData.Account.SIGNATURE, JuyouData.Account.LOGIN_STATUS,
			JuyouData.Account.TOURIST_ACCOUNT,
			JuyouData.Account.LAST_LOGIN_TIME };

	public static final int getHeadImageResource(int headId) {
		return HEAD_IMAGES[headId];
	}

	private static ContentValues getContentValuesFromAccount(AccountInfo account) {
		ContentValues values = new ContentValues();
		values.put(JuyouData.Account.USER_NAME, account.getUserName());
		values.put(JuyouData.Account.HEAD_ID, account.getHeadId());

		byte[] headBitmapData = account.getHeadData();
		if (headBitmapData == null) {
			values.put(JuyouData.Account.HEAD_DATA, new byte[] {});
		} else {
			values.put(JuyouData.Account.HEAD_DATA, headBitmapData);
		}

		values.put(JuyouData.Account.ACCOUNT_ZHAOYAN,
				account.getAccountZhaoyan());
		values.put(JuyouData.Account.PHONE_NUMBER, account.getPhoneNumber());
		values.put(JuyouData.Account.EMAIL, account.getEmail());
		values.put(JuyouData.Account.ACCOUNT_QQ, account.getAccountQQ());
		values.put(JuyouData.Account.ACCOUNT_RENREN, account.getAccountRenren());
		values.put(JuyouData.Account.ACCOUNT_SINA_WEIBO,
				account.getAccountSinaWeibo());
		values.put(JuyouData.Account.ACCOUNT_TENCENT_WEIBO,
				account.getAccountTencentWeibo());
		values.put(JuyouData.Account.SIGNATURE, account.getSignature());
		values.put(JuyouData.Account.LOGIN_STATUS, account.getLoginStatus());
		values.put(JuyouData.Account.TOURIST_ACCOUNT,
				account.getTouristAccount());
		values.put(JuyouData.Account.LAST_LOGIN_TIME,
				account.getLastLoginTime());
		return values;
	}

	private static AccountInfo getAccountFromCursor(Cursor cursor) {
		AccountInfo account = new AccountInfo();
		int id = cursor.getInt(cursor.getColumnIndex(JuyouData.Account._ID));
		account.setDatabaseId(id);

		String name = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.USER_NAME));
		account.setUserName(name);

		int headId = cursor.getInt(cursor
				.getColumnIndex(JuyouData.Account.HEAD_ID));
		account.setHeadId(headId);

		byte[] headData = cursor.getBlob(cursor
				.getColumnIndex(JuyouData.Account.HEAD_DATA));
		account.setHeadData(headData);

		String accountZhaoyan = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.ACCOUNT_ZHAOYAN));
		account.setAccountZhaoyan(accountZhaoyan);

		String phoneNumber = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.PHONE_NUMBER));
		account.setPhoneNumber(phoneNumber);

		String email = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.EMAIL));
		account.setEmail(email);

		String accountQQ = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.ACCOUNT_QQ));
		account.setAccountQQ(accountQQ);

		String accountRenren = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.ACCOUNT_RENREN));
		account.setAccountRenren(accountRenren);

		String accountSinaWeibo = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.ACCOUNT_SINA_WEIBO));
		account.setAccountSinaWeibo(accountSinaWeibo);

		String accountTencentWeibo = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.ACCOUNT_TENCENT_WEIBO));
		account.setAccountTencentWeibo(accountTencentWeibo);

		String signature = cursor.getString(cursor
				.getColumnIndex(JuyouData.Account.SIGNATURE));
		account.setSignature(signature);

		int loginStatus = cursor.getInt(cursor
				.getColumnIndex(JuyouData.Account.LOGIN_STATUS));
		account.setLoginStatus(loginStatus);

		int touristAccount = cursor.getInt(cursor
				.getColumnIndex(JuyouData.Account.TOURIST_ACCOUNT));
		account.setTouristAccount(touristAccount);

		long lastLoginTime = cursor.getLong(cursor
				.getColumnIndex(JuyouData.Account.LAST_LOGIN_TIME));
		account.setLastLoginTime(lastLoginTime);
		return account;
	}

	public static AccountInfo getCurrentAccount(Context context) {
		AccountInfo account = null;
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.Account.LOGIN_STATUS + "="
				+ JuyouData.Account.LOGIN_STATUS_LOGIN;
		Cursor cursor = contentResolver.query(JuyouData.Account.CONTENT_URI,
				PROJECTION, selection, null,
				JuyouData.Account.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count == 0) {
					Log.d(TAG, "getCurrentAccount(): There is no login user");
				} else if (count == 1) {
					if (cursor.moveToFirst()) {
						account = getAccountFromCursor(cursor);
					}
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}

		return account;
	}

	public static AccountInfo addAccount(Context context, AccountInfo account) {
		Log.d(TAG, "addAccount");
		if (account.getLoginStatus() == JuyouData.Account.LOGIN_STATUS_LOGIN) {
			throw new IllegalArgumentException(
					"addAccount, can not add account which login.");
		}
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = contentResolver.insert(JuyouData.Account.CONTENT_URI,
				getContentValuesFromAccount(account));
		Cursor cursor = contentResolver.query(uri, PROJECTION, null, null,
				JuyouData.Account.SORT_ORDER_DEFAULT);
		cursor.moveToFirst();
		AccountInfo newAddedAccount = getAccountFromCursor(cursor);
		cursor.close();
		return newAddedAccount;
	}

	public static void saveCurrentAccount(Context context, AccountInfo account) {
		Log.d(TAG, "saveCurrentAccount");
		if (account.getLoginStatus() != JuyouData.Account.LOGIN_STATUS_LOGIN) {
			throw new IllegalArgumentException(
					"saveCurrentAccount, this account does not login.");
		}
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.Account.LOGIN_STATUS + "="
				+ JuyouData.Account.LOGIN_STATUS_LOGIN;
		Cursor cursor = contentResolver.query(JuyouData.Account.CONTENT_URI,
				PROJECTION, selection, null,
				JuyouData.Account.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count == 0) {
					Log.d(TAG, "No login account.");
				} else if (count == 1) {
					Log.d(TAG, "Login account exist. Update login account.");
					if (cursor.moveToFirst()) {
						int id = cursor.getInt(cursor
								.getColumnIndex(JuyouData.Account._ID));
						updateAccountToDatabase(context, account, id);
					} else {
						Log.e(TAG, "saveCurrentAccount moveToFirst() error.");
					}
				} else {
					throw new IllegalStateException(
							"saveCurrentAccount There must be one login account at most!");
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}
	}

	private static void updateAccountToDatabase(Context context,
			AccountInfo account, int id) {
		Log.d(TAG, "updateAccountToDatabase  account = " + account);
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.Account._ID + "=" + id;
		contentResolver.update(JuyouData.Account.CONTENT_URI,
				getContentValuesFromAccount(account), selection, null);
	}

	public static void setAccountLogin(Context context, AccountInfo account) {
		Log.d(TAG, "setAccountLogin  account = " + account);
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.Account._ID + "="
				+ account.getDatabaseId();
		ContentValues values = new ContentValues();
		values.put(JuyouData.Account.LOGIN_STATUS,
				JuyouData.Account.LOGIN_STATUS_LOGIN);
		long lastLoginTime = System.currentTimeMillis();
		values.put(JuyouData.Account.LAST_LOGIN_TIME, lastLoginTime);
		contentResolver.update(JuyouData.Account.CONTENT_URI, values,
				selection, null);

		account.setLoginStatus(JuyouData.Account.LOGIN_STATUS_LOGIN);
		account.setLastLoginTime(lastLoginTime);
	}

	public static List<AccountInfo> getAllAccount(Context context) {
		ArrayList<AccountInfo> accounts = new ArrayList<AccountInfo>();
		ContentResolver contentResolver = context.getContentResolver();
		Cursor cursor = contentResolver
				.query(JuyouData.Account.CONTENT_URI, PROJECTION, null, null,
						JuyouData.Account.SORT_ORDER_LOGIN_TIME);
		if (cursor != null) {
			try {
				while (cursor.moveToNext()) {
					accounts.add(getAccountFromCursor(cursor));
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}
		return accounts;
	}

	public static AccountInfo getTouristAccount(Context context) {
		AccountInfo touristAccountInfo = null;
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.Account.TOURIST_ACCOUNT + "="
				+ JuyouData.Account.TOURIST_ACCOUNT_TRUE;
		Cursor cursor = contentResolver.query(JuyouData.Account.CONTENT_URI,
				PROJECTION, selection, null,
				JuyouData.Account.SORT_ORDER_LOGIN_TIME);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				cursor.moveToFirst();
				touristAccountInfo = getAccountFromCursor(cursor);
			}
			cursor.close();
		}
		return touristAccountInfo;
	}

	public static void logoutCurrentAccount(Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		String selection = JuyouData.Account.LOGIN_STATUS + "="
				+ JuyouData.Account.LOGIN_STATUS_LOGIN;
		Cursor cursor = contentResolver.query(JuyouData.Account.CONTENT_URI,
				PROJECTION, selection, null,
				JuyouData.Account.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			try {
				int count = cursor.getCount();
				if (count > 0) {
					while (cursor.moveToNext()) {
						ContentValues contentValues = new ContentValues();
						contentValues.put(JuyouData.Account.LOGIN_STATUS,
								JuyouData.Account.LOGIN_STATUS_NOT_LOGIN);

						int id = cursor.getInt(cursor
								.getColumnIndex(JuyouData.Account._ID));
						String selection2 = JuyouData.Account._ID + "=" + id;
						contentResolver.update(JuyouData.Account.CONTENT_URI,
								contentValues, selection2, null);
					}
				}
			} catch (Exception e) {
			} finally {
				cursor.close();
			}
		}
	}
}
