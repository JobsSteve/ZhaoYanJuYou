package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.FileHomeInfo;
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
	private List<FileHomeInfo> homeList = new ArrayList<FileHomeInfo>();
	private LayoutInflater mInflater = null;
	private Context mContext;

	public FileHomeAdapter(Context context, List<FileHomeInfo> homeList) {
		mInflater = LayoutInflater.from(context);
		this.homeList = homeList;
		mContext = context;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;

		if (null == convertView || null == convertView.getTag()) {
			view = mInflater.inflate(R.layout.file_home_item, parent, false);
		} else {
			view = convertView;
		}
		
		ImageView imageView = (ImageView) view.findViewById(R.id.iv_home_icon);
		TextView titleView = (TextView) view.findViewById(R.id.tv_home_title);
		TextView availableView = (TextView) view.findViewById(R.id.tv_available_size);
		TextView totalView = (TextView) view.findViewById(R.id.tv_total_size);

		switch (homeList.get(position).getStorageId()) {
		case FileBrowserFragment.INTERNAL:
			imageView.setImageResource(R.drawable.storage_internal_n);
			titleView.setText(R.string.internal_sdcard);
			break;
		case FileBrowserFragment.SDCARD:
			imageView.setImageResource(R.drawable.storage_sd_card_n);
			titleView.setText(R.string.sdcard);
			break;
		}
		availableView.setText(mContext.getString(R.string.storage_available, ZYUtils.getFormatSize(homeList.get(position).getAvailableSize())));
		totalView.setText(mContext.getString(R.string.storage_total, ZYUtils.getFormatSize(homeList.get(position).getTotalSize())));

		return view;
	}

}
