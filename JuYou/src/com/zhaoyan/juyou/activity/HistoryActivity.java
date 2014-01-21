package com.zhaoyan.juyou.activity;

import java.lang.ref.WeakReference;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.communication.FileTransferService;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.HistoryCursorAdapter;
import com.zhaoyan.juyou.provider.JuyouData;

public class HistoryActivity extends BaseActivity implements OnScrollListener,
		OnItemClickListener {
	private static final String TAG = "HistoryActivityTest";
	private Context mContext;

	// view
	private TextView mStorageTV;
	private ListView mHistoryMsgLV;
	private ProgressBar mLoadingBar;

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
			JuyouData.History.FILE_ICON, JuyouData.History.SEND_USER_HEADID,
			JuyouData.History.SEND_USER_ICON};

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
		setContentView(R.layout.history_main);
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
		
		//notify hide badgeview
		Intent intent = new Intent(
				FileTransferService.ACTION_NOTIFY_SEND_OR_RECEIVE);
		intent.putExtra(FileTransferService.EXTRA_BADGEVIEW_SHOW, false);
		sendBroadcast(intent);
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
//		String selection = JuyouData.History.STATUS + "!=?";
//		String[] selectionArgs = {HistoryManager.STATUS_PRE_SEND +""};
//		queryHandler.startQuery(HISTORY_QUERY_TOKEN, null, JuyouData.History.CONTENT_URI,
//				PROJECTION, selection, selectionArgs, JuyouData.History.SORT_ORDER_DEFAULT);
		
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

}
