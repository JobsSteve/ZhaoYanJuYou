package com.zhaoyan.juyou.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;

public class InfoDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView mOkButton;
	
	private TextView mTitleView;
	private TextView mTypeView,mLoacationView,mSizeView,mIncludeView,mDateView;
	private View mDividerView;
	
	private long mTotalSize;
	private int mFileNum;
	private int mFolderNum;
	
	private String mFileName;
	private String mFilePath;
	private long mModified;
	
	private Context mContext;
	
	public static final int SINGLE_FILE = 0x01;
	public static final int SINGLE_FOLDER = 0x02;
	public static final int MULTI = 0x03;
	private int type;
	
	private static final int MSG_UPDATEUI_MULTI = 0x10;
	private static final int MSG_UPDATEUI_SINGLE = 0x11;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATEUI_MULTI:
				String sizeInfo = ZYUtils.getFormatSize(mTotalSize);
				mSizeView.setText(mContext.getResources().getString(R.string.size, sizeInfo));
				int folderNum = mFolderNum;
				if (0 != mFolderNum) {
					//remove self folder
					folderNum = mFolderNum - 1;
				}
				mIncludeView.setText(mContext.getResources().getString(R.string.include_files, mFileNum, folderNum));
				break;
			case MSG_UPDATEUI_SINGLE:
				String date = ZYUtils.getFormatDate(mModified);
				setTitle(mContext.getResources().getString(R.string.info2, mFileName));
				mLoacationView.setText(mContext.getResources().getString(R.string.location, mFilePath));
				mDateView.setText(mContext.getResources().getString(R.string.modif_date, date));
				break;
			default:
				break;
			}
		};
	};
	
	public InfoDialog(Context context, int type) {
		super(context, R.style.Custom_Dialog);
		mContext = context;
		this.type = type;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_info);
		
		mTitleView = (TextView) findViewById(R.id.tv_info_title);
		mTypeView = (TextView) findViewById(R.id.tv_info_type);
		mLoacationView = (TextView) findViewById(R.id.tv_info_location);
		mSizeView = (TextView) findViewById(R.id.tv_info_size);
		mIncludeView = (TextView) findViewById(R.id.tv_info_include);
		mDateView = (TextView) findViewById(R.id.tv_info_date);
		mDividerView = findViewById(R.id.info_divider);
		
		if (MULTI == type) {
			mTypeView.setVisibility(View.GONE);
			mLoacationView.setVisibility(View.GONE);
			mDateView.setVisibility(View.GONE);
			mDividerView.setVisibility(View.GONE);
		}else if (SINGLE_FILE == type) {
			mIncludeView.setVisibility(View.GONE);
			mTypeView.setText(R.string.type_file);
		}else {
			mTypeView.setText(R.string.type_folder);
		}
		
		setTitle(R.string.info1);
		mOkButton = (TextView) findViewById(R.id.btn_info_ok);
		mOkButton.setOnClickListener(this);
		
		setCanceledOnTouchOutside(true);
	}
	
	
	public void updateUI(long size, int fileNum, int folderNum){
		this.mTotalSize = size;
		this.mFileNum = fileNum;
		this.mFolderNum = folderNum;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATEUI_MULTI));
	}
	
	public void updateUI(String fileName, String filePath, long date){
		mFileName = fileName;
		mFilePath = filePath;
		mModified = date;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATEUI_SINGLE));
	}
	
	@Override
	public void show() {
		super.show();
		WindowManager windowManager = getWindow().getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.width = (int)display.getWidth() - 100;
		getWindow().setAttributes(lp);
	}
	
	@Override
	public void setTitle(CharSequence title) {
		mTitleView.setText(title);
	}
	
	@Override
	public void setTitle(int titleId) {
		mTitleView.setText(titleId);
	}
	
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_info_ok:
			cancel();
			break;
		default:
			break;
		}
	}

}
