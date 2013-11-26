package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.communication.search.ServerInfo;
import com.zhaoyan.juyou.R;

public class ServerAdapter extends BaseAdapter {
	private LayoutInflater mInflater = null;
	private ArrayList<ServerInfo> mServerData = new ArrayList<ServerInfo>();

	public ServerAdapter(Context context, ArrayList<ServerInfo> data) {
		mInflater = LayoutInflater.from(context);
		mServerData = data;
	}

	@Override
	public int getCount() {
		return mServerData.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;
		if (null == convertView || null == convertView.getTag()) {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.server_list_item, parent, false);

			holder.userIcon = (ImageView) view
					.findViewById(R.id.iv_sli_user_icon);
			holder.userName = (TextView) view
					.findViewById(R.id.tv_sli_user_name);
			holder.tipView = (TextView) view.findViewById(R.id.tv_sli_user_tip);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		holder.userIcon.setImageResource(R.drawable.head1);
		holder.userName.setText(mServerData.get(position).getServerName());
		holder.tipView.setText("点击加入");

		return view;
	}

	private class ViewHolder {
		ImageView userIcon;
		TextView userName;
		TextView tipView;
	}
}
