package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.Constants.DialogID;
import com.zhaoyan.juyou.backuprestore.Constants.State;
import com.zhaoyan.juyou.backuprestore.RestoreService.RestoreProgress;
import com.zhaoyan.juyou.backuprestore.ResultDialog.ResultEntity;

public class DataRestoreItemActivity extends AbstractRestoreActivity {
	private String TAG = "DataRestoreItemActivity";
	private PersonalDataRestoreAdapter mRestoreAdapter;
	private File mFile;
	private boolean mIsDataInitialed;
	private boolean mIsStoped = false;
	private boolean mNeedUpdateResult = false;
	BackupFilePreview mPreview = null;
	private PersonalDataRestoreStatusListener mRestoreStoreStatusListener;
	private boolean mIsCheckedRestoreStatus = false;
	private String mRestoreFolderPath;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent == null || intent.getStringExtra(Constants.FILENAME) == null) {
			finish();
			return;
		}
		mFile = new File(intent.getStringExtra(Constants.FILENAME));
		if (!mFile.exists()) {
			Toast.makeText(this, "file don't exist", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		showButtonBar(true);
		Log.i(TAG, "onCreate");
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		if (!mFile.exists()) {
			Toast.makeText(this, R.string.file_no_exist_and_update,
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// update to avoid files deleted
		if (mFile.exists()) {
			new FilePreviewTask().execute();
			updateResultIfneed();
		} else {
			Toast.makeText(this, R.string.file_no_exist_and_update,
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mIsStoped = false;
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.e(TAG, "=======================");
		mIsStoped = true;
		if (mRestoreService != null) {
			mRestoreService.cancelRestore();
		} else {
			Log.e(TAG, "NULLLLLLLLLLLLLLLLLLLLLLLLLLLL");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler = null;
	}

	private void init() {
		initActionBar();
		updateTitle();
	}

	public void updateTitle() {
		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.backup_personal_data));
		int totalCount = getCount();
		int selectCount = getCheckedCount();
		sb.append("(" + selectCount + "/" + totalCount + ")");
		Log.d(TAG, "updateTitle:" + sb.toString());
		setTitle(sb.toString());
	}

	private void initActionBar() {
		StringBuilder builder = new StringBuilder(
				getString(R.string.backup_data));
		builder.append(" ");
		long size = FileUtils.computeAllFileSizeInFolder(mFile);
		builder.append(FileUtils.getDisplaySize(size, this));
	}

	private void showUpdatingTitle() {
	}

	@Override
	public void onCheckedCountChanged() {
		super.onCheckedCountChanged();
		Log.d(TAG, "onCheckedCountChanged");
		updateTitle();
	}

	@Override
	protected void notifyListItemCheckedChanged() {
		super.notifyListItemCheckedChanged();
		updateTitle();
	}

	private ArrayList<Integer> getSelectedItemList() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		int count = getCount();
		for (int position = 0; position < count; position++) {
			PersonalItemData item = (PersonalItemData) getItemByPosition(position);
			if (isItemCheckedByPosition(position)) {
				list.add(item.getType());
			}
		}
		return list;
	}

	@Override
	protected BaseAdapter initAdapter() {
		ArrayList<PersonalItemData> list = new ArrayList<PersonalItemData>();
		mRestoreAdapter = new PersonalDataRestoreAdapter(this, list,
				R.layout.br_data_item);
		return mRestoreAdapter;
	}

	@Override
	protected Dialog onCreateDialog(final int id, final Bundle args) {
		Dialog dialog = null;

		switch (id) {
		case DialogID.DLG_RESULT:
			dialog = ResultDialog.createResultDlg(this,
					R.string.restore_result, args, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (mRestoreService != null) {
								mRestoreService.reset();
							}
							stopService();
							NotifyManager.getInstance(
									DataRestoreItemActivity.this)
									.clearNotification();
						}
					});
			break;

		default:
			dialog = super.onCreateDialog(id, args);
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(final int id, final Dialog dialog,
			final Bundle args) {
		switch (id) {
		case DialogID.DLG_RESULT:
			AlertDialog dlg = (AlertDialog) dialog;
			ListView view = (ListView) dlg.getListView();
			if (view != null) {
				ListAdapter adapter = ResultDialog.createResultAdapter(this,
						args);
				view.setAdapter(adapter);
			}
			break;
		default:
			break;
		}
	}

	private void showRestoreResult(ArrayList<ResultEntity> list) {
		dismissProgressDialog();
		Bundle args = new Bundle();
		args.putParcelableArrayList("result", list);
		showDialog(DialogID.DLG_RESULT, args);
	}

	private void updateResultIfneed() {

		if (mRestoreService != null && mNeedUpdateResult) {
			int state = mRestoreService.getState();
			if (state == State.FINISH) {
				Log.v(TAG,
						"updateResult because of finish when onStop");
				showRestoreResult(mRestoreService.getRestoreResult());
			}
		}
		mNeedUpdateResult = false;
	}

	private String getProgressDlgMessage(int type) {
		StringBuilder builder = new StringBuilder(getString(R.string.restoring));

		builder.append("(")
				.append(ModuleType.getModuleStringFromType(this, type))
				.append(")");
		return builder.toString();
	}

	@Override
	public void startRestore() {
		if (!isCanStartRestore()) {
			return;
		}
		startService();
		Log.v(TAG, "startRestore");
		ArrayList<Integer> list = getSelectedItemList();
		mRestoreService.setRestoreModelList(list);
		mRestoreFolderPath = mFile.getAbsolutePath();
		boolean ret = mRestoreService.startRestore(mRestoreFolderPath);
		if (ret) {
			showProgressDialog();
			String msg = getProgressDlgMessage(list.get(0));
			setProgressDialogMessage(msg);
			setProgressDialogProgress(0);
			setProgress(0);
			if (mPreview != null) {
				int count = mPreview.getItemCount(list.get(0));
				setProgressDialogMax(count);
			}
		} else {
			stopService();
		}
	}

	@Override
	protected void afterServiceConnected() {
		Log.d(TAG,  "afterServiceConnected, to checkRestorestate");
		checkRestoreState();
	}

	private void checkRestoreState() {
		if (mIsCheckedRestoreStatus) {
			Log.d(TAG, 
					"can not checkRestoreState, as it has been checked");
			return;
		}
		if (!mIsDataInitialed) {
			Log.d(TAG, 
					"can not checkRestoreState, wait data to initialed");
			return;
		}
		Log.d(TAG,  "all ready. to checkRestoreState");
		mIsCheckedRestoreStatus = true;
		if (mRestoreService != null) {
			int state = mRestoreService.getState();
			Log.d(TAG,  "checkRestoreState: state = " + state);
			if (state == State.RUNNING || state == State.PAUSE) {

				RestoreProgress p = mRestoreService.getCurRestoreProgress();
				Log.d(TAG,  "checkRestoreState: Max = " + p.mMax
						+ " curprogress = " + p.mCurNum);
				String msg = getProgressDlgMessage(p.mType);

				if (state == State.RUNNING) {
					showProgressDialog();
				}
				setProgressDialogMax(p.mMax);
				setProgressDialogProgress(p.mCurNum);
				setProgressDialogMessage(msg);
			} else if (state == State.FINISH) {
				if (mIsStoped) {
					mNeedUpdateResult = true;
				} else {
					showRestoreResult(mRestoreService.getRestoreResult());
				}
			} else if (state == State.ERR_HAPPEN) {
				errChecked();
			}
		}
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.e(TAG, "onConfigurationChanged");
	}

	private class PersonalDataRestoreAdapter extends BaseAdapter {

		private ArrayList<PersonalItemData> mList;
		private int mLayoutId;
		private LayoutInflater mInflater;

		public PersonalDataRestoreAdapter(Context context,
				ArrayList<PersonalItemData> list, int resource) {
			mList = list;
			mLayoutId = resource;
			mInflater = LayoutInflater.from(context);
		}

		public void changeData(ArrayList<PersonalItemData> list) {
			mList = list;
		}

		public int getCount() {
			if (mList == null) {
				return 0;
			}
			return mList.size();
		}

		public Object getItem(int position) {
			if (mList == null) {
				return null;
			}
			return mList.get(position);

		}

		public long getItemId(int position) {
			PersonalItemData item = mList.get(position);
			return item.getType();

		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = mInflater.inflate(mLayoutId, parent, false);
			}

			PersonalItemData item = mList.get(position);
			ImageView image = (ImageView) view.findViewById(R.id.iv_item_icon);
			TextView titleView = (TextView) view.findViewById(R.id.tv_item_title);
			TextView infoView = (TextView) view.findViewById(R.id.tv_item_info);
			CheckBox chxbox = (CheckBox) view.findViewById(R.id.cb_item_check);
			image.setBackgroundResource(item.getIconId());
			titleView.setText(item.getTextId());
			infoView.setText(item.getCount() + "");
			boolean enabled = item.isEnable();
			titleView.setEnabled(enabled);
			chxbox.setEnabled(enabled);
			view.setClickable(!enabled);
			if (enabled) {
				chxbox.setChecked(isItemCheckedByPosition(position));
			} else {
				chxbox.setChecked(false);
				view.setEnabled(false);
			}
			return view;
		}
	}

	private class FilePreviewTask extends AsyncTask<Void, Void, Long> {
		private int mModule = 0;

		@Override
		protected void onPostExecute(Long arg0) {
			super.onPostExecute(arg0);
			int types[] = new int[] { ModuleType.TYPE_CONTACT,
					ModuleType.TYPE_SMS, ModuleType.TYPE_PICTURE, ModuleType.TYPE_MUSIC };

			ArrayList<PersonalItemData> list = new ArrayList<PersonalItemData>();
			int count = 0;
			for (int type : types) {
				if ((mModule & type) != 0) {
					PersonalItemData item = new PersonalItemData(type, true);
					count = mPreview.getItemCount(type);
					item.setCount(count);
					list.add(item);
				}
			}
			mRestoreAdapter.changeData(list);
			syncUnCheckedItems();
			setButtonsEnable(true);
			notifyListItemCheckedChanged();
			mIsDataInitialed = true;
			// setProgressBarIndeterminateVisibility(false);
			showLoadingContent(false);
			initActionBar();
			if (mRestoreStoreStatusListener == null) {
				mRestoreStoreStatusListener = new PersonalDataRestoreStatusListener();
			}
			setOnRestoreStatusListener(mRestoreStoreStatusListener);
			Log.d(TAG,  "mIsDataInitialed is ok");
			checkRestoreState();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setButtonsEnable(false);
			showLoadingContent(true);
			initActionBar();
			// showUpdatingTitle();
		}

		@Override
		protected Long doInBackground(Void... arg0) {
			mPreview = new BackupFilePreview(mFile);
			if (mPreview != null) {
				mModule = mPreview
						.getBackupModules(DataRestoreItemActivity.this);
			}
			return null;
		}
	}

	private class PersonalDataRestoreStatusListener extends
			NormalRestoreStatusListener {

		public void onComposerChanged(final int type, final int max) {
			Log.i(TAG, "RestoreDetailActivity: onComposerChanged type = "
					+ type);

			if (mHandler != null) {
				mHandler.post(new Runnable() {

					public void run() {
						String msg = getProgressDlgMessage(type);
						setProgressDialogMessage(msg);
						setProgressDialogMax(max);
						setProgressDialogProgress(0);
					}
				});
			}
		}

		public void onRestoreEnd(boolean bSuccess,
				ArrayList<ResultEntity> resultRecord) {
			final ArrayList<ResultEntity> iResultRecord = resultRecord;
			Log.d(TAG,  "onRestoreEnd");
			boolean hasSuccess = false;
			for (ResultEntity result : resultRecord) {
				if (ResultEntity.SUCCESS == result.getResult()) {
					hasSuccess = true;
					break;
				}
			}

			if (hasSuccess) {
				String recrodXmlFile = mRestoreFolderPath + File.separator
						+ Constants.RECORD_XML;
				String content = Utils.readFromFile(recrodXmlFile);
				ArrayList<RecordXmlInfo> recordList = new ArrayList<RecordXmlInfo>();
				if (content != null) {
					recordList = RecordXmlParser.parse(content.toString());
				}
				RecordXmlComposer xmlCompopser = new RecordXmlComposer();
				xmlCompopser.startCompose();

				RecordXmlInfo restoreInfo = new RecordXmlInfo();
				restoreInfo.setRestore(true);
				restoreInfo.setDevice(Utils.getPhoneSearialNumber());
				restoreInfo.setTime(String.valueOf(System.currentTimeMillis()));

				boolean bAdded = false;
				for (RecordXmlInfo record : recordList) {
					if (record.getDevice().equals(restoreInfo.getDevice())) {
						xmlCompopser.addOneRecord(restoreInfo);
						bAdded = true;
					} else {
						xmlCompopser.addOneRecord(record);
					}
				}

				if (!bAdded) {
					xmlCompopser.addOneRecord(restoreInfo);
				}
				xmlCompopser.endCompose();
				Utils.writeToFile(xmlCompopser.getXmlInfo(), recrodXmlFile);
			}

			if (mHandler != null) {
				mHandler.post(new Runnable() {
					public void run() {

						Log.d(TAG,  " Restore show Result Dialog");
						if (mIsStoped) {
							mNeedUpdateResult = true;
						} else {
							showRestoreResult(iResultRecord);
						}
					}
				});
			}
		}
	}
}
