package com.zhaoyan.juyou.activity;

import java.io.File;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import com.zhaoyan.juyou.common.ZYConstant;

public class AccountSettingActivity extends BaseActivity implements
		OnClickListener, OnItemClickListener {
	private static final String TAG = "AccountSettingActivity";

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
	private Bitmap mHeadBitmap;

	private Notice mNotice;

	private final String DEFAULT_NAME = android.os.Build.MANUFACTURER;

	private final static int REQUEST_IMAGE = 1;
	private final static int REQUEST_CAPTURE = 2;
	private final static int REQUEST_RESIZE = 3;

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
		case R.id.btn_capture_head:
			showCaptureDialog();
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
		User user = UserHelper.loadLocalUser(this);
		// name
		String name = mNickNameEditText.getText().toString();
		if (TextUtils.isEmpty(name)) {
			name = DEFAULT_NAME;
		}
		user.setUserName(name);
		// head id
		user.setHeadId(mCurrentHeadId);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case REQUEST_IMAGE:
			resizeImage(data.getData());
			break;
		case REQUEST_RESIZE:
			if (data != null) {
				Bitmap bitmap = data.getExtras().getParcelable("data");
				saveResizedImageToHead(bitmap);
			} else {
				Log.e(TAG, "REQUEST_RESIZE data is null.");
			}
			break;
		case REQUEST_CAPTURE:
			resizeImage(getHeadImageUri());
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void saveResizedImageToHead(Bitmap bitmap) {
		releaseHeadBitmap();
		mHeadBitmap = bitmap;
		Drawable drawable = new BitmapDrawable(getResources(), mHeadBitmap);
		mHeadImageView.setImageDrawable(drawable);
		mCurrentHeadId = User.ID_NOT_PRE_INSTALL_HEAD;
	}

	private void releaseHeadBitmap() {
		if (mHeadBitmap != null) {
			mHeadImageView.setImageDrawable(null);
			mHeadBitmap.recycle();
			mHeadBitmap = null;
		}
	}

	public void showCaptureDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.customize_head)
				.setItems(R.array.customize_head_list,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								switch (which) {
								case 0:
									// capture new picture.
									captureHead();
									break;
								case 1:
									// select from picture.
									selectHead();
									break;

								default:
									break;
								}
							}

						}).create();
		dialog.show();
	}

	private void selectHead() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		try {
			startActivityForResult(intent, REQUEST_IMAGE);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "selectHead error. " + e);
			mNotice.showToast("ActivityNotFoundException");
		}

	}

	private void resizeImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		try {
			startActivityForResult(intent, REQUEST_RESIZE);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "resizeImage error. " + e);
			mNotice.showToast("ActivityNotFoundException");
		}
	}

	private void captureHead() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, getHeadImageUri());
		try {
			startActivityForResult(intent, REQUEST_CAPTURE);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "captureHead error. " + e);
			mNotice.showToast("ActivityNotFoundException");
		}
	}

	private Uri getHeadImageUri() {
		File dir = new File(ZYConstant.JUYOU_FOLDER + "/head/");
		dir.mkdirs();
		File file = new File(dir, "juyou_head.jpg");
		return Uri.fromFile(file);
	}

}
