package com.zhaoyan.juyou.backuprestore;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.BackupEngine.BackupResultType;
import com.zhaoyan.juyou.backuprestore.BackupService.OnBackupStatusListener;
import com.zhaoyan.juyou.backuprestore.Constants.ContactType;
import com.zhaoyan.juyou.backuprestore.Constants.DialogID;
import com.zhaoyan.juyou.backuprestore.ResultDialog.ResultEntity;

public class DataBackupActivity extends AbstractBackupActivity {
	private static final String TAG = "DataBackupActivity";
	private DataBackupAdapter mAdapter;

	private ArrayList<AlertDialog> dialogs = new ArrayList<AlertDialog>();

	private InitPersonalDataTask mInitPersonalDataTask = null;
	private ArrayList<PersonalItemData> mBackupList = new ArrayList<PersonalItemData>();

	private OnBackupStatusListener mBackupListener;
	private String mBackupFolderPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showButtonBar(true);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mInitPersonalDataTask = new InitPersonalDataTask();
		mInitPersonalDataTask.execute();
	}
	
	@Override
	public void onCheckedCountChanged() {
		super.onCheckedCountChanged();
		updateTitle();
	}

	private void updateData(ArrayList<PersonalItemData> data) {
		mBackupList = data;
		mAdapter.changeData(mBackupList);
		syncUnCheckedItems();
		mAdapter.notifyDataSetChanged();
		updateTitle();
		updateButtonState();
		checkBackupState();
	}
	
	public void updateTitle() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.backup_personal_data));
        int totalNum = getCount();
        int checkedNum = getCheckedCount();
        sb.append("(" + checkedNum + "/" + totalNum + ")");
        setTitle("");
        setTitle(sb.toString());
    }

	List<String> messageEnable = new ArrayList<String>();

	private class InitPersonalDataTask extends AsyncTask<Void, Void, Long> {
		private static final String TASK_TAG = "InitPersonalDataTask";
		ArrayList<PersonalItemData> mBackupDataList;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.d(TAG, TASK_TAG + "---onPreExecute");
			// show progress and set title as "updating"
			// setProgressBarIndeterminateVisibility(true);
			showLoadingContent(true);
			setTitle("备份数据");
			setButtonsEnable(false);
		}

		@Override
		protected void onPostExecute(Long arg0) {
			Log.d(TAG, TASK_TAG + "---onPostExecute");
			showLoadingContent(false);
			setButtonsEnable(true);
			updateData(mBackupDataList);
			setOnBackupStatusListener(mBackupListener);
			// setProgressBarIndeterminateVisibility(false);
			Log.d(TAG,
					"---onPostExecute----getTitle"
							+ DataBackupActivity.this.getTitle());
			super.onPostExecute(arg0);
		}

		@Override
		protected Long doInBackground(Void... arg0) {
			messageEnable.clear();
			mBackupDataList = new ArrayList<PersonalItemData>();
			int types[] = new int[] { ModuleType.TYPE_CONTACT,
					ModuleType.TYPE_MESSAGE, ModuleType.TYPE_PICTURE,
					ModuleType.TYPE_MUSIC };
			int num = types.length;
			boolean skipAppend = false;
			for (int i = 0; i < num; i++) {
				boolean bEnabled = true;
				skipAppend = false;
				int count = 0;
				Composer composer;
				switch (types[i]) {
				case ModuleType.TYPE_CONTACT:
					count = getModulesCount(new ContactBackupComposer(
							DataBackupActivity.this));
					break;
				case ModuleType.TYPE_MESSAGE:
					int countSMS = 0;
					int countMMS = 0;
					composer = new SmsBackupComposer(DataBackupActivity.this);
					if (composer.init()) {
						countSMS = composer.getCount();
						composer.onEnd();
					}
					if (countSMS != 0) {
						messageEnable.add("短信");
					}
					count = countSMS + countMMS;
					Log.e(TAG, "countSMS = " + countSMS + "countMMS" + countMMS);
					break;
				case ModuleType.TYPE_PICTURE:
					count = getModulesCount(new PictureBackupComposer(
							DataBackupActivity.this));
					break;
				case ModuleType.TYPE_MUSIC:
					count = getModulesCount(new MusicBackupComposer(
							DataBackupActivity.this));
					break;
				default:
					break;
				}
				composer = null;
				bEnabled = !(count == 0);
				PersonalItemData item = new PersonalItemData(types[i],
						bEnabled);
				item.setCount(count);
				if (!skipAppend)
					mBackupDataList.add(item);
			}
			return null;
		}
	}

	private int getModulesCount(Composer... composers) {
		int count = 0;
		for (Composer composer : composers) {
			if (composer.init()) {
				count += composer.getCount();
				composer.onEnd();
			}
		}
		return count;
	}

	@Override
	public void startBackup() {
		showDialog(DialogID.DLG_EDIT_FOLDER_NAME);
	}

	@Override
	public BaseAdapter initBackupAdapter() {
		mAdapter = new DataBackupAdapter(getApplicationContext(), mBackupList,
				R.layout.br_data_item);
		return mAdapter;
	}

	@Override
	protected void afterServiceConnected() {
		mBackupListener = new PersonalDataBackupStatusListener();
		setOnBackupStatusListener(mBackupListener);
		checkBackupState();
	}

	public class PersonalDataBackupStatusListener extends
			NomalBackupStatusListener {
		@Override
		public void onBackupEnd(final BackupResultType resultCode,
				final ArrayList<ResultEntity> resultRecord,
				final ArrayList<ResultEntity> appResultRecord) {

			RecordXmlInfo backupInfo = new RecordXmlInfo();
			backupInfo.setRestore(false);
			backupInfo.setDevice(Utils.getPhoneSearialNumber());
			backupInfo.setTime(String.valueOf(System.currentTimeMillis()));
			RecordXmlComposer xmlCompopser = new RecordXmlComposer();
			xmlCompopser.startCompose();
			xmlCompopser.addOneRecord(backupInfo);
			xmlCompopser.endCompose();
			if (mBackupFolderPath != null && !mBackupFolderPath.isEmpty()) {
				Utils.writeToFile(xmlCompopser.getXmlInfo(), mBackupFolderPath
						+ File.separator + Constants.RECORD_XML);
			}
			final BackupResultType iResultCode = resultCode;
			final ArrayList<ResultEntity> iResultRecord = resultRecord;
			if (mHandler != null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						Log.d(TAG, "showBackupResult");
						showBackupResult(iResultCode, iResultRecord);
					}
				});
			}
		}

		@Override
		public void onComposerChanged(final Composer composer) {
			if (composer == null) {
				Log.e(TAG, "onComposerChanged: error[composer is null]");
				return;
			} else {
				Log.d(TAG,
						"onComposerChanged: type = " + composer.getModuleType()
								+ "Max = " + composer.getCount());
			}
			if (mHandler != null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						String msg = getProgressDlgMessage(composer
								.getModuleType());
						if (mProgressDialog != null) {
							mProgressDialog.setMessage(msg);
							mProgressDialog.setMax(composer.getCount());
							mProgressDialog.setProgress(0);
						}
					}
				});
			}
		}
	}

	protected String getProgressDlgMessage(final int type) {
		StringBuilder builder = new StringBuilder(getString(R.string.backuping));
		builder.append("(");
		builder.append(ModuleType.getModuleStringFromType(this, type));
		builder.append(")");
		return builder.toString();
	}

	@Override
	protected Dialog onCreateDialog(final int id, final Bundle args) {
		Dialog dialog = null;
		switch (id) {
		// input backup file name
		case DialogID.DLG_EDIT_FOLDER_NAME:
			dialog = createFolderEditorDialog();
			break;

		case DialogID.DLG_RESULT:
			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog,
						final int which) {
					stopService();
					NotifyManager.getInstance(DataBackupActivity.this)
							.clearNotification();
				}
			};
			dialog = ResultDialog.createResultDlg(this, R.string.backup_result,
					args, listener);
			break;
		case DialogID.DLG_BACKUP_CONFIRM_OVERWRITE:
			dialog = new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.notice)
					.setMessage(R.string.backup_confirm_overwrite_notice)
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(R.string.btn_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Log.d(TAG, " to backup");
									File folder = new File(mBackupFolderPath);
									File[] files = folder.listFiles();
									if (files != null && files.length > 0) {
										DeleteFolderTask task = new DeleteFolderTask();
										task.execute(files);
									} else {
										startPersonalDataBackup(mBackupFolderPath);
									}
								}
							})
					// .setCancelable(false)
					.create();
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

		case DialogID.DLG_EDIT_FOLDER_NAME:
			EditText editor = (EditText) dialog
					.findViewById(R.id.edit_folder_name);
			if (editor != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmss");
				String dateString = dateFormat.format(new Date(System
						.currentTimeMillis()));
				editor.setText(dateString);
			}
			break;
		default:
			super.onPrepareDialog(id, dialog, args);
			break;
		}
	}

	private AlertDialog createFolderEditorDialog() {

		LayoutInflater factory = LayoutInflater.from(this);
		final View view = factory.inflate(R.layout.dialog_edit_folder_name,
				null);
		EditText editor = (EditText) view.findViewById(R.id.edit_folder_name);
		final AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.edit_folder_name)
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								AlertDialog d = (AlertDialog) dialog;
								EditText editor = (EditText) d
										.findViewById(R.id.edit_folder_name);
								if (editor != null) {
									CharSequence folderName = editor.getText();
									String path = SDCardUtils
											.getPersonalDataBackupPath(DataBackupActivity.this);
									StringBuilder builder = new StringBuilder(
											path);
									builder.append(File.separator);
									builder.append(folderName);
									mBackupFolderPath = builder.toString();
									hideKeyboard(editor);
									editor.setText("");
									File folder = new File(mBackupFolderPath);
									File[] files = null;
									if (folder.exists()) {
										files = folder.listFiles();
									}

									if (files != null && files.length > 0) {
										showDialog(DialogID.DLG_BACKUP_CONFIRM_OVERWRITE);
									} else {
										startPersonalDataBackup(mBackupFolderPath);
									}
								} else {
									Log.e(TAG, " can not get folder name");
								}
							}

							private void hideKeyboard(EditText editor) {
								// TODO Auto-generated method stub
								InputMethodManager imm = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
								imm.hideSoftInputFromWindow(
										editor.getWindowToken(), 0);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create();
		editor.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().length() <= 0
						|| s.toString().matches(".*[/\\\\:#*?\"<>|].*")
						|| s.toString().matches(" *\\.+ *")
						|| s.toString().trim().length() == 0) { // characters
					// not allowed
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
				} else {
					dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}
		});
		dialogs.add(dialog);
		return dialog;
	}

	private class DeleteFolderTask extends AsyncTask<File[], String, Long> {
		private ProgressDialog mDeletingDialog;

		public DeleteFolderTask() {
			mDeletingDialog = new ProgressDialog(DataBackupActivity.this);
			mDeletingDialog.setCancelable(false);
			mDeletingDialog.setMessage(getString(R.string.delete_please_wait));
			mDeletingDialog.setIndeterminate(true);
		}

		protected void onPostExecute(Long arg0) {
			super.onPostExecute(arg0);
			if (mBackupFolderPath != null) {
				startPersonalDataBackup(mBackupFolderPath);
			}

			if (mDeletingDialog != null) {
				mDeletingDialog.dismiss();
			}
		}

		protected void onPreExecute() {
			if (mDeletingDialog != null) {
				mDeletingDialog.show();
			}
		}

		protected Long doInBackground(File[]... params) {
			File[] files = params[0];
			for (File file : files) {
				FileUtils.deleteFileOrFolder(file);
			}

			return null;
		}
	}

	private void startPersonalDataBackup(String folderName) {
		if (folderName == null || folderName.trim().equals("")) {
			return;
		}
		startService();
		if (mBackupService != null) {
			ArrayList<Integer> list = getSelectedItemList();
			mBackupService.setBackupModelList(list);
			if (list.contains(ModuleType.TYPE_CONTACT)) {
				ArrayList<String> params = new ArrayList<String>();
				params.add(ContactType.PHONE);
				mBackupService.setBackupItemParam(ModuleType.TYPE_CONTACT,
						params);
			}
			if (list.contains(ModuleType.TYPE_MESSAGE)) {
				ArrayList<String> params = new ArrayList<String>();
				params.add(Constants.ModulePath.NAME_SMS);
				mBackupService.setBackupItemParam(ModuleType.TYPE_MESSAGE,
						params);
			}
			boolean ret = mBackupService.startBackup(folderName);
			if (ret) {
				showProgress();
			} else {
				String path = SDCardUtils
						.getStoragePath(DataBackupActivity.this);
				if (path == null) {
					// no sdcard
					Log.d(TAG, "SDCard is removed");
					ret = true;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							showDialog(DialogID.DLG_SDCARD_REMOVED);
						}
					});
				} else if (SDCardUtils.getAvailableSize(path) <= SDCardUtils.MINIMUM_SIZE) {
					// no space
					Log.d(TAG, "SDCard is full");
					ret = true;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							showDialog(DialogID.DLG_SDCARD_FULL);
						}
					});
				} else {
					Log.e(TAG, "unkown error");
					Bundle b = new Bundle();
					b.putString("name", folderName.substring(folderName
							.lastIndexOf('/') + 1));
					showDialog(DialogID.DLG_CREATE_FOLDER_FAILED, b);
				}
				stopService();
			}
		} else {
			stopService();
			Log.e(TAG, "startPersonalDataBackup: error! service is null");
		}
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

	private void showBackupResult(final BackupResultType result,
			final ArrayList<ResultEntity> list) {

		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}

		if (mCancelDlg != null && mCancelDlg.isShowing()) {
			mCancelDlg.dismiss();
		}

		if (result != BackupResultType.Cancel) {
			Bundle args = new Bundle();
			args.putParcelableArrayList(Constants.RESULT_KEY, list);
			showDialog(DialogID.DLG_RESULT, args);
		} else {
			stopService();
		}
	}

	private class DataBackupAdapter extends BaseAdapter {
		private ArrayList<PersonalItemData> mDataList;
		private int mLayoutId;
		private LayoutInflater mInflater;

		public DataBackupAdapter(Context context,
				ArrayList<PersonalItemData> list, int resource) {
			mDataList = list;
			mLayoutId = resource;
			mInflater = LayoutInflater.from(context);
		}

		public void changeData(ArrayList<PersonalItemData> list) {
			mDataList = list;
		}

		public void reset() {
			mDataList = null;
		}

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public Object getItem(final int position) {
			return mDataList.get(position);
		}

		@Override
		public long getItemId(final int position) {
			return mDataList.get(position).getType();
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = mInflater.inflate(mLayoutId, parent, false);
			}

			final PersonalItemData item = mDataList.get(position);
			final ImageView imgView = (ImageView) view
					.findViewById(R.id.iv_item_icon);
			final TextView titleView = (TextView) view
					.findViewById(R.id.tv_item_title);
			final TextView infoView = (TextView) view
					.findViewById(R.id.tv_item_info);
			final CheckBox chxbox = (CheckBox) view
					.findViewById(R.id.cb_item_check);

			boolean bEnabled = item.isEnable();
			imgView.setEnabled(bEnabled);
			titleView.setEnabled(bEnabled);
			infoView.setEnabled(bEnabled);
			chxbox.setEnabled(bEnabled);

			if (!bEnabled) {
				chxbox.setChecked(false);
			}
			long id = getItemId(position);
			setItemDisabledById(id, !bEnabled);
			imgView.setImageResource(item.getIconId());
			titleView.setText(item.getTextId());
			infoView.setText(item.getCount() + "");
			if (isItemCheckedByPosition(position)) {
				if (chxbox.isEnabled()) {
					chxbox.setChecked(true);
				}
			} else {
				if (chxbox.isEnabled()) {
					chxbox.setChecked(false);
				}
			}
			return view;
		}
	}
}
