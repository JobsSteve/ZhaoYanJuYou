package com.zhaoyan.juyou.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AccountSettingActivity;
import com.zhaoyan.juyou.activity.TrafficStatisticsActivity;

public class WoFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "WoFragment";
	private View mUserInfoSettingView;
	private View mQuitView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.wo_fragment, container, false);
		initTitle(rootView, R.string.wo);
		initView(rootView);
		return rootView;
	}

	private void initView(View rootView) {
		mUserInfoSettingView = rootView.findViewById(R.id.rl_wo_head_name);
		mUserInfoSettingView.setOnClickListener(this);
		mQuitView = rootView.findViewById(R.id.ll_wo_quit);
		mQuitView.setOnClickListener(this);
		
		View trafficView = rootView.findViewById(R.id.rl_wo_traffic_statistics);
		trafficView.setOnClickListener(this);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		switch (v.getId()) {
		case R.id.rl_wo_head_name:
			openActivity(AccountSettingActivity.class);
			break;
		case R.id.rl_wo_traffic_statistics:
			openActivity(TrafficStatisticsActivity.class);
			break;
		case R.id.ll_wo_quit:
			quit();
			break;

		default:
			break;
		}
	}

	private void quit() {
		getActivity().finish();
	}

}
