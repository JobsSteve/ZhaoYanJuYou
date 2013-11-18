package com.zhaoyan.juyou.activity;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.fragment.AppFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;

public class AppActivity extends FragmentActivity{
	private AppFragment mAppFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mAppFragment= new AppFragment();
		getSupportFragmentManager().beginTransaction().replace(
				android.R.id.content, mAppFragment).commit();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			boolean ret = mAppFragment.onBackPressed();
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
		// TODO Auto-generated method stub
		if (null != mAppFragment) {
			mAppFragment = null;
		}
		super.onDestroy();
	}
}
