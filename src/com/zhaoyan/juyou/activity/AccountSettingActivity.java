package com.zhaoyan.juyou.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.juyou.R;

public class AccountSettingActivity extends BaseActivity implements
		OnClickListener {
	public static final String EXTRA_IS_FISRT_LAUNCH = "fist_launch";
	private boolean mIsFisrtLaunch = false;

	private Button mSaveButton;
	private Button mCanceButton;

	private EditText mNickNameEditText;

	private Notice mNotice;

	private final String DEFAULT_NAME = android.os.Build.MANUFACTURER;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting);

		Intent intent = getIntent();
		mIsFisrtLaunch = intent.getBooleanExtra(EXTRA_IS_FISRT_LAUNCH, false);
		mNotice = new Notice(this);

		initTitle(R.string.account_setting);
		initView();

		setUserInfo();
	}

	private void setUserInfo() {
		String name = UserHelper.loadUser(this).getUserName();
		if (!TextUtils.isEmpty(name)
				&& !UserHelper.KEY_NAME_DEFAULT.equals(name)) {
			mNickNameEditText.setText(name);
		} else {
			mNickNameEditText.setText(DEFAULT_NAME);
		}
	}

	private void initView() {
		mSaveButton = (Button) findViewById(R.id.btn_save);
		mSaveButton.setOnClickListener(this);
		mCanceButton = (Button) findViewById(R.id.btn_cancel);
		mCanceButton.setOnClickListener(this);

		mNickNameEditText = (EditText) findViewById(R.id.et_nick_name);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_save:
			saveAccount();
			if (mIsFisrtLaunch) {
				launchMain();
				finish();
			} else {
				finishWithAnimation();
			}
			break;
		case R.id.btn_cancel:
			if (mIsFisrtLaunch) {
				finish();
			} else {
				finishWithAnimation();
			}
			break;
		default:
			break;
		}
	}

	private void launchMain() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
	}

	private void saveAccount() {
		User user = UserHelper.loadUser(this);
		String name = mNickNameEditText.getText().toString();
		if (TextUtils.isEmpty(name)) {
			name = DEFAULT_NAME;
		}
		user.setUserName(name);
		UserManager userManager = UserManager.getInstance();
		userManager.setLocalUser(user);
		UserHelper.saveUser(this, user);
		mNotice.showToast(R.string.account_setting_saved_message);
	}

}
