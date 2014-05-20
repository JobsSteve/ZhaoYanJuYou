package com.zhaoyan.juyou.backuprestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.BackupEngine.BackupResultType;
import com.zhaoyan.juyou.backuprestore.BackupEngine.OnBackupDoneListner;
import com.zhaoyan.juyou.backuprestore.Constants.State;
import com.zhaoyan.juyou.backuprestore.ResultDialog.ResultEntity;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class BackupService extends Service implements ProgressReporter,
		OnBackupDoneListner {
	private static final String TAG = "BackupService";
	private BackupBinder mBinder = new BackupBinder();
	private int mState;
	private BackupEngine mBackupEngine;
	private ArrayList<ResultEntity> mResultList;
	private BackupProgress mCurrentProgress = new BackupProgress();
	private OnBackupStatusListener mStatusListener;
	private BackupResultType mResultType;
	private ArrayList<ResultEntity> mAppResultList;
	HashMap<Integer, ArrayList<String>> mParasMap = new HashMap<Integer, ArrayList<String>>();
	NewDataNotifyReceiver notificationReceiver = null;

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return mBinder;
	}

	public boolean onUnbind(Intent intent) {
		super.onUnbind(intent);
		Log.d(TAG, "onUnbind");
		return true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mState = State.INIT;
		Log.d(TAG, "onCreate");
		notificationReceiver = new NewDataNotifyReceiver();
		IntentFilter filter = new IntentFilter(
				Constants.ACTION_NEW_DATA_DETECTED);
		filter.setPriority(1000);
		registerReceiver(notificationReceiver, filter);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.d(TAG, "onStartCommand");
		return START_STICKY_COMPATIBILITY;
	}

	public void onRebind(Intent intent) {
		super.onRebind(intent);
		Log.d(TAG, "onRebind");
	}

	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
		Log.d(TAG, "onDestroy");
		if (mBackupEngine != null && mBackupEngine.isRunning()) {
			mBackupEngine.setOnBackupDoneListner(null);
			mBackupEngine.cancel();
		}

		if (notificationReceiver != null) {
			unregisterReceiver(notificationReceiver);
			notificationReceiver = null;
		}
	}

	public static class BackupProgress {
		Composer mComposer;
		int mType;
		int mMax;
		int mCurNum;
	}

	public class BackupBinder extends Binder {
		public int getState() {
			return mState;
		}

		public void setBackupModelList(ArrayList<Integer> list) {
			reset();
			if (mBackupEngine == null) {
				mBackupEngine = new BackupEngine(BackupService.this,
						BackupService.this);
			}
			mBackupEngine.setBackupModelList(list);
		}

		public void setBackupItemParam(int itemType, ArrayList<String> paraList) {
			mParasMap.put(itemType, paraList);
			mBackupEngine.setBackupItemParam(itemType, paraList);
		}

		public ArrayList<String> getBackupItemParam(int itemType) {
			return mParasMap.get(itemType);
		}

		public boolean startBackup(String folderName) {
			startForeground(1, new Notification());
			boolean ret = false;
			mBackupEngine.setOnBackupDoneListner(BackupService.this);
			ret = mBackupEngine.startBackup(folderName);
			if (ret) {
				mState = State.RUNNING;
			} else {
				mBackupEngine.setOnBackupDoneListner(null);
			}
			Log.d(TAG, "startBackup: " + ret);
			return ret;
		}

		public void stopForeground() {
			BackupService.this.stopForeground(true);
			Log.d(TAG, "stopFreground");
		}

		public void pauseBackup() {
			mState = State.PAUSE;
			if (mBackupEngine != null) {
				mBackupEngine.pause();
			}
			Log.d(TAG, "pauseBackup");
		}

		public void cancelBackup() {
			mState = State.CANCELLING;
			if (mBackupEngine != null) {
				mBackupEngine.cancel();
			}
			Log.d(TAG, "cancelBackup");
		}

		public void continueBackup() {
			mState = State.RUNNING;
			if (mBackupEngine != null) {
				mBackupEngine.continueBackup();
			}
			Log.d(TAG, "continueBackup");
		}

		public void reset() {
			mState = State.INIT;
			if (mResultList != null) {
				mResultList.clear();
			}
			if (mAppResultList != null) {
				mAppResultList.clear();
			}
			if (mParasMap != null) {
				mParasMap.clear();
			}
		}

		public BackupProgress getCurBackupProgress() {
			return mCurrentProgress;
		}

		public void setOnBackupChangedListner(OnBackupStatusListener listener) {
			mStatusListener = listener;
		}

		public ArrayList<ResultEntity> getBackupResult() {
			return mResultList;
		}

		public BackupResultType getBackupResultType() {
			return mResultType;
		}

		public ArrayList<ResultEntity> getAppBackupResult() {
			return mAppResultList;
		}
	}

	@Override
	public void onStart(Composer composer) {
		mCurrentProgress.mComposer = composer;
		mCurrentProgress.mType = composer.getModuleType();
		mCurrentProgress.mMax = composer.getCount();
		mCurrentProgress.mCurNum = 0;
		if (mStatusListener != null) {
			mStatusListener.onComposerChanged(composer);
		}
		if (mCurrentProgress.mMax != 0) {
			NotifyManager.getInstance(BackupService.this).setMaxPercent(
					mCurrentProgress.mMax);
		}
	}

	@Override
	public void onOneFinished(Composer composer, boolean result) {
		mCurrentProgress.mCurNum++;
		if (composer.getModuleType() == ModuleType.TYPE_APP) {
			if (mAppResultList == null) {
				mAppResultList = new ArrayList<ResultEntity>();
			}
			int type = result ? ResultEntity.SUCCESS : ResultEntity.FAIL;
			ResultEntity entity = new ResultEntity(ModuleType.TYPE_APP, type);
			entity.setKey(mParasMap.get(ModuleType.TYPE_APP).get(
					mCurrentProgress.mCurNum - 1));
			mAppResultList.add(entity);
		}
		if (mStatusListener != null) {
			mStatusListener.onProgressChanged(composer,
					mCurrentProgress.mCurNum);
		}

		if (mCurrentProgress.mMax != 0) {
			NotifyManager.getInstance(BackupService.this)
					.showBackupNotification(
							ModuleType.getModuleStringFromType(this,
									composer.getModuleType()),
							composer.getModuleType(), mCurrentProgress.mCurNum);
		}
	}

	@Override
	public void onEnd(Composer composer, boolean result) {
		int resultType = ResultEntity.SUCCESS;
		if (mResultList == null) {
			mResultList = new ArrayList<ResultEntity>();
		}
		if (result == false) {
			if (composer.getCount() == 0) {
				resultType = ResultEntity.NO_CONTENT;
			} else {
				resultType = ResultEntity.FAIL;
			}
		}
		Log.d(TAG, "one Composer end: type = " + composer.getModuleType()
				+ ", result = " + resultType);
		ResultEntity item = new ResultEntity(composer.getModuleType(),
				resultType);
		mResultList.add(item);
	}

	@Override
	public void onErr(IOException e) {
		Log.d(TAG, "onErr " + e.getMessage());
		if (mStatusListener != null) {
			mStatusListener.onBackupErr(e);
		}
	}

	public void onFinishBackup(BackupResultType result) {
		Log.d(TAG, "onFinishBackup result = " + result);
		mResultType = result;
		if (mStatusListener != null) {
			if (mState == State.CANCELLING) {
				result = BackupResultType.Cancel;
			}
			if (result != BackupResultType.Success
					&& result != BackupResultType.Cancel) {
				for (ResultEntity item : mResultList) {
					if (item.getResult() == ResultEntity.SUCCESS) {
						item.setResult(ResultEntity.FAIL);
					}
				}
			}
			mState = State.FINISH;
			mStatusListener.onBackupEnd(result, mResultList, mAppResultList);
		} else {
			mState = State.FINISH;
		}
		if (mResultType != BackupResultType.Cancel) {
			NotifyManager.getInstance(BackupService.this)
					.showFinishNotification(NotifyManager.NOTIFY_BACKUPING,
							true);
		} else {
			NotifyManager.getInstance(BackupService.this).clearNotification();
		}
		Intent intent = new Intent(Constants.ACTION_SCAN_DATA);
		this.sendBroadcast(intent);
	}

	public interface OnBackupStatusListener {
		public void onComposerChanged(Composer composer);

		public void onProgressChanged(Composer composer, int progress);

		public void onBackupEnd(final BackupResultType resultCode,
				final ArrayList<ResultEntity> resultRecord,
				final ArrayList<ResultEntity> appResultRecord);

		public void onBackupErr(IOException e);
	}

	class NewDataNotifyReceiver extends BroadcastReceiver {
		public static final String CLASS_TAG = "NotificationReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (Constants.ACTION_NEW_DATA_DETECTED.equals(intent.getAction())) {
				Log.d(TAG,
						"BackupService ------>ACTION_NEW_DATA_DETECTED received");
				int type = intent.getIntExtra(Constants.NOTIFY_TYPE, 0);
				String folder = intent.getStringExtra(Constants.FILENAME);
				if (mBackupEngine != null && mBackupEngine.isRunning()) {
					NotifyManager.getInstance(context)
							.showNewDetectionNotification(type, folder);
					Toast.makeText(getApplicationContext(),
							getString(R.string.backup_running_toast),
							Toast.LENGTH_SHORT).show();
					abortBroadcast();
				}
			}
		}

	}

}
