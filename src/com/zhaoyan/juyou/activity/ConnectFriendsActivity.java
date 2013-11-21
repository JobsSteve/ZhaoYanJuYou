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
	private UserManager mUserManager;

	private FragmentManager mFragmentManager;
	private SearchConnectFragment mSearchAndConnectFragment;
	private ConnectedInfoFragment mConnectedInfoFragment;
	private static final String FRAGMENT_TAG_SEARCH_CONNECT = "SearchConnectFragment";
	private static final String FRAGMENT_TAG_CONNECTED_INFO = "ConnectedInfoFragment";
	private String mCurrentFragmentTag;

	private static final int MSG_SHOW_LOGIN_DIALOG = 1;
	private static final int MSG_UPDATE_NETWORK_STATUS = 2;
	private static final int MSG_UPDATE_USER = 3;
	private Handler mHandler;
	private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connect_friends);
		mHandler = new UiHandler();
		mCommunicationManager = SocketCommunicationManager
				.getInstance(getApplicationContext());
		mUserManager = UserManager.getInstance();
		mUserManager.registerOnUserChangedListener(this);

		mFragmentManager = getSupportFragmentManager();
		initTitle(R.string.connect_friends);

		initFragment(savedInstanceState);
		updateFragment();

		mReceiver = new CreateServerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectHelper.ACTION_SERVER_CREATED);
		registerReceiver(mReceiver, filter);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("fragment", mCurrentFragmentTag);
		super.onSaveInstanceState(outState);
	}

	private void initFragment(Bundle savedInstanceState) {
		mSearchAndConnectFragment = (SearchConnectFragment) mFragmentManager
				.findFragmentByTag(FRAGMENT_TAG_SEARCH_CONNECT);
		if (mSearchAndConnectFragment == null) {
			mSearchAndConnectFragment = new SearchConnectFragment();
		}

		mConnectedInfoFragment = (ConnectedInfoFragment) mFragmentManager
				.findFragmentByTag(FRAGMENT_TAG_CONNECTED_INFO);
		if (mConnectedInfoFragment == null) {
			mConnectedInfoFragment = new ConnectedInfoFragment();
		}

		// Restore from save instance.
		if (savedInstanceState != null) {
			mCurrentFragmentTag = savedInstanceState.getString("fragment");
			if (FRAGMENT_TAG_CONNECTED_INFO.equals(mCurrentFragmentTag)) {
				if (mSearchAndConnectFragment.isAdded()) {
					mFragmentManager.beginTransaction()
							.hide(mSearchAndConnectFragment).commit();
				}
			} else if (FRAGMENT_TAG_SEARCH_CONNECT.equals(mCurrentFragmentTag)) {
				if (mConnectedInfoFragment.isAdded()) {
					mFragmentManager.beginTransaction()
							.hide(mSearchAndConnectFragment).commit();
				}
			}
		}
	}

	private void updateFragment() {
		if (mCommunicationManager.isConnected()
				|| mCommunicationManager.isServerAndCreated()) {
			transactTo(mConnectedInfoFragment, FRAGMENT_TAG_CONNECTED_INFO);
		} else {
			transactTo(mSearchAndConnectFragment, FRAGMENT_TAG_SEARCH_CONNECT);
		}
	}

	private void transactTo(Fragment fragment, String tag) {
		if (tag.equals(mCurrentFragmentTag)) {
			return;
		}
		Fragment currentFragment = mFragmentManager
				.findFragmentByTag(mCurrentFragmentTag);
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (currentFragment != null) {
			transaction.hide(currentFragment);
		}
		try {
			if (!fragment.isAdded()) {
				transaction.add(R.id.fl_cf_container, fragment, tag).commit();
			} else {
				transaction.show(fragment).commit();
			}
		} catch (Exception e) {
			Log.e(TAG, "transactTo " + e);
		}
		mCurrentFragmentTag = tag;
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

	private class UiHandler extends Handler {

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
		mReceiver = null;
		mHandler = null;

		super.onDestroy();
	}

	private class CreateServerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ConnectHelper.ACTION_SERVER_CREATED.equals(action)) {
				updateFragment();
			}
		}
	};
}
