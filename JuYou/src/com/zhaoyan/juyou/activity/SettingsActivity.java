package com.zhaoyan.juyou.activity;

import com.zhaoyan.juyou.fragment.WoFragment;

import android.os.Bundle;

public class SettingsActivity extends BaseFragmentActivity {
	private WoFragment mWoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWoFragment = new WoFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(android.R.id.content, mWoFragment).commit();
	}

}
