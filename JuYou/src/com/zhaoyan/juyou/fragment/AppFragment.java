package com.zhaoyan.juyou.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AppActivity;
import com.zhaoyan.juyou.adapter.AppCursorAdapter;
import com.zhaoyan.juyou.adapter.AppCursorAdapter.ViewHolder;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.AppManager;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.MenuBarInterface;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.dialog.AppDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;
import com.zhaoyan.juyou.notification.NotificationMgr;
import com.zhaoyan.juyou.provider.AppData;

/**
 * use this to load app
 */
public class AppFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener,
		 MenuBarInterface {
	private static final String TAG = "AppFragment";
	
	private GridView mGridView;
	private ProgressBar mLoadingBar;

	private AppCursorAdapter mAdapter = null;
	
	private AppDialog mAppDialog = null;
	private List<String> mUninstallList = null;
	private PackageManager pm = null;
	private Notice mNotice = null;
	private static final int REQUEST_CODE_UNINSTALL = 0x101;
	
	private QueryHandler mQueryHandler;
	
	private int mType = -1;
	
	private NotificationMgr mNotificationMgr;
	private boolean mIsBackupHide = false;
	
	private static final int MSG_TOAST = 0;
	private static final int MSG_BACKUPING = 1;
	private static final int MSG_BACKUP_OVER = 3;
	private static final int MSG_UPDATE_UI = 4;
	private static final int MSG_UPDATE_LIST= 5;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int size = msg.arg1;
				count = size;
				updateTitleNum(-1);
				break;
			case MSG_UPDATE_LIST:
				Intent intent = new Intent(AppManager.ACTION_REFRESH_APP);
				mContext.sendBroadcast(intent);
				break;
			case MSG_TOAST:
				String message = (String) msg.obj;
				mNotice.showToast(message);
				break;
			case MSG_BACKUPING:
				int progress = msg.arg1;
				int max = msg.arg2;
				String name = (String) msg.obj;
				mNotificationMgr.updateBackupNotification(progress, max, name);
				break;
			case MSG_BACKUP_OVER:
				long duration = (Long) msg.obj;
				mNotificationMgr.appBackupOver(duration);
				break;
			default:
				break;
			}
		};
	};
	
	public static AppFragment newInstance(int type){
		AppFragment f = new AppFragment();
		Bundle args = new Bundle();
		args.putInt(AppActivity.APP_TYPE, type);
		f.setArguments(args);
		
		return f;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mType = getArguments() != null ? getArguments().getInt(AppActivity.APP_TYPE) : AppActivity.TYPE_APP;
		if (null != savedInstanceState) {
			mType = savedInstanceState.getInt(AppActivity.APP_TYPE);
		}
		
		mNotice = new Notice(getActivity().getApplicationContext());
		pm = getActivity().getPackageManager();
		
		mNotificationMgr = new NotificationMgr(getActivity().getApplicationContext());
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt(AppActivity.APP_TYPE, mType);
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
		
		if (isGameUI()) {
			initTitle(rootView.findViewById(R.id.rl_ui_app), R.string.game);
		}else {
			initTitle(rootView.findViewById(R.id.rl_ui_app), R.string.app);
		}
		
		initMenuBar(rootView);
		
		mQueryHandler = new QueryHandler(getActivity().getApplicationContext().getContentResolver());

		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mAdapter = new AppCursorAdapter(getActivity().getApplicationContext());
		query();
		super.onActivityCreated(savedInstanceState);
	}
	
	private static final String[] PROJECTION = {
		AppData.App._ID,AppData.App.PKG_NAME
	};
	
	public void query(){
		mLoadingBar.setVisibility(View.VISIBLE);
		//查询类型为应用的所有数据
		String selectionString = AppData.App.TYPE + "=?" ;
		String[] args = new String[1];
		if (isGameUI()) {
			args[0] = "" + AppManager.GAME_APP;
		}else {
			args[0] = "" + AppManager.NORMAL_APP;
		}
		
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
			Log.d(TAG, "onQueryComplete");
			mLoadingBar.setVisibility(View.INVISIBLE);
			Message message = mHandler.obtainMessage();
			if (null != cursor && cursor.getCount() > 0) {
				Log.d(TAG, "onQueryComplete.count=" + cursor.getCount());
				mAdapter.changeCursor(cursor);
				mGridView.setAdapter(mAdapter);
				mAdapter.checkedAll(false);
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
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			mAdapter.setChecked(position);
			mAdapter.notifyDataSetChanged();
			
			updateMenuBar();
		} else {
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(position);
			String packagename = cursor.getString(cursor
					.getColumnIndex(AppData.App.PKG_NAME));
			if (ZYConstant.PACKAGE_NAME.equals(packagename)) {
				mNotice.showToast(R.string.app_has_started);
				return;
			}

			Intent intent = pm.getLaunchIntentForPackage(packagename);
			if (null != intent) {
				startActivity(intent);
			} else {
				mNotice.showToast(R.string.cannot_start_app);
				return;
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			//do nothing
			//doCheckAll();
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1);
		}
		
		mAdapter.setChecked(position);
		mAdapter.notifyDataSetChanged();
		
		mActionMenu = new ActionMenu(getActivity().getApplicationContext());
		if (isGameUI()) {
			getActionMenuInflater().inflate(R.menu.game_menu, mActionMenu);
		}else {
			getActionMenuInflater().inflate(R.menu.app_menu, mActionMenu);
		}
		
		startMenuBar();
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

	public void reQuery() {
		if (null == mAdapter || mAdapter.getCursor() == null) {
			query();
		} else {
			mAdapter.getCursor().requery();
			notifyUpdateUI();
		}
	}
	
	public boolean onBackPressed(){
		if (null != mAdapter && mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		}
		return true;
	}

	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send:
			ArrayList<String> selectedList = (ArrayList<String>) mAdapter.getCheckedPathList();
			//send
			FileTransferUtil fileTransferUtil = new FileTransferUtil(getActivity());
			fileTransferUtil.sendFiles(selectedList, new TransportCallback() {
				@Override
				public void onTransportSuccess() {
					int first = mGridView.getFirstVisiblePosition();
					int last = mGridView.getLastVisiblePosition();
					List<Integer> checkedItems = mAdapter.getCheckedPosList();
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
					
					destroyMenuBar();
				}
				
				@Override
				public void onTransportFail() {
				}
			});
			break;
		case R.id.menu_uninstall:
			mUninstallList = mAdapter.getCheckedPkgList();
			showUninstallDialog();
			uninstallApp();
			destroyMenuBar();
			break;
		case R.id.menu_move_app:
			showMoveDialog();
			break;
		case R.id.menu_app_info:
			String packageName = mAdapter.getCheckedPkgList().get(0);
			showInstalledAppDetails(packageName);
			destroyMenuBar();
			break;
		case R.id.menu_select:
			doCheckAll();
			break;
		case R.id.menu_backup:
			List<String> backupList = mAdapter.getCheckedPkgList();
			showBackupDialog(backupList);
			destroyMenuBar();
			break;

		default:
			break;
		}
	}
	
	public void showMoveDialog(){
		final List<String> packageList = mAdapter.getCheckedPkgList();
		new MoveAsyncTask(packageList).execute();
		destroyMenuBar();
	}
	
	private class MoveAsyncTask extends AsyncTask<Void, Void, Void>{
		List<String> pkgList = new ArrayList<String>();
		AppDialog dialog;
		
		MoveAsyncTask(List<String> list){
			pkgList = list;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (null == dialog) {
				dialog = new AppDialog(mContext, pkgList.size());
				dialog.setTitle(R.string.handling);
				dialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						cancel(true);
					}
				});
				dialog.show();
			}
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			String label = null;
			for (int i = 0; i < pkgList.size(); i++) {
				label = AppManager.getAppLabel(pkgList.get(i), pm);
				dialog.updateUI(i + 1, label);
				doMoveTo(pkgList.get(i));
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
	
	private void doMoveTo(String packageName){
		if (isGameUI()) {
			moveToApp(packageName);
		}else {
			moveToGame(packageName);
		}
	}
	
	private void moveToGame(String packageName){
		Log.d(TAG, "moveToGame:" + packageName);
		//move to game
		//1，将该记录的type设置为game
		//2，将数据插入到game表中
		//3，通知GameFragment
		//4，重新查询数据库
		ContentResolver contentResolver = getActivity().getContentResolver();
		ContentValues values = null;
		
		values = new ContentValues();
		values.put(AppData.App.TYPE, AppManager.GAME_APP);
		contentResolver.update(AppData.App.CONTENT_URI, values, 
				AppData.App.PKG_NAME + "='" + packageName + "'", null);
		
		//insert to db
		values = new ContentValues();
		values.put(AppData.App.PKG_NAME, packageName);
		contentResolver.insert(AppData.AppGame.CONTENT_URI, values);
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
	
	@Override
	public void doCheckAll(){
		int selectedCount1 = mAdapter.getCheckedCount();
		if (mAdapter.getCount() != selectedCount1) {
			mAdapter.checkedAll(true);
		} else {
			mAdapter.checkedAll(false);
		}
		updateMenuBar();
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void destroyMenuBar() {
		super.destroyMenuBar();
		
		updateTitleNum(-1);
		
		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.checkedAll(false);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void updateMenuBar(){
		int selectCount = mAdapter.getCheckedCount();
		updateTitleNum(selectCount);
		
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
        	mActionMenu.findItem(R.id.menu_backup).setEnable(false);
        	mActionMenu.findItem(R.id.menu_uninstall).setEnable(false);
        	mActionMenu.findItem(R.id.menu_move_app).setEnable(false);
        	mActionMenu.findItem(R.id.menu_app_info).setEnable(false);
		} else if (1 == selectCount) {
			mActionMenu.findItem(R.id.menu_send).setEnable(true);
        	mActionMenu.findItem(R.id.menu_backup).setEnable(true);
        	mActionMenu.findItem(R.id.menu_uninstall).setEnable(true);
        	mActionMenu.findItem(R.id.menu_move_app).setEnable(true);
        	mActionMenu.findItem(R.id.menu_app_info).setEnable(true);
		} else {
			mActionMenu.findItem(R.id.menu_send).setEnable(true);
        	mActionMenu.findItem(R.id.menu_backup).setEnable(true);
        	mActionMenu.findItem(R.id.menu_uninstall).setEnable(true);
        	mActionMenu.findItem(R.id.menu_move_app).setEnable(true);
        	mActionMenu.findItem(R.id.menu_app_info).setEnable(false);
		}
		
		mMenuBarManager.refreshMenus(mActionMenu);
	}
	
	protected void uninstallApp(){
		if (mUninstallList.size() <= 0) {
			mUninstallList = null;
			
			if (null != mAppDialog) {
				mAppDialog.cancel();
				mAppDialog = null;
			}
			return;
		}
		String uninstallPkg = mUninstallList.get(0);
		mAppDialog.updateUI(mAppDialog.getMax() - mUninstallList.size() + 1, 
				AppManager.getAppLabel(uninstallPkg, pm));
		Uri packageUri = Uri.parse("package:" + uninstallPkg);
		Intent deleteIntent = new Intent();
		deleteIntent.setAction(Intent.ACTION_DELETE);
		deleteIntent.setData(packageUri);
		startActivityForResult(deleteIntent, REQUEST_CODE_UNINSTALL);
		mUninstallList.remove(0);
	}
    
    @SuppressWarnings("unchecked")
	protected void showBackupDialog(List<String> packageList){
//    	Log.d(TAG, "Environment.getExternalStorageState():" + Environment.getExternalStorageState());
    	String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    	if (path.isEmpty()) {
    		mNotice.showToast(R.string.no_sdcard);
			return;
		}
    	
    	File file = new File(path);
    	if (null == file.listFiles() || file.listFiles().length < 0) {
    		mNotice.showToast(R.string.no_sdcard);
			return;
		}
    	
    	final BackupAsyncTask task = new BackupAsyncTask();
    	
    	mAppDialog = new AppDialog(getActivity(), packageList.size());
    	mAppDialog.setTitle(R.string.backup_app);
    	mAppDialog.setPositiveButton(R.string.hide, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				mIsBackupHide = true;
				mNotificationMgr.startBackupNotification();
				updateNotification(task.currentProgress, task.size, task.currentAppLabel);
				dialog.dismiss();
			}
		});
    	mAppDialog.setNegativeButton(R.string.cancel, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				Log.d(TAG, "showBackupDialog.onCancel");
				if (null != task) {
					task.cancel(true);
				}
				dialog.dismiss();
			}
		});
    	mAppDialog.show();
		task.execute(packageList);
    }
    
    private class BackupAsyncTask extends AsyncTask<List<String>, Integer, Void>{
    	public int currentProgress = 0;
    	public int size = 0;
    	public String currentAppLabel;
    	private long startTime = 0;
    	private long endTime = 0;
    	
		@Override
		protected Void doInBackground(List<String>... params) {
			startTime = System.currentTimeMillis();
			size = params[0].size();
			File file = new File(ZYConstant.JUYOU_BACKUP_FOLDER); 
			if (!file.exists()){
				boolean ret = file.mkdirs();
				if (!ret) {
					Log.e(TAG, "create file fail:" + file.getAbsolutePath());
					return null;
				}
			}
			
			String label = "";
			String version = "";
			String sourceDir = "";
			String packageName = "";
			for (int i = 0; i < size; i++) {
				if (isCancelled()) {
					Log.d(TAG, "doInBackground.isCancelled");
					return null;
				}
				packageName = params[0].get(i);
				label = AppManager.getAppLabel(packageName, pm);
				version = AppManager.getAppVersion(packageName, pm);
				sourceDir = AppManager.getAppSourceDir(packageName, pm);
				
				currentAppLabel = label;
				currentProgress = i + 1;
				
				mAppDialog.updateName(label);
				String desPath = ZYConstant.JUYOU_BACKUP_FOLDER + "/" + label + "_" + version + ".apk";
				if (!new File(desPath).exists()) {
					boolean ret = FileManager.copyFile(sourceDir, desPath);
					if (!ret) {
						Message message = mHandler.obtainMessage();
						message.obj = getString(R.string.backup_fail, label);
						message.what = MSG_TOAST;
						message.sendToTarget();
					}
				}
				mAppDialog.updateProgress(i + 1);
				if (mIsBackupHide) {
					updateNotification(currentProgress, size, currentAppLabel);
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.d(TAG, "onPostExecute");
			
			if (null != mAppDialog && mAppDialog.isShowing()) {
				mAppDialog.cancel();
				mAppDialog = null;
			}
			mNotice.showToast(R.string.backup_over);
			endTime = System.currentTimeMillis();
			if (mIsBackupHide) {
				mIsBackupHide = false;
				
				Message message = mHandler.obtainMessage();
				message.obj = endTime - startTime;
				message.what = MSG_BACKUP_OVER;
				message.sendToTarget();
			}
		}
    }
    
    protected void showUninstallDialog(){
    	mAppDialog = new AppDialog(getActivity(), mUninstallList.size());
		mAppDialog.setTitle(R.string.handling);
		mAppDialog.setNegativeButton(R.string.cancel, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				if (null != mUninstallList) {
					mUninstallList.clear();
					mUninstallList = null;
				}
				dialog.dismiss();
			}
		});
		mAppDialog.show();
    }
    
    public void updateNotification(int progress, int max, String name){
    	Message message = mHandler.obtainMessage();
    	message.arg1 = progress;
    	message.arg2 = max;
    	message.obj = name;
    	message.what = MSG_BACKUPING;
    	message.sendToTarget();
    }
    
    public void showInstalledAppDetails(String packageName){
		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= Build.VERSION_CODES.GINGERBREAD) {
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", packageName, null);
			intent.setData(uri);
		}else {
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra("pkg", packageName);
		}
		startActivity(intent);
	}
    
    private boolean isGameUI(){
    	return AppActivity.TYPE_GAME == mType;
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_CODE_UNINSTALL == requestCode) {
			uninstallApp();
		}
	}
}
