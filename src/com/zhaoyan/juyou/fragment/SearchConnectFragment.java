package com.zhaoyan.juyou.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.view.TableTitleView;
import com.zhaoyan.common.view.TableTitleView.OnTableSelectChangeListener;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.fragment.SearchConnectBaseFragment.OnServerChangeListener;

public class SearchConnectFragment extends Fragment implements
		OnTableSelectChangeListener, OnServerChangeListener {
	private static final String TAG = "SearchConnectFragment";

	private String mCurrentFragmentTag;
	private SearchConnectApFragment mApFragment;
	private SearchConnectWifiFragment mWifiFragment;
	private static final String FRAGMENT_TAG_AP = "ap";
	private static final String FRAGMENT_TAG_WIFI = "wifi";
	private static final int POSTION_AP_FRAGMENT = 0;
	private static final int POSTION_WIFI_FRAGMENT = 1;
	private FragmentManager mFragmentManager;

	private TableTitleView mTableTitleView;

	private Context mContext;
	private WifiManager mWifiManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		mContext = getActivity();
		mWifiManager = (WifiManager) mContext.getApplicationContext()
				.getSystemService(Context.WIFI_SERVICE);
		View rootView = inflater.inflate(R.layout.search_connect_pages,
				container, false);

		mFragmentManager = getFragmentManager();
		initView(rootView);
		initFragment(savedInstanceState);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mContext.registerReceiver(mWifiBroadcastReceiver, intentFilter);
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("fragment", mCurrentFragmentTag);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
		try {
			mContext.unregisterReceiver(mWifiBroadcastReceiver);
		} catch (Exception e) {
		}

		if (mApFragment != null) {
			mApFragment.setOnServerChangeListener(null);
		}
		if (mWifiFragment != null) {
			mWifiFragment.setOnServerChangeListener(null);
		}
		mWifiManager = null;
	}

	private void transactTo(Fragment fragment, String tag) {
		if (tag.equals(mCurrentFragmentTag)) {
			return;
		}
		Fragment currentFragment = (Fragment) mFragmentManager
				.findFragmentByTag(mCurrentFragmentTag);
		Log.d(TAG, "transactTo " + fragment.getClass().getSimpleName());
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		if (currentFragment != null) {
			transaction.hide(currentFragment);
		}
		if (!fragment.isAdded()) {
			transaction.add(R.id.fl_sc_container, fragment, tag).commit();
		} else {
			transaction.show(fragment).commit();
		}
		mCurrentFragmentTag = tag;
	}

	private void initView(View rootView) {
		mTableTitleView = (TableTitleView) rootView
				.findViewById(R.id.ttv_sc_title);
		String apServerNumber = getString(R.string.ap_server_number, 0);

		mTableTitleView.initTitles(new String[] { apServerNumber,
				getWifiServerNumberTitle(0) });
		mTableTitleView.setOnTableSelectChangeListener(this);

	}

	private String getWifiServerNumberTitle(int number) {
		boolean isWifiConnected = NetWorkUtil.isWifiConnected(mContext);
		String wifiServerNumberTitle;
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
		if (isWifiConnected && wifiInfo != null) {
			String ssid = wifiInfo.getSSID();
			wifiServerNumberTitle = getString(R.string.wifi_server_number,
					ssid, number);
		} else {
			wifiServerNumberTitle = getString(R.string.wifi_server_number,
					getString(R.string.wifi_server_number_wifi_name_default),
					number);
		}
		return wifiServerNumberTitle;
	}

	private void initFragment(Bundle savedInstanceState) {
		mApFragment = (SearchConnectApFragment) mFragmentManager
				.findFragmentByTag(FRAGMENT_TAG_AP);
		if (mApFragment == null) {
			mApFragment = new SearchConnectApFragment();
		}
		mApFragment.setOnServerChangeListener(this);

		mWifiFragment = (SearchConnectWifiFragment) mFragmentManager
				.findFragmentByTag(FRAGMENT_TAG_WIFI);
		if (mWifiFragment == null) {
			mWifiFragment = new SearchConnectWifiFragment();
		}
		mWifiFragment.setOnServerChangeListener(this);

		// Restore from save instance.
		if (savedInstanceState != null) {
			mCurrentFragmentTag = savedInstanceState.getString("fragment");
			if (mCurrentFragmentTag.equals(FRAGMENT_TAG_AP)) {
				if (mWifiFragment.isAdded()) {
					mFragmentManager.beginTransaction().hide(mWifiFragment)
							.commit();
				}
			} else if (mCurrentFragmentTag.equals(FRAGMENT_TAG_WIFI)) {
				if (mApFragment.isAdded()) {
					mFragmentManager.beginTransaction().hide(mApFragment)
							.commit();
				}
			}
		}

		if (NetWorkUtil.isWifiConnected(mContext)) {
			transactTo(mWifiFragment, FRAGMENT_TAG_WIFI);
			mTableTitleView.setSelectedPostion(POSTION_WIFI_FRAGMENT);
		} else {
			transactTo(mApFragment, FRAGMENT_TAG_AP);
			mTableTitleView.setSelectedPostion(POSTION_AP_FRAGMENT);
		}
	}

	@Override
	public void onTableSelect(int position) {
		switch (position) {
		case POSTION_AP_FRAGMENT:
			transactTo(mApFragment, FRAGMENT_TAG_AP);
			break;
		case POSTION_WIFI_FRAGMENT:
			transactTo(mWifiFragment, FRAGMENT_TAG_WIFI);
			break;
		default:
			break;
		}
	}

	@Override
	public void onServerChanged(SearchConnectBaseFragment fragment,
			int serverNumber) {
		Log.d(TAG, "onServerChanged fragment = "
				+ fragment.getClass().getSimpleName() + ", number = "
				+ serverNumber);
		if (fragment == mApFragment) {
			mTableTitleView.setTableTitle(POSTION_AP_FRAGMENT,
					getString(R.string.ap_server_number, serverNumber));
		} else if (fragment == mWifiFragment) {
			mTableTitleView.setTableTitle(POSTION_WIFI_FRAGMENT,
					getWifiServerNumberTitle(serverNumber));
		}
	}

	private BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent
					.getAction())) {
				handleNetworkSate((NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));
			}
		}

		private void handleNetworkSate(NetworkInfo networkInfo) {
			if (networkInfo.isConnected()) {
				int serverNumber = mWifiFragment.getServerNumber();
				String ssid = mWifiManager.getConnectionInfo().getSSID();
				mTableTitleView.setTableTitle(POSTION_WIFI_FRAGMENT,
						getWifiServerNumberTitle(serverNumber));
			} else {
				int serverNumber = mWifiFragment.getServerNumber();
				mTableTitleView.setTableTitle(POSTION_WIFI_FRAGMENT,
						getWifiServerNumberTitle(serverNumber));
			}
		};
	};
}
