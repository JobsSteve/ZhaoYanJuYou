package com.zhaoyan.juyou.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.juyou.JuYouApplication;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.dialog.LoginProgressDialog;

public class LoginActivity extends Activity implements View.OnClickListener {
	private ImageView mTopLogoImageView;
	private Bitmap mTopLogoBitmap;
	private EditText mAccountEditText;
	private EditText mPasswordEditText;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.login);

		initView();
	}

	private void initView() {
		mTopLogoBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.login_top);
		mTopLogoImageView = (ImageView) findViewById(R.id.iv_login_top);
		mTopLogoImageView.setImageBitmap(mTopLogoBitmap);
		Button loginButton = (Button) findViewById(R.id.btn_login);
		loginButton.setOnClickListener(this);
		Button loginDirectButton = (Button) findViewById(R.id.btn_login_direct);
		loginDirectButton.setOnClickListener(this);
		Button registerAccountButton = (Button) findViewById(R.id.btn_register);
		registerAccountButton.setOnClickListener(this);
		Button loginWeiXinButton = (Button) findViewById(R.id.btn_login_weixin);
		loginWeiXinButton.setOnClickListener(this);
		Button loginTencentWeiboButton = (Button) findViewById(R.id.btn_login_tencent_weibo);
		loginTencentWeiboButton.setOnClickListener(this);
		Button loginSinaWeiboButton = (Button) findViewById(R.id.btn_login_sina_weibo);
		loginSinaWeiboButton.setOnClickListener(this);
		Button loginQQButon = (Button) findViewById(R.id.btn_login_qq);
		loginQQButon.setOnClickListener(this);
		Button loginRenRenButton = (Button) findViewById(R.id.btn_login_renren);
		loginRenRenButton.setOnClickListener(this);

		mAccountEditText = (EditText) findViewById(R.id.et_account);
		mPasswordEditText = (EditText) findViewById(R.id.et_psw);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			loginZhanYan();
			break;
		case R.id.btn_login_direct:
			loginDirect();
			break;
		case R.id.btn_register:
			launchRegisterAccount();
			break;
		case R.id.btn_login_weixin:
			break;
		case R.id.btn_login_tencent_weibo:
			break;
		case R.id.btn_login_sina_weibo:
			break;
		case R.id.btn_login_qq:
			break;
		case R.id.btn_login_renren:
			break;
		default:
			break;
		}
	}

	private void loginZhanYan() {
		String account = mAccountEditText.getText().toString();
		if (TextUtils.isEmpty(account)) {
			Toast.makeText(mContext, R.string.account_empty_please_input,
					Toast.LENGTH_SHORT).show();
			return;
		}
		String password = mPasswordEditText.getText().toString();
		if (TextUtils.isEmpty(password)) {
			Toast.makeText(mContext, R.string.password_empty_please_input,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (NetWorkUtil.isNetworkConnected(getApplicationContext())) {
			Toast.makeText(this, R.string.login_message_no_network,
					Toast.LENGTH_SHORT).show();
			return;
		}

		LoginZhaoYanTask task = new LoginZhaoYanTask();
		task.execute();
	}

	private void loginDirect() {
		if (UserHelper.loadLocalUser(this) == null) {
			launchAccountSetting();
		} else {
			launchMain();
		}
		finish();
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
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
			finish();
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

	private class LoginZhaoYanTask extends AsyncTask<Void, Void, Integer> {
		private LoginProgressDialog mLoginDialog;
		private static final int LOGIN_TIMEOUT = 5000;
		private static final int LOGIN_RESULT_SUCCESS = 1;
		private static final int LOGIN_RESULT_FAIL_ACCOUNT_ERROR = 2;
		private Timer mTimer;
		private boolean mIsTimeout = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoginDialog = new LoginProgressDialog(LoginActivity.this);
			mLoginDialog.show();
			mIsTimeout = false;
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					LoginZhaoYanTask.this.cancel(true);
					mIsTimeout = true;
				}
			}, LOGIN_TIMEOUT);

		}

		@Override
		protected Integer doInBackground(Void... params) {
			int result = doLoginZhaoyan();
			return result;
		}

		private int doLoginZhaoyan() {
			// TODO
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return LOGIN_RESULT_FAIL_ACCOUNT_ERROR;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (mLoginDialog.isShowing()) {
				mLoginDialog.dismiss();
			}
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
			switch (result) {
			case LOGIN_RESULT_SUCCESS:
				launchMain();
				finish();
				break;
			case LOGIN_RESULT_FAIL_ACCOUNT_ERROR:
				Toast.makeText(mContext, R.string.login_fail_account_error,
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

		@Override
		protected void onCancelled() {
			if (mLoginDialog.isShowing()) {
				mLoginDialog.dismiss();
			}
			if (mIsTimeout) {
				Toast.makeText(mContext, R.string.login_fail_timeout,
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
