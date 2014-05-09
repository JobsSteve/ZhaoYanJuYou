package com.zhaoyan.juyou.activity;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.fragment.FileBrowserFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;

public class FileBrowserActivity extends FragmentActivity{
	private FileBrowserFragment mBrowserFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mBrowserFragment= new FileBrowserFragment();
		getSupportFragmentManager().beginTransaction().replace(
				android.R.id.content, mBrowserFragment).commit();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			boolean ret = mBrowserFragment.onBackPressed();
			if (ret) {
				finish();
				overridePendingTransition(0, R.anim.activity_right_out);
				return true;
			}else {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		if (null != mBrowserFragment) {
			mBrowserFragment = null;
		}
		super.onDestroy();
	}
}
