package com.zhaoyan.juyou.dialog;

import com.zhaoyan.juyou.R;

import android.content.Context;
import android.os.Bundle;

public class LoginProgressDialog extends ZyAlertDialog {

	public LoginProgressDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_progress_dialog);
		setCancelable(false);
	}
}
