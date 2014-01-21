package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.CharacterParser;

public class AudioCursorAdapter extends BaseCursorAdapter {
	private static final String TAG = "AudioCursorAdapter";
	private LayoutInflater mInflater = null;
	private CharacterParser mCharacterParser;

	public AudioCursorAdapter(Context context) {
		super(context, null, true);
		mInflater = LayoutInflater.from(context);
		mCharacterParser = CharacterParser.getInstance();
	}
	
	@Override
	public void checkedAll(boolean isChecked) {
		int count = this.getCount();
		for (int i = 0; i < count; i++) {
			setChecked(i, isChecked);
		}
	}
	
	@Override
	public List<String> getCheckedPathList() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = getCursor();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				cursor.moveToPosition(i);
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				list.add(url);
			}
		}
		return list;
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String title = cursor.getString((cursor
				.getColumnIndex(MediaStore.Audio.Media.TITLE))); // 音乐标题
		String artist = cursor.getString(cursor
				.getColumnIndex(MediaStore.Audio.Media.ARTIST)); // 艺术家
		long duration = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Audio.Media.DURATION)); // 时长
		long size = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Audio.Media.SIZE)); // 文件大小
		
		holder.iconView.setImageResource(R.drawable.icon_audio);
		holder.titleView.setText((cursor.getPosition() + 1) + "." + title);
		holder.artistView.setText(artist);
		holder.timeView.setText(ZYUtils.mediaTimeFormat(duration));
		holder.sizeView.setText(ZYUtils.getFormatSize(size));
		
		boolean isChecked = isChecked(cursor.getPosition());
		updateViewBackground(isChecked, cursor.getPosition(), view);
	}

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup arg2) {
		View view = mInflater.inflate(R.layout.audio_item, null);
		ViewHolder holder = new ViewHolder();
		holder.iconView = (ImageView) view.findViewById(R.id.audio_icon);
		holder.titleView = (TextView) view.findViewById(R.id.audio_title);
		holder.timeView = (TextView) view.findViewById(R.id.audio_time);
		holder.artistView = (TextView) view.findViewById(R.id.audio_artist);
		holder.sizeView = (TextView) view.findViewById(R.id.audio_size);
		holder.sortView = (TextView) view.findViewById(R.id.tv_selection);
		
		view.setTag(holder);
		return view;
	}
	
	public class ViewHolder {
		public ImageView iconView;
		TextView titleView;
		TextView timeView;
		TextView artistView;
		TextView sizeView;
		TextView sortView;
	}

}
