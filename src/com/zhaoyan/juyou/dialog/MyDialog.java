package com.zhaoyan.juyou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhaoyan.juyou.R;

public class MyDialog extends Dialog implements android.view.View.OnClickListener {
	
	private TextView mNameView;
	private TextView mNumView;
	private ProgressBar mProgressBar;
	
	private Button mCancelButton, mHideButton;
	private boolean mShowHideBtn = false;
	
	private OnHideListener mHideListener;
	
	private int progress;
	private String mName;
	private int max;
	private static final int MSG_UPDATE_PROGRESS = 0x10;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_PROGRESS:
				mProgressBar.setProgress(progress);
				mNameView.setText(mName);
				mNumView.setText(progress + "/" + max);
				break;

			default:
				break;
			}
		};
	};
	
	public MyDialog(Context context, int size){
		super(context, R.style.Custom_Dialog);
		max = size;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_dialog);
		
		mProgressBar = (ProgressBar) findViewById(R.id.bar_move);
		mProgressBar.setMax(max);
		
		mNameView = (TextView) findViewById(R.id.name_view);
		mNumView = (TextView) findViewById(R.id.num_view);
		
		mCancelButton = (Button) findViewById(R.id.btn_cancel);
		mCancelButton.setText(android.R.string.cancel);
		mCancelButton.setOnClickListener(this);
		
		if (mShowHideBtn) {
			mHideButton = (Button) findViewById(R.id.btn_hide);
			mHideButton.setVisibility(View.VISIBLE);
			mHideButton.setOnClickListener(this);
			
			View dividerView = findViewById(R.id.divider_one);
			dividerView.setVisibility(View.VISIBLE);
		}
		
		setCancelable(false);
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
	
	public void updateName(String name){
		this.mName = name;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS));
	}
	
	public void updateProgress(int progress){
		this.progress = progress;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS));
	}
	
	public void updateUI(int progress, String fileName){
		this.progress = progress;
		this.mName = fileName;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS));
	}
	
	public int getMax(){
		return max;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_cancel:
			cancel();
			break;
		case R.id.btn_hide:
			mHideListener.onHide();
			dismiss();
			break;

		default:
			break;
		}
	}
	
	public interface OnHideListener{
		void onHide();
	}
	
	public void setOnHideListener(OnHideListener listener){
		mShowHideBtn = true;
		mHideListener = listener;
	}

}
