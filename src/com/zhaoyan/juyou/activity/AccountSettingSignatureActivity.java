package com.zhaoyan.juyou.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.juyou.R;

public class AccountSettingSignatureActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "AccountSettingNameActivity";
	private Button mSaveButton;
	private Button mCanceButton;
	private EditText mSignaturEditText;
	private TextView mSignatureWordCount;
	private Notice mNotice;
	private static final int MAX_SIGNATURE_WORD_COUNT = 60;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_setting_signature);
		mNotice = new Notice(this);

		initTitle(R.string.account_setting);
		initView();

		loadSignature();
	}

	private void loadSignature() {

	}

	private void initView() {
		mSaveButton = (Button) findViewById(R.id.btn_save);
		mSaveButton.setOnClickListener(this);
		mCanceButton = (Button) findViewById(R.id.btn_cancel);
		mCanceButton.setOnClickListener(this);

		mSignaturEditText = (EditText) findViewById(R.id.et_as_signature);
		mSignaturEditText.addTextChangedListener(mTextWatcher);
		mSignaturEditText.setSelection(mSignaturEditText.length());

		mSignatureWordCount = (TextView) findViewById(R.id.tv_as_signature_wordcount);
		setLeftWordCount();
	}

	private void setLeftWordCount() {
		mSignatureWordCount.setText(String.valueOf(MAX_SIGNATURE_WORD_COUNT
				- getInputWordCount()));
	}

	private long getInputWordCount() {
		return calculateWordCount(mSignaturEditText.getText().toString());
	}

	/**
	 * Calculate Chinese word count. 1 Chinese word = 2 English word.
	 * 
	 * @param c
	 * @return
	 */
	private long calculateWordCount(CharSequence c) {
		double len = 0;
		for (int i = 0; i < c.length(); i++) {
			int tmp = (int) c.charAt(i);
			if (tmp > 0 && tmp < 127) {
				len += 0.5;
			} else {
				len++;
			}
		}
		return Math.round(len);
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
		finishWithAnimation();
	}

	private void saveAndQuit() {
		saveSignature();
		finishWithAnimation();
	}

	private void saveSignature() {

	}

	private TextWatcher mTextWatcher = new TextWatcher() {
		private int mStart;
		private int mEnd;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {
			mStart = mSignaturEditText.getSelectionStart();
			mEnd = mSignaturEditText.getSelectionEnd();

			while (calculateWordCount(s.toString()) > MAX_SIGNATURE_WORD_COUNT) {
				s.delete(mStart - 1, mEnd);
				mStart--;
				mEnd--;
			}
			mSignaturEditText.setSelection(mSignaturEditText.length());
			setLeftWordCount();
		}
	};
}
