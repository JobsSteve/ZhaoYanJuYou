package com.zhaoyan.juyou.fragment;

import com.zhaoyan.juyou.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SearchConnectWifiFragment extends SearchConnectBaseFragment {
	private Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setInitView(R.string.search_wifi_tips);
		return view;
	}

	@Override
	protected void startSearch() {
		mConnectHelper.searchServer(this);
	}

	@Override
	protected void createServer() {
		mConnectHelper.createServer("wifi", this);
	}

	@Override
	protected int getServerTypeFilter() {
		return FILTER_SERVER_WIFI;
	}

}
