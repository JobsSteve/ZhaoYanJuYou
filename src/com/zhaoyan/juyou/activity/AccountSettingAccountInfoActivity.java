package com.zhaoyan.juyou.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.zhaoyan.juyou.R;


public class AccountSettingAccountInfoActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "AccountSettingAccountInfoActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting_account_info);

		initTitle(R.string.account_setting);
		initView();
	}

	private void initView() {
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		default:
			break;
		}
	}
}
