package com.zhaoyan.juyou.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.juyou.AccountHelper;
import com.zhaoyan.juyou.AccountInfo;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant;

public class AccountSettingAccountZhaoyanActivity extends BaseActivity
		implements OnClickListener {
	private static final String TAG = "AccountSettingAccountZhaoyanActivity";

	public static final int MIN_ZHAOYAN_ACCOUNT_LENGTH = 6;
	public static final int MAX_ZHAOYAN_ACCOUNT_LENGTH = 20;

	private Button mSaveButton;
	private Button mCanceButton;
	private EditText mZhaoyanEditText;
	private Notice mNotice;

	private TextWatcher mTextWatcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting_account_zhaoyan);
		mNotice = new Notice(this);

		initTitle(R.string.account_setting_account_zhaoyan_title);
		initView();

		setAccountInfo();
	}

	private void setAccountInfo() {
		AccountInfo accountInfo = AccountHelper.getCurrentAccount(this);

		String accountZhaoyan = accountInfo.getAccountZhaoyan();
		mZhaoyanEditText.setText(accountZhaoyan);
		mZhaoyanEditText.setSelection(mZhaoyanEditText.length());

		updateSaveButton();
	}

	private void initView() {
		mSaveButton = (Button) findViewById(R.id.btn_save);
		mSaveButton.setOnClickListener(this);
		mCanceButton = (Button) findViewById(R.id.btn_cancel);
		mCanceButton.setOnClickListener(this);

		mZhaoyanEditText = (EditText) findViewById(R.id.et_asai_zhaoyan);
		mTextWatcher = new ZhaoyanAccountTextWatcher(mZhaoyanEditText);
		mZhaoyanEditText.addTextChangedListener(mTextWatcher);
		mZhaoyanEditText.setFilters(new InputFilter[] { mInputFilter });

		TextView inputLimiTextView = (TextView) findViewById(R.id.tv_asai_zhaoyan_input_limit);
		inputLimiTextView.setText(getString(
				R.string.account_setting_account_zhaoyan_input_limit,
				MIN_ZHAOYAN_ACCOUNT_LENGTH, MAX_ZHAOYAN_ACCOUNT_LENGTH));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_save:
			saveAndQuit();
			break;
		case R.id.btn_cancel:
			cancelAndQuit();
			break;
		default:
			break;
		}
	}

	private void cancelAndQuit() {
		hideInputMethodManager();
		finishWithAnimation();
	}

	private void saveAndQuit() {
		hideInputMethodManager();
		saveAccount();
		finishWithAnimation();
	}

	private void hideInputMethodManager() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(
				mZhaoyanEditText.getWindowToken(), 0);
	}

	private void saveAccount() {
		String accountZhaoyan = mZhaoyanEditText.getText().toString();
		if (!TextUtils.isEmpty(accountZhaoyan)) {
			// save account
			AccountInfo accountInfo = AccountHelper.getCurrentAccount(this);
			accountInfo.setAccountZhaoyan(accountZhaoyan);
			AccountHelper.saveCurrentAccount(this, accountInfo);

			// Send broadcast
			Intent intent = new Intent(
					ZYConstant.CURRENT_ACCOUNT_CHANGED_ACTION);
			sendBroadcast(intent);

			mNotice.showToast(R.string.account_setting_saved_message);
		}
	}

	private void updateSaveButton() {
		Editable s = mZhaoyanEditText.getText();
		if (s.length() == 0) {
			mSaveButton.setEnabled(false);
		} else if (s.length() < MIN_ZHAOYAN_ACCOUNT_LENGTH) {
			mNotice.showToast(R.string.zhaoyan_account_edit_message_too_short);
			mSaveButton.setEnabled(false);
		} else if (!Character.isLetter(s.charAt(0))) {
			mNotice.showToast(R.string.zhaoyan_account_edit_message_begin_with_letter);
			mSaveButton.setEnabled(false);
		} else {
			mSaveButton.setEnabled(true);
		}
	}

	/**
	 * Only allow a-z A-Z 0-9 _.
	 * @param s
	 * @return
	 */
	private String filterCharactors(CharSequence s) {
		Pattern pattern = Pattern.compile("[^a-zA-Z0-9_]");
		Matcher matcher = pattern.matcher(s);
		return matcher.replaceAll("");
	}

	private class ZhaoyanAccountTextWatcher implements TextWatcher {
		private EditText mEditText;
		private int mStart;
		private int mEnd;

		public ZhaoyanAccountTextWatcher(EditText editText) {
			mEditText = editText;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() > MAX_ZHAOYAN_ACCOUNT_LENGTH) {
				mStart = mEditText.getSelectionStart();
				mEnd = mEditText.getSelectionEnd();

				while (s.length() > MAX_ZHAOYAN_ACCOUNT_LENGTH) {
					s.delete(mStart - 1, mEnd);
					mStart--;
					mEnd--;
				}
				mEditText.setSelection(mEditText.length());
			}

			updateSaveButton();
		}
	}

	private InputFilter mInputFilter = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) {

			return filterCharactors(source);
		}
	};
}
