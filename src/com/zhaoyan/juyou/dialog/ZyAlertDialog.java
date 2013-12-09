package com.zhaoyan.juyou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.zhaoyan.juyou.R;

/**
 * Custom ALert dialog,make 2.3os, can have 4.0 dialog ui
 */
public class ZyAlertDialog extends Dialog implements android.view.View.OnClickListener {
	
	private String mTitle;
	private String mMessage;
	
	private TextView mTitleTV, mMessageTV;
	
	private View mTitleView;
	
	private View mDivideOneView, mDivideTwoView;
	private Button mNegativeBtn, mPositiveBtn, mNeutralBtn;
	
	private boolean mHasMessage = false;
	private boolean mShowTitle = false;
	private boolean mShowNegativeBtn = false;
	private boolean mShowPositiveBtn = false;
	private boolean mShowNeutralBtn = false;
	
	private String mNegativeMessage,mPositiveMessage,mNeutralMessage;
	
	private OnZyAlertDlgClickListener mNegativeListener;
	private OnZyAlertDlgClickListener mPositiveListener;
	private OnZyAlertDlgClickListener mNeutralListener;
	
	private LinearLayout mContentLayout;
	private LinearLayout mButtonLayout;
	
	private View mCustomeView;
	
	private Context mContext;
	
	private int padding_left = 0;
	private int padding_right = 0;
	private int padding_top = 10;
	private int padding_bottom = 10;
	
	public ZyAlertDialog(Context context){
		super(context, R.style.Custom_Dialog);
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_alertdialog);
		
		if (mShowTitle) {
			mTitleView = findViewById(R.id.rl_dialog_title);
			mTitleView.setVisibility(View.VISIBLE);
			
			mTitleTV = (TextView) findViewById(R.id.tv_dialog_title);
			mTitleTV.setText(mTitle);
		}
		
		if (mHasMessage) {
			mMessageTV = (TextView) findViewById(R.id.tv_dialog_msg);
			mMessageTV.setVisibility(View.VISIBLE);
			mMessageTV.setVisibility(View.VISIBLE);
			mMessageTV.setText(mMessage);
		}
		
		mContentLayout = (LinearLayout) findViewById(R.id.ll_content);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(padding_left, padding_top, padding_right, padding_bottom);
		mContentLayout.setLayoutParams(lp);
		if (null != mCustomeView) {
			mContentLayout.addView(mCustomeView);
		}
		
		mButtonLayout = (LinearLayout) findViewById(R.id.ll_button);
		
		if (!mShowNegativeBtn && !mShowPositiveBtn && !mShowNeutralBtn) {
			mButtonLayout.setVisibility(View.GONE);
		}else {
			if (mShowNegativeBtn) {
				mNegativeBtn = (Button) findViewById(R.id.btn_negative);
				mNegativeBtn.setText(mNegativeMessage);
				mNegativeBtn.setOnClickListener(this);
				mNegativeBtn.setVisibility(View.VISIBLE);
			}
			
			if (mShowNeutralBtn) {
				mNeutralBtn = (Button) findViewById(R.id.btn_neutral);
				mNeutralBtn.setText(mNeutralMessage);
				mNeutralBtn.setOnClickListener(this);
				mNeutralBtn.setVisibility(View.VISIBLE);
			}
			
			if (mShowPositiveBtn) {
				mPositiveBtn = (Button) findViewById(R.id.btn_positive);
				mPositiveBtn.setText(mPositiveMessage);
				mPositiveBtn.setOnClickListener(this);
				mPositiveBtn.setVisibility(View.VISIBLE);
			}
			
			if (mShowNegativeBtn && mShowNeutralBtn) {
				mDivideOneView = findViewById(R.id.divider_one);
				mDivideOneView.setVisibility(View.VISIBLE);
			}
			
			if (mShowNeutralBtn && mShowPositiveBtn) {
				mDivideTwoView = findViewById(R.id.divider_two);
				mDivideTwoView.setVisibility(View.VISIBLE);
			}
			
			if (mShowNegativeBtn && mShowPositiveBtn) {
				mDivideOneView = findViewById(R.id.divider_one);
				mDivideOneView.setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
		mShowTitle = true;
	}
	
	@Override
	public void setTitle(int titleId) {
		String title = mContext.getString(titleId);
		setTitle(title);
	}
	
	public void setMessage(String message) {
		mHasMessage = true;
		mMessage = message;
	}
	
	public void setMessage(int messageId) {
		String msg = mContext.getString(messageId);
		setMessage(msg);
	}
	
	
	public void setCustomView(View view) {
		mCustomeView = view;
	}
	
	public void setCustomViewMargins(int left, int top, int right, int bottom){
		this.padding_left = left;
		this.padding_top = top;
		this.padding_right = right;
		this.padding_bottom = bottom;
	}
	
	@Override
	public void show() {
		super.show();
		WindowManager windowManager = getWindow().getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.width = (int)display.getWidth() - 60;
		getWindow().setAttributes(lp);
	}
	
	

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_negative:
			if (null == mNegativeListener) {
				dismiss();
			}else {
				mNegativeListener.onClick(this);
			}
			break;
		case R.id.btn_positive:
			if (null == mPositiveListener) {
				dismiss();
			}else {
				mPositiveListener.onClick(this);
			}
			break;
		case R.id.btn_neutral:
			if (null == mNeutralListener) {
				dismiss();
			}else {
				mNeutralListener.onClick(this);
			}
			break;

		default:
			break;
		}
	}
	
	public interface OnZyAlertDlgClickListener{
		void onClick(Dialog dialog);
	}
	
	public void setNegativeButton(String text, OnZyAlertDlgClickListener listener){
		mNegativeMessage = text;
		mShowNegativeBtn = true;
		mNegativeListener = listener;
	}
	
	public void setNegativeButton(int textId, OnZyAlertDlgClickListener listener){
		String text = mContext.getString(textId);
		setNegativeButton(text, listener);
	}
	
	public void setPositiveButton(String text, OnZyAlertDlgClickListener listener){
		mPositiveMessage = text;
		mShowPositiveBtn = true;
		mPositiveListener = listener;
	}
	
	public void setPositiveButton(int textId, OnZyAlertDlgClickListener listener){
		String text = mContext.getString(textId);
		setPositiveButton(text, listener);
	}
	
	public void setNeutralButton(String text, OnZyAlertDlgClickListener listener){
		mNeutralMessage = text;
		mShowNeutralBtn = true;
		mNeutralListener = listener;
	}
	
	public void setNeutralButton(int textId, OnZyAlertDlgClickListener listener){
		String text = mContext.getString(textId);
		setPositiveButton(text, listener);
	}

}
