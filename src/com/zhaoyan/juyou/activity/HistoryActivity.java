package com.zhaoyan.juyou.activity;

import java.io.File;
import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.file.SingleMediaScanner;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.HistoryCursorAdapter;
import com.zhaoyan.juyou.common.HistoryManager;
import com.zhaoyan.juyou.provider.JuyouData;
import com.zhaoyan.juyou.provider.JuyouData.History;

public class HistoryActivity extends BaseActivity implements OnScrollListener,
		OnItemClickListener, OnClickListener {
	private static final String TAG = "HistoryActivityTest";
	private Context mContext;

	// view
	private TextView mStorageTV;
	private ListView mHistoryMsgLV;
	private ProgressBar mLoadingBar;
	
	private Button mStatisticsBtn, mClearHistoryBtn;

	// adapter
	private HistoryCursorAdapter mAdapter;

	private HistoryContent mHistoryContent = null;

	private Notice mNotice;

	// msg
	private static final int MSG_UPDATE_UI = 1;

	private QueryHandler queryHandler = null;
	private static final int HISTORY_QUERY_TOKEN = 11;

	private static final String[] PROJECTION = { JuyouData.History._ID,
			JuyouData.History.FILE_PATH, JuyouData.History.FILE_NAME,
			JuyouData.History.FILE_SIZE, JuyouData.History.SEND_USERNAME,
			JuyouData.History.RECEIVE_USERNAME, JuyouData.History.PROGRESS,
			JuyouData.History.DATE, JuyouData.History.STATUS,
			JuyouData.History.MSG_TYPE, JuyouData.History.FILE_TYPE,
			JuyouData.History.FILE_ICON };

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_UI:
				int num = msg.arg1;
				updateTitleNum(-1, num);
				break;

			default:
				break;
			}
		};
	};

	class HistoryContent extends ContentObserver {
		public HistoryContent(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			int count = mAdapter.getCount();
			updateUI(count);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.history);
		mContext = this;
		mNotice = new Notice(getApplicationContext());

		initTitle(R.string.history);
		mTitleNumView.setVisibility(View.VISIBLE);

		initView();
		queryHandler = new QueryHandler(getApplicationContext().getContentResolver(), this);

		mHistoryContent = new HistoryContent(new Handler());
		getApplicationContext().getContentResolver().registerContentObserver(
				JuyouData.History.CONTENT_URI, true, mHistoryContent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		query();
	}

	@Override
	protected void onDestroy() {
		if (mAdapter != null && mAdapter.getCursor() != null) {
			mAdapter.getCursor().close();
			mAdapter.swapCursor(null);
		}

		if (mHistoryContent != null) {
			getApplicationContext().getContentResolver().unregisterContentObserver(mHistoryContent);
			mHistoryContent = null;
		}
		
		if (queryHandler != null) {
			queryHandler.cancelOperation(HISTORY_QUERY_TOKEN);
			queryHandler = null;
		}
		super.onDestroy();
	}

	public void query() {
		queryHandler.startQuery(HISTORY_QUERY_TOKEN, null, JuyouData.History.CONTENT_URI,
				PROJECTION, null, null, JuyouData.History.SORT_ORDER_DEFAULT);
	}

	@SuppressLint("NewApi")
	private void initView() {
		mStorageTV = (TextView) findViewById(R.id.tv_storage);
		String space = getResources().getString(
				R.string.storage_space,
				ZYUtils.getFormatSize(Environment.getExternalStorageDirectory()
						.getTotalSpace()),
				ZYUtils.getFormatSize(Environment.getExternalStorageDirectory()
						.getFreeSpace()));
		mStorageTV.setText(space);
		mLoadingBar = (ProgressBar) findViewById(R.id.pb_history_loading);
		mHistoryMsgLV = (ListView) findViewById(R.id.lv_history_msg);
		mHistoryMsgLV.setOnScrollListener(this);
		mHistoryMsgLV.setOnItemClickListener(this);

		mAdapter = new HistoryCursorAdapter(mContext, mHistoryMsgLV);
		mHistoryMsgLV.setAdapter(mAdapter);
		mHistoryMsgLV.setSelection(0);
		
		mStatisticsBtn = (Button) findViewById(R.id.btn_statistics);
		mClearHistoryBtn = (Button) findViewById(R.id.btn_clear_history);
		mStatisticsBtn.setOnClickListener(this);
		mClearHistoryBtn.setOnClickListener(this);
		
	}

	// query db
	private static class QueryHandler extends AsyncQueryHandler {
		WeakReference<HistoryActivity> mActivity;
		public QueryHandler(ContentResolver cr, HistoryActivity activity) {
			super(cr);
			mActivity = new WeakReference<HistoryActivity>(activity);
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			Log.d(TAG, "onQueryComplete");
			HistoryActivity historyActivity = mActivity.get();
			historyActivity.mLoadingBar.setVisibility(View.INVISIBLE);
			int num = 0;
			if (null != cursor) {
				Log.d(TAG, "onQueryComplete.count=" + cursor.getCount());
				historyActivity.mAdapter.swapCursor(cursor);
				num = cursor.getCount();
				if (0 == num) {
					historyActivity.mClearHistoryBtn.setEnabled(false);
				}
			}else {
				historyActivity.mClearHistoryBtn.setEnabled(false);
			}

			historyActivity.updateUI(num);
		}

	}

	private void updateUI(int num) {
		Message message = mHandler.obtainMessage();
		message.arg1 = num;
		message.what = MSG_UPDATE_UI;
		message.sendToTarget();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mNotice.showToast(position + "" + ",view = " + view);
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_statistics:
			Intent intent = new Intent(this, TrafficStatisticsActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_right_in, 0);
			break;
		case R.id.btn_clear_history:
			int defaultChoiceItem = 0;
			ClearHistoryClick clearHistoryClick = new ClearHistoryClick(defaultChoiceItem);
			new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.clear_history_tip)
				.setSingleChoiceItems(R.array.clear_history_menu, defaultChoiceItem, clearHistoryClick)
				.setPositiveButton(R.string.ok, clearHistoryClick)
				.setNegativeButton(R.string.cancel, null)
				.create().show();
			break;

		default:
			break;
		}
	}
	
	class ClearHistoryClick implements DialogInterface.OnClickListener{
		int index;
		
		ClearHistoryClick(int index){
			this.index = index;
		}
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//which表示单击的按钮索引，所有的列表选项的索引都是大于0的，按钮的索引都是小于0的
			if (which >= 0) {
				//如果单击的是列表项，保存索引
				index = which;
			}else {
				//单击的是按钮，这里只可能是确定按钮
				mClearHistoryBtn.setEnabled(false);
				switch (index) {
				case 0:
					//delete history
					//delete history table
					getApplicationContext().getContentResolver().delete(History.CONTENT_URI, null, null);
					break;
				case 1:
					//delete history and received file
					Cursor cursor = mAdapter.getCursor();
					if (cursor.moveToFirst()) {
						String filePath;
						int type;
						File file = null;
						do {
							type = cursor.getInt(cursor.getColumnIndex(History.MSG_TYPE));
							//just delete the file that received from friends
							if (HistoryManager.TYPE_RECEIVE == type) {
								filePath = cursor.getString(cursor.getColumnIndex(History.FILE_PATH));
								file = new File(filePath);
								if (file.delete()) {
									//if delete file success,update mediaProvider
									new SingleMediaScanner(getApplicationContext(), file);
								}
							}
						} while (cursor.moveToNext());
					}
					cursor.close();
					getApplicationContext().getContentResolver().delete(History.CONTENT_URI, null, null);
					//init the index to 0,because default select is 0
					index = 0;
					break;
				}
				mNotice.showToast(R.string.operator_over);
			}
		}
	}

}
