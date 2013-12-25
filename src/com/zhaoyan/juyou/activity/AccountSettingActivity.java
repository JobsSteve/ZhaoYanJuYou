package com.zhaoyan.juyou.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
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
	private static final int MSG_UPDATE_USER_INFO = 1;
	private Bitmap mHeadBitmap;

	private BroadcastReceiver mUserInfoBroadcastReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting);

		initTitle(R.string.account_setting);
		initView();

		loadUserInfo();

		mHandler = new UiHandler();

		mUserInfoBroadcastReceiver = new UserInfoBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ZYConstant.LOCAL_USER_INFO_CHANGED_ACTION);
		registerReceiver(mUserInfoBroadcastReceiver, intentFilter);
	}

	private void loadUserInfo() {
		UserInfo userInfo = UserHelper.loadLocalUser(this);
		int headId = userInfo.getHeadId();
		if (headId != UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
			mHeadImageView.setImageResource(UserHelper
					.getHeadImageResource(headId));
		} else {
			releaseHeadBitmap();
			mHeadBitmap = userInfo.getHeadBitmap();
			mHeadImageView.setImageBitmap(mHeadBitmap);
		}

		mNameTextView.setText(userInfo.getUser().getUserName());
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
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseHeadBitmap();
		unregisterReceiver(mUserInfoBroadcastReceiver);
	}

	private class UserInfoBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mHandler.obtainMessage(MSG_UPDATE_USER_INFO).sendToTarget();
		}
	}

	private class UiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_USER_INFO:
				loadUserInfo();
				break;

			default:
				break;
			}
		}
	}

}
