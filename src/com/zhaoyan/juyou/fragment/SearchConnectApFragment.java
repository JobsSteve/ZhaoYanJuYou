package com.zhaoyan.juyou.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.juyou.R;

public class SearchConnectApFragment extends SearchConnectBaseFragment {
	private Context mContext;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setInitView(R.string.search_ap_tips);
		return view;
	}

	@Override
	protected void startSearch() {
		mConnectHelper.searchServer(this);
	}

	@Override
	protected void createServer() {
		// Disable wifi AP first to avoid wifi AP name is not the newest.
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}
		mConnectHelper.createServer("wifi-ap", null);

	}
	
	@Override
	protected int getServerTypeFilter() {
		return FILTER_SERVER_AP;
	}
	
}
