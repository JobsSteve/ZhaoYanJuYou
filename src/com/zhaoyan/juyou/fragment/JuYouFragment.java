package com.zhaoyan.juyou.fragment;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.common.view.ViewUtil;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.ConnectFriendsActivity;
import com.zhaoyan.juyou.activity.InviteActivity;

public class JuYouFragment extends BaseFragment implements OnClickListener,
		OnUserChangedListener {
	private Context mContext;
	private View mConnectView;
	private LinearLayout mConnectedUserViewLinearLayout;
	private View mConnectViewIcon;
	private View mConnectViewText;
	private View mInviteView;

	private static final int MSG_UPDATE_CONNECTED_USERS = 1;
	private Handler mHandler;

	private UserManager mUserManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		mHandler = new MyHandler();
		mUserManager = UserManager.getInstance();
		mUserManager.registerOnUserChangedListener(this);

		View rootView = inflater.inflate(R.layout.juyou_fragment, container,
				false);
		initTitle(rootView, R.string.juyou);
		initView(rootView);
		updateConnectedUsers();
		return rootView;
	}

	@Override
	public void onDestroyView() {
		mUserManager.unregisterOnUserChangedListener(this);
		mHandler = null;
		super.onDestroyView();
	}

	private void initView(View rootView) {
		mConnectView = rootView.findViewById(R.id.rl_juyou_connect);
		mConnectView.setOnClickListener(this);
		mConnectedUserViewLinearLayout = (LinearLayout) rootView
				.findViewById(R.id.ll_juyou_connected_users);
		mConnectViewIcon = rootView.findViewById(R.id.iv_juyou_connect_friends);
		mConnectViewText = rootView.findViewById(R.id.ll_juyou_connect_friends);

		mInviteView = rootView.findViewById(R.id.rl_juyou_invite);
		mInviteView.setOnClickListener(this);
	}

	private void updateConnectedUsers() {
		// Clear all user.
		mConnectedUserViewLinearLayout.removeAllViews();
		// Add connected user.
		UserManager userManager = UserManager.getInstance();
		Map<Integer, User> users = userManager.getAllUser();
		if (users.isEmpty()) {
			mConnectedUserViewLinearLayout.setVisibility(View.INVISIBLE);
			mConnectViewIcon.setVisibility(View.VISIBLE);
			mConnectViewText.setVisibility(View.VISIBLE);
		} else {
			mConnectedUserViewLinearLayout.setVisibility(View.VISIBLE);
			mConnectViewIcon.setVisibility(View.INVISIBLE);
			mConnectViewText.setVisibility(View.INVISIBLE);
			User[] usersSorted = UserHelper.sortUsersById(users);
			for (User user : usersSorted) {
				UserInfo userInfo = UserHelper.getUserInfo(mContext, user);
				if (userInfo != null) {
					addUser(userInfo);
				}
			}
		}
	}

	private void addUser(UserInfo userInfo) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.juyou_fragment_user_icon, null,
				false);
		ImageView headImageView = (ImageView) view
				.findViewById(R.id.iv_connected_user_head);
		int headId = userInfo.getHeadId();
		if (headId == UserInfo.HEAD_ID_NOT_PRE_INSTALL) {
			headImageView.setImageBitmap(userInfo.getHeadBitmap());
		} else {
			headImageView.setImageResource(UserHelper
					.getHeadImageResource(headId));
		}
		TextView nameTextView = (TextView) view
				.findViewById(R.id.tv_connected_user_name);
		if (userInfo.isLocal()) {
			nameTextView.setText(R.string.user_name_local);
		} else {
			nameTextView.setText(userInfo.getUser().getUserName());
		}
		LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layoutParams.rightMargin = ViewUtil.dp2px(mContext, 5);

		mConnectedUserViewLinearLayout.addView(view, layoutParams);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		switch (v.getId()) {
		case R.id.rl_juyou_connect:
			intent.setClass(getActivity(), ConnectFriendsActivity.class);
			startActivity(intent);
			getActivity()
					.overridePendingTransition(R.anim.activity_right_in, 0);
			break;
		case R.id.rl_juyou_invite:
			intent.setClass(getActivity(), InviteActivity.class);
			startActivity(intent);
			getActivity()
					.overridePendingTransition(R.anim.activity_right_in, 0);
			break;

		default:
			break;
		}
	}

	@Override
	public void onUserConnected(User user) {
		mHandler.obtainMessage(MSG_UPDATE_CONNECTED_USERS).sendToTarget();
	}

	@Override
	public void onUserDisconnected(User user) {
		mHandler.obtainMessage(MSG_UPDATE_CONNECTED_USERS).sendToTarget();
	}

	private class MyHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_CONNECTED_USERS:
				updateConnectedUsers();
				break;

			default:
				break;
			}
		}
	}
}
