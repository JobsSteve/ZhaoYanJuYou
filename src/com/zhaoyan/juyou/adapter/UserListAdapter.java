package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.juyou.R;

/**
 * Adapter for network fragment user list view.
 * 
 */
public class UserListAdapter extends BaseAdapter {
	private ArrayList<UserInfo> mUserInfos;
	private LayoutInflater mInflater;

	public UserListAdapter(Context context, ArrayList<UserInfo> userInfos) {
		mInflater = LayoutInflater.from(context);
		mUserInfos = userInfos;
	}

	public void setData(ArrayList<UserInfo> userInfos) {
		mUserInfos = userInfos;
	}

	public ArrayList<UserInfo> getData() {
		return mUserInfos;
	}

	@Override
	public int getCount() {
		return mUserInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mUserInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.connected_info_item, null);

			holder = new ViewHolder();
			holder.userIcon = (ImageView) convertView
					.findViewById(R.id.iv_cii_user_icon);
			holder.userName = (TextView) convertView
					.findViewById(R.id.tv_cii_user_name);
			holder.userStatus = (TextView) convertView
					.findViewById(R.id.tv_cii_user_status);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.userIcon
				.setImageDrawable(mUserInfos.get(position).getUserIcon());
		holder.userName.setText(mUserInfos.get(position).getUserName());
		holder.userStatus.setText(mUserInfos.get(position).getUserStatus());
		return convertView;
	}

	private class ViewHolder {
		ImageView userIcon;
		TextView userName;
		TextView userStatus;
	}

}
