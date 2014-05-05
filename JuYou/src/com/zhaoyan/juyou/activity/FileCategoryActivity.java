package com.zhaoyan.juyou.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Dialog;
import android.content.SharedPreferences;
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
import com.zhaoyan.juyou.common.FileDeleteHelper.OnDeleteListener;
import com.zhaoyan.juyou.common.FileDeleteHelper;
import com.zhaoyan.juyou.common.FileIconHelper;
import com.zhaoyan.juyou.common.FileInfo;
import com.zhaoyan.juyou.common.FileInfoManager;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.MenuBarInterface;
import com.zhaoyan.juyou.common.ZyStorageManager;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.dialog.ZyDeleteDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;

public class FileCategoryActivity extends BaseActivity implements
		OnItemClickListener, OnItemLongClickListener, OnScrollListener,
		FileCategoryScanListener, MenuBarInterface {
	private static final String TAG = "FileCategoryActivity";
	private ProgressBar mLoadingBar;
	private ListView mListView;
	private TextView mTipView;
	private ViewGroup mViewGroup;
	private List<FileInfo> mItemLists = new ArrayList<FileInfo>();
	protected FileInfoAdapter mAdapter;
	protected FileInfoManager mFileInfoManager;
	private FileCategoryScanner mFileCategoryScanner;

	public static final int TYPE_DOC = 0;
	public static final int TYPE_ARCHIVE = 1;
	public static final int TYPE_APK = 2;
	public static final String CATEGORY_TYPE = "CATEGORY_TYPE";
	private int mType = -1;
	private String[] filterType = null;
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
				updateTitleNum(-1, mAdapter.getCount());
				
				mNotice.showToast(R.string.operator_over);
				
				List<Integer> poslist = new ArrayList<Integer>();
				Bundle bundle = msg.getData();
				if (null != bundle) {
					poslist = bundle.getIntegerArrayList("position");
//					Log.d(TAG, "poslist.size=" + poslist);
					int removePosition;
					for(int i = 0; i < poslist.size() ; i++){
						//remove from the last item to the first item
						removePosition = poslist.get(poslist.size() - (i + 1));
//						Log.d(TAG, "removePosition:" + removePosition);
						fileList.remove(removePosition);
						mAdapter.notifyDataSetChanged();
					}
					
					updateTitleNum(-1, fileList.size());
				}else {
					Log.e(TAG, "bundle is null");
				}
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

		mNotice = new Notice(getApplicationContext());
		initMenuBar();

		ZyStorageManager zsm = ZyStorageManager.getInstance(getApplicationContext());
		String[] volumnPaths = zsm.getVolumePaths();
		if (volumnPaths == null) {
			Log.e(TAG, "No storage.");
			mTipView.setVisibility(View.VISIBLE);
			mTipView.setText(R.string.no_sdcard);
			mListView.setEmptyView(mTipView);
		} else if (volumnPaths.length == 1) {
			//only internal storage
			Log.d(TAG, "internal path:" + volumnPaths[0]);
			File internalFileRoot = new File(volumnPaths[0]);
			mFileCategoryScanner = new FileCategoryScanner(getApplicationContext(),
					internalFileRoot,filterType, mType);
		} else {
			//have internal & external
			Log.d(TAG, "internal path:" + volumnPaths[0]);
			Log.d(TAG, "external path:" + volumnPaths[1]);
			File[] rootDirs = new File[2];
			rootDirs[0] = new File(volumnPaths[0]);
			rootDirs[1] = new File(volumnPaths[1]);
			mFileCategoryScanner = new FileCategoryScanner(getApplicationContext(),
					rootDirs,filterType, mType);
			//遇到的问题：万一有三张卡呢？  不会吧，如今的手机我可没见过
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
			//do nothing
			//doCheckAll();
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1, mAdapter.getCount());
		}
		boolean isSelected = mAdapter.isSelected(position);
		mAdapter.setSelected(position, !isSelected);
		mAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(getApplicationContext());
		getActionMenuInflater().inflate(R.menu.filecategory_menu, mActionMenu);
		
		startMenuBar();
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
			mMenuBarManager.refreshMenus(mActionMenu);
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
	public void onMenuItemClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send:
			doTransfer();
			break;
		case R.id.menu_delete:
			showDeleteDialog();
			break;
		case R.id.menu_info:
			List<FileInfo> list = mAdapter.getSelectedFileInfos();
			 mFileInfoManager.showInfoDialog(this, list);
			break;
		case R.id.menu_select:
			doCheckAll();
			break;
		case R.id.menu_rename:
			List<FileInfo> renameList = mAdapter.getSelectedFileInfos();
			mFileInfoManager.showRenameDialog(this, renameList);
			mAdapter.notifyDataSetChanged();
			destroyMenuBar();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void destroyMenuBar() {
		super.destroyMenuBar();
		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.clearSelected();
		mAdapter.notifyDataSetChanged();
		
		updateTitleNum(-1, mAdapter.getCount());
	}

	@Override
	public void updateMenuBar() {
		int selectCount = mAdapter.getSelectedItems();
		updateTitleNum(selectCount, mAdapter.getCount());

		ActionMenuItem selectItem = mActionMenu.findItem(R.id.menu_select);
		if (mAdapter.getCount() == selectCount) {
			selectItem.setTitle(R.string.unselect_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_unselect);
		} else {
			selectItem.setTitle(R.string.select_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_select);
		}

		if (0==selectCount) {
			mActionMenu.findItem(R.id.menu_send).setEnable(false);
			mActionMenu.findItem(R.id.menu_delete).setEnable(false);
			mActionMenu.findItem(R.id.menu_rename).setEnable(false);
			mActionMenu.findItem(R.id.menu_info).setEnable(false);
		}else {
			mActionMenu.findItem(R.id.menu_send).setEnable(true);
			mActionMenu.findItem(R.id.menu_delete).setEnable(true);
			mActionMenu.findItem(R.id.menu_rename).setEnable(true);
			mActionMenu.findItem(R.id.menu_info).setEnable(true);
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
		mMenuBarManager.refreshMenus(mActionMenu);
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
				
				destroyMenuBar();
			}

			@Override
			public void onTransportFail() {

			}
		});
	}
	
	/**
	 * show delete confrim dialog
	 */
	public void showDeleteDialog() {
		final List<FileInfo> fileList = mAdapter.getList();
		final List<Integer> posList = mAdapter.getSelectedItemsPos();
		
		ZyDeleteDialog deleteDialog = new ZyDeleteDialog(this);
		deleteDialog.setTitle(R.string.delete_file);
		String msg = "";
		if (posList.size() == 1) {
			msg = getString(R.string.delete_file_confirm_msg, fileList.get(posList.get(0)).fileName);
		}else {
			msg = getString(R.string.delete_file_confirm_msg_file, posList.size());
		}
		deleteDialog.setMessage(msg);
		deleteDialog.setPositiveButton(R.string.menu_delete, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				FileDeleteHelper deleteHelper = new FileDeleteHelper(FileCategoryActivity.this);
				deleteHelper.setDeletePathList(mAdapter.getSelectedFilePaths());
				deleteHelper.setOnDeleteListener(new OnDeleteListener() {
					@Override
					public void onDeleteFinished() {
						//when delete over,send message to update ui
						Message message = mHandler.obtainMessage();
						Bundle bundle = new Bundle();
						bundle.putIntegerArrayList("position", (ArrayList<Integer>)posList);
						message.setData(bundle);
						message.what = MSG_UPDATE_LIST;
						message.sendToTarget();
					}
				});
				deleteHelper.doDelete();
				
				dialog.dismiss();
				destroyMenuBar();
			}
		});
		deleteDialog.setNegativeButton(R.string.cancel, null);
		deleteDialog.show();
	}

	@Override
	public boolean onBackKeyPressed() {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
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
