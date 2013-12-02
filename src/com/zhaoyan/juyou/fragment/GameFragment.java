package com.zhaoyan.juyou.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.AppCursorAdapter;
import com.zhaoyan.juyou.adapter.AppCursorAdapter.ViewHolder;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.AppManager;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.MenuTabManager;
import com.zhaoyan.juyou.common.MenuTabManager.onMenuItemClickListener;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.dialog.AppDialog;
import com.zhaoyan.juyou.provider.AppData;

/**
 * use this to load app
 */
public class GameFragment extends AppBaseFragment implements OnItemClickListener, OnItemLongClickListener, onMenuItemClickListener {
	private static final String TAG = "GameFragment";
	
	private QueryHandler mQueryHandler;
	
	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_UPDATE_LIST= 1;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int size = msg.arg1;
				count  = size;
				updateTitleNum(-1);
				break;
			case MSG_UPDATE_LIST:
				Intent intent = new Intent(AppManager.ACTION_REFRESH_APP);
				mContext.sendBroadcast(intent);
				break;

			default:
				break;
			}
		};
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAppId = getArguments() != null ? getArguments().getInt(Extra.APP_ID) : 1;
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
		View rootView = inflater.inflate(R.layout.app_main, container, false);
		
		mGridView = (GridView) rootView.findViewById(R.id.app_normal_gridview);
		mLoadingBar = (ProgressBar) rootView.findViewById(R.id.app_progressbar);
		
		mMenuBottomView = rootView.findViewById(R.id.menubar_bottom);
		mMenuBottomView.setVisibility(View.GONE);
		mMenuHolder = (LinearLayout) rootView.findViewById(R.id.ll_menutabs_holder);
		
		initTitle(rootView.findViewById(R.id.rl_ui_app), R.string.game);
		
		mQueryHandler = new QueryHandler(getActivity().getContentResolver());

		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mAdapter = new AppCursorAdapter(mContext);
		query();
		super.onActivityCreated(savedInstanceState);
	}
	
	private static final String[] PROJECTION = {
		AppData.App._ID,AppData.App.PKG_NAME
	};
	
	public void query(){
		mLoadingBar.setVisibility(View.VISIBLE);
		//查询类型为游戏的所有数据
		String selectionString = AppData.App.TYPE + "=?" ;
    	String args[] = {"" + AppManager.GAME_APP};
		mQueryHandler.startQuery(11, null, AppData.App.CONTENT_URI, PROJECTION, selectionString, args, AppData.App.SORT_ORDER_LABEL);
	}
	
	//query db
	private class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// super.onQueryComplete(token, cookie, cursor);
			Log.d(TAG, "onQueryComplete");
			mLoadingBar.setVisibility(View.INVISIBLE);
			Message message = mHandler.obtainMessage();
			if (null != cursor && cursor.getCount() > 0) {
				Log.d(TAG, "onQueryComplete.count=" + cursor.getCount());
				mAdapter.changeCursor(cursor);
				mGridView.setAdapter(mAdapter);
				mAdapter.selectAll(false);
				message.arg1 = cursor.getCount();
			} else {
				message.arg1 = 0;
			}

			message.what = MSG_UPDATE_UI;
			message.sendToTarget();
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter.getMode() == ActionMenu.MODE_EDIT) {
			mAdapter.setSelected(position);
			mAdapter.notifyDataSetChanged();
			
			int selectedCount = mAdapter.getSelectedItemsCount();
			updateTitleNum(selectedCount);
			updateMenuBar();
			mMenuManager.refreshMenus(mActionMenu);
		}else {
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(position);
			String packagename = cursor.getString(cursor.getColumnIndex(AppData.App.PKG_NAME));
			if (ZYConstant.PACKAGE_NAME.equals(packagename)) {
				mNotice.showToast(R.string.app_has_started);
				return;
			}
			
			Intent intent = pm.getLaunchIntentForPackage(packagename);
			if (null != intent) {
				startActivity(intent);
			}else {
				mNotice.showToast(R.string.cannot_start_app);
				return;
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		int mode = mAdapter.getMode();
		if (ActionMenu.MODE_EDIT == mode) {
			doSelectAll();
			return true;
		}else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1);
		}
		boolean isSelected = mAdapter.isSelected(position);
		mAdapter.setSelected(position, !isSelected);
		mAdapter.notifyDataSetChanged();
		
		mActionMenu = new ActionMenu(getActivity().getApplicationContext());
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SEND, R.drawable.ic_action_send, R.string.menu_send);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_BACKUP, R.drawable.ic_action_backup, R.string.menu_backup);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_UNINSTALL,R.drawable.ic_aciton_uninstall,R.string.menu_uninstall);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_MOVE_TO_APP,R.drawable.ic_action_move_app,R.string.menu_move_to_app);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_INFO,R.drawable.ic_action_app_info,R.string.menu_app_info);
		mActionMenu.addItem(ActionMenu.ACTION_MENU_SELECT, R.drawable.ic_aciton_select, R.string.select_all);

		mMenuManager = new MenuTabManager(mContext, mMenuHolder);
		showMenuBar(true);
		mMenuManager.refreshMenus(mActionMenu);
		mMenuManager.setOnMenuItemClickListener(this);
		return true;
	}
    
    public void notifyUpdateUI(){
		Message message = mHandler.obtainMessage();
		message.arg1 = mAdapter.getCount();
		message.what = MSG_UPDATE_UI;
		message.sendToTarget();
	}
    
	@Override
	public void onDestroyView() {
		if (mAdapter != null && mAdapter.getCursor() != null) {
			mAdapter.getCursor().close();
			mAdapter.changeCursor(null);
		}
		super.onDestroyView();
	}

	public void reQuery(){
		if (null == mAdapter || mAdapter.getCursor() == null) {
			query();
		}else {
			mAdapter.getCursor().requery();
			notifyUpdateUI();
		}
	}
	
	public boolean onBackPressed(){
		if (null != mAdapter && mAdapter.getMode() == ActionMenu.MODE_EDIT) {
			showMenuBar(false);
			return false;
		}
		return true;
	}

	@Override
	public void onMenuClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case ActionMenu.ACTION_MENU_SEND:
			ArrayList<String> selectedList = (ArrayList<String>) mAdapter.getSelectItemPathList();
			//send
			FileTransferUtil fileTransferUtil = new FileTransferUtil(getActivity());
			fileTransferUtil.sendFiles(selectedList, new TransportCallback() {
				@Override
				public void onTransportSuccess() {
					int first = mGridView.getFirstVisiblePosition();
					int last = mGridView.getLastVisiblePosition();
					List<Integer> checkedItems = mAdapter.getSelectedItemPos();
					ArrayList<ImageView> icons = new ArrayList<ImageView>();
					for(int id : checkedItems) {
						if (id >= first && id <= last) {
							View view = mGridView.getChildAt(id - first);
							if (view != null) {
								ViewHolder viewHolder = (ViewHolder)view.getTag();
								icons.add(viewHolder.iconView);
							}
						}
					}
//					
					if (icons.size() > 0) {
						ImageView[] imageViews = new ImageView[0];
						showTransportAnimation(icons.toArray(imageViews));
					}
				}
				
				@Override
				public void onTransportFail() {
				}
			});
			showMenuBar(false);
			break;
		case ActionMenu.ACTION_MENU_UNINSTALL:
			mUninstallList = mAdapter.getSelectedPkgList();
			showUninstallDialog();
			uninstallApp();
			showMenuBar(false);
			break;
		case ActionMenu.ACTION_MENU_MOVE_TO_APP:
			showMoveDialog();
			break;
		case ActionMenu.ACTION_MENU_INFO:
			String packageName = mAdapter.getSelectedPkgList().get(0);
			AppManager.showInstalledAppDetails(getActivity().getApplicationContext(), packageName);
			showMenuBar(false);
			break;
		case ActionMenu.ACTION_MENU_SELECT:
			doSelectAll();
			break;
		case ActionMenu.ACTION_MENU_BACKUP:
			List<String> backupList = mAdapter.getSelectedPkgList();
			showBackupDialog(backupList);
			showMenuBar(false);
			break;

		default:
			break;
		}
	}
	
	public void showMoveDialog(){
		List<String> packageList = mAdapter.getSelectedPkgList();
		new MoveAsyncTask(packageList).execute();
		showMenuBar(false);
	}
	
	private class MoveAsyncTask extends AsyncTask<Void, Void, Void>{
		List<String> pkgList = new ArrayList<String>();
		AppDialog dialog = null;
		
		MoveAsyncTask(List<String> list){
			pkgList = list;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (null == dialog) {
				dialog = new AppDialog(mContext, pkgList.size());
				dialog.setTitle(R.string.handling);
				dialog.show();
			}
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			String label = null;
			for (int i = 0; i < pkgList.size(); i++) {
				label = AppManager.getAppLabel(pkgList.get(i), pm);
				dialog.updateUI(i + 1, label);
				moveToApp(pkgList.get(i));
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (null != dialog) {
				dialog.cancel();
				dialog = null;
			}
			notifyUpdateUI();
		}
	}
	
	private void moveToApp(String packageName){
//		Log.d(TAG, "moveToApp:" + packageName);
		//move to app
		//1，删除game表中的数据
		//2，将app表中的type改为app
		//3，通知AppFragment
		//4，重新查询数据库
		ContentResolver contentResolver = getActivity().getContentResolver();
		Uri uri = Uri.parse(AppData.AppGame.CONTENT_URI + "/" + packageName);
		contentResolver.delete(uri, null, null);
		
		//update db
		ContentValues values = new ContentValues();
		values.put(AppData.App.TYPE, AppManager.NORMAL_APP);
		contentResolver.update(AppData.App.CONTENT_URI, values, 
				AppData.App.PKG_NAME + "='" + packageName + "'", null);
	}
	
	/**
	 * do select all items or unselect all items
	 */
	public void doSelectAll(){
		int selectedCount1 = mAdapter.getSelectedItemsCount();
		if (mAdapter.getCount() != selectedCount1) {
			mAdapter.selectAll(true);
		} else {
			mAdapter.selectAll(false);
		}
		updateMenuBar();
		mMenuManager.refreshMenus(mActionMenu);
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * set menubar visible or gone
	 * @param show
	 */
	public void showMenuBar(boolean show){
		if (show) {
			mMenuBottomView.setVisibility(View.VISIBLE);
		}else {
			mMenuBottomView.setVisibility(View.GONE);
			updateTitleNum(-1);
			onActionMenuDone();
		}
	}
	
	
	public void onActionMenuDone() {
		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.selectAll(false);
		mAdapter.notifyDataSetChanged();
	}
	
	/**
	 * update menu bar item icon and text color,enable or disable
	 */
	public void updateMenuBar(){
		int selectCount = mAdapter.getSelectedItemsCount();
		updateTitleNum(selectCount);
		
		if (mAdapter.getCount() == selectCount) {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SELECT).setTitle(R.string.unselect_all);
		}else {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SELECT).setTitle(R.string.select_all);
		}
		
		if (0==selectCount) {
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(false);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_BACKUP).setEnable(false);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_UNINSTALL).setEnable(false);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_MOVE_TO_APP).setEnable(false);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_INFO).setEnable(false);
		} else if (1 == selectCount) {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_BACKUP).setEnable(true);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_UNINSTALL).setEnable(true);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_MOVE_TO_APP).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_INFO).setEnable(true);
		} else {
			mActionMenu.findItem(ActionMenu.ACTION_MENU_SEND).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_BACKUP).setEnable(true);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_UNINSTALL).setEnable(true);
        	mActionMenu.findItem(ActionMenu.ACTION_MENU_MOVE_TO_APP).setEnable(true);
			mActionMenu.findItem(ActionMenu.ACTION_MENU_INFO).setEnable(false);
		}
	}
	
	@Override
	public int getSelectedCount() {
		if (null != mAdapter) {
			return mAdapter.getSelectedItemsCount();
		}
		return super.getSelectedCount();
	}
	
	@Override
	public int getMenuMode() {
		if (null != mAdapter) {
			return mAdapter.getMode();
		}
		return super.getMenuMode();
	}
}
