package com.zhaoyan.juyou.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.util.IntentBuilder;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.FileInfoAdapter;
import com.zhaoyan.juyou.adapter.FileInfoAdapter.ViewHolder;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.FileCategoryScanner;
import com.zhaoyan.juyou.common.FileCategoryScanner.FileCategoryScanListener;
import com.zhaoyan.juyou.common.FileIconHelper;
import com.zhaoyan.juyou.common.FileInfo;
import com.zhaoyan.juyou.common.FileInfoManager;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.MenuBarInterface;
import com.zhaoyan.juyou.common.MountManager;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.MenuBarManager;
import com.zhaoyan.juyou.common.MenuBarManager.onMenuItemClickListener;
import com.zhaoyan.juyou.dialog.DeleteDialog;
import com.zhaoyan.juyou.dialog.DeleteDialog.OnDelClickListener;

public class FileCategoryActivity extends BaseActivity implements
		OnItemClickListener, OnItemLongClickListener, OnScrollListener,
		onMenuItemClickListener, FileCategoryScanListener, MenuBarInterface {
	private static final String TAG = "CategoryActivity";
	private ProgressBar mLoadingBar;
	private ListView mListView;
	private TextView mTipView;
	private ViewGroup mViewGroup;
	private List<FileInfo> mItemLists = new ArrayList<FileInfo>();
	protected FileInfoAdapter mAdapter;
	protected FileInfoManager mFileInfoManager;
	private FileCategoryScanner mFileCategoryScanner;

	private LinearLayout mMenuHolder;
	private View mMenuBarView;
	private MenuBarManager mMenuTabManager;
	private ActionMenu mActionMenu;

	public static final int TYPE_DOC = 0;
	public static final int TYPE_ARCHIVE = 1;
	public static final int TYPE_APK = 2;
	public static final String CATEGORY_TYPE = "CATEGORY_TYPE";
	private int mType = -1;
	private String[] filterType = null;
	private DeleteDialog mDeleteDialog = null;
	private FileIconHelper mIconHelper;
	
	private Notice mNotice = null;

	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_UPDATE_LIST = 1;
	private static final int MSG_SCAN_START = 2;
	private static final int MSG_SCAN_COMPLETE = 3;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SCAN_START:
				mLoadingBar.setVisibility(View.VISIBLE);
				mTipView.setVisibility(View.VISIBLE);
				mTipView.setText(R.string.file_Category_loading);
				break;
			case MSG_SCAN_COMPLETE:
				mLoadingBar.setVisibility(View.INVISIBLE);
				mTipView.setVisibility(View.INVISIBLE);

				Vector<FileInfo> fileInfos = (Vector<FileInfo>) msg.obj;
				mAdapter.setList(fileInfos);
				mAdapter.selectAll(false);
				mAdapter.notifyDataSetChanged();
				updateTitleNum(-1, fileInfos.size());
				break;
			case MSG_UPDATE_LIST:
				List<FileInfo> fileList = mAdapter.getList();
				fileList.remove(msg.arg1);
				mAdapter.notifyDataSetChanged();
				updateTitleNum(-1, mAdapter.getCount());
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.category_main);

		Bundle bundle = getIntent().getExtras();
		mType = bundle.getInt(CATEGORY_TYPE);
		if (TYPE_DOC == mType) {
			initTitle(R.string.file_document);
			filterType = getResources().getStringArray(R.array.doc_file);
		} else if (TYPE_ARCHIVE == mType) {
			initTitle(R.string.file_compressed);
			filterType = getResources().getStringArray(R.array.archive_file);
		} else if (TYPE_APK == mType) {
			initTitle(R.string.file_apk);
			filterType = getResources().getStringArray(R.array.apk_file);
		}
		setTitleNumVisible(true);

		mListView = (ListView) findViewById(R.id.lv_Category);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnScrollListener(this);

		mTipView = (TextView) findViewById(R.id.tv_Category_tip);
		mLoadingBar = (ProgressBar) findViewById(R.id.bar_loading_Category);
		
		mViewGroup = (ViewGroup) findViewById(R.id.rl_Category_main);
		
		mIconHelper = new FileIconHelper(getApplicationContext());
		mAdapter = new FileInfoAdapter(getApplicationContext(), mItemLists, mIconHelper);
		mListView.setAdapter(mAdapter);

		mFileInfoManager = new FileInfoManager();

		mMenuHolder = (LinearLayout) findViewById(R.id.ll_menutabs_holder);
		mMenuBarView = findViewById(R.id.menubar_bottom);
		mMenuBarView.setVisibility(View.GONE);
		mNotice = new Notice(getApplicationContext());
		mMenuTabManager = new MenuBarManager(getApplicationContext(),
				mMenuHolder);

		SharedPreferences sp = SharedPreferenceUtil.getSharedPreference(getApplicationContext());
		String sdcard_path = sp.getString(SharedPreferenceUtil.SDCARD_PATH, MountManager.NO_EXTERNAL_SDCARD);
		String internal_path = sp.getString(SharedPreferenceUtil.INTERNAL_PATH, MountManager.NO_INTERNAL_SDCARD);
		Log.d(TAG, "sdcard_path:" + sdcard_path + "\n," + "internal_path:" + internal_path);

		// init
		//get storage status
		int status = MountManager.getStorageStatus(internal_path, sdcard_path);
		switch (status) {
		case 0:
			File[] rootDirs = new File[2];
			rootDirs[0] = new File(sdcard_path);
			rootDirs[1] = new File(internal_path);
			
			mFileCategoryScanner = new FileCategoryScanner(getApplicationContext(),
					rootDirs,filterType, mType);
			break;
		case 1:
			File internalFileRoot = new File(internal_path);
			mFileCategoryScanner = new FileCategoryScanner(getApplicationContext(),
					internalFileRoot,filterType, mType);
			break;
		case 2:
			File sdcardFileRoot = new File(sdcard_path);
			mFileCategoryScanner = new FileCategoryScanner(getApplicationContext(),
					sdcardFileRoot,filterType, mType);
			break;
		case 3:
			//no storage
			mTipView.setVisibility(View.VISIBLE);
			mTipView.setText(R.string.no_sdcard);
			mListView.setEmptyView(mTipView);
			break;
		default:
			break;
		}
		
		if (null != mFileCategoryScanner) {
			long start = System.currentTimeMillis();
			mFileCategoryScanner.setScanListener(this);
			mFileCategoryScanner.startScan();
			Log.d(TAG, "mFileCategoryScanner cost time = " + (System.currentTimeMillis() - start));
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mFileCategoryScanner) {
			mFileCategoryScanner.cancelScan();
			mFileCategoryScanner.setScanListener(null);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			doCheckAll();
			return true;
		} else {
			updateTitleNum(1, mAdapter.getCount());
		}
		boolean isSelected = mAdapter.isSelected(position);
		mAdapter.setSelected(position, !isSelected);
		mAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(getApplicationContext());
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SEND, R.drawable.ic_action_send, R.string.menu_send);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_DELETE,R.drawable.ic_action_delete_enable,R.string.menu_delete);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_RENAME, R.drawable.ic_action_rename, R.string.rename);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_INFO,R.drawable.ic_action_info,R.string.menu_info);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SELECT, R.drawable.ic_aciton_select, R.string.select_all);
		
		showMenuBar(true);
		mMenuTabManager.refreshMenus(mActionMenu);
		mMenuTabManager.setOnMenuItemClickListener(this);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			mAdapter.setSelected(position);
			mAdapter.notifyDataSetChanged();

			int selectedCount = mAdapter.getSelectedItems();
			updateTitleNum(selectedCount, mAdapter.getCount());
			updateMenuBar();
			mMenuTabManager.refreshMenus(mActionMenu);
		} else {
			// open file
			IntentBuilder.viewFile(this, mAdapter.getItem(position).filePath);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (TYPE_DOC == mType || TYPE_ARCHIVE == mType) {
			return;
		}

		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_FLING:
			mAdapter.setFlag(false);
			break;
		case OnScrollListener.SCROLL_STATE_IDLE:
			mAdapter.setFlag(true);
			mAdapter.notifyDataSetChanged();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mAdapter.setFlag(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void onMenuClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case ActionMenu.ACTION_MENU_SEND:
			doTransfer();
			showMenuBar(false);
			break;
		case ActionMenu.ACTION_MENU_DELETE:
			List<Integer> posList = mAdapter.getSelectedItemsPos();
			showDeleteDialog(posList);
			break;
		case ActionMenu.ACTION_MENU_INFO:
			List<FileInfo> list = mAdapter.getSelectedFileInfos();
			 mFileInfoManager.showInfoDialog(this, list);
			break;
		case ActionMenu.ACTION_MENU_SELECT:
			doCheckAll();
			break;
		case ActionMenu.ACTION_MENU_RENAME:
			List<FileInfo> renameList = mAdapter.getSelectedFileInfos();
			mFileInfoManager.showRenameDialog(this, renameList);
			mAdapter.notifyDataSetChanged();
			showMenuBar(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void showMenuBar(boolean show) {
		if (show) {
			mMenuBarView.setVisibility(View.VISIBLE);
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
		} else {
			mMenuBarView.setVisibility(View.GONE);
			
			mAdapter.changeMode(ActionMenu.MODE_NORMAL);
			mAdapter.clearSelected();
			mAdapter.notifyDataSetChanged();
			
			updateTitleNum(-1, mAdapter.getCount());
		}
	}

	@Override
	public void updateMenuBar() {
		int selectCount = mAdapter.getSelectedItems();
		updateTitleNum(selectCount, mAdapter.getCount());

		if (mAdapter.getCount() == selectCount) {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SELECT).setTitle(
					R.string.unselect_all);
		} else {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SELECT).setTitle(
					R.string.select_all);
		}

		if (0 == selectCount) {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(false);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_DELETE)
					.setEnable(false);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_RENAME)
					.setEnable(false);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_INFO).setEnable(false);
		} else {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_DELETE).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_RENAME).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_INFO).setEnable(true);
		}
	}

	@Override
	public void doCheckAll() {
		int selectedCount = mAdapter.getSelectedItems();
		if (mAdapter.getCount() != selectedCount) {
			mAdapter.selectAll(true);
		} else {
			mAdapter.selectAll(false);
		}
		updateMenuBar();
		mMenuTabManager.refreshMenus(mActionMenu);
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * do Tranfer files
	 */
	public void doTransfer() {
		ArrayList<String> checkedList = (ArrayList<String>) mAdapter
				.getSelectedFilePaths();

		// send
		FileTransferUtil fileTransferUtil = new FileTransferUtil(
				FileCategoryActivity.this);
		fileTransferUtil.sendFiles(checkedList, new TransportCallback() {

			@Override
			public void onTransportSuccess() {
				int first = mListView.getFirstVisiblePosition();
				int last = mListView.getLastVisiblePosition();
				List<Integer> checkedItems = mAdapter
						.getSelectedItemsPos();
				ArrayList<ImageView> icons = new ArrayList<ImageView>();
				for (int id : checkedItems) {
					if (id >= first && id <= last) {
						View view = mListView.getChildAt(id - first);
						if (view != null) {
							ViewHolder viewHolder = (ViewHolder) view.getTag();
							icons.add(viewHolder.iconView);
						}
					}
				}

				if (icons.size() > 0) {
					ImageView[] imageViews = new ImageView[0];
					showTransportAnimation(mViewGroup, icons.toArray(imageViews));
				}
			}

			@Override
			public void onTransportFail() {

			}
		});
	}
	
	/**
	 * show delete confrim dialog
	 */
	public void showDeleteDialog(final List<Integer> posList) {
		// get name list
		List<String> nameList = new ArrayList<String>();
		final List<FileInfo> fileList = mAdapter.getList();
		for (int position : posList) {
			nameList.add(fileList.get(position).fileName);
		}

		mDeleteDialog = new DeleteDialog(this, nameList);
		mDeleteDialog.setButton(AlertDialog.BUTTON_POSITIVE, R.string.menu_delete, new OnDelClickListener() {
			@Override
			public void onClick(View view, String path) {
				showMenuBar(false);
				new DeleteTask(posList).execute();
			}
		});
		mDeleteDialog.setButton(AlertDialog.BUTTON_NEGATIVE, R.string.cancel, null);
		mDeleteDialog.show();
	}

	/**
	 * Delete file task
	 */
	private class DeleteTask extends AsyncTask<Void, String, String> {
		List<Integer> positionList = new ArrayList<Integer>();

		DeleteTask(List<Integer> list) {
			positionList = list;
		}

		@Override
		protected String doInBackground(Void... params) {
			List<FileInfo> fileList = mAdapter.getList();
			List<File> deleteList = new ArrayList<File>();
			// get delete path list
			File file = null;
			FileInfo fileInfo = null;
			for (int i = 0; i < positionList.size(); i++) {
				int position = positionList.get(i);
				fileInfo = fileList.get(position);
				file = new File(fileInfo.filePath);
				deleteList.add(file);
			}

			for (int i = 0; i < deleteList.size(); i++) {
				deleteList.get(i).delete();
				int position = positionList.get(i) - i;
				
				Message message = mHandler.obtainMessage();
				message.arg1 = position;
				message.what = MSG_UPDATE_LIST;
				message.sendToTarget();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (null != mDeleteDialog) {
				mDeleteDialog.cancel();
				mDeleteDialog = null;
			}
			mNotice.showToast(R.string.operator_over);
		}
	}
	
	@Override
	public boolean onBackKeyPressed() {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			showMenuBar(false);
			return false;
		}
		return super.onBackKeyPressed();
	}

	@Override
	public void onScanStart() {
		Log.d(TAG, "onScanStart");
		mHandler.sendEmptyMessage(MSG_SCAN_START);
	}

	@Override
	public void onScanComplete(Vector<FileInfo> fileInfos) {
		Log.d(TAG, "onScanComplete");
		Message message = mHandler.obtainMessage(MSG_SCAN_COMPLETE);
		message.obj = fileInfos;
		message.sendToTarget();
	}

	@Override
	public void onScanCancel() {
		Log.d(TAG, "onScanCancel");
	}
}
