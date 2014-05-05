package com.zhaoyan.juyou.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.FileTransferService;
import com.zhaoyan.juyou.JuYouApplication;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.AppInfo;
import com.zhaoyan.juyou.common.AppManager;
import com.zhaoyan.juyou.common.HistoryManager;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.provider.AppData;

public class WelcomeActivity extends Activity {
	private static final String TAG = "WelcomeActivity";
	/** The minimum time(ms) of loading page. */
	private static final int MIN_LOADING_TIME = 1500;
	private ImageView mBackgroundImageView;
	private Bitmap mBackgroundBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		mBackgroundBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.welcome);
		mBackgroundImageView = (ImageView) findViewById(R.id.iv_welcome);
		mBackgroundImageView.setImageBitmap(mBackgroundBitmap);
		

		LoadAsyncTask loadAsyncTask = new LoadAsyncTask();
		loadAsyncTask.execute();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// Not allow exit when loading, just wait loading jobs to be done.
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		releaseResource();
		super.onDestroy();
	}

	/**
	 * Do application initialize and load resources. This does not run in ui
	 * thread.
	 */
	private void load() {
		JuYouApplication.initApplication(getApplicationContext());
		startFileTransferService();
		
		HistoryManager.modifyHistoryDb(getApplicationContext());
		
		// Delete old APP Database table and refresh it.
		getContentResolver().delete(AppData.App.CONTENT_URI, null, null);
		LoadAppThread thread =  new LoadAppThread();
		thread.start();
		
		ZYConstant.initJuyouFolder();
	}
	
	private void startFileTransferService(){
		Intent intent = new Intent();
		intent.setClass(this, FileTransferService.class);
		startService(intent);
	}

	/**
	 * Load is finished
	 */
	private void loadFinished() {
		if (PreviewPagesActivity
				.isNeedShowPreviewPages(getApplicationContext())) {
			launchPreviewPages();
		} else {
			launchLogin();
		}

		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
	}

	private void launchPreviewPages() {
		Intent intent = new Intent();
		intent.setClass(this, PreviewPagesActivity.class);
		startActivity(intent);
	}

	private void launchLogin() {
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
	}

	private void releaseResource() {
		mBackgroundImageView.setImageDrawable(null);
		mBackgroundBitmap.recycle();
		mBackgroundImageView = null;
		mBackgroundBitmap = null;
		System.gc();
	}

	class LoadAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			long start = System.currentTimeMillis();
			// Do load.
			load();
			long end = System.currentTimeMillis();
			if (end - start < MIN_LOADING_TIME) {
				try {
					Thread.sleep(MIN_LOADING_TIME - (end - start));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			loadFinished();
		}
	};
	
	private class LoadAppThread extends Thread {
		@Override
		public void run() {
			loadAppToDb();
		}
	}

	private void loadAppToDb() {
		long start = System.currentTimeMillis();

		List<ContentValues> valuesList = new ArrayList<ContentValues>();
		// Retrieve all known applications.
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> apps = pm.getInstalledApplications(0);
		if (apps == null) {
			apps = new ArrayList<ApplicationInfo>();
		}
		ContentValues values = null;
		for (int i = 0; i < apps.size(); i++) {
			ApplicationInfo info = apps.get(i);
			// 获取非系统应用
			int flag1 = info.flags & ApplicationInfo.FLAG_SYSTEM;
			if (flag1 <= 0) {
				AppInfo entry = new AppInfo(WelcomeActivity.this, apps.get(i));
				entry.setPackageName(info.packageName);
				entry.loadLabel();
				entry.setAppIcon(info.loadIcon(pm));
				entry.loadVersion();
				boolean is_game_app = AppManager.isGameApp(getApplicationContext(), info.packageName);
				if (is_game_app) {
					entry.setType(AppManager.GAME_APP);
				} else {
					entry.setType(AppManager.NORMAL_APP);
				}
				values = AppManager.getValuesByAppInfo(entry);
				valuesList.add(values);
			} else {
				// system app
			}
		}

		// get values
		ContentValues[] contentValues = new ContentValues[0];
		contentValues = valuesList.toArray(contentValues);
		getContentResolver().bulkInsert(AppData.App.CONTENT_URI, contentValues);
		Log.d(TAG, "loadAppToDb cost time = "
				+ (System.currentTimeMillis() - start));
	}

}
