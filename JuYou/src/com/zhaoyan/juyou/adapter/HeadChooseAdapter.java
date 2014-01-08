package com.zhaoyan.juyou.adapter;

import com.zhaoyan.juyou.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class HeadChooseAdapter extends BaseAdapter {
	private int[] mHeadImages;
	private LayoutInflater mLayoutInflater;

	public HeadChooseAdapter(Context context, int[] images) {
		mHeadImages = images;
		mLayoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mHeadImages.length;
	}

	@Override
	public Object getItem(int position) {
		return mHeadImages[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mLayoutInflater.inflate(R.layout.account_setting_head_item,
					null);
		}
		ImageView headImageView = (ImageView) view
				.findViewById(R.id.iv_ashi_head);
		headImageView.setImageResource(mHeadImages[position]);

		return view;
	}

}
