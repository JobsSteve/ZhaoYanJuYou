package com.zhaoyan.juyou.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.dreamlink.communication.aidl.User;
import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.HeadChooseAdapter;

public class AccountSettingActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {
	public static final String EXTRA_IS_FISRT_LAUNCH = "fist_launch";
	private boolean mIsFisrtLaunch = false;

	private Button mSaveButton;
	private Button mCanceButton;

	private EditText mNickNameEditText;

	private ImageView mHeadImageView;
	private ImageButton mCaptureHeadButton;
	private GridView mChooseHeadGridView;

	private HeadChooseAdapter mHeadChooseAdapter;
	private int[] mHeadImages = UserHelper.HEAD_IMAGES;
	private int mCurrentHeadId = 0;

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
		User user = UserHelper.loadLocalUser(this);

		if (user == null) {
			mNickNameEditText.setText(DEFAULT_NAME);
			mCurrentHeadId = 0;
			mHeadImageView.setImageResource(mHeadImages[mCurrentHeadId]);
		} else {
			String name = user.getUserName();
			if (!TextUtils.isEmpty(name)) {
				mNickNameEditText.setText(name);
			} else {
				mNickNameEditText.setText(DEFAULT_NAME);
			}

			int headId = user.getHeadId();
			if (headId != User.ID_NOT_PRE_INSTALL_HEAD) {
				mCurrentHeadId = headId;
				mHeadImageView.setImageResource(mHeadImages[mCurrentHeadId]);
			}
		}

	}

	private void initView() {
		mSaveButton = (Button) findViewById(R.id.btn_save);
		mSaveButton.setOnClickListener(this);
		mCanceButton = (Button) findViewById(R.id.btn_cancel);
		mCanceButton.setOnClickListener(this);

		mNickNameEditText = (EditText) findViewById(R.id.et_nick_name);

		mHeadImageView = (ImageView) findViewById(R.id.iv_as_head);
		mCaptureHeadButton = (ImageButton) findViewById(R.id.btn_capture_head);
		mCaptureHeadButton.setOnClickListener(this);

		mChooseHeadGridView = (GridView) findViewById(R.id.gv_as_choose_head);
		mChooseHeadGridView.setOnItemClickListener(this);
		mHeadChooseAdapter = new HeadChooseAdapter(this, mHeadImages);
		mChooseHeadGridView.setAdapter(mHeadChooseAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_save:
			saveAccount();
			setResult(RESULT_OK);
			if (mIsFisrtLaunch) {
				launchMain();
				finish();
			} else {
				finishWithAnimation();
			}
			break;
		case R.id.btn_cancel:
			setResult(RESULT_CANCELED);
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
		User user = new User();
		// name
		String name = mNickNameEditText.getText().toString();
		if (TextUtils.isEmpty(name)) {
			name = DEFAULT_NAME;
		}
		user.setUserName(name);
		// id
		user.setUserID(0);
		// head id
		user.setHeadId(mCurrentHeadId);
		// user type
		user.setIsLocal(true);
		// Save to database
		UserHelper.saveUser(this, user);

		// Update UserManager.
		UserManager userManager = UserManager.getInstance();
		userManager.setLocalUser(user);
		mNotice.showToast(R.string.account_setting_saved_message);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mCurrentHeadId = position;
		mHeadImageView.setImageResource(mHeadImages[mCurrentHeadId]);
	}

}
