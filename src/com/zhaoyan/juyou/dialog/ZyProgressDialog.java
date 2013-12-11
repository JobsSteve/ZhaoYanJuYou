package com.zhaoyan.juyou.dialog;

import com.zhaoyan.juyou.R;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class ZyProgressDialog extends ZyAlertDialog {
	private String mMessage;

	public ZyProgressDialog(Context context) {
		super(context);
		setCancelable(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress_dialog);
		if (mMessage != null) {
			TextView textView = (TextView) findViewById(R.id.tv_pd_message);
			textView.setText(mMessage);
		}
	}

	@Override
	public void setMessage(String message) {
		mMessage = message;
	}

	@Override
	public void setMessage(int message) {
		mMessage = getContext().getString(message);
	}
}
