package com.zhaoyan.juyou.common;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.dialog.ZyProgressDialog;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class MediaDeleteHelper {
	private static final String TAG = "MediaDeleteHelper";
	
	private List<String> mCurDelList = new ArrayList<String>();
	
	private ZyProgressDialog progressDialog = null;
	
	private OnDeleteListener mOnDeleteListener = null;
	private Context mContext;
	
	public interface OnDeleteListener{
		public void onFinished();
	}
	
	public void setOnDeleteListener(OnDeleteListener listener){
		mOnDeleteListener = listener;
	}
	
	public void cancelDeleteListener(){
		if (null != mOnDeleteListener) {
			mOnDeleteListener = null;
		}
	}
	
	public MediaDeleteHelper(Context context){
		mContext = context;
	}
	
	/**
	 * record current operation fileinfos
	 * @param files
	 */
	public void copy(List<String> paths) {
        copyFileList(paths);
    }
	
	private void copyFileList(List<String> paths) {
        synchronized(mCurDelList) {
        	mCurDelList.clear();
            for (String path : paths) {
            	mCurDelList.add(path);
            }
        }
    }
	
	public void doDelete(final Uri uri){
		asyncExecute(new Runnable() {
			@Override
			public void run() {
				for(String path: mCurDelList){
					if (mStopDelete) {
						break;
					}
					FileManager.deleteFileInMediaStore(mContext, uri, path);
				}
				
				clear();
			}
		});
	}
	
	private void asyncExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask<Void, Void, Void>() {
        	protected void onPreExecute() {
        		progressDialog = new ZyProgressDialog(mContext);
        		progressDialog.setMessage(R.string.deleting);
        		progressDialog.show();
        	};
        	
        	
			@Override
			protected Void doInBackground(Void... params) {
				synchronized (mCurDelList) {
					_r.run();
				}
				
				if (mOnDeleteListener != null) {
					mOnDeleteListener.onFinished();
				}
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if (null != progressDialog) {
					progressDialog.cancel();
					progressDialog = null;
				}
			};
		}.execute();
    }
	
	public void clear() {
		Log.d(TAG, "clear");
		synchronized (mCurDelList) {
			mCurDelList.clear();
		}
		mStopDelete = false;
	}
	
	private boolean mStopDelete = false;
	public void stopCopy(){
		mStopDelete = true;
		clear();
	}
}
