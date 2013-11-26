package com.zhaoyan.juyou.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AccountSettingActivity;
import com.zhaoyan.juyou.activity.TrafficStatisticsActivity;

public class WoFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "WoFragment";
	private View mUserInfoSettingView;
	private ImageView mHeadImageView;
	private TextView mNickNameTextView;
	private TextView mAccountTextView;
	private View mQuitView;
	private static final int REQUEST_SET_USER_INFO = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.wo_fragment, container, false);
		initTitle(rootView, R.string.wo);
		initView(rootView);
		updateUserInfo();
		return rootView;
	}

	private void initView(View rootView) {
		mUserInfoSettingView = rootView.findViewById(R.id.rl_wo_head_name);
		mUserInfoSettingView.setOnClickListener(this);
		mQuitView = rootView.findViewById(R.id.ll_wo_quit);
		mQuitView.setOnClickListener(this);

		View trafficView = rootView.findViewById(R.id.rl_wo_traffic_statistics);
		trafficView.setOnClickListener(this);

		mHeadImageView = (ImageView) rootView
				.findViewById(R.id.iv_wo_head_name);
		mNickNameTextView = (TextView) rootView
				.findViewById(R.id.tv_wo_nick_name);
		mAccountTextView = (TextView) rootView.findViewById(R.id.tv_wo_account);
	}

	private void updateUserInfo() {
		User user = UserHelper.loadLocalUser(mContext);
		int headId = user.getHeadId();
		if (headId != User.ID_NOT_PRE_INSTALL_HEAD) {
			mHeadImageView.setImageResource(UserHelper
					.getHeadImageResource(headId));
		} else {
			// TODO
			mHeadImageView.setImageResource(UserHelper.getHeadImageResource(0));
		}

		mNickNameTextView.setText(user.getUserName());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_wo_head_name:
			Intent intent = new Intent(getActivity(),
					AccountSettingActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivityForResult(intent, REQUEST_SET_USER_INFO);
			getActivity()
					.overridePendingTransition(R.anim.activity_right_in, 0);
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SET_USER_INFO:
			if (resultCode == Activity.RESULT_OK) {
				updateUserInfo();
			}
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void quit() {
		getActivity().finish();
	}

}
