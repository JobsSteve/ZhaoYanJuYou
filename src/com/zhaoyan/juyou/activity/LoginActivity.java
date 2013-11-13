package com.zhaoyan.juyou.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.juyou.JuYouApplication;
import com.zhaoyan.juyou.R;

public class LoginActivity extends Activity implements View.OnClickListener {
	private Button mLoginButton;
	private Button mRegisterAccountButton;
	private ImageView mTopLogoImageView;
	private Bitmap mTopLogoBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		initView();
	}

	private void initView() {
		mTopLogoBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.login_top);
		mTopLogoImageView = (ImageView) findViewById(R.id.iv_login_top);
		mTopLogoImageView.setImageBitmap(mTopLogoBitmap);
		mLoginButton = (Button) findViewById(R.id.btn_login);
		mLoginButton.setOnClickListener(this);

		mRegisterAccountButton = (Button) findViewById(R.id.btn_register);
		mRegisterAccountButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			if (UserHelper.getUserName(getApplicationContext()) == null) {
				launchAccountSetting();
			} else {
				launchMain();
			}
			finish();
			break;

		case R.id.btn_register:
			launchRegisterAccount();
			break;
		default:
			break;
		}
	}

	private void launchAccountSetting() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.putExtra(AccountSettingActivity.EXTRA_IS_FISRT_LAUNCH, true);
		intent.setClass(this, AccountSettingActivity.class);
		startActivity(intent);
	}

	private void launchRegisterAccount() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(this, RegisterAccountActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_right_in, 0);
	}

	private void launchMain() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		releaseResource();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			JuYouApplication.quitApplication(this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void releaseResource() {
		mTopLogoImageView.setImageDrawable(null);
		mTopLogoBitmap.recycle();
		mTopLogoImageView = null;
		mTopLogoBitmap = null;
		System.gc();
	}

}
