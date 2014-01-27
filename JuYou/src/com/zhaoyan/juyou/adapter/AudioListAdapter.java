package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.MediaInfo;

public class AudioListAdapter extends BaseAdapter implements SelectInterface{
	private static final String TAG = "AudioListAdapter";
	private LayoutInflater mInflater = null;
	private List<MediaInfo> mDataList;
	
	/**save status of item selected*/
	private SparseBooleanArray mCheckArray;
	/**current menu mode,ActionMenu.MODE_NORMAL,ActionMenu.MODE_EDIT*/
	private int mMenuMode = ActionMenu.MODE_NORMAL;

	public AudioListAdapter(Context context, List<MediaInfo> itemList){
		mInflater = LayoutInflater.from(context);
		mDataList = itemList;
		mCheckArray = new SparseBooleanArray();
		checkedAll(false);
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		ViewHolder holder = null;
		if (convertView != null) {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		} else {
			view = mInflater.inflate(R.layout.audio_item, null);
			holder = new ViewHolder();
			holder.iconView = (ImageView) view.findViewById(R.id.audio_icon);
			holder.titleView = (TextView) view.findViewById(R.id.audio_title);
			holder.artistView = (TextView) view.findViewById(R.id.audio_artist);
			holder.sizeView = (TextView) view.findViewById(R.id.audio_size);
			holder.timeView = (TextView) view.findViewById(R.id.audio_time);
			holder.sortView = (TextView) view.findViewById(R.id.tv_selection);
			view.setTag(holder);
		}
		
		updateViewBackground(position, view);

		holder.iconView.setImageResource(R.drawable.icon_audio);
		holder.titleView.setText(mDataList.get(position).getTitle());
		holder.artistView.setText(mDataList.get(position).getArtist());
		holder.sizeView.setText(mDataList.get(position).getFormatSize());
		holder.timeView.setText(ZYUtils.mediaTimeFormat(mDataList.get(position).getDuration()));
		
		String sortLetter = mDataList.get(position).getSortLetter();
		String preLetter = (position - 1) >= 0 ? 
				mDataList.get(position - 1).getSortLetter() : " ";
		
		if (!preLetter.equals(sortLetter)) {
			holder.sortView.setVisibility(View.VISIBLE);
			holder.sortView.setText(sortLetter);
		} else {
			holder.sortView.setVisibility(View.GONE);
		}
				
		return view;
	}
	
	public void updateViewBackground(int position, View view){
		boolean isChecked = isChecked(position);
		if (isChecked) {
			view.setBackgroundResource(R.color.holo_blue1);
		}else {
			view.setBackgroundResource(Color.TRANSPARENT);
		}
	}
	
	private class ViewHolder {
		public ImageView iconView;
		TextView titleView;
		TextView timeView;
		TextView artistView;
		TextView sizeView;
		TextView sortView;
	}


	@Override
	public void changeMode(int mode) {
		mMenuMode = mode;
	}

	@Override
	public boolean isMode(int mode) {
		return mMenuMode == mode;
	}

	@Override
	public void checkedAll(boolean isChecked) {
		int count = this.getCount();
		for (int i = 0; i < count; i++) {
			setChecked(i, isChecked);
		}
	}

	@Override
	public void setChecked(int position, boolean isChecked) {
		mCheckArray.put(position, isChecked);
	}

	@Override
	public void setChecked(int position) {
		mCheckArray.put(position, !isChecked(position));
	}

	@Override
	public boolean isChecked(int position) {
		return mCheckArray.get(position);
	}

	@Override
	public int getCheckedCount() {
		int count = 0;
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				count ++;
			}
		}
		return count;
	}

	@Override
	public List<Integer> getCheckedPosList() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				list.add(i);
			}
		}
		return list;
	}

	@Override
	public List<String> getCheckedNameList() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				list.add(mDataList.get(i).getDisplayName());
			}
		}
		return list;
	}

	@Override
	public List<String> getCheckedPathList() {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				list.add(mDataList.get(i).getUrl());
			}
		}
		return list;
	}

	@Override
	public void setIdleFlag(boolean flag) {
	}
	
}
 

