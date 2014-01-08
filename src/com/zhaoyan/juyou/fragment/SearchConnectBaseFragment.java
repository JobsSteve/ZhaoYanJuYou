package com.zhaoyan.juyou.fragment;

import java.util.Timer;
import java.util.TimerTask;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketServer;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.communication.connect.ServerConnector;
import com.zhaoyan.communication.connect.ServerCreator;
import com.zhaoyan.communication.search.SearchUtil;
import com.zhaoyan.communication.search.ServerSearcher;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.ServerCursorAdapter;
import com.zhaoyan.juyou.provider.JuyouData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public abstract class SearchConnectBaseFragment extends ListFragment implements
		OnClickListener, OnUserChangedListener, LoaderCallbacks<Cursor> {
	private static final String TAG = "SearchConnectBaseFragment";
	protected UserManager mUserManager;
	protected ServerCreator mServerCreator;
	protected ServerConnector mServerConnector;
	protected ServerSearcher mServerSearcher;

	protected static final String[] PROJECTION = { JuyouData.User._ID,
			JuyouData.User.USER_NAME, JuyouData.User.USER_ID,
			JuyouData.User.HEAD_ID, JuyouData.User.THIRD_LOGIN, JuyouData.User.HEAD_DATA,
			JuyouData.User.IP_ADDR, JuyouData.User.STATUS, JuyouData.User.TYPE,
			JuyouData.User.SSID };

	protected static final int MSG_SEARCH_SUCCESS = 1;
	protected static final int MSG_CONNECT_SERVER = 2;
	protected static final int MSG_SEARCH_WIFI_DIRECT_FOUND = 3;
	protected static final int MSG_SEARCH_STOP = 4;
	protected static final int MSG_CONNECTED = 6;

	protected static final int STATUS_INIT = 0;
	protected static final int STATUS_SEARCHING = 1;
	protected static final int STATUS_SEARCH_OVER = 2;
	protected static final int STATUS_CONNECTING = 3;
	protected static final int STATUS_CREATING_SERVER = 4;
	protected int mCurrentStatus = STATUS_INIT;

	private Timer mStopSearchTimer = null;
	/** set search time out 15s */
	private static final int SEARCH_TIME_OUT = 15 * 1000;

	private Context mContext;

	protected View mInitView;
	protected TextView mInitSearchTipsTextView;
	protected Button mInitSearchServerButton;
	protected Button mInitCreateServerButton;

	protected View mSearchResultView;
	protected View mSearchResultProgressBar;
	protected View mSearchResultNone;
	protected Button mSearchResultSearchButton;
	protected Button mSearchResultCreateServerButton;
	protected ListView mListView;
	protected ServerCursorAdapter mServerAdapter;

	protected View mConnectingView;
	protected View mCreatingServerView;
	private OnServerChangeListener mOnServerChangeListener;

	BroadcastReceiver mServerCreateBroadcastReceiver;

	protected abstract int getServerSearchType();

	protected abstract int getServerCreateType();

	protected abstract int getServerUserType();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.search_connect, container,
				false);
		initView(rootView);
		mServerAdapter = new ServerCursorAdapter(getActivity(), null, false);

		mUserManager = UserManager.getInstance();
		mUserManager.registerOnUserChangedListener(this);

		mServerCreator = ServerCreator.getInstance(mContext);
		mServerConnector = ServerConnector.getInstance(mContext);
		mServerSearcher = ServerSearcher.getInstance(mContext);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ServerCreator.ACTION_SERVER_CREATED);
		if (mServerCreateBroadcastReceiver == null) {
			mServerCreateBroadcastReceiver = new ServerCreateBroadcastReceiver();
			mContext.registerReceiver(mServerCreateBroadcastReceiver,
					intentFilter);
		}
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		updateUI(mCurrentStatus);
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		mUserManager.unregisterOnUserChangedListener(this);
		try {
			mContext.unregisterReceiver(mServerCreateBroadcastReceiver);
		} catch (Exception e) {
			Log.e(TAG, "onDestroyView " + e);
		}
		cancelStopSearchTimer();
		stopSearch();
		clearSearchResult();
		mServerAdapter.swapCursor(null);
		super.onDestroyView();
	}

	protected void setInitView(int searchTips) {
		mInitSearchTipsTextView.setText(searchTips);
	}

	private void initView(View rootView) {
		mInitView = rootView.findViewById(R.id.search_init);
		mInitSearchServerButton = (Button) rootView
				.findViewById(R.id.btn_search_init_search);
		mInitSearchServerButton.setOnClickListener(this);
		mInitCreateServerButton = (Button) rootView
				.findViewById(R.id.btn_search_init_create_server);
		mInitCreateServerButton.setOnClickListener(this);
		mInitSearchTipsTextView = (TextView) rootView
				.findViewById(R.id.tv_search_init_tips);

		mSearchResultView = rootView.findViewById(R.id.search_seach_result);
		mSearchResultView.setVisibility(View.GONE);
		mSearchResultProgressBar = rootView
				.findViewById(R.id.ll_search_searching);
		mSearchResultNone = rootView.findViewById(R.id.tv_search_result_none);
		mSearchResultSearchButton = (Button) rootView
				.findViewById(R.id.btn_search_searching_search);
		mSearchResultSearchButton.setOnClickListener(this);
		mSearchResultCreateServerButton = (Button) rootView
				.findViewById(R.id.btn_search_searching_create);
		mSearchResultCreateServerButton.setOnClickListener(this);

		mConnectingView = rootView.findViewById(R.id.search_connecting);

		mCreatingServerView = rootView
				.findViewById(R.id.search_creating_server);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();

		mListView.setAdapter(mServerAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_search_searching_search:
		case R.id.btn_search_init_search:
			preStartSearch();
			break;
		case R.id.btn_search_searching_create:
		case R.id.btn_search_init_create_server:
			preCreateServer();
			break;
		default:
			break;
		}
	}

	private void preStartSearch() {
		// Restart search.
		SearchUtil.clearWifiConnectHistory(mContext);
		if (SocketServer.getInstance().isServerStarted()) {
			SocketServer.getInstance().stopServer();
		}
		stopSearch();
		cancelStopSearchTimer();
		clearSearchResult();

		startSearch();
		setStopSearchTimer();
		updateUI(STATUS_SEARCHING);
	}

	private void preCreateServer() {
		// Recreate server.
		stopSearch();
		cancelStopSearchTimer();
		clearSearchResult();

		stopServer();
		createServer();
		updateUI(STATUS_CREATING_SERVER);
	}

	protected void startSearch() {
		mServerSearcher.startSearch(getServerSearchType());
	}

	protected void stopSearch() {
		mServerSearcher.stopSearch(getServerSearchType());
	}

	protected void clearSearchResult() {
		mServerSearcher.clearServerInfo(getServerSearchType());
	}

	protected void createServer() {
		mServerCreator.createServer(getServerCreateType());
	}

	protected void stopServer() {
		mServerCreator.stopServer();
	}

	private void cancelStopSearchTimer() {
		if (mStopSearchTimer != null) {
			try {
				mStopSearchTimer.cancel();
				mStopSearchTimer = null;
			} catch (Exception e) {
				Log.d(TAG, "cancelStopSearchTimer." + e);
			}
		}
	}

	private void setStopSearchTimer() {
		cancelStopSearchTimer();

		mStopSearchTimer = new Timer();
		mStopSearchTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				mStopSearchTimer = null;
				stopSearch();
				mHandler.obtainMessage(MSG_SEARCH_STOP).sendToTarget();
			}
		}, SEARCH_TIME_OUT);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor cursor = (Cursor) mServerAdapter.getItem(position);
		if (cursor != null) {
			mServerConnector.connectServer(getUserInfoFromCursor(cursor));
			updateUI(STATUS_CONNECTING);
		}
	}

	private UserInfo getUserInfoFromCursor(Cursor cursor) {
		String name = cursor.getString(cursor
				.getColumnIndex(JuyouData.User.USER_NAME));
		String ip = cursor.getString(cursor
				.getColumnIndex(JuyouData.User.IP_ADDR));
		int type = cursor.getInt(cursor.getColumnIndex(JuyouData.User.TYPE));
		String ssid = cursor.getString(cursor
				.getColumnIndex(JuyouData.User.SSID));

		UserInfo userInfo = new UserInfo();
		User user = new User();
		user.setUserName(name);
		userInfo.setUser(user);
		userInfo.setType(type);
		userInfo.setIpAddress(ip);
		userInfo.setSsid(ssid);
		return userInfo;
	}

	/**
	 * according to search server status to update ui</br> 1.when is searching,
	 * show progress bar to tell user that is searching,please wait</br> 2.when
	 * is search success,show the server list to user to choose connect</br>
	 * 3.when is search failed,show the re-search ui allow user re-search
	 * 
	 * @param status
	 *            search status
	 */
	private void updateUI(int status) {
		Log.d(TAG, "updateUI status = " + status);
		mCurrentStatus = status;
		switch (status) {
		case STATUS_INIT:
			mInitView.setVisibility(View.VISIBLE);
			mSearchResultView.setVisibility(View.GONE);
			mConnectingView.setVisibility(View.GONE);
			mCreatingServerView.setVisibility(View.GONE);
			break;
		case STATUS_SEARCHING:
			mInitView.setVisibility(View.GONE);
			mSearchResultView.setVisibility(View.VISIBLE);
			mSearchResultProgressBar.setVisibility(View.VISIBLE);
			mSearchResultNone.setVisibility(View.GONE);
			mConnectingView.setVisibility(View.GONE);
			break;
		case STATUS_SEARCH_OVER:
			mInitView.setVisibility(View.GONE);
			mSearchResultView.setVisibility(View.VISIBLE);
			mSearchResultProgressBar.setVisibility(View.GONE);
			if (getServerNumber() == 0) {
				mSearchResultNone.setVisibility(View.VISIBLE);
			} else {
				mSearchResultNone.setVisibility(View.GONE);
			}
			break;
		case STATUS_CONNECTING:
			mConnectingView.setVisibility(View.VISIBLE);
			mSearchResultProgressBar.setVisibility(View.GONE);
			break;
		case STATUS_CREATING_SERVER:
			mInitView.setVisibility(View.GONE);
			mSearchResultView.setVisibility(View.GONE);
			mConnectingView.setVisibility(View.GONE);
			mCreatingServerView.setVisibility(View.VISIBLE);
			cancelStopSearchTimer();
			break;
		default:
			break;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_SEARCH_STOP:
				updateUI(STATUS_SEARCH_OVER);
				break;
			case MSG_CONNECTED:
				stopSearch();
				clearSearchResult();
				cancelStopSearchTimer();
				updateUI(STATUS_INIT);
				break;
			case MSG_SEARCH_WIFI_DIRECT_FOUND:
				break;
			default:
				break;
			}

		}
	};

	public void setOnServerChangeListener(OnServerChangeListener listener) {
		mOnServerChangeListener = listener;
	}

	public int getServerNumber() {
		int n = 0;
		if (mServerAdapter != null) {
			n = mServerAdapter.getCount();
		}
		return n;
	}

	@Override
	public void onUserConnected(User user) {
		mHandler.obtainMessage(MSG_CONNECTED).sendToTarget();
	}

	@Override
	public void onUserDisconnected(User user) {

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		String selection = JuyouData.User.TYPE + "=" + getServerUserType();
		return new CursorLoader(mContext, JuyouData.User.CONTENT_URI,
				PROJECTION, selection, null, JuyouData.User.SORT_ORDER_DEFAULT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (mOnServerChangeListener != null) {
			if (cursor != null) {
				mOnServerChangeListener
						.onServerChanged(this, cursor.getCount());
			} else {
				mOnServerChangeListener.onServerChanged(this, 0);
			}
		}
		mServerAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (mOnServerChangeListener != null) {
			mOnServerChangeListener.onServerChanged(this, 0);
		}
		mServerAdapter.swapCursor(null);
	}

	public interface OnServerChangeListener {
		void onServerChanged(SearchConnectBaseFragment fragment,
				int serverNumber);
	}

	private class ServerCreateBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ServerCreator.ACTION_SERVER_CREATED.equals(action)) {
				updateUI(STATUS_INIT);
			}
		}
	}
}
