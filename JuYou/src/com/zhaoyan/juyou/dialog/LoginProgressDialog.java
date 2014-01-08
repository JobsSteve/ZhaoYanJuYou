package com.zhaoyan.juyou.dialog;

import com.zhaoyan.juyou.R;

import android.content.Context;

public class LoginProgressDialog extends ZyProgressDialog {

	public LoginProgressDialog(Context context) {
		super(context);
		setMessage(R.string.login_dialog_message);
	}
}
