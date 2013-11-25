package com.zhaoyan.communication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.TimeUtil;
import com.zhaoyan.communication.TrafficStaticInterface.TrafficStaticsRxListener;
import com.zhaoyan.communication.TrafficStaticInterface.TrafficStaticsTxListener;
import com.zhaoyan.juyou.provider.JuyouData.TrafficStaticsRX;
import com.zhaoyan.juyou.provider.JuyouData.TrafficStaticsTX;

public class TrafficStatics implements TrafficStaticsRxListener,
		TrafficStaticsTxListener {

	private static final String TAG = "TrafficStatics";
	private Context mContext;

	/**
	 * To reduce save frequency.
	 */
	private static final int CACHE_TIME = 1000;
	private static final int CACHE_AUTO_SAVE_DELAY = 1000;
	private int mRxStaticsCache = 0;
	private int mTxStaticsCache = 0;

	private static TrafficStatics mInstance;
	private StaticsHandlerThread mHandlerThread;
	private Handler mHandler;

	private static final int MSG_ADD_RX_BYTES = 1;
	private static final int MSG_ADD_TX_BYTES = 2;
	private static final int MSG_AUTO_SAVE_CACHE = 3;

	private static final String[] RX_PROJECTION = { TrafficStaticsRX.TOTAL_RX_BYTES };
	private static final String[] TX_PROJECTION = { TrafficStaticsTX.TOTAL_TX_BYTES };

	private TrafficStatics() {
		mHandlerThread = new StaticsHandlerThread(
				"TrafficStatics-HandlerThread");
		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper(), mHandlerThread);
	}

	public void init(Context context) {
		mContext = context;
	}

	public void quit() {
		mHandlerThread.quit();
		mHandler = null;
		mHandlerThread = null;
		mInstance = null;
	}

	public synchronized static TrafficStatics getInstance() {
		if (mInstance == null) {
			mInstance = new TrafficStatics();
		}
		return mInstance;
	}

	public long getTotalRxBytes() {
		long result = 0;

		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(TrafficStaticsRX.CONTENT_URI,
				RX_PROJECTION, null, null, TrafficStaticsRX.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				result += cursor.getLong(cursor
						.getColumnIndex(TrafficStaticsRX.TOTAL_RX_BYTES));
			}
			cursor.close();
		}

		return result;
	}

	public long getTotalTxBytes() {
		long result = 0;

		ContentResolver resolver = mContext.getContentResolver();
		Cursor cursor = resolver.query(TrafficStaticsTX.CONTENT_URI,
				TX_PROJECTION, null, null, TrafficStaticsTX.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				result += cursor.getLong(cursor
						.getColumnIndex(TrafficStaticsTX.TOTAL_TX_BYTES));
			}
			cursor.close();
		}
		return result;
	}

	public long getRxBytesToady() {
		long result = 0;

		ContentResolver resolver = mContext.getContentResolver();
		String selection = TrafficStaticsRX.DATE + "='" + TimeUtil.getDate()
				+ "'";
		Cursor cursor = resolver.query(TrafficStaticsRX.CONTENT_URI,
				RX_PROJECTION, selection, null,
				TrafficStaticsRX.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				result += cursor.getLong(cursor
						.getColumnIndex(TrafficStaticsRX.TOTAL_RX_BYTES));
			}
			cursor.close();
		}
		return result;
	}

	public long getTxBytesToady() {
		long result = 0;

		ContentResolver resolver = mContext.getContentResolver();
		String selection = TrafficStaticsTX.DATE + "='" + TimeUtil.getDate()
				+ "'";
		Cursor cursor = resolver.query(TrafficStaticsTX.CONTENT_URI,
				TX_PROJECTION, selection, null,
				TrafficStaticsTX.SORT_ORDER_DEFAULT);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				result += cursor.getLong(cursor
						.getColumnIndex(TrafficStaticsTX.TOTAL_TX_BYTES));
			}
			cursor.close();
		}
		return result;
	}

	public void addRxBytes(int byteCount) {
		Message msg = mHandler.obtainMessage(MSG_ADD_RX_BYTES);
		msg.arg1 = byteCount;
		mHandler.sendMessage(msg);
	}

	public void addTxBytes(int byteCount) {
		Message msg = mHandler.obtainMessage(MSG_ADD_TX_BYTES);
		msg.arg1 = byteCount;
		mHandler.sendMessage(msg);
	}

	private void saveRxBytesToDB(long rxBytesToday) {
		Log.d(TAG, "saveRxBytesToDB " + rxBytesToday);

		ContentResolver resolver = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(TrafficStaticsRX.DATE, TimeUtil.getDate());
		values.put(TrafficStaticsRX.TOTAL_RX_BYTES, rxBytesToday);
		resolver.insert(TrafficStaticsRX.CONTENT_URI, values);
	}

	private void saveTxBytesToDB(long txBytesToday) {
		Log.d(TAG, "saveTxBytesToDB " + txBytesToday);

		ContentResolver resolver = mContext.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(TrafficStaticsTX.DATE, TimeUtil.getDate());
		values.put(TrafficStaticsTX.TOTAL_TX_BYTES, txBytesToday);
		resolver.insert(TrafficStaticsTX.CONTENT_URI, values);
	}

	class StaticsHandlerThread extends HandlerThread implements Callback {
		private long mLastSaveTime = 0;

		public StaticsHandlerThread(String name) {
			super(name);
		}

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ADD_RX_BYTES:
				handleRx(msg);
				break;
			case MSG_ADD_TX_BYTES:
				handleTx(msg);
				break;
			case MSG_AUTO_SAVE_CACHE:
				saveRx();
				saveTx();
				break;
			default:
				break;
			}
			return false;
		}

		private void handleRx(Message msg) {
			int rxBytes = msg.arg1;
			mRxStaticsCache += rxBytes;

			if (System.currentTimeMillis() - mLastSaveTime > CACHE_TIME) {
				saveRx();
				mLastSaveTime = System.currentTimeMillis();
			}

			autoSaveCache();
		}

		private void handleTx(Message msg) {
			int txBytes = msg.arg1;
			mTxStaticsCache += txBytes;

			if (System.currentTimeMillis() - mLastSaveTime > CACHE_TIME) {
				saveTx();
				mLastSaveTime = System.currentTimeMillis();
			}

			autoSaveCache();
		}

		private void autoSaveCache() {
			if (mHandler.hasMessages(MSG_AUTO_SAVE_CACHE)) {
				mHandler.removeMessages(MSG_AUTO_SAVE_CACHE);
			}
			mHandler.sendEmptyMessageDelayed(MSG_AUTO_SAVE_CACHE,
					CACHE_AUTO_SAVE_DELAY);
		}

		private void saveRx() {
			if (mRxStaticsCache != 0) {
				long rxBytesToday = getRxBytesToady() + mRxStaticsCache;
				saveRxBytesToDB(rxBytesToday);
				mRxStaticsCache = 0;
			}
		}

		private void saveTx() {
			if (mTxStaticsCache != 0) {
				long txBytesToday = getTxBytesToady() + mTxStaticsCache;
				saveTxBytesToDB(txBytesToday);
				mTxStaticsCache = 0;
			}
		}
	}

}
