package com.zhaoyan.juyou.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.communication.search.ConnectHelper;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.fragment.ConnectedInfoFragment;
import com.zhaoyan.juyou.fragment.SearchConnectFragment;

public class ConnectFriendsActivity extends BaseFragmentActivity implements
		OnUserChangedListener {
	private static final String TAG = "ConnectFriendsActivity";
	private SocketCommunicationManager mCommunicationManager;
	private FragmentManager mFragmentManager;
	private SearchConnectFragment mSearchAndConnectFragment;
	private ConnectedInfoFragment mConnectedInfoFragment;
	private Fragment mCurrentFragment;
	private UserManager mUserManager;

	private static final int MSG_SHOW_LOGIN_DIALOG = 1;
	private static final int MSG_UPDATE_NETWORK_STATUS = 2;
	private static final int MSG_UPDATE_USER = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_friends);

		mCommunicationManager = SocketCommunicationManager
				.getInstance(getApplicationContext());
		mUserManager = UserManager.getInstance();
		mUserManager.registerOnUserChangedListener(this);

		mFragmentManager = getSupportFragmentManager();
		initTitle(R.string.connect_friends);

		initView();
		initFragment();
		updateFragment();

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectHelper.ACTION_SERVER_CREATED);
		registerReceiver(mReceiver, filter);
	}

	private void initFragment() {
		mSearchAndConnectFragment = new SearchConnectFragment();
		mConnectedInfoFragment = new ConnectedInfoFragment();
	}

	private void updateFragment() {
		if (mCommunicationManager.isConnected()
				|| mCommunicationManager.isServerAndCreated()) {
			transactTo(mConnectedInfoFragment);
		} else {
			transactTo(mSearchAndConnectFragment);
		}
	}

	private void transactTo(Fragment fragment) {
		if (fragment == mCurrentFragment) {
			return;
		}
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (mCurrentFragment != null) {
			transaction.hide(mCurrentFragment);
		}
		try {
			if (!fragment.isAdded()) {
				transaction.add(R.id.fl_cf_container, fragment).commit();
			} else {
				transaction.show(fragment).commit();
			}
		} catch (Exception e) {
			Log.e(TAG, "transactTo " + e);
		}

		mCurrentFragment = fragment;
	}

	private void initView() {
	}

	@Override
	public void onUserConnected(User user) {
		mHandler.sendEmptyMessage(MSG_UPDATE_NETWORK_STATUS);
		mHandler.sendEmptyMessage(MSG_UPDATE_USER);
	}

	@Override
	public void onUserDisconnected(User user) {
		mHandler.sendEmptyMessage(MSG_UPDATE_NETWORK_STATUS);
		mHandler.sendEmptyMessage(MSG_UPDATE_USER);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SHOW_LOGIN_DIALOG:
				break;
			case MSG_UPDATE_NETWORK_STATUS:
				updateFragment();
				break;
			case MSG_UPDATE_USER:
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		mUserManager.unregisterOnUserChangedListener(this);
		try {
			unregisterReceiver(mReceiver);
		} catch (Exception e) {
			Log.e(TAG, "onDestroy " + e);
		}

		super.onDestroy();
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ConnectHelper.ACTION_SERVER_CREATED.equals(action)) {
				updateFragment();
			}
		}
	};
}
