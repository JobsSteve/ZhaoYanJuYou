package com.zhaoyan.juyou.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaoyan.communication.connect.ServerCreator;
import com.zhaoyan.communication.search.ServerSearcher;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.provider.JuyouData;

public class SearchConnectApFragment extends SearchConnectBaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		setInitView(R.string.search_ap_tips);
		return view;
	}

	@Override
	protected int getServerUserType() {
		return JuyouData.User.TYPE_REMOTE_SEARCH_AP;
	}

	@Override
	protected int getServerSearchType() {
		return ServerSearcher.SERVER_TYPE_AP;
	}

	@Override
	protected int getServerCreateType() {
		return ServerCreator.TYPE_AP;
	}

}
