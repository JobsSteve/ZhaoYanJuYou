package com.zhaoyan.juyou.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.IntentBuilder;
import com.zhaoyan.common.util.Log;
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
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.common.FileInfoManager.NavigationRecord;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.MenuTabManager;
import com.zhaoyan.juyou.common.MenuTabManager.onMenuItemClickListener;
import com.zhaoyan.juyou.common.MountManager;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.dialog.ZyAlertDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;
import com.zhaoyan.juyou.dialog.DeleteDialog;
import com.zhaoyan.juyou.dialog.DeleteDialog.OnDelClickListener;

public class FileBrowserFragment extends BaseFragment implements OnClickListener, OnItemClickListener, OnScrollListener,
		OnItemLongClickListener, onMenuItemClickListener {
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
	private MountManager mountManager;
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

	public static final int INTERNAL = MountManager.INTERNAL;
	public static final int SDCARD = MountManager.SDCARD;
	private static final int STATUS_FILE = 0;
	private static final int STATUS_HOME = 1;
	private int mStatus = STATUS_HOME;

	private Context mContext;

	/**
	 * current dir path
	 */
	private String mCurrentPath;
	/**
	 * cut files'path
	 * 剪切文件所在的目录
	 */
	private String mCutPath;

	// context menu
	// save current sdcard type
	private static int storge_type = -1;
	// save current sdcard type path
	private String mCurrent_root_path;

	private SharedPreferences sp = null;

	private String sdcard_path;
	private String internal_path;

	private ActionMenu mActionMenu;
	private MenuTabManager mMenuTabManager;
	private LinearLayout mMenuHolder;
	private View mMenuBarView;
	
	private Comparator<FileInfo> NAME_COMPARATOR = FileInfo.getNameComparator();

	private DeleteDialog mDeleteDialog;

	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_UPDATE_LIST = 2;
	private static final int MSG_UPDATE_HOME = 3;
	private static final int MSG_UPDATE_FILE = 4;
	private static final int MSG_REFRESH = 5;
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
				List<FileInfo> fileList = mAdapter.getList();
				fileList.remove(msg.arg1);
				mAdapter.notifyDataSetChanged();
				updateUI(fileList.size());
				break;
			case MSG_UPDATE_HOME:
				mHomeAdapter.notifyDataSetChanged();
				break;
			case MSG_REFRESH:
				browserTo(new File(mCurrentPath));
				break;
			default:
				break;
			}
		};
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		mContext = getActivity().getApplicationContext();

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

		mMenuHolder = (LinearLayout) rootView.findViewById(R.id.ll_menutabs_holder);
		mMenuBarView = rootView.findViewById(R.id.bottom);
		mMenuBarView.setVisibility(View.GONE);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		sp = mContext.getSharedPreferences(Extra.SHARED_PERFERENCE_NAME, Context.MODE_PRIVATE);

		mFileInfoManager = new FileInfoManager();
		mountManager = new MountManager(mContext);

		sdcard_path = sp.getString(Extra.SDCARD_PATH, MountManager.NO_EXTERNAL_SDCARD);
		internal_path = sp.getString(Extra.INTERNAL_PATH, MountManager.NO_INTERNAL_SDCARD);
		Log.d(TAG, "sdcard_path:" + sdcard_path + "\n," + "internal_path:" + internal_path);

		mHomeList.clear();
		// init
		if (!MountManager.NO_INTERNAL_SDCARD.equals(internal_path)) {
			mHomeList.add(INTERNAL);
		}

		if (!MountManager.NO_EXTERNAL_SDCARD.equals(sdcard_path)) {
			mHomeList.add(SDCARD);
		}

		mHomeAdapter = new FileHomeAdapter(mContext, mHomeList);
		mIconHelper = new FileIconHelper(mContext);
		mAdapter = new FileInfoAdapter(mContext, mAllLists, mIconHelper);

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
			showMenuBar(false);
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
				mMenuTabManager.refreshMenus(mActionMenu);
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
			doSelectAll();
			return true;
		} else if (mAdapter.isMode(ActionMenu.MODE_COPY)
				|| mAdapter.isMode(ActionMenu.MODE_CUT)) {
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
		}
		
		boolean isSelected = mAdapter.isSelected(position);
		mAdapter.setSelected(position, !isSelected);
		mAdapter.notifyDataSetChanged();

		mActionMenu = new ActionMenu(mContext);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SEND, R.drawable.ic_action_send, R.string.menu_send);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_COPY, R.drawable.ic_action_copy, R.string.copy);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_CUT, R.drawable.ic_action_cut, R.string.cut);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_DELETE, R.drawable.ic_action_delete_enable, R.string.menu_delete);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SELECT, R.drawable.ic_aciton_select, R.string.select_all);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_MORE, R.drawable.ic_action_overflow, R.string.menu_more);

		mMenuTabManager = new MenuTabManager(mContext, mMenuHolder);
		showMenuBar(true);
		if (mAllLists.get(position).isDir) {
			//we do not support send folder
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(false);
		}
		mMenuTabManager.refreshMenus(mActionMenu);
		mMenuTabManager.setOnMenuItemClickListener(this);
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

			if (currentFile.isDirectory()) {
				fileInfo = new FileInfo(currentFile.getName());
				fileInfo.fileDate = currentFile.lastModified();
				fileInfo.filePath = currentFile.getAbsolutePath();
				fileInfo.isDir = true;
				fileInfo.fileSize = 0;
				fileInfo.icon = getResources().getDrawable(R.drawable.icon_folder);
				fileInfo.type = FileInfoManager.UNKNOW;
				if (currentFile.isHidden()) {
					// do nothing
				} else {
					mFolderLists.add(fileInfo);
				}
			} else {
				fileInfo = mFileInfoManager.getFileInfo(mContext, currentFile);
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
		List<String> nameList = new ArrayList<String>();
		List<FileInfo> fileList = mAdapter.getList();
		for (int position : posList) {
			nameList.add(fileList.get(position).fileName);
		}

		mDeleteDialog = new DeleteDialog(getActivity(), nameList);
		mDeleteDialog.setButton(AlertDialog.BUTTON_POSITIVE, R.string.menu_delete, new OnDelClickListener() {
			@Override
			public void onClick(View view, String path) {
				showMenuBar(false);
				DeleteTask deleteTask = new DeleteTask(posList);
				deleteTask.execute();
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
				doDelete(mContext, deleteList.get(i));
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

	/**
	 * do delete file</br> if file is media file(image,audio,video),need delete
	 * in db
	 * 
	 * @param path
	 * @param type
	 */
	public void doDelete(Context context, File file) {
		if (file.isFile()) {
			int type = mFileInfoManager.fileFilter(context, file.getAbsolutePath());
			Log.d(TAG, "doDelete.type:" + type);
			switch (type) {
			case FileInfoManager.IMAGE:
				mFileInfoManager.deleteFileInMediaStore(context, ZYConstant.IMAGE_URI, file.getAbsolutePath());
				break;
			case FileInfoManager.AUDIO:
				mFileInfoManager.deleteFileInMediaStore(context, ZYConstant.AUDIO_URI, file.getAbsolutePath());
				break;
			case FileInfoManager.VIDEO:
				mFileInfoManager.deleteFileInMediaStore(context, ZYConstant.VIDEO_URI, file.getAbsolutePath());
				break;
			default:
				// 普通文件直接删除，不删除数据库，因为在3.0以前，还没有普通文件的数据哭
				file.delete();
				break;
			}
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (null == childFiles || 0 == childFiles.length) {
				file.delete();
				return;
			}
			for (File childFile : childFiles) {
				doDelete(context, childFile);
			}

			file.delete();
		}
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
				String[] result = mountManager.getShowPath(mCurrent_root_path, curFilePath).split(MountManager.SEPERATOR);
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
				btn = new Button(mContext);
				mlp = new LinearLayout.LayoutParams(new ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.MATCH_PARENT));
				mlp.setMargins(0, 0, 0, 0);
				btn.setLayoutParams(mlp);
			} else {
				btn = new Button(mContext);

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
					showMenuBar(false);
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
					String[] result = mountManager.getShowPath(mCurrent_root_path, curFilePath).split(MountManager.SEPERATOR);
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
			Log.d(TAG, "SCROLL_STATE_FLING");
			mAdapter.setFlag(false);
			break;
		case OnScrollListener.SCROLL_STATE_IDLE:
			Log.d(TAG, "SCROLL_STATE_IDLE");
			mAdapter.setFlag(true);
			mAdapter.notifyDataSetChanged();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			Log.d(TAG, "SCROLL_STATE_TOUCH_SCROLL");
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
			showMenuBar(false);
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
		case ActionMenu.ACTION_MENU_SELECT:
			doSelectAll();
			break;
		case ActionMenu.ACTION_MENU_COPY:
			mAdapter.changeMode(ActionMenu.MODE_COPY);
			startPasteMenu();
			break;
		case ActionMenu.ACTION_MENU_CUT:
			mCutPath = mCurrentPath;
			mAdapter.changeMode(ActionMenu.MODE_CUT);
			mAdapter.notifyDataSetChanged();
			startPasteMenu();
			break;
		case ActionMenu.ACTION_MENU_PASTE:
			Log.d(TAG, "ACTION_MENU_PASTE.mCutPath:" + mCutPath);
//			if (null != mCutPath && !"".equals(mCutPath)) {
//				if (mCurrentPath.equals(mCutPath)) {
//					//do nothing
//				} else if (mCurrentPath.contains(mCutPath)) {
//					ZyAlertDialog dialog = new ZyAlertDialog(getActivity());
//					dialog.setTitle(R.string.cut_fail);
//					dialog.setMessage(R.string.cut_fail_msg_one);
//					dialog.setPositiveButton(R.string.ok, null);
//					dialog.setCancelable(true);
//					dialog.show();
//					showMenuBar(false);
//					break;
//				}
//			}
			new CopyCutTask().execute();
			break;
		case ActionMenu.ACTION_MENU_CANCEL:
			showMenuBar(false);
			break;
		case ActionMenu.ACTION_MENU_MORE:
			ActionMenu actionMenu = new ActionMenu(mContext);
			actionMenu.addItem(ActionMenu.ACTION_MENU_RENAME, R.drawable.ic_action_rename, R.string.rename);
			actionMenu.addItem(ActionMenu.ACTION_MENU_INFO, R.drawable.ic_action_info, R.string.menu_info);
			ZyPopupMenu popupMenu = new ZyPopupMenu(getActivity(), actionMenu);
			popupMenu.showAsLoaction(mMenuBarView, Gravity.RIGHT | Gravity.BOTTOM, 5, (int) mContext.getResources().getDimension(R.dimen.menubar_height));
			popupMenu.setOnPopupViewListener(new PopupViewClickListener() {
				@Override
				public void onActionMenuItemClick(ActionMenuItem item) {
					switch (item.getItemId()) {
					case ActionMenu.ACTION_MENU_RENAME:
						List<FileInfo> renameList = mAdapter.getSelectedFileInfos();
						mFileInfoManager.showRenameDialog(getActivity(), renameList);
						mAdapter.notifyDataSetChanged();
						showMenuBar(false);
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
		case ActionMenu.ACTION_MENU_CREATE_FOLDER:
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View view = inflater.inflate(R.layout.dialog_rename, null);
			final EditText editText = (EditText) view.findViewById(R.id.et_rename);
			ZyAlertDialog dialog = new ZyAlertDialog(getActivity());
			dialog.setTitle(R.string.create_folder);
			dialog.setMessage(R.string.folder_input);
			dialog.setContentView(view);
			dialog.setNegativeButton(R.string.cancel, null);
			dialog.setPositiveButton(R.string.ok, new OnZyAlertDlgClickListener() {
				@Override
				public void onClick(Dialog dialog) {
					String folderName = editText.getText().toString();
					String newPath = mCurrentPath + File.separator + folderName;
					File file = new File(newPath);
					if (file.exists()) {
						mNotice.showToast(R.string.folder_exist);
					}else {
						if (!file.mkdir()) {
							mNotice.showToast(R.string.folder_create_failed);
						}else {
							browserTo(file);
						}
					}
					dialog.dismiss();
				}
			});
			dialog.show();
			showMenuBar(false);
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
			updateTitleNum(-1);
		}
	}

	/**
	 * update menu bar item icon and text color,enable or disable
	 */
	public void updateMenuBar() {
		int selectCount = mAdapter.getSelectedItems();
		updateTitleNum(selectCount);

		if (mAdapter.getCount() == selectCount) {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SELECT).setTitle(R.string.unselect_all);
		} else {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SELECT).setTitle(R.string.select_all);
		}

		if (0 == selectCount) {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(false);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_COPY).setEnable(false);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_CUT).setEnable(false);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_DELETE).setEnable(false);
		} else if (mAdapter.hasDirSelected()) {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(false);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_COPY).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_CUT).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_DELETE).setEnable(true);
		}else {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_COPY).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_CUT).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_DELETE).setEnable(true);
		}
	}

	// Cancle Action menu
	public void onActionMenuDone() {
		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.clearSelected();
		mAdapter.notifyDataSetChanged();
		mCopyList.clear();
		mCutPath = null;
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
	
	public void startPasteMenu(){
		mCopyList = mAdapter.getSelectedFileInfos();
		//update new menu
		mActionMenu = new ActionMenu(mContext);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_CREATE_FOLDER, R.drawable.ic_action_createfolder, R.string.create_folder);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_PASTE, R.drawable.ic_action_paste, R.string.paste);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_CANCEL, R.drawable.ic_action_cancel, R.string.cancel);
		mMenuTabManager.refreshMenus(mActionMenu);
	}
	
	//Copy and Cut Task
	class CopyCutTask extends AsyncTask<String, Object, Object>{
		ProgressDialog dialog = null;
		String cutFailFolder = "";

		@Override
		protected Object doInBackground(String... params) {
			//copy
			String srcPath = "";
			String desPath = "";
			String fileName = "";
			if (mAdapter.isMode(ActionMenu.MODE_COPY)) {
				for (FileInfo fileInfo : mCopyList) {
					srcPath = fileInfo.filePath;
					fileName = fileInfo.fileName;
					desPath = mCurrentPath + File.separator + fileName;
					//if desFile is exist,auto rename
					if (new File(desPath).exists()) {
						fileName = FileInfoManager.autoRename(fileName);
						desPath = mCurrentPath + File.separator + fileName;
					}
					
					if (fileInfo.isDir) {
						FileManager.copyFolder(srcPath, desPath);
					} else {
						FileManager.copyFile(srcPath, desPath);
					}
				}
			}else {
				//cut
				File file = null;
				for(FileInfo fileInfo : mCopyList){
					srcPath = fileInfo.filePath;
					fileName = fileInfo.fileName;
					desPath = mCurrentPath + File.separator + fileName;
					
					if (new File(desPath).exists()) {
						//if desFile is exist,break
						break;
					}
					
					if (mCurrentPath.equals(srcPath)) {
						cutFailFolder = fileName;
					}else {
						if (fileInfo.isDir) {
							FileManager.copyFolder(srcPath, desPath);
						}else {
							FileManager.copyFile(srcPath, desPath);
						}
						
						//cut over ,delete src file
						file = new File(srcPath);
						doDelete(mContext, file);
					}
				}
			}
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(getActivity());
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setProgressDrawable(mContext.getResources().getDrawable(R.drawable.loading));
			if (mAdapter.isMode(ActionMenu.MODE_COPY)) {
				dialog.setMessage(mContext.getResources().getString(R.string.copying));
			}else {
				dialog.setMessage(mContext.getResources().getString(R.string.cuting));
			}
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			Log.d(TAG, "CopyTask.onPostExecut");
			showMenuBar(false);
			if (null != dialog) {
				dialog.cancel();
				dialog = null;
			}
			mHandler.sendMessage(mHandler.obtainMessage(MSG_REFRESH));
			//if have a folder cut fail,show a dialog to user
			if (null != cutFailFolder && !"".equals(cutFailFolder)) {
				ZyAlertDialog dialog = new ZyAlertDialog(getActivity());
				dialog.setTitle(mContext.getString(R.string.cut_fail_format, cutFailFolder));
				dialog.setMessage(R.string.cut_fail_msg_one);
				dialog.setPositiveButton(R.string.ok, null);
				dialog.setCancelable(true);
				dialog.show();
			}
		}
		
	}
	//copy & cut
}
