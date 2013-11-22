package com.zhaoyan.juyou.fragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.AppCursorAdapter;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.AppManager;
import com.zhaoyan.juyou.common.MenuTabManager;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.dialog.MyDialog;
import com.zhaoyan.juyou.dialog.MyDialog.OnHideListener;
import com.zhaoyan.juyou.notification.NotificationMgr;

public class AppBaseFragment extends BaseFragment{
	private static final String TAG = "AppBaseFragment";
	
	protected GridView mGridView;
	protected ProgressBar mLoadingBar;

	protected AppCursorAdapter mAdapter = null;
	
	protected int mAppId = -1;
	
	protected ActionMenu mActionMenu;
	protected MenuTabManager mMenuManager;
	
	protected View mMenuBottomView;
	protected LinearLayout mMenuHolder;
	
	protected MyDialog mMyDialog = null;
	protected List<String> mUninstallList = null;
	protected PackageManager pm = null;
	protected Notice mNotice = null;
	private static final int REQUEST_CODE_UNINSTALL = 0x101;
	
	private NotificationMgr mNotificationMgr;
	private boolean mIsBackupHide = false;
	
	private static final int MSG_TOAST = 0;
	private static final int MSG_BACKUPING = 1;
	private static final int MSG_BACKUP_OVER = 2;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_TOAST:
				String message = (String) msg.obj;
				mNotice.showToast(message);
				break;
			case MSG_BACKUPING:
				int progress = msg.arg1;
				int max = msg.arg2;
				String name = (String) msg.obj;
				mNotificationMgr.updateBackupNotification(progress, max, name);
				break;
			case MSG_BACKUP_OVER:
				long duration = (Long) msg.obj;
				mNotificationMgr.appBackupOver(duration);
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNotice = new Notice(getActivity().getApplicationContext());
		pm = getActivity().getPackageManager();
		
		mNotificationMgr = new NotificationMgr(getActivity().getApplicationContext());
	}
	
	protected void uninstallApp(){
		if (mUninstallList.size() <= 0) {
			mUninstallList = null;
			if (null != mMyDialog) {
				mMyDialog.cancel();
				mMyDialog = null;
			}
			return;
		}
		String uninstallPkg = mUninstallList.get(0);
		mMyDialog.updateUI(mMyDialog.getMax() - mUninstallList.size() + 1, 
				AppManager.getAppLabel(uninstallPkg, pm));
		Uri packageUri = Uri.parse("package:" + uninstallPkg);
		Intent deleteIntent = new Intent();
		deleteIntent.setAction(Intent.ACTION_DELETE);
		deleteIntent.setData(packageUri);
		startActivityForResult(deleteIntent, REQUEST_CODE_UNINSTALL);
		mUninstallList.remove(0);
	}
    
    @SuppressWarnings("unchecked")
	protected void showBackupDialog(List<String> packageList){
    	Log.d(TAG, "Environment.getExternalStorageState():" + Environment.getExternalStorageState());
    	if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			mNotice.showToast(R.string.no_sdcard);
			return;
		}
    	final BackupAsyncTask task = new BackupAsyncTask();
    	task.execute(packageList);
    	mMyDialog = new MyDialog(getActivity(), packageList.size());
		mMyDialog.setTitle(R.string.backuping);
		mMyDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "showBackupDialog.onCancel");
				if (null != task) {
					task.cancel(true);
				}
			}
		});
		mMyDialog.setOnHideListener(new OnHideListener() {
			
			@Override
			public void onHide() {
				mIsBackupHide = true;
				mNotificationMgr.startBackupNotification();
				updateNotification(task.currentProgress, task.size, task.currentAppLabel);
			}
		});
		mMyDialog.show();
    }
    
    private class BackupAsyncTask extends AsyncTask<List<String>, Integer, Void>{
    	public int currentProgress = 0;
    	public int size = 0;
    	public String currentAppLabel;
    	private long startTime = 0;
    	private long endTime = 0;
    	
		@Override
		protected Void doInBackground(List<String>... params) {
			startTime = System.currentTimeMillis();
			size = params[0].size();
			File file = new File(ZYConstant.BACKUP_FOLDER); 
			if (!file.exists()){
				boolean ret = file.mkdirs();
				if (!ret) {
					Log.e(TAG, "create file fail:" + file.getAbsolutePath());
					return null;
				}
			}
			
			String label = "";
			String version = "";
			String sourceDir = "";
			String packageName = "";
			for (int i = 0; i < size; i++) {
				if (isCancelled()) {
					Log.d(TAG, "doInBackground.isCancelled");
					return null;
				}
				packageName = params[0].get(i);
				label = AppManager.getAppLabel(packageName, pm);
				version = AppManager.getAppVersion(packageName, pm);
				sourceDir = AppManager.getAppSourceDir(packageName, pm);
				
				currentAppLabel = label;
				currentProgress = i + 1;
				
				mMyDialog.updateName(label);
				String desPath = ZYConstant.BACKUP_FOLDER + "/" + label + "_" + version + ".apk";
				if (!new File(desPath).exists()) {
					try {
						ZYUtils.fileStreamCopy(sourceDir, desPath);
					} catch (IOException e) {
						Log.e(TAG, "doInBackground.Error:" + e.toString());
						Message message = mHandler.obtainMessage();
						message.obj = getString(R.string.backup_fail, label);
						message.what = MSG_TOAST;
						message.sendToTarget();
					}
				}
				mMyDialog.updateProgress(i + 1); 
				if (mIsBackupHide) {
					updateNotification(currentProgress, size, currentAppLabel);
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute");
			if (null != mMyDialog && mMyDialog.isShowing()) {
				mMyDialog.cancel();
				mMyDialog = null;
			}
			mNotice.showToast(R.string.backup_over);
			endTime = System.currentTimeMillis();
			if (mIsBackupHide) {
				mIsBackupHide = false;
				
				Message message = mHandler.obtainMessage();
				message.obj = endTime - startTime;
				message.what = MSG_BACKUP_OVER;
				message.sendToTarget();
			}
		}
    }
    
    public void updateNotification(int progress, int max, String name){
    	Message message = mHandler.obtainMessage();
    	message.arg1 = progress;
    	message.arg2 = max;
    	message.obj = name;
    	message.what = MSG_BACKUPING;
    	message.sendToTarget();
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_CODE_UNINSTALL == requestCode) {
			uninstallApp();
		}
	}
}
