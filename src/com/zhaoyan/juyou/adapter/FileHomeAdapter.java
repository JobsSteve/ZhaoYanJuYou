package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.fragment.FileBrowserFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileHomeAdapter extends BaseAdapter {
	private static final String TAG = "FileHomeAdapter";
	private List<Integer> homeList = new ArrayList<Integer>();
	private LayoutInflater mInflater = null;

	public FileHomeAdapter(Context context, List<Integer> homeList) {
		mInflater = LayoutInflater.from(context);
		this.homeList = homeList;
	}

	@Override
	public int getCount() {
		return homeList.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	class ViewHolder {
		ImageView iconView;
		TextView nameView;
		TextView dateAndSizeView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;

		if (null == convertView || null == convertView.getTag()) {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.file_item, parent, false);
			holder.iconView = (ImageView) view
					.findViewById(R.id.file_icon_imageview);
			holder.nameView = (TextView) view
					.findViewById(R.id.tv_filename);
			holder.dateAndSizeView = (TextView) view
					.findViewById(R.id.tv_fileinfo);
			holder.dateAndSizeView.setVisibility(View.GONE);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		switch (homeList.get(position)) {
		case FileBrowserFragment.INTERNAL:
			holder.iconView.setImageResource(R.drawable.storage_internal_n);
			holder.nameView.setText(R.string.internal_sdcard);
			break;
		case FileBrowserFragment.SDCARD:
			holder.iconView.setImageResource(R.drawable.storage_sd_card_n);
			holder.nameView.setText(R.string.sdcard);
			break;
		}

		return view;
	}

}
