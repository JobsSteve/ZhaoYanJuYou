package com.zhaoyan.juyou.fragment;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dreamlink.communication.aidl.User;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.UserManager.OnUserChangedListener;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.ConnectFriendsActivity;
import com.zhaoyan.juyou.activity.InviteActivity;

public class JuYouFragment extends Fragment implements OnClickListener,
		OnUserChangedListener {
	private Context mContext;
	private View mConnectView;
	private View mInviteView;
	private LinearLayout mConnectedUserPhotoView;

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
		mInviteView = rootView.findViewById(R.id.rl_juyou_invite);
		mInviteView.setOnClickListener(this);
		mConnectedUserPhotoView = (LinearLayout) rootView
				.findViewById(R.id.ll_juyou_connected_users);
	}

	private void updateConnectedUsers() {
		// Clear all user.
		mConnectedUserPhotoView.removeAllViews();
		// Add connected user.
		UserManager userManager = UserManager.getInstance();
		Map<Integer, User> users = userManager.getAllUser();
		for (Map.Entry<Integer, User> entry : users.entrySet()) {
			addUser(entry.getValue());
		}
	}

	private void addUser(User user) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.juyou_fragment_user_icon, null,
				false);
		mConnectedUserPhotoView.addView(view);
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
	};
}
