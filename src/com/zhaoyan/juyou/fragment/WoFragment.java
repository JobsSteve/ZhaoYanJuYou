package com.zhaoyan.juyou.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AccountSettingActivity;

public class WoFragment extends Fragment implements OnClickListener {
	private static final String TAG = "WoFragment";
	private View mUserInfoSettingView;
	private View mQuitView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.wo_fragment, container, false);
		initView(rootView);
		return rootView;
	}

	private void initView(View rootView) {
		mUserInfoSettingView = rootView.findViewById(R.id.rl_userinfo_setting);
		mUserInfoSettingView.setOnClickListener(this);
		mQuitView = rootView.findViewById(R.id.ll_wo_quit);
		mQuitView.setOnClickListener(this);
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
		case R.id.rl_userinfo_setting:
			intent.setClass(getActivity(), AccountSettingActivity.class);
			startActivity(intent);
			getActivity()
					.overridePendingTransition(R.anim.activity_right_in, 0);
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
