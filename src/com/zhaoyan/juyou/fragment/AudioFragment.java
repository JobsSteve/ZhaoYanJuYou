package com.zhaoyan.juyou.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.zhaoyan.common.util.IntentBuilder;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.AudioCursorAdapter;
import com.zhaoyan.juyou.adapter.AudioCursorAdapter.ViewHolder;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.FileInfoManager;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.MenuBarInterface;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.dialog.DeleteDialog;
import com.zhaoyan.juyou.dialog.InfoDialog;
import com.zhaoyan.juyou.dialog.DeleteDialog.OnDelClickListener;

public class AudioFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, 
			OnClickListener, MenuBarInterface {
	private static final String TAG = "AudioFragment";
	private ListView mListView;
	private AudioCursorAdapter mAdapter;
	private ProgressBar mLoadingBar;
	private FileInfoManager mFileInfoManager = null;
	
	private QueryHandler mQueryHandler = null;
	
	private DeleteDialog mDeleteDialog;
	
	private static final String[] PROJECTION = {
		MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
		MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
		MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION,
		MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATA,
		MediaStore.Audio.Media.IS_MUSIC, MediaStore.Audio.Media.DATE_MODIFIED
	};
	
	private static final int MSG_UPDATE_UI = 0;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int size = msg.arg1;
				count = size;
				updateTitleNum(-1);
				break;
			default:
				break;
			}
		};
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		View rootView = inflater.inflate(R.layout.audio_main, container, false);
		mListView = (ListView) rootView.findViewById(R.id.audio_listview);
		mLoadingBar = (ProgressBar) rootView.findViewById(R.id.audio_progressbar);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mAdapter = new AudioCursorAdapter(mContext);
		mListView.setAdapter(mAdapter);
		
		initTitle(rootView.findViewById(R.id.rl_audio_main), R.string.music);
		initMenuBar(rootView);
		
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView()");
		if (mAdapter != null && mAdapter.getCursor() != null) {
			mAdapter.getCursor().close();
			mAdapter.changeCursor(null);
		}
		super.onDestroyView();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mFileInfoManager = new FileInfoManager();
		
		mQueryHandler = new QueryHandler(getActivity().getApplicationContext()
				.getContentResolver());
		query();
	}
	
	/**
	 * Query Audio from Audio DB
	 */
	public void query() {
		mQueryHandler.startQuery(0, null, ZYConstant.AUDIO_URI,
				PROJECTION, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}
	
	// query db
	private class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.d(TAG, "onQueryComplete");
			mLoadingBar.setVisibility(View.INVISIBLE);
			int num = 0;
			if (null != cursor) {
				mAdapter.changeCursor(cursor);
				mAdapter.checkedAll(false);
				num = mAdapter.getCount();
			}
			updateUI(num);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_NORMAL)) {
			//open audio
			Cursor cursor = mAdapter.getCursor();
			cursor.moveToPosition(position);
			String url = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路径
			IntentBuilder.viewFile(getActivity(), url);
		}else {
			mAdapter.setChecked(position);
			mAdapter.notifyDataSetChanged();
			
			int selectedCount = mAdapter.getCheckedCount();
			updateTitleNum(selectedCount);
			updateMenuBar();
			mMenuBarManager.refreshMenus(mActionMenu);
		}
	} 
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			doCheckAll();
			return true;
		} else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1);
		}
		
		boolean isChecked = mAdapter.isChecked(position);
		mAdapter.setChecked(position, !isChecked);
		mAdapter.notifyDataSetChanged();
		mActionMenu = new ActionMenu(getActivity().getApplicationContext());
		getActionMenuInflater().inflate(R.menu.audio_menu, mActionMenu);
		
		startMenuBar();
		return true;
	}
	
	/**
     * show delete confrim dialog
     * @param path file path
     */
    public void showDeleteDialog(final List<Integer> posList) {
    	List<String> deleteNameList = new ArrayList<String>();
    	Cursor cursor = mAdapter.getCursor();
    	for (int i = 0; i < posList.size(); i++) {
			cursor.moveToPosition(posList.get(i));
			String name = cursor.getString(cursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE));
			deleteNameList.add(name);
		}
    	mDeleteDialog = new DeleteDialog(mContext, deleteNameList);
    	mDeleteDialog.setButton(AlertDialog.BUTTON_POSITIVE, R.string.menu_delete, new OnDelClickListener() {
			@Override
			public void onClick(View view, String path) {
				List<String> deleteList = mAdapter.getCheckedPathList();
				DeleteTask deleteTask = new DeleteTask(deleteList);
				deleteTask.execute(posList);
				destroyMenuBar();
			}
		});
    	mDeleteDialog.setButton(AlertDialog.BUTTON_NEGATIVE, R.string.cancel, null);
		mDeleteDialog.show();
    }
    
    /**
     * Delete file task
     */
    private class DeleteTask extends AsyncTask<List<Integer>, String, String>{
    	List<String> deleteList = new ArrayList<String>();
    	
    	DeleteTask(List<String> list){
    		deleteList = list;
    	}
    	
		@Override
		protected String doInBackground(List<Integer>... params) {
			//start delete file from delete list
			for (int i = 0; i < deleteList.size(); i++) {
				doDelete(deleteList.get(i));
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
			updateUI(mAdapter.getCount());
			mNotice.showToast(R.string.operator_over);
		}
    	
    }
    
    private void doDelete(String path) {
		boolean ret = mFileInfoManager.deleteFileInMediaStore(getActivity().getApplicationContext(), ZYConstant.AUDIO_URI, path);
		if (!ret) {
			mNotice.showToast(R.string.delete_fail);
			Log.e(TAG, path + " delete failed");
		}
	}
	
    /**
     * get selected file list total size
     * @param list
     * @return
     */
	public long getTotalSize(List<Integer> list){
		long totalSize = 0;
		Cursor cursor = mAdapter.getCursor();
		for(int pos : list){
			cursor.moveToPosition(pos);
			long size = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
			totalSize += size;
		}
		
		return totalSize;
	}
	 
	public void updateUI(int num) {
		Message message = mHandler.obtainMessage();
		message.arg1 = num;
		message.what = MSG_UPDATE_UI;
		message.sendToTarget();
	}
	
	@Override
	public boolean onBackPressed() {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void onMenuClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send:
			ArrayList<String> selectedList = (ArrayList<String>) mAdapter.getCheckedPathList();
			//send
			FileTransferUtil fileTransferUtil = new FileTransferUtil(getActivity());
			fileTransferUtil.sendFiles(selectedList, new TransportCallback() {
				@Override
				public void onTransportSuccess() {
					int first = mListView.getFirstVisiblePosition();
					int last = mListView.getLastVisiblePosition();
					List<Integer> checkedItems = mAdapter.getCheckedPosList();
					ArrayList<ImageView> icons = new ArrayList<ImageView>();
					for(int id : checkedItems) {
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
					Log.e(TAG, "onMenuClick.onTransportFail");
				}
			});
			destroyMenuBar();
			break;
		case R.id.menu_delete:
			//delete
			List<Integer> selectPosList = mAdapter.getCheckedPosList();
			showDeleteDialog(selectPosList);
			break;
		case R.id.menu_info:
			List<Integer> list = mAdapter.getCheckedPosList();
			InfoDialog dialog = null;
			if (1 == list.size()) {
				dialog = new InfoDialog(mContext,InfoDialog.SINGLE_FILE);
				Cursor cursor = mAdapter.getCursor();
				cursor.moveToPosition(list.get(0));
				
				long size = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA)); // 文件路径
				String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
				long date = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
				
				dialog.updateUI(size, 0, 0);
				dialog.updateUI(title, url, date);
			}else {
				dialog = new InfoDialog(mContext,InfoDialog.MULTI);
				int fileNum = list.size();
				long size = getTotalSize(list);
				dialog.updateUI(size, fileNum, 0);
			}
			dialog.show();
			dialog.invisbileLoadBar();
			//info
			break;
		case R.id.menu_select:
			doCheckAll();
			break;

		default:
			break;
		}
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
		mMenuBarManager.refreshMenus(mActionMenu);
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
		}else {
			selectItem.setTitle(R.string.select_all);
			selectItem.setEnableIcon(R.drawable.ic_aciton_select);
		}
		
		if (0==selectCount) {
			mActionMenu.findItem(R.id.menu_send).setEnable(false);
        	mActionMenu.findItem(R.id.menu_delete).setEnable(false);
        	mActionMenu.findItem(R.id.menu_info).setEnable(false);
		}else {
			mActionMenu.findItem(R.id.menu_send).setEnable(true);
        	mActionMenu.findItem(R.id.menu_delete).setEnable(true);
        	mActionMenu.findItem(R.id.menu_info).setEnable(true);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}
}
