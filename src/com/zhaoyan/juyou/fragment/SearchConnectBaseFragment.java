package com.zhaoyan.juyou.fragment;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketServer;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.communication.search.ConnectHelper;
import com.zhaoyan.communication.search.Search;
import com.zhaoyan.communication.search.SearchUtil;
import com.zhaoyan.communication.search.ServerInfo;
import com.zhaoyan.communication.search.SearchProtocol.OnSearchListener;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.ServerAdapter;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint("NewApi")
public abstract class SearchConnectBaseFragment extends ListFragment implements
		OnClickListener, OnSearchListener, OnUserChangedListener {
	private static final String TAG = "SearchConnectBaseFragment";
	protected ConnectHelper mConnectHelper;
	protected UserManager mUserManager;

	protected static final int MSG_SEARCH_SUCCESS = 1;
	protected static final int MSG_CONNECT_SERVER = 2;
	protected static final int MSG_SEARCH_WIFI_DIRECT_FOUND = 3;
	protected static final int MSG_SEARCH_STOP = 4;
	protected static final int MSG_SEARCHING = 5;
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
	protected ArrayList<ServerInfo> mServerData = new ArrayList<ServerInfo>();
	protected ServerAdapter mServerAdapter;

	protected View mConnectingView;

	protected View mCreatingServerView;

	private OnServerChangeListener mOnServerChangeListener;

	protected abstract void startSearch();

	protected abstract void createServer();

	protected static final int FILTER_SERVER_AP = 0x1;
	protected static final int FILTER_SERVER_WIFI = 0x2;
	protected static final int FILTER_SERVER_WIFI_DIRECT = 0x4;
	protected static final int FILTER_SERVER_ALL = FILTER_SERVER_AP
			| FILTER_SERVER_WIFI | FILTER_SERVER_WIFI_DIRECT;

	protected int getServerTypeFilter() {
		return FILTER_SERVER_ALL;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		mContext = getActivity();
		View rootView = inflater.inflate(R.layout.search_connect, container,
				false);
		initView(rootView);

		mConnectHelper = ConnectHelper.getInstance(mContext
				.getApplicationContext());
		mUserManager = UserManager.getInstance();
		mUserManager.registerOnUserChangedListener(this);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectHelper.ACTION_SERVER_CREATED);
		mContext.registerReceiver(mReceiver, filter);

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
		try {
			mContext.unregisterReceiver(mReceiver);
		} catch (Exception e) {
			Log.e(TAG, "onDestroyView unregisterReceiver " + e);
		}
		mUserManager.unregisterOnUserChangedListener(this);
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
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mListView = getListView();
		mServerAdapter = new ServerAdapter(getActivity(), mServerData);
		mListView.setAdapter(mServerAdapter);
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
		SearchUtil.clearWifiConnectHistory(mContext);
		clearServerList();
		if (SocketServer.getInstance().isServerStarted()) {
			SocketServer.getInstance().stopServer();
		}

		startSearch();

		setStopSearchTimer();

		updateUI(STATUS_SEARCHING);
	}

	private void preCreateServer() {
		createServer();

		updateUI(STATUS_CREATING_SERVER);
	}

	private void cancelStopSearchTimer() {
		if (mStopSearchTimer != null) {
			try {
				mStopSearchTimer.cancel();
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
				mConnectHelper.stopSearch(false);
				mHandler.obtainMessage(MSG_SEARCH_STOP).sendToTarget();
			}
		}, SEARCH_TIME_OUT);
	}

	private boolean mIsAPSelected = false;

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		ServerInfo serverInfo = mServerData.get(position);
		if (serverInfo.getServerType().equals("wifi-ap"))
			mIsAPSelected = true;
		mHandler.obtainMessage(MSG_CONNECT_SERVER, serverInfo).sendToTarget();
	}

	private void clearServerList() {
		mServerData.clear();
		mServerAdapter.notifyDataSetChanged();

		if (mOnServerChangeListener != null) {
			mOnServerChangeListener.onServerChanged(this, mServerData.size());
		}
	}

	@Override
	public void onSearchSuccess(String serverIP, String name) {
		Log.d(TAG, "onSearchSuccess");
		Log.d(TAG, "mIsApSelected:" + mIsAPSelected + "\n" + "serverIP:"
				+ serverIP + "\n" + "serverName:" + name);
		if (mIsAPSelected && serverIP.equals(Search.ANDROID_AP_ADDRESS)) {
			// Auto connect to the server.
			Message message = mHandler.obtainMessage(MSG_CONNECT_SERVER);
			ServerInfo info = new ServerInfo();
			info.setServerType("wifi");
			info.setServerIp(serverIP);
			info.setServerName(name);
			message.obj = info;
			mHandler.sendMessage(message);
			mIsAPSelected = false;
		} else {

			Log.i(TAG, "serverIp:" + serverIP + "-->serverName:" + name);
			// Add to server list and wait user for choose.
			Message message = mHandler.obtainMessage(MSG_SEARCH_SUCCESS);
			ServerInfo info = new ServerInfo();
			info.setServerType("wifi");
			info.setServerIp(serverIP);
			info.setServerName(name);
			message.obj = info;
			mHandler.sendMessage(message);
		}
	}

	@Override
	public void onSearchSuccess(ServerInfo serverInfo) {
		Log.d(TAG, "onSearchSuccess.serverInfo=" + serverInfo.getServerName());
		Message message = mHandler.obtainMessage(MSG_SEARCH_SUCCESS);
		message.obj = serverInfo;
		message.sendToTarget();
	}

	private boolean isServerAlreadyAdded(ServerInfo info) {
		boolean result = false;

		for (ServerInfo serverInfo : mServerData) {
			if (ConnectHelper.SERVER_TYPE_WIFI.equals(info.getServerType())
					&& ConnectHelper.SERVER_TYPE_WIFI.equals(serverInfo
							.getServerType())) {
				if (info.getServerName().equals(serverInfo.getServerName())
						&& info.getServerIp().equals(serverInfo.getServerIp())) {
					// The server is already added to list.
					result = true;
					break;
				}
			} else if (ConnectHelper.SERVER_TYPE_WIFI_AP.equals(info
					.getServerType())
					&& ConnectHelper.SERVER_TYPE_WIFI_AP.equals(serverInfo
							.getServerType())) {
				if (info.getServerName().equals(serverInfo.getServerName())
						&& info.getServerSsid().equals(
								serverInfo.getServerSsid())) {
					// The server is already added to list.
					result = true;
					break;
				}
			} else if (ConnectHelper.SERVER_TYPE_WIFI_DIRECT.equals(info
					.getServerType())
					&& ConnectHelper.SERVER_TYPE_WIFI_DIRECT.equals(serverInfo
							.getServerType())) {
				if (info.getServerName().equals(serverInfo.getServerName())
						&& info.getServerDevice().deviceAddress
								.equals(serverInfo.getServerDevice().deviceAddress)) {
					// The server is already added to list.
					result = true;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public void onSearchStop() {
		Log.d(TAG, "onSearchStop");
	}

	private boolean filterServer(ServerInfo info) {
		int filter = getServerTypeFilter();
		Log.d(TAG, "filterServer server = " + info + ", filter = " + filter);
		String type = info.getServerType();
		if (FILTER_SERVER_ALL == (filter & FILTER_SERVER_ALL)) {
			return true;
		}
		if (ConnectHelper.SERVER_TYPE_WIFI.equals(type)
				&& FILTER_SERVER_WIFI == (filter & FILTER_SERVER_WIFI)) {
			Log.d(type, "filterServer SERVER_TYPE_WIFI");
			return true;
		} else if (ConnectHelper.SERVER_TYPE_WIFI_AP.equals(type)
				&& FILTER_SERVER_AP == (filter & FILTER_SERVER_AP)) {
			Log.d(type, "filterServer FILTER_SERVER_AP");
			return true;
		} else {
			return false;
		}
	}

	private void addServer(ServerInfo info) {
		if (!filterServer(info)) {
			Log.d(TAG, "server dismatch filter." + info);
			return;
		}

		if (isServerAlreadyAdded(info)) {
			Log.d(TAG, "Server is already added. " + info);
			return;
		}
		mServerData.add(info);
		Log.i(TAG, "addServer:" + info);
		mServerAdapter.notifyDataSetChanged();
		if (mOnServerChangeListener != null) {
			mOnServerChangeListener.onServerChanged(this, mServerData.size());
		}
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
			if (mServerData.size() == 0) {
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
			case MSG_SEARCH_SUCCESS:
				addServer((ServerInfo) msg.obj);
				break;
			case MSG_SEARCH_STOP:
				updateUI(STATUS_SEARCH_OVER);
				break;
			case MSG_SEARCHING:
				updateUI(STATUS_SEARCHING);
				break;
			case MSG_CONNECT_SERVER:
				updateUI(STATUS_CONNECTING);
				ServerInfo info = (ServerInfo) msg.obj;
				mConnectHelper.connenctToServer(info);
				break;
			case MSG_CONNECTED:
				if (!UserManager.isManagerServer(mUserManager.getLocalUser())) {
					mConnectHelper.stopSearch();
				}
				clearServerList();
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

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ConnectHelper.ACTION_SERVER_CREATED.equals(action)) {
				updateUI(STATUS_INIT);
			}
		}
	};

	public interface OnServerChangeListener {
		void onServerChanged(SearchConnectBaseFragment fragment,
				int serverNumber);
	}

	@Override
	public void onUserConnected(User user) {
		mHandler.obtainMessage(MSG_CONNECTED).sendToTarget();
	}

	@Override
	public void onUserDisconnected(User user) {

	}

	public int getServerNumber() {
		return mServerData.size();
	}
}
