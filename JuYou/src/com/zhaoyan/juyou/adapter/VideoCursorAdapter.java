package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.AsyncVideoLoader;
import com.zhaoyan.juyou.common.AsyncVideoLoader.ILoadVideoCallback;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.common.VideoGridItem;

public class VideoCursorAdapter extends BaseCursorAdapter {
	private static final String TAG = "VideoCursorAdapter";
	private LayoutInflater mInflater = null;
	private AsyncVideoLoader asyncVideoLoader;
	private boolean mIdleFlag = true;

	public VideoCursorAdapter(Context context) {
		super(context, null, true);
		mInflater = LayoutInflater.from(context);
		asyncVideoLoader = new AsyncVideoLoader(context);
	}
	
	public void setIdleFlag(boolean flag){
		this.mIdleFlag = flag;
	}
	
	@Override
	public void checkedAll(boolean isChecked) {
		int count = this.getCount();
		for (int i = 0; i < count; i++) {
			setChecked(i, isChecked);
		}
	}
	
	@Override
	public List<String> getCheckedPathList(){
		List<String> list = new ArrayList<String>();
		Cursor cursor = getCursor();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				cursor.moveToPosition(i);
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Video.Media.DATA));
				list.add(url);
			}
		}
		return list;
	}
	
	@Override
	public List<String> getCheckedNameList() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = getCursor();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor
						.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
				list.add(name);
			}
		}
		return list;
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		if (Extra.VIEW_TYPE_LIST == mViewType) {
			final ViewHolder holder = (ViewHolder) view.getTag();
			long id = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Video.Media._ID));
			long duration = cursor.getLong(cursor
					.getColumnIndex(MediaStore.Video.Media.DURATION)); // 时长
			String name = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
			long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
			
			if (!mIdleFlag) {
				if (AsyncVideoLoader.bitmapCaches.size() > 0
						&& AsyncVideoLoader.bitmapCaches.get(id) != null) {
					holder.iconView.setImageBitmap(AsyncVideoLoader.bitmapCaches.get(id)
							.get());
				} else {
					holder.iconView.setImageResource(R.drawable.default_video_iv);
				}
			} else {
				Bitmap bitmap = asyncVideoLoader.loadBitmap(id, new ILoadVideoCallback() {
					@Override
					public void onObtainBitmap(Bitmap bitmap, long id) {
						holder.iconView.setImageBitmap(bitmap);
					}
				});
				
				if (null == bitmap) {
					//在图片没有读取出来的情况下预先放一张图
					holder.iconView.setImageResource(R.drawable.default_video_iv);
				}else {
					holder.iconView.setImageBitmap(bitmap);
				}
			}
			
			holder.nameView.setText(name);
			holder.timeView.setText(ZYUtils.mediaTimeFormat(duration) + "  "
					+ ZYUtils.getFormatSize(size));
			
			boolean isSelected = isChecked(cursor.getPosition());
			updateViewBackground(isSelected, cursor.getPosition(), view);
			
			return;
		}
		
		final VideoGridItem item = (VideoGridItem) view;
		long id = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Video.Media._ID));
		long duration = cursor.getLong(cursor
				.getColumnIndex(MediaStore.Video.Media.DURATION)); // 时长
		
		if (!mIdleFlag) {
			if (AsyncVideoLoader.bitmapCaches.size() > 0
					&& AsyncVideoLoader.bitmapCaches.get(id) != null) {
				item.setIconBitmap(AsyncVideoLoader.bitmapCaches.get(id)
						.get());
			} else {
				item.setIconResId(R.drawable.default_video_iv);
			}
		}else {
			Bitmap bitmap = asyncVideoLoader.loadBitmap(id,
					new ILoadVideoCallback() {
						@Override
						public void onObtainBitmap(Bitmap bitmap, long id) {
							item.setIconBitmap(bitmap);
						}
					});

			if (null == bitmap) {
				// 在图片没有读取出来的情况下预先放一张图
				item.setIconResId(R.drawable.default_video_iv);
			} else {
				item.setIconBitmap(bitmap);
			}
		}

		item.setVideoTime(duration);
		item.setChecked(mCheckArray.get(cursor.getPosition()));
	}

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup arg2) {
		if (Extra.VIEW_TYPE_LIST == mViewType) {
			View view = mInflater.inflate(R.layout.video_item_list, null);
			ViewHolder holder = new ViewHolder();
			holder.iconView = (ImageView) view.findViewById(R.id.iv_video_icon);
			holder.nameView = (TextView) view.findViewById(R.id.tv_video_name);
			holder.timeView = (TextView) view.findViewById(R.id.tv_video_time);
			view.setTag(holder);
			return view;
		}
		
		VideoGridItem item = new VideoGridItem(arg0);
		//do not set layoutParams here,because 2.3 will force close by Java.lang.ClassCastException
//		item.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		return item;
	}
	
	public class ViewHolder{
		ImageView iconView;
		TextView nameView;
		TextView timeView;
	}

}
