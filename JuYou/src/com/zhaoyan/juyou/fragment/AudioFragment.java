package com.zhaoyan.juyou.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.MediaColumns;
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

import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.IntentBuilder;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.AudioListAdapter;
import com.zhaoyan.juyou.adapter.AudioListAdapter.ViewHolder;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.CharacterParser;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.FileDeleteHelper;
import com.zhaoyan.juyou.common.FileDeleteHelper.OnDeleteListener;
import com.zhaoyan.juyou.common.MediaInfo;
import com.zhaoyan.juyou.common.MenuBarInterface;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.dialog.InfoDialog;
import com.zhaoyan.juyou.dialog.ZyDeleteDialog;
import com.zhaoyan.juyou.dialog.ZyEditDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;

/**
 * use baseAdapter
 * @author Yuri
 *
 */
public class AudioFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, 
			OnClickListener, MenuBarInterface {
	private static final String TAG = "AudioFragment";
	private ListView mListView;
	private AudioListAdapter mAdapter;
	//save audios
	private List<MediaInfo> mAudioLists = new ArrayList<MediaInfo>();
	private ProgressBar mLoadingBar;
	
	private QueryHandler mQueryHandler = null;
	
	private CharacterParser mCharacterParser;
	
	private static final String[] PROJECTION = {
		MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
		MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
		MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DURATION,
		MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.DATA,
		MediaStore.Audio.Media.IS_MUSIC, MediaStore.Audio.Media.DATE_MODIFIED,
		MediaStore.Audio.Media.DISPLAY_NAME
	};
	
	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_DELETE_OVER = 1;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int size = msg.arg1;
				count = size;
				updateTitleNum(-1);
				break;
			case MSG_DELETE_OVER:
				mNotice.showToast(R.string.operator_over);
				
				List<Integer> poslist = new ArrayList<Integer>();
				Bundle bundle = msg.getData();
				if (null != bundle) {
					poslist = bundle.getIntegerArrayList("position");
					int removePosition;
					for(int i = 0; i < poslist.size() ; i++){
						//remove from the last item to the first item
						removePosition = poslist.get(poslist.size() - (i + 1));
						mAudioLists.remove(removePosition);
						mAdapter.notifyDataSetChanged();
					}
					
					count = mAudioLists.size();
					updateTitleNum(-1);
				}else {
					Log.e(TAG, "bundle is null");
				}
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
		
		initTitle(rootView.findViewById(R.id.rl_audio_main), R.string.music);
		initMenuBar(rootView);
		
		mCharacterParser = CharacterParser.getInstance();
		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView()");
		super.onDestroyView();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mQueryHandler = new QueryHandler(getActivity().getApplicationContext()
				.getContentResolver());
		query();
	}
	
	/**
	 * Query Audio from Audio DB
	 */
	public void query() {
		//just show music files
		String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
		mQueryHandler.startQuery(0, null, ZYConstant.AUDIO_URI,
				PROJECTION, selection, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}
	
	// query db
	private class QueryHandler extends AsyncQueryHandler {

		public QueryHandler(ContentResolver cr) {
			super(cr);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.d(TAG, "onQueryComplete");
			int num = 0;
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					do {
						MediaInfo mediaInfo = new MediaInfo();
						long id = cursor.getLong(cursor.getColumnIndex(MediaColumns._ID));
						String title = cursor.getString(cursor.getColumnIndex(MediaColumns.TITLE));
						String artist = cursor.getString(cursor.getColumnIndex(Media.ARTIST));
						long duration = cursor.getLong(cursor
								.getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
						long size = cursor.getLong(cursor
								.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
						String url = 
							cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
						String name = 
							cursor.getString(cursor.getColumnIndex(MediaColumns.DISPLAY_NAME));
						long date_modify = cursor.getLong(cursor
								.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
						date_modify = date_modify * 1000L;

						mediaInfo.setId(id);
						mediaInfo.setTitle(title);
						mediaInfo.setArtist(artist);
						mediaInfo.setDisplayName(name);
						mediaInfo.setDuration(duration);
						mediaInfo.setSize(size);
						mediaInfo.setUrl(url);
						mediaInfo.setDate(date_modify);

						String sortLetter = getSortLetter(title);
						mediaInfo.setSortLetter(sortLetter);

						mAudioLists.add(mediaInfo);
						Collections.sort(mAudioLists, MediaInfo.getNameComparator());
						
					} while (cursor.moveToNext());
					cursor.close();
				}
				num = mAudioLists.size();
				
				mAdapter = new AudioListAdapter(mContext, mAudioLists);
				mListView.setAdapter(mAdapter);
			}
			mLoadingBar.setVisibility(View.INVISIBLE);
			updateUI(num);
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_NORMAL)) {
			//open audio
			String url = mAudioLists.get(position).getUrl();
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
	public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			//do nothing
			//doCheckAll();
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
		ZyDeleteDialog deleteDialog = new ZyDeleteDialog(mContext);
		deleteDialog.setTitle(R.string.delete_music);
		String msg = "";
		if (posList.size() == 1) {
			String title = mAudioLists.get(posList.get(0)).getTitle();
			msg = mContext.getString(R.string.delete_file_confirm_msg, title);
		}else {
			msg = mContext.getString(R.string.delete_file_confirm_msg_music, posList.size());
		}
		deleteDialog.setMessage(msg);
		deleteDialog.setPositiveButton(R.string.menu_delete, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				List<String> deleteList = mAdapter.getCheckedPathList();
				
				FileDeleteHelper mediaDeleteHelper = new FileDeleteHelper(mContext);
				mediaDeleteHelper.setDeletePathList(deleteList);
				mediaDeleteHelper.setOnDeleteListener(new OnDeleteListener() {
					@Override
					public void onDeleteFinished() {
						Log.d(TAG, "onFinished");
						Message message = mHandler.obtainMessage();
						Bundle bundle = new Bundle();
						bundle.putIntegerArrayList("position", (ArrayList<Integer>)posList);
						message.setData(bundle);
						message.what = MSG_DELETE_OVER;
						message.sendToTarget();
					}
				});
				mediaDeleteHelper.doDelete();
				
				destroyMenuBar();
				dialog.dismiss();
			}
		});
		deleteDialog.setNegativeButton(R.string.cancel, null);
		deleteDialog.show();
    }
	
    /**
     * get selected file list total size
     * @param list
     * @return
     */
	public long getTotalSize(List<Integer> list){
		long totalSize = 0;
		for(int pos : list){
			long size = mAudioLists.get(pos).getSize(); // 文件大小
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
	public void onMenuItemClick(ActionMenuItem item) {
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
					destroyMenuBar();
				}
				
				@Override
				public void onTransportFail() {
					Log.e(TAG, "onMenuClick.onTransportFail");
				}
			});
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
				dialog.setTitle(R.string.info_music_info);
				final int position = list.get(0);
				MediaInfo mediaInfo = mAudioLists.get(position);
				
				final long id = mediaInfo.getId();
				long size = mediaInfo.getSize();
				String url = mediaInfo.getUrl();
				final String title = mediaInfo.getTitle();
				long date = mediaInfo.getDate();
				String displayName = mediaInfo.getDisplayName();
				
				String musicType = FileManager.getExtFromFilename(displayName);
				if ("".equals(musicType)) {
					musicType = mContext.getResources().getString(R.string.unknow);
				}
				
				dialog.setFileType(InfoDialog.MUSIC, musicType);
				dialog.setFileName(title);
				dialog.setFilePath(ZYUtils.getParentPath(url));
				dialog.setModifyDate(date);
				dialog.setFileSize(size);
				dialog.setPositiveButton(R.string.modify, new OnZyAlertDlgClickListener() {
					@Override
					public void onClick(Dialog dialog) {
						showModifyDialog(mContext, id, position, title);
						dialog.dismiss();
						destroyMenuBar();
					}
				});
				dialog.setNegativeButton(R.string.cancel, null);
			}else {
				dialog = new InfoDialog(mContext,InfoDialog.MULTI);
				dialog.setTitle(R.string.info_music_info);
				int fileNum = list.size();
				long size = getTotalSize(list);
				dialog.updateUI(size, fileNum, 0);
			}
			dialog.show();
			dialog.scanOver();
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
	
	private void showModifyDialog(final Context context, final long id, final int position, String oldName) {
		final ZyEditDialog editDialog = new ZyEditDialog(context);
		editDialog.setTitle(R.string.info_modify_music_name);
		editDialog.setEditStr(oldName);
		editDialog.selectAll();
		editDialog.showIME(true);
		editDialog.setPositiveButton(R.string.ok, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				String newName = editDialog.getEditTextStr();
				//verify name's format
				String ret = ZYUtils.FileNameFormatVerify(context, newName);
				if (null != ret) {
					editDialog.showTipMessage(true, ret);
					return;
				}else {
					editDialog.showTipMessage(false, null);
				}
				
				ContentResolver contentResolver = context.getContentResolver();
				ContentValues values = new ContentValues();
				values.put(Media.TITLE, newName);
				
				try {
					contentResolver.update(ZYConstant.AUDIO_URI, values, MediaColumns._ID + "=" + id, null);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "showModifyDialog.update db error");
				}
				mAudioLists.get(position).setTitle(newName);
				mAdapter.notifyDataSetChanged();
				dialog.dismiss();
			}
		});		
		editDialog.setNegativeButton(R.string.cancel, null);
		editDialog.show();
	}
	
	private String getSortLetter(String title){
		if (title == null) {
			return "#";
		}
		
		if (title.trim().length() == 0) {
			return "#";
		}
		
		String pinyin = mCharacterParser.getSelling(title);
		String sortLetter = pinyin.substring(0, 1).toUpperCase();
		
		if (!sortLetter.matches("[A-Z]")) {
			return "#";
		}
		
		return sortLetter;
	}
}
