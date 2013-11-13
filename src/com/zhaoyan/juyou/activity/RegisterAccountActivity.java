package com.zhaoyan.juyou.activity;


import android.os.Bundle;

import com.zhaoyan.juyou.R;

public class RegisterAccountActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_account);

		initTitle(R.string.register);
		initView();
	}

	private void initView() {
	}

}
