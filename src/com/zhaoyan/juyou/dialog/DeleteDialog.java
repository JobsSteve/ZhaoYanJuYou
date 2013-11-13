package com.zhaoyan.juyou.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhaoyan.juyou.R;

public class DeleteDialog extends Dialog implements android.view.View.OnClickListener {

	private TextView mDialogTitle;
	
	private View mDeletingView;
	private TextView mFileNameView;
	private ProgressBar mProgressBar;
	private ListView mDeleteListView;
	
	private Button mButton1,mButton2, mButton3;
	private View mDividerOne,mDividerTwo;
	private List<Integer> mButtonList = new ArrayList<Integer>();
	private String mBtnText1,mBtnText2,mBtnText3;
	
	private String mFilePath;
	private int mSize;
	private List<String> mDeleteNameList = new ArrayList<String>();
	
	private Context mContext;
	
	private OnDelClickListener mClickListener1;
	private OnDelClickListener mClickListener2;
	private OnDelClickListener mClickListener3;
	
	private int progress;
	private String fileName;
	private static final int MSG_UPDATE_PROGRESS = 0x10;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_PROGRESS:
				mProgressBar.setProgress(progress);
				mFileNameView.setText(fileName);
				break;

			default:
				break;
			}
		};
	};
	
	
	public DeleteDialog(Context context, int theme, String path) {
		super(context, theme);
		mFilePath = path;
		mContext = context;
	}
	
	public DeleteDialog(Context context, int theme, int size){
		super(context, theme);
		mContext = context;
		mSize = size;
	}
	
	public DeleteDialog(Context context, List<String> nameList){
		super(context);
		mContext = context;
		mDeleteNameList = nameList;
	}
	
	public DeleteDialog(Context context, int size){
		super(context);
		mContext = context;
		mSize = size;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_delete);
		
		setTitle(R.string.delete_confirm);
		
		mDeletingView = findViewById(R.id.rl_deleting);
		
		mProgressBar = (ProgressBar) findViewById(R.id.bar_delete);
		mProgressBar.setMax(mDeleteNameList.size());
		
		mFileNameView = (TextView) findViewById(R.id.name_view);
		mDialogTitle = (TextView) findViewById(R.id.title_veiw);
		
		mDeleteListView = (ListView) findViewById(R.id.lv_delete);
		
		mDividerOne = findViewById(R.id.divider_one);
		mDividerTwo = findViewById(R.id.divider_two);
		
		View buttonView = findViewById(R.id.button_layout);
		if (mButtonList.size() > 0) {
			buttonView.setVisibility(View.VISIBLE);
			initButton();
		}
		
		if (mDeleteNameList.size() > 0) {
			mDeleteListView.setVisibility(View.VISIBLE);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, 
					R.layout.dialog_delete_item, mDeleteNameList);
			mDeleteListView.setAdapter(adapter);
			mDeletingView.setVisibility(View.GONE);
		}else {
			mDeletingView.setVisibility(View.VISIBLE);
			mDeleteListView.setVisibility(View.GONE);
		}
	}
	
	public void initButton(){
		for(int whichButton : mButtonList){
			switch (whichButton) {
			case AlertDialog.BUTTON_POSITIVE:
				mButton3 = (Button) findViewById(R.id.button3);
				mButton3.setVisibility(View.VISIBLE);
				mButton3.setOnClickListener(this);
				mButton3.setText(mBtnText3);
				break;
			case AlertDialog.BUTTON_NEUTRAL:
				mButton2 = (Button) findViewById(R.id.button2);
				mButton2.setVisibility(View.VISIBLE);
				mButton2.setOnClickListener(this);
				mButton2.setText(mBtnText2);
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				mButton1 = (Button) findViewById(R.id.button1);
				mButton1.setVisibility(View.VISIBLE);
				mButton1.setOnClickListener(this);
				mButton1.setText(mBtnText1);
				break;
			default:
				break;
			}
		}
		
		if (mButtonList.size() == 2) {
			mDividerOne.setVisibility(View.VISIBLE);
		}else if (mButtonList.size() == 3) {
			mDividerTwo.setVisibility(View.VISIBLE);
		}
	}
	
	public void setProgress(int progress, String fileName){
		this.progress = progress;
		this.fileName = fileName;
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_PROGRESS));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			if (null != mClickListener1) {
				mClickListener1.onClick(v, mFilePath);
			}else {
				dismiss();
			}
			break;
		case R.id.button2:
			if (null != mClickListener2) {
				mClickListener2.onClick(v, mFilePath);
			}else {
				dismiss();
			}
			break;
		case R.id.button3:
			if (null != mClickListener3) {
				mClickListener3.onClick(v, mFilePath);
				setTitle("正在删除");
				mDialogTitle.setVisibility(View.GONE);
				mDeleteListView.setVisibility(View.GONE);
				mDeletingView.setVisibility(View.VISIBLE);
				mButton3.setVisibility(View.GONE);
			}else {
				dismiss();
			}
			break;

		default:
			break;
		}
	}
	
	public void setButton(int whichButton, int textResId, OnDelClickListener listener){
		String text = mContext.getResources().getString(textResId);
		setButton(whichButton, text, listener);
	}
	
	public void setButton(int whichButton, String text, OnDelClickListener listener){
		mButtonList.add(whichButton);
		switch (whichButton) {
		case AlertDialog.BUTTON_POSITIVE:
			mClickListener3 = listener;
			mBtnText3 = text;
			break;
		case AlertDialog.BUTTON_NEUTRAL:
			mClickListener2 = listener;
			mBtnText2 = text;
			break;
		case AlertDialog.BUTTON_NEGATIVE:
			mClickListener1 = listener;
			mBtnText1 = text;
			break;
		default:
			break;
		}
	}
	
	public interface OnDelClickListener{
		public void onClick(View view, String path);
	}

}
