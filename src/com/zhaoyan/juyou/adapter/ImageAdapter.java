package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.view.CheckableImageView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.AsyncPictureLoader;
import com.zhaoyan.juyou.common.ImageInfo;

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

	public ImageAdapter(Context context, List<ImageInfo> itemList){
		mInflater = LayoutInflater.from(context);
		mDataList = itemList;
		pictureLoader = new AsyncPictureLoader(context);
		mCheckArray = new SparseBooleanArray();
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
		View view = null;
		ViewHolder holder = null;
		if (convertView != null) {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		} else {
			view = mInflater.inflate(R.layout.image_item, null);
			holder = new ViewHolder();
			holder.imageView = (CheckableImageView) view.findViewById(R.id.iv_image_item);
			view.setTag(holder);
		}

		long id = mDataList.get(position).getImage_id();
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
	
	private class ViewHolder{
		CheckableImageView imageView;//folder icon
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
 

