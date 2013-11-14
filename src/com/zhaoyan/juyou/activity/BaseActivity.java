package com.zhaoyan.juyou.activity;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.zhaoyan.juyou.R;

public class BaseActivity extends Activity {

	// title view
	protected View mCustomTitleView;
	protected TextView mTitleNameView;
	protected TextView mTitleNumView;

	protected void initTitle(int titleName) {
		mCustomTitleView = findViewById(R.id.title);

		// title name view
		mTitleNameView = (TextView) mCustomTitleView
				.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(titleName);
		mTitleNumView = (TextView) mCustomTitleView.findViewById(R.id.tv_title_num);
	}
	
	protected void updateTitleNum(int num) {
		mTitleNumView.setText(getString(R.string.num_format, num));
	}

	protected void finishWithAnimation() {
		finish();
		overridePendingTransition(0, R.anim.activity_right_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
