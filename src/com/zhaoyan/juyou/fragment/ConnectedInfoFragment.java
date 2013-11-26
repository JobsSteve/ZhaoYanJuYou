package com.zhaoyan.juyou.fragment;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.communication.search.ConnectHelper;
import com.zhaoyan.communication.search.SearchUtil;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.UserInfo;
import com.zhaoyan.juyou.adapter.UserListAdapter;

public class ConnectedInfoFragment extends ListFragment implements
		OnClickListener, OnUserChangedListener {
	private static final String TAG = "ConnectedInfoFragment";
	private Button mDisconnectButton;
	private Context mContext;
	private UserManager mUserManager;
	private SocketCommunicationManager mCommunicationManager;
	private ConnectHelper mConnectHelper;

	private ListView mUserListView;
	private UserListAdapter mUserListAdapter;
	private ArrayList<UserInfo> mUserInfos = new ArrayList<UserInfo>();
	private static final int MSG_UPDATE_USER_LIST = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		mContext = getActivity();
		mUserManager = UserManager.getInstance();
		mCommunicationManager = SocketCommunicationManager
				.getInstance(mContext);
		mConnectHelper = ConnectHelper.getInstance(mContext);
		mUserManager.registerOnUserChangedListener(this);
		View rootView = inflater.inflate(R.layout.connected_info, container,
				false);
		initView(rootView);
		mUserListAdapter = new UserListAdapter(mContext, mUserInfos);
		return rootView;
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		updateUserList();
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
		mUserListAdapter = null;
		mUserManager.unregisterOnUserChangedListener(this);
	}

	private void initView(View rootView) {
		mDisconnectButton = (Button) rootView
				.findViewById(R.id.btn_ci_disconnect);
		mDisconnectButton.setOnClickListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mUserListView = getListView();
		mUserListView.setAdapter(mUserListAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_ci_disconnect:
			disconnect();
			break;

		default:
			break;
		}
	}

	private void disconnect() {
		// 1. clear user list.
		// mUserInfos.clear();
		// mUserListAdapter.notifyDataSetChanged();
		// 2. stop search if this is server.
		mConnectHelper.stopSearch();
		// 3. close all communications.
		mCommunicationManager.closeAllCommunication();
		mCommunicationManager.stopServer();
		// 4. reset local user.
		mUserManager.resetLocalUserID();
		// 5. close WiFi AP if WiFi AP is enabled.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}
		// 6. disconnect current network if connected to the WiFi AP created by
		// our application.
		SearchUtil.clearWifiConnectHistory(mContext);

	}

	private void updateUserList() {
		mUserInfos.clear();

		Map<Integer, User> users = mUserManager.getAllUser();
		UserInfo userInfo = null;
		// TODO User icon is not implement.
		Drawable userIcon = mContext.getResources().getDrawable(
				R.drawable.head1);
		for (Entry<Integer, User> entry : users.entrySet()) {
			if (UserManager.isManagerServer(entry.getValue())) {
				userInfo = new UserInfo(userIcon, entry.getValue()
						.getUserName(), mContext.getResources().getString(
						R.string.network_creator));
			} else {
				userInfo = new UserInfo(userIcon, entry.getValue()
						.getUserName(), mContext.getResources().getString(
						R.string.connected));
			}

			mUserInfos.add(userInfo);
		}
		mUserListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onUserConnected(User user) {
		mHandler.obtainMessage(MSG_UPDATE_USER_LIST).sendToTarget();
	}

	@Override
	public void onUserDisconnected(User user) {
		mHandler.obtainMessage(MSG_UPDATE_USER_LIST).sendToTarget();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			updateUserList();
		}
	};

	@Override
	public void onDestroy() {
		mUserManager.unregisterOnUserChangedListener(this);
		super.onDestroy();
	}

}
