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

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.view.CheckableImageView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.AsyncPictureLoader;
import com.zhaoyan.juyou.common.ImageInfo;
import com.zhaoyan.juyou.common.ZYConstant.Extra;

public class ImageAdapter extends BaseAdapter implements SelectInterface{
	private static final String TAG = "ImageAdapter";
	private LayoutInflater mInflater = null;
	private List<ImageInfo> mDataList;
	private AsyncPictureLoader pictureLoader;
	
	private boolean mIdleFlag = true;
	
	/**save status of item selected*/
	private SparseBooleanArray mCheckArray;
	/**current menu mode,ActionMenu.MODE_NORMAL,ActionMenu.MODE_EDIT*/
	private int mMenuMode = ActionMenu.MODE_NORMAL;
	
	private int mViewType = Extra.VIEW_TYPE_DEFAULT;

	public ImageAdapter(Context context, int viewType, List<ImageInfo> itemList){
		mInflater = LayoutInflater.from(context);
		mDataList = itemList;
		pictureLoader = new AsyncPictureLoader(context);
		mCheckArray = new SparseBooleanArray();
		
		mViewType = viewType;
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (Extra.VIEW_TYPE_LIST == mViewType) {
			return getListView(convertView, position);
		} 
		
		return getGridView(convertView, position);
	}
	
	private View getGridView(View convertView, int position){
		View view = null;
		ViewHolder holder = null;
		if (convertView != null) {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		} else {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.image_item_grid, null);
			holder.imageView = (CheckableImageView) view.findViewById(R.id.iv_image_item);
			view.setTag(holder);
		}

		long id = mDataList.get(position).getImageId();
		if (mIdleFlag) {
			pictureLoader.loadBitmap(id, holder.imageView);
			holder.imageView.setChecked(mCheckArray.get(position));
		} else {
			if (AsyncPictureLoader.bitmapCaches.size() > 0
					&& AsyncPictureLoader.bitmapCaches.get(id) != null) {
				holder.imageView.setImageBitmap(AsyncPictureLoader.bitmapCaches
						.get(id).get());
			} else {
				holder.imageView.setImageResource(R.drawable.photo_l);
			}
			holder.imageView.setChecked(mCheckArray.get(position));
		}
		return view;
	}
	
	private View getListView(View convertView, int position){
		View view = null;
		ViewHolder holder = null;
		if (convertView != null) {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		} else {
			holder = new ViewHolder();
			view = mInflater.inflate(R.layout.image_item_list, null);
			holder.imageView2 = (ImageView) view.findViewById(R.id.iv_image_item);
			holder.nameView = (TextView) view.findViewById(R.id.tv_image_name);
			holder.sizeView = (TextView) view.findViewById(R.id.tv_image_size);
			view.setTag(holder);
		}
		
		long id = mDataList.get(position).getImageId();
		String name = mDataList.get(position).getDisplayName();
		String size = mDataList.get(position).getFormatSize();
		
		holder.nameView.setText(name);
		holder.sizeView.setText(size);
		
		
		if (mIdleFlag) {
			pictureLoader.loadBitmap(id, holder.imageView2);
		} else {
			if (AsyncPictureLoader.bitmapCaches.size() > 0
					&& AsyncPictureLoader.bitmapCaches.get(id) != null) {
				holder.imageView2.setImageBitmap(AsyncPictureLoader.bitmapCaches
						.get(id).get());
			} else {
				holder.imageView2.setImageResource(R.drawable.photo_l);
			}
		}
		
		updateViewBackground(position, view);
		return view;
	}
	
	public void updateViewBackground(int position, View view){
		boolean selected = isChecked(position);
		if (selected) {
			view.setBackgroundResource(R.color.holo_blue1);
		}else {
			view.setBackgroundResource(Color.TRANSPARENT);
		}
	}
	
	private class ViewHolder{
		CheckableImageView imageView;//folder icon
		
		ImageView imageView2;
		TextView nameView;
		TextView sizeView;
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
				list.add(mDataList.get(i).getPath());
			}
		}
		return list;
	}

	@Override
	public void setIdleFlag(boolean flag) {
		this.mIdleFlag = flag;
	}
}
 

