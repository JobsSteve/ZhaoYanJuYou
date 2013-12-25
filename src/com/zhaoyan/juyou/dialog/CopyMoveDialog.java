package com.zhaoyan.juyou.dialog;


import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.HistoryManager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * use for view copy or move ui dialog
 * @author Yuri
 */
public class CopyMoveDialog extends ZyAlertDialog{
	private static final String TAG = "CopyMoveDialog";
	
	private TextView mMessageView;
	private TextView mFileCountView;
	private TextView mFileCountPercentView;
	private TextView mFileSizeView;
	private TextView mFileSizePercentView;
	
	private ProgressBar mFileCountBar;
	private ProgressBar mFileSizeBar;
	
	/**
	 * the path that copy or move to directory
	 */
	private String mDespath;
	/**
	 * current copy or move file count
	 */
	private int mTotalCount;
	private long mSingleFileTotalSize;
	private long mCopySize;
	
	float mCountPrev = 0;
	private float mSizePrev = 0;
	
	private String mTotalSizeStr;
	private String mProgressSizeStr;

	private static final int MSG_UPDATE_PROGRESS_COUNT = 0;
	private static final int MSG_UPDATE_PROGRESS_SIZE = 1;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_PROGRESS_COUNT:
				int count = msg.arg1;
				String fileName = msg.obj.toString();
				mMessageView.setText(fileName + "->" + mDespath);
				mFileCountView.setText(count + "/" + mTotalCount);
				float per = (float) count / mTotalCount;
				if (mCountPrev != per * 100) {
					mFileCountBar.setProgress((int)(per * 100));
					mFileCountPercentView.setText(HistoryManager.nf.format(per));
					mCountPrev = per * 100;
				}
				break;
			case MSG_UPDATE_PROGRESS_SIZE:
				float percent = (float)mCopySize / mSingleFileTotalSize;
				if (mSizePrev != percent * 100) {
					mFileSizeBar.setProgress((int)(percent * 100));
//					mFileSizePercentView.setText(HistoryManager.nf.format(percent));
//					mFileSizeView.setText(mProgressSizeStr + "/" + mTotalSizeStr);
					mSizePrev = percent * 100;
				}
				break;

			default:
				break;
			}
		};
	};
	
	public CopyMoveDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = getLayoutInflater().inflate(R.layout.dialog_copy, null);
		mMessageView = (TextView) view.findViewById(R.id.tv_copy_msg);
		
		mFileCountView = (TextView) view.findViewById(R.id.tv_file_count);
		mFileCountPercentView = (TextView) view.findViewById(R.id.tv_file_count_percent);
		mFileCountBar = (ProgressBar) view.findViewById(R.id.bar_copy_one);
		mFileCountBar.setMax(100);
		
		mFileSizeView = (TextView) view.findViewById(R.id.tv_file_size);
		mFileSizePercentView = (TextView) view.findViewById(R.id.tv_file_size_percent);
		mFileSizeBar = (ProgressBar) view.findViewById(R.id.bar_copy_two);
		mFileSizeBar.setMax(100);
		
		setCustomView(view);
		super.onCreate(savedInstanceState);
	}
	
	public void setDesPath(String despath){
		this.mDespath = despath;
	}
	
	public void setTotalCount(int totalCount){
		this.mTotalCount = totalCount;
	}
	
	public void setSignleFileTotalSize(long size){
		this.mSingleFileTotalSize = size;
	}
	
	public void updateMessage(String fileName){
		
	}
	
	public void updateCountProgress(String fileName, int count, long filesize){
		if (filesize == -1) {
			//is move operation
			mFileSizeBar.setVisibility(View.GONE);
		}else {
			this.mSingleFileTotalSize = filesize;
			mTotalSizeStr = ZYUtils.getFormatSize(filesize);
		}
		Message message = mHandler.obtainMessage();
		message.arg1 = count;
		message.obj = fileName;
		message.what = MSG_UPDATE_PROGRESS_COUNT;
		message.sendToTarget();
	}
	
	public void updateSingleFileProgress(long copysize){
		mCopySize = copysize;
		mProgressSizeStr = ZYUtils.getFormatSize(copysize);
		Message message = mHandler.obtainMessage();
		message.what = MSG_UPDATE_PROGRESS_SIZE;
		message.sendToTarget();
	}

}
