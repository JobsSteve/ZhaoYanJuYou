package com.zhaoyan.juyou.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.IntentBuilder;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.common.view.SlowHorizontalScrollView;
import com.zhaoyan.common.view.ZyPopupMenu;
import com.zhaoyan.common.view.ZyPopupMenu.PopupViewClickListener;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.FileHomeAdapter;
import com.zhaoyan.juyou.adapter.FileInfoAdapter;
import com.zhaoyan.juyou.adapter.FileInfoAdapter.ViewHolder;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.FileIconHelper;
import com.zhaoyan.juyou.common.FileInfo;
import com.zhaoyan.juyou.common.FileInfoManager;
import com.zhaoyan.juyou.common.FileOperationHelper;
import com.zhaoyan.juyou.common.FileDeleteHelper;
import com.zhaoyan.juyou.common.FileDeleteHelper.OnDeleteListener;
import com.zhaoyan.juyou.common.MenuBarInterface;
import com.zhaoyan.juyou.common.FileOperationHelper.OnOperationListener;
import com.zhaoyan.juyou.common.FileInfoManager.NavigationRecord;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.MountManager;
import com.zhaoyan.juyou.dialog.CopyMoveDialog;
import com.zhaoyan.juyou.dialog.ZyDeleteDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;
import com.zhaoyan.juyou.dialog.ZyEditDialog;

public class FileBrowserFragment extends BaseFragment implements OnClickListener, OnItemClickListener, OnScrollListener,
		OnItemLongClickListener, OnOperationListener, MenuBarInterface, OnDeleteListener {
	private static final String TAG = "FileBrowserFragment";

	// File path navigation bar
	private SlowHorizontalScrollView mNavigationBar = null;

	private ListView mListView = null;
	private TextView mListViewTip;
	private ProgressBar mLoadingBar;
	private LinearLayout mNavBarLayout;

	//fast to go to home view
	private View mHomeView;

	private TabManager mTabManager;
	private View rootView = null;
	private FileInfo mSelectedFileInfo = null;
	private int mTop = -1;

	private FileHomeAdapter mHomeAdapter = null;
	private FileInfoManager mFileInfoManager = null;
	
	private FileInfoAdapter mAdapter;
	private FileIconHelper mIconHelper;

	// save all files
	private List<FileInfo> mAllLists = new ArrayList<FileInfo>();
	// save folders
	private List<FileInfo> mFolderLists = new ArrayList<FileInfo>();
	// save files
	private List<FileInfo> mFileLists = new ArrayList<FileInfo>();
	private List<Integer> mHomeList = new ArrayList<Integer>();
	
	//copy or cut file path list
	private List<FileInfo> mCopyList = new ArrayList<FileInfo>();
	
	//delete item positions
	private List<Integer> mDeletePosList = new ArrayList<Integer>();

	public static final int INTERNAL = MountManager.INTERNAL;
	public static final int SDCARD = MountManager.SDCARD;
	private static final int STATUS_FILE = 0;
	private static final int STATUS_HOME = 1;
	private int mStatus = STATUS_HOME;

	private Context mApplicationContext;

	/**
	 * current dir path
	 */
	private String mCurrentPath;

	// context menu
	// save current sdcard type
	private static int storge_type = -1;
	// save current sdcard type path
	private String mCurrent_root_path;

	private SharedPreferences sp = null;

	private String sdcard_path;
	private String internal_path;
	
	private Comparator<FileInfo> NAME_COMPARATOR = FileInfo.getNameComparator();
	
	private FileOperationHelper mFileOperationHelper;
	private FileDeleteHelper mDeleteHelper;

	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_UPDATE_LIST = 2;
	private static final int MSG_UPDATE_HOME = 3;
	private static final int MSG_UPDATE_FILE = 4;
	private static final int MSG_REFRESH = 5;
	private static final int MSG_OPERATION_OVER = 6;
	private static final int MSG_OPERATION_NOTIFY = 7;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int size = msg.arg1;
				count = size;
				updateTitleNum(-1);
				break;
			case MSG_UPDATE_FILE:
				mAdapter.notifyDataSetChanged();
				break;
			case MSG_UPDATE_LIST:
				mNotice.showToast(R.string.operator_over);
				List<FileInfo> fileList = mAdapter.getList();
				
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
					updateUI(fileList.size());
				}else {
					Log.e(TAG, "bundle is null");
				}
				break;
			case MSG_UPDATE_HOME:
				mHomeAdapter.notifyDataSetChanged();
				break;
			case MSG_REFRESH:
				browserTo(new File(mCurrentPath));
				break;
			case MSG_OPERATION_OVER:
				destroyMenuBar();
				browserTo(new File(mCurrentPath));
				break;
			case MSG_OPERATION_NOTIFY:
				mNotice.showToast(msg.obj.toString());
				break;
			default:
				break;
			}
		};
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFileOperationHelper = new FileOperationHelper(getActivity().getApplicationContext());
		mFileOperationHelper.setOnOperationListener(this);
		
		mDeleteHelper = new FileDeleteHelper(getActivity());
		mDeleteHelper.setOnDeleteListener(this);
		Log.d(TAG, "onCreate.mStatus=" + mStatus);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.file_main, container, false);
		mApplicationContext = getActivity().getApplicationContext();
		mListView = (ListView) rootView.findViewById(R.id.lv_file);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);
		mListView.setOnItemLongClickListener(this);

		initTitle(rootView.findViewById(R.id.rl_file_browser_main), R.string.all_file);

		mListViewTip = (TextView) rootView.findViewById(R.id.tv_file_listview_tip);
		mLoadingBar = (ProgressBar) rootView.findViewById(R.id.bar_loading_file);
		mNavBarLayout = (LinearLayout) rootView.findViewById(R.id.navigation_bar);
		mNavigationBar = (SlowHorizontalScrollView) rootView.findViewById(R.id.navigation_bar_view);
		if (mNavigationBar != null) {
			mNavigationBar.setVerticalScrollBarEnabled(false);
			mNavigationBar.setHorizontalScrollBarEnabled(false);
			mTabManager = new TabManager();
		}
		mHomeView = rootView.findViewById(R.id.ll_home);
		mHomeView.setOnClickListener(this);

		initMenuBar(rootView);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		sp = SharedPreferenceUtil.getSharedPreference(mApplicationContext);

		mFileInfoManager = new FileInfoManager();

		sdcard_path = sp.getString(SharedPreferenceUtil.SDCARD_PATH, MountManager.NO_EXTERNAL_SDCARD);
		internal_path = sp.getString(SharedPreferenceUtil.INTERNAL_PATH, MountManager.NO_INTERNAL_SDCARD);
		Log.d(TAG, "sdcard_path:" + sdcard_path + "\n," + "internal_path:" + internal_path);

		mHomeList.clear();
		// init
		if (!MountManager.NO_INTERNAL_SDCARD.equals(internal_path)) {
			mHomeList.add(INTERNAL);
		}

		if (!MountManager.NO_EXTERNAL_SDCARD.equals(sdcard_path)) {
			mHomeList.add(SDCARD);
		}

		mHomeAdapter = new FileHomeAdapter(mApplicationContext, mHomeList);
		mIconHelper = new FileIconHelper(mApplicationContext);
		mAdapter = new FileInfoAdapter(mApplicationContext, mAllLists, mIconHelper);

		if (mHomeList.size() <= 0) {
			mNavBarLayout.setVisibility(View.GONE);
			mListViewTip.setVisibility(View.VISIBLE);
			mListViewTip.setText(R.string.no_sdcard);
		} else {
			goToHome();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ll_home:
			destroyMenuBar();
			goToHome();
			break;
		default:
			mTabManager.updateNavigationBar(v.getId(), storge_type);
			break;
		}
	}

	private int restoreSelectedPosition() {
		if (mSelectedFileInfo == null) {
			Log.d(TAG, "restoreSelectedPosition.mSelectedFileInfo is null");
			return -1;
		} else {
			int curSelectedItemPosition = mAdapter.getPosition(mSelectedFileInfo);
			Log.d(TAG, "restoreSelectedPosition.curSelectedItemPosition=" + curSelectedItemPosition);
			mSelectedFileInfo = null;
			return curSelectedItemPosition;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (STATUS_HOME == mStatus) {
			int type = mHomeList.get(position);
			mNavBarLayout.setVisibility(View.VISIBLE);
			mStatus = STATUS_FILE;
			setAdapter(mAllLists);
			switch (type) {
			case INTERNAL:
				doInternal();
				break;
			case SDCARD:
				doSdcard();
				break;
			default:
				break;
			}
		} else {
			if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
				mAdapter.setSelected(position);
				mAdapter.notifyDataSetChanged();

				int selectedCount = mAdapter.getSelectedItems();
				updateTitleNum(selectedCount);
				updateMenuBar();
				mMenuBarManager.refreshMenus(mActionMenu);
			} else {
				FileInfo selectedFileInfo = mAdapter.getItem(position);
				if (selectedFileInfo.isDir) {
					int top = view.getTop();
					addToNavigationList(mCurrentPath, top, selectedFileInfo);
					browserTo(new File(selectedFileInfo.filePath));
				} else {
					// open file
					IntentBuilder.viewFile(getActivity(), selectedFileInfo.filePath);
				}
			}
		}
	}

	private void setAdapter(List<FileInfo> list) {
		updateUI(list.size());
		mAdapter.setList(list);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, final View view, final int position, long arg3) {
		if (STATUS_HOME == mStatus) {
			return false;
		}

		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			doCheckAll();
			return true;
		} else if (mAdapter.isMode(ActionMenu.MODE_COPY)
				|| mAdapter.isMode(ActionMenu.MODE_CUT)) {
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1);
		}
		
		boolean isSelected = mAdapter.isSelected(position);
		mAdapter.setSelected(position, !isSelected);
		mAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(mApplicationContext);
		getActionMenuInflater().inflate(R.menu.allfile_menu, mActionMenu);

		if (mAllLists.get(position).isDir) {
			//we do not support send folder
			mActionMenu.findItem(R.id.menu_send).setEnable(false);
		}
		
		startMenuBar();
		return true;
	}

	public void browserTo(File file) {
		Log.d(TAG, "browserTo.status=" + mStatus);
		if (file.isDirectory()) {
			mCurrentPath = file.getAbsolutePath();

			clearList();

			fillList(file.listFiles());

			// sort
			Collections.sort(mFolderLists, NAME_COMPARATOR);
			Collections.sort(mFileLists, NAME_COMPARATOR);

			mAllLists.addAll(mFolderLists);
			mAllLists.addAll(mFileLists);

			mAdapter.notifyDataSetChanged();
			int seletedItemPosition = restoreSelectedPosition();
			// Log.d(TAG, "seletedItemPosition:" + seletedItemPosition +
			// ",mTop=" + mTop);
			if (seletedItemPosition == -1) {
				mListView.setSelectionAfterHeaderView();
			} else if (seletedItemPosition >= 0 && seletedItemPosition < mAdapter.getCount()) {
				if (mTop == -1) {
					mListView.setSelection(seletedItemPosition);
				} else {
					mListView.setSelectionFromTop(seletedItemPosition, mTop);
					mTop = -1;
				}
			}

			mAdapter.selectAll(false);
			updateUI(mAllLists.size());
			mTabManager.refreshTab(mCurrentPath, storge_type);
		} else {
			Log.e(TAG, "It is a file");
		}
	}

	private void clearList() {
		mAllLists.clear();
		mFolderLists.clear();
		mFileLists.clear();
	}

	/** fill current folder's files into list */
	private void fillList(File[] file) {
		for (File currentFile : file) {
			FileInfo fileInfo = null;

			fileInfo = new FileInfo(currentFile.getName());
			fileInfo.fileDate = currentFile.lastModified();
			fileInfo.filePath = currentFile.getAbsolutePath();
			if (currentFile.isDirectory()) {
				fileInfo.isDir = true;
				fileInfo.fileSize = 0;
				fileInfo.type = FileInfoManager.UNKNOW;
				//do not count hidden files
				File[] files = currentFile.listFiles();
				if (null == files) {
					fileInfo.count = 0;
				}else {
					int count = files.length;
					for(File f : files){
						if (f.isHidden()) {
							count --;
						}
					}
					fileInfo.count = count;
				}
				
				if (currentFile.isHidden()) {
					// do nothing
				} else {
					mFolderLists.add(fileInfo);
				}
			} else {
				fileInfo.isDir = false;
				fileInfo.fileSize = currentFile.length();
				fileInfo.type = FileManager.getFileType(mApplicationContext, currentFile);
				if (currentFile.isHidden()) {
					// do nothing
				} else {
					mFileLists.add(fileInfo);
				}
			}
		}
	}

	/**
	 * show delete confrim dialog
	 */
	public void showDeleteDialog(final List<Integer> posList) {
		// get name list
		List<FileInfo> fileList = mAdapter.getList();
		
		ZyDeleteDialog deleteDialog = new ZyDeleteDialog(getActivity());
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
				mDeletePosList = mAdapter.getSelectedItemsPos();
				
				mDeleteHelper.setDeletePathList(mAdapter.getSelectedFilePaths());
				mDeleteHelper.doDelete();
				
				dialog.dismiss();
				destroyMenuBar();
			}
		});
		deleteDialog.setNegativeButton(R.string.cancel, null);
		deleteDialog.show();
	}

	/**
	 * do Tranfer files
	 */
	public void doTransfer() {
		ArrayList<String> checkedList = (ArrayList<String>) mAdapter.getSelectedFilePaths();

		// send
		FileTransferUtil fileTransferUtil = new FileTransferUtil(getActivity());
		fileTransferUtil.sendFiles(checkedList, new TransportCallback() {

			@Override
			public void onTransportSuccess() {
				int first = mListView.getFirstVisiblePosition();
				int last = mListView.getLastVisiblePosition();
				List<Integer> checkedItems = mAdapter.getSelectedItemsPos();
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
					showTransportAnimation(icons.toArray(imageViews));
				}
			}

			@Override
			public void onTransportFail() {

			}
		});
	}

	public void addToNavigationList(String currentPath, int top, FileInfo selectFile) {
		mFileInfoManager.addToNavigationList(new NavigationRecord(currentPath, top, selectFile));
	}

	/** file path tab manager */
	protected class TabManager {
		private List<String> mTabNameList = new ArrayList<String>();
		protected LinearLayout mTabsHolder = null;
		private String curFilePath = null;
		private Button mBlankTab;

		public TabManager() {
			mTabsHolder = (LinearLayout) rootView.findViewById(R.id.tabs_holder);
			// 添加一个空的button，为了UI更美观
			mBlankTab = new Button(getActivity());
			mBlankTab.setBackgroundResource(R.drawable.fm_blank_tab);
			LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(new ViewGroup.MarginLayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

			mlp.setMargins((int) getResources().getDimension(R.dimen.tab_margin_left), 0,
					(int) getResources().getDimension(R.dimen.tab_margin_right), 0);
			mBlankTab.setLayoutParams(mlp);
			mTabsHolder.addView(mBlankTab);
		}

		protected void updateHomeButton(int type) {
			Button homeBtn = (Button) mTabsHolder.getChildAt(0);
			if (homeBtn == null) {
				Log.e(TAG, "HomeBtm is null,return.");
				return;
			}
			Resources resources = getResources();
			homeBtn.setBackgroundResource(R.drawable.custom_home_ninepatch_tab);
			homeBtn.setPadding((int) resources.getDimension(R.dimen.home_btn_padding), 0,
					(int) resources.getDimension(R.dimen.home_btn_padding), 0);
			homeBtn.setTextColor(Color.BLACK);
			switch (type) {
			case INTERNAL:
				homeBtn.setText(R.string.internal_sdcard);
				break;
			case SDCARD:
				homeBtn.setText(R.string.sdcard);
				break;
			default:
				break;
			}
		}

		public void refreshTab(String initFileInfo, int type) {
			Log.d(TAG, "refreshTab.initFileInfo:" + initFileInfo);
			int count = mTabsHolder.getChildCount();
			mTabsHolder.removeViews(0, count);
			mTabNameList.clear();

			curFilePath = initFileInfo;

			if (curFilePath != null) {
				String[] result = MountManager.getShowPath(mCurrent_root_path, curFilePath).split(MountManager.SEPERATOR);
				for (String string : result) {
					// add string to tab
					addTab(string);
				}
				startActionBarScroll();
			}

			updateHomeButton(type);
		}

		private void startActionBarScroll() {
			// scroll to right with slow-slide animation
			// To pass the Launch performance test, avoid the scroll
			// animation when launch.
			int tabHostCount = mTabsHolder.getChildCount();
			int navigationBarCount = mNavigationBar.getChildCount();
			if ((tabHostCount > 2) && (navigationBarCount >= 1)) {
				int width = mNavigationBar.getChildAt(navigationBarCount - 1).getRight();
				mNavigationBar.startHorizontalScroll(mNavigationBar.getScrollX(), width - mNavigationBar.getScrollX());
			}
		}

		/**
		 * This method updates the navigation view to the previous view when
		 * back button is pressed
		 * 
		 * @param newPath
		 *            the previous showed directory in the navigation history
		 */
		private void showPrevNavigationView(String newPath) {
			// refreshTab(newPath, storge_type);
			browserTo(new File(newPath));
		}

		/**
		 * This method creates tabs on the navigation bar
		 * 
		 * @param text
		 *            the name of the tab
		 */
		protected void addTab(String text) {
			LinearLayout.LayoutParams mlp = null;

			mTabsHolder.removeView(mBlankTab);
			View btn = null;
			if (mTabNameList.isEmpty()) {
				btn = new Button(mApplicationContext);
				mlp = new LinearLayout.LayoutParams(new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.MATCH_PARENT));
				mlp.setMargins(0, 0, 0, 0);
				btn.setLayoutParams(mlp);
			} else {
				btn = new Button(mApplicationContext);

				((Button) btn).setTextColor(getResources().getColor(R.drawable.path_selector));
				btn.setBackgroundResource(R.drawable.custom_tab);
				if (text.length() <= 10) {
					((Button) btn).setText(text);
				} else {
					String tabItemText = text.substring(0, 10 - 3) + "...";
					((Button) btn).setText(tabItemText);
				}
				mlp = new LinearLayout.LayoutParams(new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.MATCH_PARENT));
				mlp.setMargins((int) getResources().getDimension(R.dimen.tab_margin_left), 0, 0, 0);
				btn.setLayoutParams(mlp);
			}
			btn.setOnClickListener(FileBrowserFragment.this);
			btn.setId(mTabNameList.size());
			mTabsHolder.addView(btn);
			mTabNameList.add(text);

			// add blank tab to the tab holder
			mTabsHolder.addView(mBlankTab);
		}

		/**
		 * The method updates the navigation bar
		 * 
		 * @param id
		 *            the tab id that was clicked
		 */
		protected void updateNavigationBar(int id, int type) {
			Log.d(TAG, "updateNavigationBar,id = " + id);
			// click current button do not response
			if (id < mTabNameList.size() - 1) {
				if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
					destroyMenuBar();
				}
				int count = mTabNameList.size() - id;
				mTabsHolder.removeViews(id, count);

				for (int i = 1; i < count; i++) {
					// update mTabNameList
					mTabNameList.remove(mTabNameList.size() - 1);
				}
				// mTabsHolder.addView(mBlankTab);

				if (id == 0) {
					curFilePath = mCurrent_root_path;
				} else {
					String[] result = MountManager.getShowPath(mCurrent_root_path, curFilePath).split(MountManager.SEPERATOR);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i <= id; i++) {
						sb.append(MountManager.SEPERATOR);
						sb.append(result[i]);
					}
					curFilePath = mCurrent_root_path + sb.toString();
				}

				int top = -1;
				FileInfo selectedFileInfo = null;
				if (mListView.getCount() > 0) {
					View view = mListView.getChildAt(0);
					selectedFileInfo = mAdapter.getItem(mListView.getPositionForView(view));
					top = view.getTop();
				}
				addToNavigationList(mCurrentPath, top, selectedFileInfo);
				browserTo(new File(curFilePath));
				updateHomeButton(type);
			} else {
				// Refresh current page
				browserTo(new File(mCurrentPath));
			}
		}
		// end tab manager
	}

	public void doInternal() {
		storge_type = MountManager.INTERNAL;
		if (MountManager.NO_INTERNAL_SDCARD.equals(internal_path)) {
			// 没有外部&内部sdcard
			return;
		}
		mCurrent_root_path = internal_path;
		browserTo(new File(mCurrent_root_path));
	}

	public void doSdcard() {
		storge_type = MountManager.SDCARD;
		mCurrent_root_path = sdcard_path;
		if (mCurrent_root_path == null) {
			Log.e(TAG, "MountManager.SDCARD_PATH = null.");
			return;
		}
		browserTo(new File(mCurrent_root_path));
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (STATUS_HOME == mStatus) {
			return;
		}

		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_FLING:
//			Log.d(TAG, "SCROLL_STATE_FLING");
			mAdapter.setFlag(false);
			break;
		case OnScrollListener.SCROLL_STATE_IDLE:
//			Log.d(TAG, "SCROLL_STATE_IDLE");
			mAdapter.setFlag(true);
			mAdapter.notifyDataSetChanged();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//			Log.d(TAG, "SCROLL_STATE_TOUCH_SCROLL");
			mAdapter.setFlag(false);
			break;
		default:
			break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	public void updateUI(int num) {
		Message message = mHandler.obtainMessage();
		message.arg1 = num;
		message.what = MSG_UPDATE_UI;
		message.sendToTarget();
	}

	public void goToHome() {
		Log.d(TAG, "goToHome");
		mAllLists.clear();
		mNavBarLayout.setVisibility(View.GONE);

		mStatus = STATUS_HOME;
		updateUI(mHomeList.size());
		mListView.setAdapter(mHomeAdapter);
		mHomeAdapter.notifyDataSetChanged();
	}

	/**
	 * back key callback
	 */
	@Override
	public boolean onBackPressed() {
		Log.d(TAG, "onBackPressed.mStatus=" + mStatus);
		mIconHelper.stopLoader();
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		}

		switch (mStatus) {
		case STATUS_HOME:
			return true;
		case STATUS_FILE:
			// if is root path,back to Home view
			if (mCurrent_root_path.equals(mCurrentPath)) {
				goToHome();
			} else {
				NavigationRecord navRecord = mFileInfoManager.getPrevNavigation();
				String prevPath = null;
				if (null != navRecord) {
					prevPath = navRecord.getRecordPath();
					mSelectedFileInfo = navRecord.getSelectedFile();
					mTop = navRecord.getTop();
					if (null != prevPath) {
						mTabManager.showPrevNavigationView(prevPath);
						Log.d(TAG, "onBackPressed.prevPath=" + prevPath);
					}
				}
			}
			break;
		default:
			goToHome();
			break;
		}
		return false;
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send:
			doTransfer();
			destroyMenuBar();
			break;
		case R.id.menu_delete:
			List<Integer> posList = mAdapter.getSelectedItemsPos();
			showDeleteDialog(posList);
			break;
		case R.id.menu_select:
			doCheckAll();
			break;
		case R.id.menu_copy:
			mFileOperationHelper.copy(mAdapter.getSelectedFileInfos());
			mAdapter.changeMode(ActionMenu.MODE_COPY);
			startPasteMenu();
			break;
		case R.id.menu_cut:
			mFileOperationHelper.copy(mAdapter.getSelectedFileInfos());
			
			mAdapter.changeMode(ActionMenu.MODE_CUT);
			mAdapter.notifyDataSetChanged();
			startPasteMenu();
			break;
		case R.id.menu_paste:
			if (mAdapter.isMode(ActionMenu.MODE_COPY)) {
				onOperationPaste();
			}else if (mAdapter.isMode(ActionMenu.MODE_CUT)) {
				onOperationCut();
			}else {
				Log.e(TAG, "ACTION_MENU_PASTE.Error");
			}
			break;
		case R.id.menu_cancel:
			mFileOperationHelper.clear();
			destroyMenuBar();
			break;
		case R.id.menu_more:
			ActionMenu actionMenu = new ActionMenu(mApplicationContext);
			actionMenu.addItem(ActionMenu.ACTION_MENU_RENAME, R.drawable.ic_action_rename_enable, R.string.rename);
			actionMenu.addItem(ActionMenu.ACTION_MENU_INFO, R.drawable.ic_action_info_enable, R.string.menu_info);
			ZyPopupMenu popupMenu = new ZyPopupMenu(getActivity(), actionMenu);
			popupMenu.showAsLoaction(mMenuBarView, Gravity.RIGHT | Gravity.BOTTOM, 5, (int) mApplicationContext.getResources().getDimension(R.dimen.menubar_height));
			popupMenu.setOnPopupViewListener(new PopupViewClickListener() {
				@Override
				public void onActionMenuItemClick(ActionMenuItem item) {
					switch (item.getItemId()) {
					case ActionMenu.ACTION_MENU_RENAME:
						List<FileInfo> renameList = mAdapter.getSelectedFileInfos();
						mFileInfoManager.showRenameDialog(getActivity(), renameList);
						mAdapter.notifyDataSetChanged();
						destroyMenuBar();
						break;
					case ActionMenu.ACTION_MENU_INFO:
						List<FileInfo> list = mAdapter.getSelectedFileInfos();
						mFileInfoManager.showInfoDialog(getActivity(), list);
						break;

					default:
						break;
					}
				}
			});
			break;
		case R.id.menu_create_folder:
			final ZyEditDialog editDialog = new ZyEditDialog(getActivity());
			editDialog.setTitle(R.string.create_folder);
			editDialog.setEditDialogMsg(mApplicationContext.getString(R.string.folder_input));
			editDialog.setEditStr(mApplicationContext.getString(R.string.new_folder));
			editDialog.selectAll();
			editDialog.showIME(true);
			editDialog.setPositiveButton(R.string.ok, new OnZyAlertDlgClickListener() {
				@Override
				public void onClick(Dialog dialog) {
					String folderName = editDialog.getEditTextStr();
					String newPath = mCurrentPath + File.separator + folderName;
					File file = new File(newPath);
					if (file.exists()) {
						mNotice.showToast(R.string.folder_exist);
					}else {
						if (!file.mkdir()) {
							mNotice.showToast(R.string.folder_create_failed);
						}else {
							browserTo(new File(mCurrentPath));
						}
					}
					dialog.dismiss();
				}
			});
			editDialog.setNegativeButton(R.string.cancel, null);
			editDialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public void destroyMenuBar() {
		super.destroyMenuBar();
		
		updateTitleNum(-1);
		
		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.clearSelected();
		mAdapter.notifyDataSetChanged();
		mCopyList.clear();
	}

	@Override
	public void updateMenuBar() {
		int selectCount = mAdapter.getSelectedItems();
		updateTitleNum(selectCount);
		
		ActionMenuItem selectItem = mActionMenu.findItem(R.id.menu_select);
		if (mAdapter.getCount() == selectCount) {
			selectItem.setTitle(R.string.unselect_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_unselect);
		} else {
			selectItem.setTitle(R.string.select_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_select);
		}

		if (0 == selectCount) {
			mActionMenu.findItem(R.id.menu_send).setEnable(false);
			mActionMenu.findItem(R.id.menu_copy).setEnable(false);
			mActionMenu.findItem(R.id.menu_cut).setEnable(false);
			mActionMenu.findItem(R.id.menu_delete).setEnable(false);
			mActionMenu.findItem(R.id.menu_more).setEnable(false);
		} else if (mAdapter.hasDirSelected()) {
			mActionMenu.findItem(R.id.menu_send).setEnable(false);
			mActionMenu.findItem(R.id.menu_copy).setEnable(true);
			mActionMenu.findItem(R.id.menu_cut).setEnable(true);
			mActionMenu.findItem(R.id.menu_delete).setEnable(true);
			mActionMenu.findItem(R.id.menu_more).setEnable(true);
		}else {
			mActionMenu.findItem(R.id.menu_send).setEnable(true);
			mActionMenu.findItem(R.id.menu_copy).setEnable(true);
			mActionMenu.findItem(R.id.menu_cut).setEnable(true);
			mActionMenu.findItem(R.id.menu_delete).setEnable(true);
			mActionMenu.findItem(R.id.menu_more).setEnable(true);
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
	
	public void startPasteMenu(){
		mCopyList = mAdapter.getSelectedFileInfos();
		//update new menu
		mActionMenu = new ActionMenu(mApplicationContext);
		getActionMenuInflater().inflate(R.menu.allfile_menu_paste, mActionMenu);
		mMenuBarManager.refreshMenus(mActionMenu);
	}
	
	private void onOperationPaste(){
		showCopyDialog(mApplicationContext.getString(R.string.filecopy));
		if (!mFileOperationHelper.doCopy(mCurrentPath)) {
			onFinished();
		}
	}
	
	private void onOperationCut(){
		showCopyDialog(mApplicationContext.getString(R.string.filecut));
		if (!mFileOperationHelper.doCut(mCurrentPath)) {
			onFinished();
		}
	}
	
	private ProgressDialog progressDialog = null;
	public void showProgressDialog(String msg){
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(true);
		progressDialog.setMessage(msg);
		progressDialog.setIndeterminate(true);
		progressDialog.show();
	}
	
	private CopyMoveDialog mCopyDialog = null;
	public void showCopyDialog(String title){
		mCopyDialog = new CopyMoveDialog(getActivity());
		mCopyDialog.setTitle(title);
		mCopyDialog.setDesPath(mCurrentPath);
		mCopyDialog.setCancelable(false);
		mCopyDialog.setNegativeButton(R.string.cancel, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				mFileOperationHelper.stopCopy();
				dialog.dismiss();
				mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_OVER));
			}
		});
		mCopyDialog.show();
	}

	@Override
	public void onFinished() {
		Log.d(TAG, "onFinished");
		if (null != progressDialog) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		
		if (null != mCopyDialog) {
			mCopyDialog.dismiss();
			mCopyDialog = null;
		}
		mHandler.sendMessage(mHandler.obtainMessage(MSG_OPERATION_OVER));
	}
	
	@Override
	public void onNotify(int msg) {
		switch (msg) {
		case FileOperationHelper.MSG_COPY_CUT_TO_CHILD:
			Message message = mHandler.obtainMessage();
			message.obj = mApplicationContext.getString(R.string.copy_cut_fail);
			message.what = MSG_OPERATION_NOTIFY;
			message.sendToTarget();
			break;
		default:
			break;
		}
	}

	@Override
	public void onRefreshFiles(String fileName, int count, long filesize, long copysize) {
		if (null == fileName && count != -1) {
			mCopyDialog.setTotalCount(count);
		}else if(null != fileName && copysize == -1){
			mCopyDialog.updateCountProgress(fileName, count, filesize);
		}else if (null == fileName && copysize != -1) {
			mCopyDialog.updateSingleFileProgress(copysize);
		}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (null != mFileOperationHelper) {
			mFileOperationHelper.cancelOperationListener();
			mFileOperationHelper = null;
		}
		
		if (null != mDeleteHelper) {
			mDeleteHelper.cancelDeleteListener();
			mDeleteHelper = null;
		}
	}

	@Override
	public void onDeleteFinished() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDeleteFinished");
		//when delete over,send message to update ui
		Message message = mHandler.obtainMessage();
		Bundle bundle = new Bundle();
		bundle.putIntegerArrayList("position", (ArrayList<Integer>)mDeletePosList);
		message.setData(bundle);
		message.what = MSG_UPDATE_LIST;
		message.sendToTarget();
	}
}
