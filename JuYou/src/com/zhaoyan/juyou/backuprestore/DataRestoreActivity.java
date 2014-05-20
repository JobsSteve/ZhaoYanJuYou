package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.Constants.MessageID;
import com.zhaoyan.juyou.backuprestore.SDCardReceiver.OnSDCardStatusChangedListener;

public class DataRestoreActivity extends ListActivity implements
		OnClickListener, OnItemClickListener, OnItemLongClickListener {
	private static final String TAG = "DataRestoreActivity";
	private TextView mTitleView;
	private TextView mEmptyView;
	
	private View mButtonBarView;
	private Button mDeleteButton,mSelectAllBtn, mCancelBtn;

	private RestoreAdapter mAdapter;
	private List<BackupFilePreview> mBackupFilePreviews;

	private ProgressDialog mLoadingDialog;
	private BackupFileScanner mFileScanner;

	private Handler mHandler;

	OnSDCardStatusChangedListener mSDCardListener;
	private boolean mIsActive = false;
	private boolean mDeleteActionMode = false;

	public Handler getmHandler() {
		return mHandler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data_restore_main);

		View titleView = findViewById(R.id.title);
		View backView = titleView.findViewById(R.id.ll_title);
		mTitleView = (TextView) titleView.findViewById(R.id.tv_custom_title);
		mTitleView.setText(R.string.local_backup_list);
		backView.setOnClickListener(this);

		mButtonBarView = findViewById(R.id.ll_backup);
		mDeleteButton  = (Button) findViewById(R.id.btn_backup);
		mDeleteButton.setText(R.string.menu_delete);
		mSelectAllBtn = (Button) findViewById(R.id.btn_selectall);
		mCancelBtn = (Button) findViewById(R.id.btn_cancel);
		mDeleteButton.setOnClickListener(this);
		mSelectAllBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		init();
	}

	private void init() {
		initHandler();
		initListView();
		initLoadingDialog();
		registerSDCardListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (SDCardUtils.isSdCardAvailable(this)) {
			startScanFiles();
		}
		mIsActive = true;
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(final Message msg) {
				switch (msg.what) {

				case MessageID.SCANNER_FINISH:
					Log.d(TAG, "SCANNER_FINISH");
					addScanResult(msg.obj);

					if (mLoadingDialog != null) {
						mLoadingDialog.cancel();
					}
					break;

				default:
					break;
				}
			}
		};
	}

	private void initListView() {
		mAdapter = new RestoreAdapter(mBackupFilePreviews,
				getApplicationContext());
		setListAdapter(mAdapter);
		getListView().setOnItemClickListener(this);
		getListView().setOnItemLongClickListener(this);

		mEmptyView = new TextView(getApplicationContext());
		mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mEmptyView.setGravity(Gravity.CENTER);
		mEmptyView.setVisibility(View.GONE);
		mEmptyView.setText("No Data");
	}

	private void initLoadingDialog() {
		mLoadingDialog = new ProgressDialog(this);
		mLoadingDialog.setCancelable(false);
		mLoadingDialog.setMessage(getString(R.string.loading_please_wait));
		mLoadingDialog.setIndeterminate(true);
	}
	
	private void updateTitle(){
		StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.local_backup_list));
        int totalNum = mAdapter.getCount();
        int selectNum = mAdapter.getCheckedCount();
        sb.append("(" + selectNum + "/" + totalNum + ")");
        mTitleView.setText(sb.toString());
        
        if (selectNum == 0) {
			mDeleteButton.setEnabled(false);
			mSelectAllBtn.setText(R.string.select_all);
		} else if (selectNum == totalNum) {
			mDeleteButton.setEnabled(true);
			mSelectAllBtn.setText(R.string.unselect_all);
		} else {
			mDeleteButton.setEnabled(true);
			mSelectAllBtn.setText(R.string.select_all);
		}
	}

	private void startScanFiles() {
		mLoadingDialog.show();

		if (mFileScanner == null) {
			mFileScanner = new BackupFileScanner(getApplicationContext(),
					mHandler);
		} else {
			mFileScanner.setHandler(mHandler);
		}
		Log.i(TAG, "RestoreTabFragment: startScanFiles");
		mFileScanner.startScan();
	}

	@SuppressWarnings("unchecked")
	private void addScanResult(Object obj) {

		if (obj == null) {
			return;
		}

		HashMap<String, List<BackupFilePreview>> map = (HashMap<String, List<BackupFilePreview>>) obj;
		Log.d(TAG,
				"addScanResult.map:"
						+ map
						+ ",map:"
						+ map.containsKey(Constants.SCAN_RESULT_KEY_PERSONAL_DATA));
		boolean noRecord = true;
		// personal data
		List<BackupFilePreview> items = map
				.get(Constants.SCAN_RESULT_KEY_PERSONAL_DATA);
		if (items != null && !items.isEmpty()) {
			noRecord = false;
			mBackupFilePreviews = items;
			Log.d(TAG, "addScanResult.size=" + mBackupFilePreviews.size());
			mAdapter.changeData(mBackupFilePreviews);
			return;
		}
		Log.d(TAG, "no record");
	}

	private void registerSDCardListener() {
		mSDCardListener = new OnSDCardStatusChangedListener() {
			@Override
			public void onSDCardStatusChanged(final boolean mount) {
				if (mIsActive) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							if (mIsActive) {
								startScanFiles();
								int resId = mount ? R.string.sdcard_swap_insert
										: R.string.sdcard_swap_remove;
								// Toast.makeText(getActivity(), resId,
								// Toast.LENGTH_SHORT).show();
							}
							if (!mount) {
							}
						}
					});
				}
			}
		};

		SDCardReceiver receiver = SDCardReceiver.getInstance();
		receiver.registerOnSDCardChangedListener(mSDCardListener);
	}

	private void unRegisteSDCardListener() {
		if (mSDCardListener != null) {
			SDCardReceiver receiver = SDCardReceiver.getInstance();
			receiver.unRegisterOnSDCardChangedListener(mSDCardListener);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mFileScanner != null) {
			mFileScanner.quitScan();
		}
		mIsActive = false;
	}

	protected void onDestroy() {
		super.onDestroy();
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
			mLoadingDialog = null;
		}
		if (mFileScanner != null) {
			mFileScanner.setHandler(null);
		}

		unRegisteSDCardListener();
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_title:
			finishWithAnimation();
			break;
		case R.id.btn_backup:
			//delete
			List<Integer> checkedPosList =  mAdapter.getCheckedPosList();
			startDeleteItems(checkedPosList);
			break;
		case R.id.btn_selectall:
			if (mAdapter.isAllChecked()) {
				mAdapter.checkedAll(false);
			} else {
				mAdapter.checkedAll(true);
			}
			mAdapter.notifyDataSetChanged();
			
			updateTitle();
			break;
		case R.id.btn_cancel:
			mDeleteActionMode = false;
			mAdapter.checkedAll(false);
			mAdapter.notifyDataSetChanged();
			
			mTitleView.setText(R.string.local_backup_list);
			
			mButtonBarView.setVisibility(View.GONE);
			mButtonBarView.clearAnimation();
			mButtonBarView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_down_out));
			break;

		default:
			break;
		}
	}
	
	private void startDeleteItems(List<Integer> posList){
		HashSet<File> files = new HashSet<File>();
		for(int pos : posList){
			files.add(mBackupFilePreviews.get(pos).getFile());
		}
		
		new DeleteCheckedItemsTask().execute(files);
	}
	
	private class DeleteCheckedItemsTask extends AsyncTask<HashSet<File>, String, Long>{
		ProgressDialog deleteDialog = null;
		
		@Override
		protected void onPreExecute() {
			deleteDialog = new ProgressDialog(DataRestoreActivity.this);
			deleteDialog.setCancelable(false);
			deleteDialog.setMessage(getString(R.string.delete_please_wait));
			deleteDialog.setIndeterminate(true);
			deleteDialog.show();
			super.onPreExecute();
		}
		
		@Override
		protected Long doInBackground(HashSet<File>... params) {
			HashSet<File> deleteFiles = params[0];
			for (File file : deleteFiles) {
				FileUtils.deleteFileOrFolder(file);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Long result) {
			startScanFiles();
			if (deleteDialog != null) {
				deleteDialog.cancel();
				deleteDialog = null;
			}
			super.onPostExecute(result);
		}
		
	}

	public class RestoreAdapter extends BaseAdapter {
		private static final String TAG = "RestoreAdapter";

		private LayoutInflater mInflater = null;
		private List<BackupFilePreview> mDataList = new ArrayList<BackupFilePreview>();

		private SparseBooleanArray mCheckedArray = null;
		
		public RestoreAdapter(List<BackupFilePreview> list, Context context) {
			mInflater = LayoutInflater.from(context);
			mDataList = list;
			
			mCheckedArray = new SparseBooleanArray();
		}

		public void changeData(List<BackupFilePreview> list) {
			mDataList = list;
			notifyDataSetChanged();
		}
		
		public void setChecked(int postion, boolean checked){
			mCheckedArray.put(postion, checked);
		}
		
		public void setChecked(int position){
			mCheckedArray.put(position, !isChecked(position));
		}
		
		public boolean isChecked(int position){
			return mCheckedArray.get(position);
		}
		
		public void checkedAll(boolean isChecked){
			int count = this.getCount();
			for (int i = 0; i < count; i++) {
				setChecked(i, isChecked);
			}
		}
		
		public boolean isAllChecked(){
			int count = this.getCount();
			int checkedCount = getCheckedCount();
			return count == checkedCount;
		}
		
		public int getCheckedCount(){
			int count = 0;
			for (int i = 0; i < mCheckedArray.size(); i++) {
				if (mCheckedArray.valueAt(i)) {
					count ++;
				}
			}
			return count;
		}
		
		public List<Integer> getCheckedPosList() {
			List<Integer> list = new ArrayList<Integer>();
			for (int i = 0; i < mCheckedArray.size(); i++) {
				if (mCheckedArray.valueAt(i)) {
					list.add(i);
				}
			}
			return list;
		}

		@Override
		public int getCount() {
			if (mDataList == null) {
				Log.d(TAG, "getCOunt.mDataList:" + mDataList);
				return 0;
			}
			return mDataList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = mInflater.inflate(R.layout.br_data_item2, null);
			TextView titleView = (TextView) view
					.findViewById(R.id.tv_item_title);
			TextView infoView = (TextView) view.findViewById(R.id.tv_item_info);
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_item_check);
			BackupFilePreview item = mDataList.get(position);

			titleView.setText(item.getFileName());
			infoView.setText(ZYUtils.getFormatSize(item.getFileSize()));
			
			if (mDeleteActionMode) {
				checkBox.setVisibility(View.VISIBLE);
				checkBox.setChecked(isChecked(position));
			} else {
				checkBox.setVisibility(View.GONE);
			}
			return view;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mDeleteActionMode) {
			mAdapter.setChecked(position);
			mAdapter.notifyDataSetChanged();
			
			updateTitle();
		} else {
			String path = mBackupFilePreviews.get(position).getFile().getAbsolutePath();
			Intent intent = new Intent();
			intent.setClass(DataRestoreActivity.this, DataRestoreItemActivity.class);
			intent.putExtra(Constants.FILENAME, path);
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mDeleteActionMode = true;
		mAdapter.setChecked(position, true);
		mAdapter.notifyDataSetChanged();
		
		updateTitle();
		
		mButtonBarView.setVisibility(View.VISIBLE);
		mButtonBarView.clearAnimation();
		mButtonBarView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_up_in));
		return true;
	}
	
	private void finishWithAnimation() {
		finish();
		overridePendingTransition(0, R.anim.activity_right_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(0, R.anim.activity_right_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
