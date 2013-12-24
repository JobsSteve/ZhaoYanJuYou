package com.zhaoyan.juyou.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.ImageAdapter;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.FileInfoManager;
import com.zhaoyan.juyou.common.FileTransferUtil;
import com.zhaoyan.juyou.common.ImageInfo;
import com.zhaoyan.juyou.common.MenuBarInterface;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.FileTransferUtil.TransportCallback;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.dialog.InfoDialog;
import com.zhaoyan.juyou.dialog.ZyDeleteDialog;
import com.zhaoyan.juyou.dialog.ZyProgressDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;

import android.app.Dialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.AbsListView.OnScrollListener;

public class ImageActivity extends BaseActivity implements OnScrollListener, OnItemClickListener, 
		OnItemLongClickListener, MenuBarInterface {
	private static final String TAG = "ImageActivity";
	
	public static final String IMAGE_TYPE = "IMAGE_TYPE";
	public static final int TYPE_PHOTO = 0;
	public static final int TYPE_GALLERY = 1;
	private int mImageType = -1;
	
	private GridView mGridView;
	private ProgressBar mLoadingBar;
	private ViewGroup mViewGroup;
	
	private ImageAdapter mAdapter;
	private List<ImageInfo> mPictureItemInfoList = new ArrayList<ImageInfo>();
	
	private static final int QUERY_TOKEN_FOLDER = 0x11;
	private static final int QUERY_TOKEN_ITEM = 0x12;
	
	private static final String[] PROJECTION = new String[] {MediaColumns._ID, 
		MediaColumns.DATE_MODIFIED, MediaColumns.SIZE,MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
		MediaColumns.DATA, MediaColumns.DISPLAY_NAME};
	
	private static final String[] PROJECTION_ICS = new String[] {MediaColumns._ID, 
		MediaColumns.DATE_MODIFIED, MediaColumns.SIZE,MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
		MediaColumns.DATA, MediaColumns.DISPLAY_NAME, "width", "height"};
	
	/**order by date_modified DESC*/
	public static final String SORT_ORDER_DATE = MediaColumns.DATE_MODIFIED + " DESC"; 
	private static final String CAMERA = "Camera";
	private static final String GALLERY = "Gallery";
	private static final String MIMETYPE_PNG = "image/png";
	
	private Notice mNotice = null;
	private FileInfoManager mFileInfoManager = null;
	
	private static final int MSG_UPDATE_UI = 0;
	private static final int MSG_UPDATE_LIST = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int num = msg.arg1;
				updateTitleNum(-1, num);
				break;
			case MSG_UPDATE_LIST:
				mPictureItemInfoList.remove(msg.arg1);
				mAdapter.notifyDataSetChanged();
				updateTitleNum(-1, mAdapter.getCount());
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_main);
		
		Bundle bundle = getIntent().getExtras();
		mImageType = bundle.getInt(IMAGE_TYPE);
		
		initView();
		
		if (TYPE_PHOTO == mImageType) {
			initTitle(R.string.camera);
			queryFolderItem(CAMERA);
		}else if (TYPE_GALLERY == mImageType) {
			initTitle(R.string.gallery);
			queryFolderItem(GALLERY);
		}else {
			Log.e(TAG, "onCreate.error.mImageType=" + mImageType);
		}
		
		setTitleNumVisible(true);
		mNotice = new Notice(getApplicationContext());
		mFileInfoManager = new FileInfoManager();
	}
	
	private void initView(){
		mViewGroup = (ViewGroup) findViewById(R.id.rl_picture_main);
		mGridView = (GridView) findViewById(R.id.gv_picture_item);
		mGridView.setOnScrollListener(this);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnItemLongClickListener(this);
		mLoadingBar = (ProgressBar) findViewById(R.id.bar_loading_image);
		
		mAdapter = new ImageAdapter(getApplicationContext(), mPictureItemInfoList);
		mGridView.setAdapter(mAdapter);
		
		initMenuBar();
	}
	
	public void query(int token, String selection, String[] selectionArgs, String orderBy) {
		String[] projection = PROJECTION;
		if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			projection = PROJECTION_ICS;
		}
		
		new QueryHandler(getApplicationContext().getContentResolver()).startQuery(token, null, ZYConstant.IMAGE_URI,
				projection, selection, selectionArgs, orderBy);		
	}
	
	public void queryFolderItem(String bucketName){
		String selection;
		//do not load png image
		if (GALLERY.equals(bucketName)) {
			selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "!=?"
					+ " and " + MediaStore.Images.Media.MIME_TYPE + "!=?";
		}else {
			selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?"
					+ " and " + MediaStore.Images.Media.MIME_TYPE + "!=?";
		}
		String selectionArgs[] = {CAMERA, MIMETYPE_PNG};
		mPictureItemInfoList.clear();
		query(QUERY_TOKEN_ITEM, selection, selectionArgs, SORT_ORDER_DATE);
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
				Log.d(TAG, "PictureFragment onQueryComplete.count=" + cursor.getCount()+":"+token);
				switch (token) {
				case QUERY_TOKEN_FOLDER:
					break;
				case QUERY_TOKEN_ITEM:
					if (cursor.moveToFirst()) {
						do {
							ImageInfo imageInfo = new ImageInfo();
							long id = cursor.getLong(cursor.getColumnIndex(MediaColumns._ID));
							String url = 
								cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
							String name = 
								cursor.getString(cursor.getColumnIndex(MediaColumns.DISPLAY_NAME));

							imageInfo.setImage_id(id);
							imageInfo.setPath(url);
							imageInfo.setDisplayName(name);

							mPictureItemInfoList.add(imageInfo);
						} while (cursor.moveToNext());
						cursor.close();
					}
					num = mPictureItemInfoList.size();
					mAdapter.notifyDataSetChanged();
					mAdapter.checkedAll(false);
					updateUI(num);
					break;
				default:
					Log.e(TAG, "Error token:" + token);
					break;
				}
			}
		}
	}
	
	private void updateUI(int num) {
		Message message = mHandler.obtainMessage();
		message.arg1 = num;
		message.what = MSG_UPDATE_UI;
		message.sendToTarget();
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
		case OnScrollListener.SCROLL_STATE_FLING:
			mAdapter.setIdleFlag(false);
			break;
		case OnScrollListener.SCROLL_STATE_IDLE:
			mAdapter.setIdleFlag(true);
			mAdapter.notifyDataSetChanged();
			break;
		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			mAdapter.setIdleFlag(false);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
	}
	
	private void startPagerActivityByPosition(int position){
		List<String> urlList = new ArrayList<String>();
		int count = mPictureItemInfoList.size();
		for (int i = 0; i < count; i++) {
			String url = mPictureItemInfoList.get(i).getPath();
			urlList.add(url);
		}
		Intent intent = new Intent(this, ImagePagerActivity.class);
		intent.putExtra(Extra.IMAGE_POSITION, position);
		intent.putStringArrayListExtra(Extra.IMAGE_INFO, (ArrayList<String>) urlList);
		startActivity(intent);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			mAdapter.setChecked(position);
			mAdapter.notifyDataSetChanged();
			
			int selectedCount = mAdapter.getCheckedCount();
			updateTitleNum(selectedCount, mAdapter.getCount());
			updateMenuBar();
			mMenuBarManager.refreshMenus(mActionMenu);
		} else {
			startPagerActivityByPosition(position);
		}
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			doCheckAll();
			return true;
		}else {
			mAdapter.changeMode(ActionMenu.MODE_EDIT);
			updateTitleNum(1, mAdapter.getCount());
		}
		
		mAdapter.setChecked(position, true);
		mAdapter.notifyDataSetChanged();
		
		mActionMenu = new ActionMenu(getApplicationContext());
		getActionMenuInflater().inflate(R.menu.image_menu, mActionMenu);
		
		startMenuBar();
		return true;
	}
	
	@Override
	public boolean onBackKeyPressed() {
		if (mAdapter.isMode(ActionMenu.MODE_EDIT)) {
			destroyMenuBar();
			return false;
		}else {
			return true;
		}
	}

	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send:
			Log.d(TAG, "send menu click");
			ArrayList<String> selectedList = (ArrayList<String>) mAdapter.getCheckedPathList();
			//send
			FileTransferUtil fileTransferUtil = new FileTransferUtil(this);
			fileTransferUtil.sendFiles(selectedList, new TransportCallback() {
				@Override
				public void onTransportSuccess() {
					Log.d(TAG, "onTransportSuccess.start");
					int first = mGridView.getFirstVisiblePosition();
					int last = mGridView.getLastVisiblePosition();
					List<Integer> checkedItems = mAdapter.getCheckedPosList();
					ArrayList<ImageView> icons = new ArrayList<ImageView>();
					for(int id : checkedItems) {
						if (id >= first && id <= last) {
							View view = mGridView.getChildAt(id - first);
							if (view != null) {
								ImageView item = (ImageView) view.findViewById(R.id.iv_image_item);
								icons.add(item);
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
			break;
		case R.id.menu_delete:
			showDeleteDialog();
			break;
		case R.id.menu_info:
			List<Integer> list = mAdapter.getCheckedPosList();
			InfoDialog dialog = null;
			if (1 == list.size()) {
				dialog = new InfoDialog(this,InfoDialog.SINGLE_FILE);
				dialog.setTitle(R.string.info_image_info);
				int position = list.get(0);
				String url = mPictureItemInfoList.get(position).getPath();
				String displayName = mPictureItemInfoList.get(position).getDisplayName();
				File file = new File(url);
				long size = file.length();
				long date = file.lastModified();
				
				String imageType = FileManager.getExtFromFilename(displayName);
				if ("".equals(imageType)) {
					imageType = getApplicationContext().getResources().getString(R.string.unknow);
				}
				
				dialog.setFileType(InfoDialog.IMAGE, imageType);
				dialog.setFileName(displayName);
				dialog.setFilePath(ZYUtils.getParentPath(url));
				dialog.setModifyDate(date);
				dialog.setFileSize(size);
				
			}else {
				dialog = new InfoDialog(this,InfoDialog.MULTI);
				dialog.setTitle(R.string.info_image_info);
				int fileNum = list.size();
				long totalSize = 0;
				long size = 0;
				for (int pos : list) {
					size = new File(mPictureItemInfoList.get(pos).getPath()).length();
					totalSize += size;
				}
				dialog.updateUI(totalSize, fileNum, 0);
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
	
	/**
     * show confrim dialog
     * @param path file path
     */
    public void showDeleteDialog() {
    	List<String> deleteNameList = mAdapter.getCheckedNameList();
    	
    	ZyDeleteDialog deleteDialog = new ZyDeleteDialog(this);
		deleteDialog.setTitle(R.string.delete_image);
		String msg = "";
		if (deleteNameList.size() == 1) {
			msg = getString(R.string.delete_file_confirm_msg, deleteNameList.get(0));
		}else {
			msg = getString(R.string.delete_file_confirm_msg_image, deleteNameList.size());
		}
		deleteDialog.setMessage(msg);
		deleteDialog.setPositiveButton(R.string.menu_delete, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				DeleteTask deleteTask = new DeleteTask();
				deleteTask.execute();
				
				dialog.dismiss();
			}
		});
		deleteDialog.setNegativeButton(R.string.cancel, null);
		deleteDialog.show();
    }
    
    /**
     * Delete file task
     */
    private class DeleteTask extends AsyncTask<Void, String, String>{
    	ZyProgressDialog progressDialog = null;
    	
    	@Override
    	protected void onPreExecute() {
    		super.onPreExecute();
    		progressDialog = new ZyProgressDialog(ImageActivity.this);
    		progressDialog.setMessage(R.string.deleting);
    		progressDialog.show();
    	}
    	
		@Override
		protected String doInBackground(Void... params) {
			List<Integer> posList = mAdapter.getCheckedPosList();
			List<String> selectedPathList = mAdapter.getCheckedPathList();
			int currentDelPos = -1;
			for (int i = 0; i < selectedPathList.size(); i++) {
				doDelete(selectedPathList.get(i));
				currentDelPos = posList.get(i) - i;
				Message message = mHandler.obtainMessage();
				message.arg1 = currentDelPos;
				message.what = MSG_UPDATE_LIST;
				message.sendToTarget();
			}
			//start delete file from delete list
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			destroyMenuBar();
			if (null != progressDialog) {
				progressDialog.cancel();
				progressDialog = null;
			}
			mNotice.showToast(R.string.operator_over);
		}
    }
    
    private void doDelete(String path) {
		boolean ret = FileManager.deleteFileInMediaStore(getApplicationContext(), ZYConstant.IMAGE_URI, path);
		if (!ret) {
			Log.e(TAG, path + " delete failed");
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
		updateTitleNum(-1, mAdapter.getCount());
		
		mAdapter.changeMode(ActionMenu.MODE_NORMAL);
		mAdapter.checkedAll(false);
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void updateMenuBar(){
		int selectCount = mAdapter.getCheckedCount();
		updateTitleNum(selectCount,mAdapter.getCount());
		
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
}
