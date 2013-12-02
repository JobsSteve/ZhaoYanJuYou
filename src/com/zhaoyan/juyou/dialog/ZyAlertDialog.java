package com.zhaoyan.juyou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoyan.juyou.R;

/**
 * Custom ALert dialog,make 2.3os, can have 4.0 dialog ui
 * but just have one or two button in dialog
 */
public class ZyAlertDialog extends Dialog implements android.view.View.OnClickListener {
	
	private String mTitle;
	private String mMessage;
	
	private TextView mTitleView, mMessageView;
	
	private View mDivideView;
	private Button mNegativeBtn, mPositiveBtn;
	
	private boolean mHasMessage = false;
	private boolean mShowNegativeBtn = false;
	private boolean mShowPositiveBtn = false;
	
	private String mNegativeMessage,mPositiveMessage;
	
	private OnCustomAlertDlgClickListener mNegativeListener;
	private OnCustomAlertDlgClickListener mPositiveListener;
	
	private LinearLayout mContentLayout;
	private LinearLayout mButtonLayout;
	
	private View mContentView;
	
	private Context mContext;
	
	
	public ZyAlertDialog(Context context){
		super(context, R.style.Custom_Dialog);
		mContext = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_alertdialog);
		
		mTitleView = (TextView) findViewById(R.id.tv_dialog_title);
		mTitleView.setText(mTitle);
		
		if (mHasMessage) {
			mMessageView = (TextView) findViewById(R.id.tv_dialog_msg);
			mMessageView.setVisibility(View.VISIBLE);
			mMessageView.setVisibility(View.VISIBLE);
			mMessageView.setText(mMessage);
		}
		
		if (null != mContentView) {
			mContentLayout = (LinearLayout) findViewById(R.id.ll_content);
			mContentLayout.addView(mContentView);
		}
		
		mButtonLayout = (LinearLayout) findViewById(R.id.ll_button);
		
		if (!mShowNegativeBtn && !mShowPositiveBtn) {
			mButtonLayout.setVisibility(View.GONE);
		}else {
			if (mShowNegativeBtn) {
				mNegativeBtn = (Button) findViewById(R.id.btn_negative);
				mNegativeBtn.setText(mNegativeMessage);
				mNegativeBtn.setOnClickListener(this);
				mNegativeBtn.setVisibility(View.VISIBLE);
			}
			
			if (mShowPositiveBtn) {
				mPositiveBtn = (Button) findViewById(R.id.btn_positive);
				mPositiveBtn.setText(mPositiveMessage);
				mPositiveBtn.setOnClickListener(this);
				mPositiveBtn.setVisibility(View.VISIBLE);
			}
			
			if (mShowNegativeBtn && mShowPositiveBtn) {
				mDivideView = findViewById(R.id.divider_one);
				mDivideView.setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitle = title.toString();
	}
	
	@Override
	public void setTitle(int titleId) {
		mTitle = mContext.getString(titleId);
	}
	
	public void setMessage(String message) {
		mHasMessage = true;
		mMessage = message;
	}
	
	public void setMessage(int messageId) {
		String msg = mContext.getString(messageId);
		setMessage(msg);
	}
	
	@Override
	public void setContentView(View view) {
		mContentView = view;
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

		default:
			break;
		}
	}
	
	public interface OnCustomAlertDlgClickListener{
		void onClick(Dialog dialog);
	}
	
	public void setNegativeButton(String text, OnCustomAlertDlgClickListener listener){
		mNegativeMessage = text;
		mShowNegativeBtn = true;
		mNegativeListener = listener;
	}
	
	public void setNegativeButton(int textId, OnCustomAlertDlgClickListener listener){
		String text = mContext.getString(textId);
		setNegativeButton(text, listener);
	}
	
	public void setPositiveButton(String text, OnCustomAlertDlgClickListener listener){
		mPositiveMessage = text;
		mShowPositiveBtn = true;
		mPositiveListener = listener;
	}
	
	public void setPositiveButton(int textId, OnCustomAlertDlgClickListener listener){
		String text = mContext.getString(textId);
		setPositiveButton(text, listener);
	}

}
