package com.zhaoyan.juyou.backuprestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.Constants.MessageID;
import com.zhaoyan.juyou.backuprestore.SDCardReceiver.OnSDCardStatusChangedListener;

public class DataRestoreActivity extends ListActivity implements
		OnClickListener, OnItemClickListener {
	private static final String TAG = "DataRestoreActivity";
	private TextView mTitleView;
	private TextView mEmptyView;

	private RestoreAdapter mAdapter;
	private List<BackupFilePreview> mBackupFilePreviews;

	private ProgressDialog mLoadingDialog;
	private BackupFileScanner mFileScanner;

	private Handler mHandler;

	OnSDCardStatusChangedListener mSDCardListener;
	private boolean mIsActive = false;

	public Handler getmHandler() {
		return mHandler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.br_data_main);

		View titleView = findViewById(R.id.title);
		View backView = titleView.findViewById(R.id.ll_title);
		mTitleView = (TextView) titleView.findViewById(R.id.tv_custom_title);
		mTitleView.setText("本地数据恢复");
		backView.setOnClickListener(this);

		findViewById(R.id.btn_backup).setVisibility(View.GONE);

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
			finish();
			break;

		default:
			break;
		}
	}

	public class RestoreAdapter extends BaseAdapter {
		private static final String TAG = "RestoreAdapter";

		private LayoutInflater mInflater = null;
		private List<BackupFilePreview> mDataList = new ArrayList<BackupFilePreview>();

		public RestoreAdapter(List<BackupFilePreview> list, Context context) {
			mInflater = LayoutInflater.from(context);
			mDataList = list;
		}

		public void changeData(List<BackupFilePreview> list) {
			mDataList = list;
			notifyDataSetChanged();
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

			BackupFilePreview item = mDataList.get(position);

			titleView.setText(item.getFileName());
			infoView.setText(ZYUtils.getFormatSize(item.getFileSize()));
			return view;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		String path = mBackupFilePreviews.get(position).getFile().getAbsolutePath();
		Intent intent = new Intent();
		intent.setClass(DataRestoreActivity.this, DataRestoreItemActivity.class);
		intent.putExtra(Constants.FILENAME, path);
		startActivity(intent);
	}

}
