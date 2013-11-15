package com.zhaoyan.juyou.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.zhaoyan.communication.FileTransferService;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.HistoryManager;

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
		startFileTransferService();
		
		HistoryManager.modifyHistoryDb(WelcomeActivity.this);
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

}
