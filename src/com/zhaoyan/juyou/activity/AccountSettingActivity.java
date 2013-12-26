package com.zhaoyan.juyou.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.juyou.AccountHelper;
import com.zhaoyan.juyou.AccountInfo;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant;

public class AccountSettingActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "AccountSettingActivity";
	public static final String EXTRA_IS_FISRT_LAUNCH = "fist_launch";

	private ImageView mHeadImageView;
	private TextView mNameTextView;
	private TextView mAccountInfoTextView;
	private TextView mSignatureTextView;
	private TextView mSoundTextView;
	private TextView mTransmitDirectoryTextView;
	private TextView mClearTransmitFilesTextView;

	private Handler mHandler;
	private static final int MSG_UPDATE_ACCOUNT_INFO = 1;
	private Bitmap mHeadBitmap;

	private BroadcastReceiver mAccountInfoBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting);

		initTitle(R.string.account_setting);
		initView();

		loadCurrentAccount();

		mHandler = new UiHandler();

		mAccountInfoBroadcastReceiver = new AccountInfoBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ZYConstant.CURRENT_ACCOUNT_CHANGED_ACTION);
		registerReceiver(mAccountInfoBroadcastReceiver, intentFilter);
	}

	private void loadCurrentAccount() {
		AccountInfo accountInfo = AccountHelper.getCurrentAccount(this);
		int headId = accountInfo.getHeadId();
		if (headId != AccountInfo.HEAD_ID_NOT_PRE_INSTALL) {
			mHeadImageView.setImageResource(AccountHelper
					.getHeadImageResource(headId));
		} else {
			releaseHeadBitmap();
			mHeadBitmap = accountInfo.getHeadBitmap();
			mHeadImageView.setImageBitmap(mHeadBitmap);
		}

		mNameTextView.setText(accountInfo.getUserName());
		
		String signature = accountInfo.getSignature();
		if (!TextUtils.isEmpty(signature)) {
			mSignatureTextView.setText(signature);
		}else {
			mSignatureTextView.setText(R.string.account_setting_not_set);
		}
	}

	private void releaseHeadBitmap() {
		if (mHeadBitmap != null) {
			mHeadImageView.setImageDrawable(null);
			mHeadBitmap.recycle();
			mHeadBitmap = null;
		}
	}

	private void initView() {
		View headView = findViewById(R.id.rl_as_head);
		headView.setOnClickListener(this);
		mHeadImageView = (ImageView) findViewById(R.id.iv_as_head);

		View nameView = findViewById(R.id.rl_as_name);
		nameView.setOnClickListener(this);
		mNameTextView = (TextView) findViewById(R.id.tv_as_name);

		View accountInfo = findViewById(R.id.rl_as_account_info);
		accountInfo.setOnClickListener(this);
		mAccountInfoTextView = (TextView) findViewById(R.id.tv_as_account_info);

		View signatureView = findViewById(R.id.rl_as_signature);
		signatureView.setOnClickListener(this);
		mSignatureTextView = (TextView) findViewById(R.id.tv_as_signature);

		View soundView = findViewById(R.id.rl_as_sound);
		soundView.setOnClickListener(this);
		mSoundTextView = (TextView) findViewById(R.id.tv_as_sound);

		View transmitDirectoryView = findViewById(R.id.rl_as_transmit_directory);
		transmitDirectoryView.setOnClickListener(this);
		mTransmitDirectoryTextView = (TextView) findViewById(R.id.tv_as_transmit_directory);

		View clearTransmitFiles = findViewById(R.id.rl_as_clear_transmit_files);
		clearTransmitFiles.setOnClickListener(this);
		mClearTransmitFilesTextView = (TextView) findViewById(R.id.tv_as_clear_transmit_files);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_as_head:
			openActivity(AccountSettingHeadActivity.class);
			break;
		case R.id.rl_as_name:
			openActivity(AccountSettingNameActivity.class);
			break;
		case R.id.rl_as_account_info:
			openActivity(AccountSettingAccountInfoActivity.class);
			break;
		case R.id.rl_as_signature:
			openActivity(AccountSettingSignatureActivity.class);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseHeadBitmap();
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
				loadCurrentAccount();
				break;

			default:
				break;
			}
		}
	}

}
