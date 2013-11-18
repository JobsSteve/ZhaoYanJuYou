package com.zhaoyan.juyou.activity;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.fragment.GameFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Window;

public class GameActivity extends FragmentActivity{
	private GameFragment mGameFragment;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mGameFragment= new GameFragment();
		getSupportFragmentManager().beginTransaction().replace(
				android.R.id.content, mGameFragment).commit();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			boolean ret = mGameFragment.onBackPressed();
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
		if (null != mGameFragment) {
			mGameFragment = null;
		}
		super.onDestroy();
	}
}
