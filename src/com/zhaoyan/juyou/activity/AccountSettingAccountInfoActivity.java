package com.zhaoyan.juyou.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.zhaoyan.juyou.AccountHelper;
import com.zhaoyan.juyou.AccountInfo;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant;

public class AccountSettingAccountInfoActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "AccountSettingAccountInfoActivity";

	private TextView mAccountZhaoyanTextView;
	private TextView mPhoneNumberTextView;
	private TextView mEmailTextView;
	private TextView mAccountQQTextView;
	private TextView mAccountRenrenTextView;
	private TextView mAccountSinaWeiboTextView;
	private TextView mAccountTencentWeiboTextView;

	private Handler mHandler;
	private static final int MSG_UPDATE_ACCOUNT_INFO = 1;
	private BroadcastReceiver mAccountInfoBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting_account_info);

		initTitle(R.string.account_setting_account_info_title);
		initView();

		getAccountInfo();

		mHandler = new UiHandler();

		mAccountInfoBroadcastReceiver = new AccountInfoBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ZYConstant.CURRENT_ACCOUNT_CHANGED_ACTION);
		registerReceiver(mAccountInfoBroadcastReceiver, intentFilter);
	}

	private void getAccountInfo() {
		AccountInfo accountInfo = AccountHelper.getCurrentAccount(this);
		String accountZhaoyan = accountInfo.getAccountZhaoyan();
		setTextWithEmptyText(mAccountZhaoyanTextView, accountZhaoyan);

		String phoneNumber = accountInfo.getPhoneNumber();
		setTextWithEmptyText(mPhoneNumberTextView, phoneNumber);

		String email = accountInfo.getEmail();
		setTextWithEmptyText(mEmailTextView, email);

		String accountQQ = accountInfo.getAccountQQ();
		setTextWithEmptyText(mAccountQQTextView, accountQQ);

		String accountRenren = accountInfo.getAccountRenren();
		setTextWithEmptyText(mAccountRenrenTextView, accountRenren);

		String accountSinaWeibo = accountInfo.getAccountSinaWeibo();
		setTextWithEmptyText(mAccountSinaWeiboTextView, accountSinaWeibo);

		String accountTencentWeibo = accountInfo.getAccountTencentWeibo();
		setTextWithEmptyText(mAccountTencentWeiboTextView, accountTencentWeibo);
	}

	private void setTextWithEmptyText(TextView textView, CharSequence text) {
		if (TextUtils.isEmpty(text)) {
			textView.setText(R.string.account_setting_not_set);
		} else {
			textView.setText(text);
		}
	}

	private void initView() {
		View accountZhaoyanView = findViewById(R.id.rl_asai_zhaoyan);
		accountZhaoyanView.setOnClickListener(this);
		mAccountZhaoyanTextView = (TextView) findViewById(R.id.tv_asai_zhaoyan);

		View phoneNumberView = findViewById(R.id.rl_asai_phone);
		phoneNumberView.setOnClickListener(this);
		mPhoneNumberTextView = (TextView) findViewById(R.id.tv_asai_phone);

		View emailView = findViewById(R.id.rl_asai_email);
		emailView.setOnClickListener(this);
		mEmailTextView = (TextView) findViewById(R.id.tv_asai_email);

		View accountQQView = findViewById(R.id.rl_asai_qq);
		accountQQView.setOnClickListener(this);
		mAccountQQTextView = (TextView) findViewById(R.id.tv_asai_qq);

		View accountRenrenView = findViewById(R.id.rl_asai_renren);
		accountRenrenView.setOnClickListener(this);
		mAccountRenrenTextView = (TextView) findViewById(R.id.tv_asai_renren);

		View accountSinaWeiboView = findViewById(R.id.rl_asai_weibo_sina);
		accountSinaWeiboView.setOnClickListener(this);
		mAccountSinaWeiboTextView = (TextView) findViewById(R.id.tv_asai_weibo_sina);

		View accountTencentWeiboView = findViewById(R.id.rl_asai_weibo_tencent);
		accountTencentWeiboView.setOnClickListener(this);
		mAccountTencentWeiboTextView = (TextView) findViewById(R.id.tv_asai_weibo_tencent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_asai_zhaoyan:
			openActivity(AccountSettingAccountZhaoyanActivity.class);
			break;
		case R.id.rl_asai_phone:

			break;
		case R.id.rl_asai_email:

			break;
		case R.id.rl_asai_qq:

			break;
		case R.id.rl_asai_renren:

			break;
		case R.id.rl_asai_weibo_sina:

			break;
		case R.id.rl_asai_weibo_tencent:

			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mAccountInfoBroadcastReceiver);
	}

	private class AccountInfoBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mHandler.obtainMessage(MSG_UPDATE_ACCOUNT_INFO).sendToTarget();
		}
	}

	private class UiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_ACCOUNT_INFO:
				getAccountInfo();
				break;

			default:
				break;
			}
		}
	}
}
