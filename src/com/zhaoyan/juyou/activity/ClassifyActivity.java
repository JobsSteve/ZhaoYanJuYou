package com.zhaoyan.juyou.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
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

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.FileInfoAdapter;
import com.zhaoyan.juyou.adapter.FileInfoAdapter.ViewHolder;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.FileClassifyScanner;
import com.zhaoyan.juyou.common.FileClassifyScanner.FileClassifyScanListener;
import com.zhaoyan.juyou.common.FileInfo;
import com.zhaoyan.juyou.common.FileInfoManager;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.MenuTabManager;
import com.zhaoyan.juyou.common.MenuTabManager.onMenuItemClickListener;
import com.zhaoyan.juyou.common.ZYConstant;

public class ClassifyActivity extends BaseActivity implements
		OnItemClickListener, OnItemLongClickListener, OnScrollListener,
		onMenuItemClickListener, FileClassifyScanListener {
	private static final String TAG = "ClassifyActivity";
	protected ProgressBar mLoadingBar;
	private ListView mListView;
	protected TextView mTipView;
	protected List<FileInfo> mItemLists = new ArrayList<FileInfo>();
	private List<FileInfo> mTestList = new ArrayList<FileInfo>();
	protected FileInfoAdapter mAdapter;
	protected FileInfoManager mFileInfoManager;
	private FileClassifyScanner mFileClassifyScanner;

	private LinearLayout mMenuHolder;
	private View mMenuBarView;
	private MenuTabManager mMenuTabManager;
	private ActionMenu mActionMenu;

	public static final int TYPE_DOC = 0;
	public static final int TYPE_ARCHIVE = 1;
	public static final int TYPE_APK = 2;
	public static final String CLASSIFY_TYPE = "CLASSIFY_TYPE";
	private int mType = -1;
	private String[] filterType = null;

	private static final int MSG_SCAN_START = 1;
	private static final int MSG_SCAN_COMPLETE = 2;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SCAN_START:
				mLoadingBar.setVisibility(View.VISIBLE);
				mTipView.setVisibility(View.VISIBLE);
				mTipView.setText(R.string.file_classify_loading);
				break;
			case MSG_SCAN_COMPLETE:
				mLoadingBar.setVisibility(View.INVISIBLE);
				mTipView.setVisibility(View.INVISIBLE);

				Vector<FileInfo> fileInfos = (Vector<FileInfo>) msg.obj;
				mAdapter.setList(fileInfos);
				mAdapter.selectAll(false);
				mAdapter.notifyDataSetChanged();
				updateTitleNum(-1, mTestList.size());
				break;

			default:
				break;
			}

		};
	};

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.classify_main);

		Bundle bundle = getIntent().getExtras();
		mType = bundle.getInt(CLASSIFY_TYPE);
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

		mListView = (ListView) findViewById(R.id.lv_classify);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnScrollListener(this);

		mTipView = (TextView) findViewById(R.id.tv_classify_tip);
		mLoadingBar = (ProgressBar) findViewById(R.id.bar_loading_classify);

		mAdapter = new FileInfoAdapter(getApplicationContext(), mItemLists,
				mListView);
		mListView.setAdapter(mAdapter);

		mFileInfoManager = new FileInfoManager();

		mMenuHolder = (LinearLayout) findViewById(R.id.ll_menutabs_holder);
		mMenuBarView = findViewById(R.id.menubar_bottom);
		mMenuBarView.setVisibility(View.GONE);
		mMenuTabManager = new MenuTabManager(getApplicationContext(),
				mMenuHolder);

		long start = System.currentTimeMillis();
		mFileClassifyScanner = new FileClassifyScanner(getApplicationContext(),
				Environment.getExternalStorageDirectory().getAbsoluteFile(),
				filterType);
		mFileClassifyScanner.setScanListener(this);
		mFileClassifyScanner.startScan();
		Log.d(TAG, "mFileClassifyScanner cost time = " + (System.currentTimeMillis() - start));
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mFileClassifyScanner.cancelScan();
		mFileClassifyScanner.setScanListener(null);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (mAdapter.isMode(ZYConstant.MENU_MODE_EDIT)) {
			doSelectAll();
			return true;
		} else {
			mAdapter.changeMode(ZYConstant.MENU_MODE_EDIT);
			updateTitleNum(1, mAdapter.getCount());
		}
		boolean isSelected = mAdapter.isSelected(position);
		mAdapter.setSelected(position, !isSelected);
		mAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(getApplicationContext());
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SEND,
				R.drawable.ic_action_send, R.string.menu_send);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_DELETE,
				R.drawable.ic_action_delete_enable, R.string.menu_delete);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_RENAME,
				R.drawable.ic_action_rename, R.string.menu_rename);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_INFO,
				R.drawable.ic_action_info, R.string.menu_info);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SELECT,
				R.drawable.ic_aciton_select, R.string.select_all);

		showMenuBar(true);
		mMenuTabManager.refreshMenus(mActionMenu);
		mMenuTabManager.setOnMenuItemClickListener(this);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mAdapter.isMode(ZYConstant.MENU_MODE_EDIT)) {
			mAdapter.setSelected(position);
			mAdapter.notifyDataSetChanged();

			int selectedCount = mAdapter.getSelectedItems();
			updateTitleNum(selectedCount, mAdapter.getCount());
			updateMenuBar();
			mMenuTabManager.refreshMenus(mActionMenu);
		} else {
			// open file
			mFileInfoManager.openFile(getApplicationContext(),
					mAdapter.getItem(position).filePath);
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
			// doTransfer();
			showMenuBar(false);
			break;
		case ActionMenu.ACTION_MENU_DELETE:
			List<Integer> posList = mAdapter.getSelectedItemPositions();
			// showDeleteDialog(posList);
			break;
		case ActionMenu.ACTION_MENU_INFO:
			List<FileInfo> list = mAdapter.getSelectedFileInfos();
			// mFileInfoManager.showInfoDialog(list);
			showMenuBar(false);
			break;
		case ActionMenu.ACTION_MENU_SELECT:
			doSelectAll();
			break;
		default:
			break;
		}
	}

	/**
	 * set menubar visible or gone
	 * 
	 * @param show
	 */
	public void showMenuBar(boolean show) {
		if (show) {
			mMenuBarView.setVisibility(View.VISIBLE);
		} else {
			mMenuBarView.setVisibility(View.GONE);
			onActionMenuDone();
			updateTitleNum(-1, mAdapter.getCount());
		}
	}

	/**
	 * update menu bar item icon and text color,enable or disable
	 */
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

	// Cancle Action menu
	public void onActionMenuDone() {
		mAdapter.changeMode(ZYConstant.MENU_MODE_NORMAL);
		mAdapter.clearSelected();
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * do select all items or unselect all items
	 */
	public void doSelectAll() {
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
				ClassifyActivity.this);
		fileTransferUtil.sendFiles(checkedList, new TransportCallback() {

			@Override
			public void onTransportSuccess() {
				int first = mListView.getFirstVisiblePosition();
				int last = mListView.getLastVisiblePosition();
				List<Integer> checkedItems = mAdapter
						.getSelectedItemPositions();
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
					// showTransportAnimation(icons.toArray(imageViews));
				}
			}

			@Override
			public void onTransportFail() {

			}
		});
	}

	@Override
	public boolean onBackKeyPressed() {
		if (mAdapter.isMode(ZYConstant.MENU_MODE_EDIT)) {
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
