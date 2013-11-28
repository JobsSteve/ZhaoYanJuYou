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

import com.zhaoyan.juyou.R;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.common.FileIconHelper;
import com.zhaoyan.juyou.common.FileInfo;
import com.zhaoyan.juyou.common.FileListItem;
import com.zhaoyan.juyou.common.ZYConstant;

public class FileInfoAdapter extends BaseAdapter {
	private static final String TAG = "FileInfoAdapter";
	private List<FileInfo> mList = new ArrayList<FileInfo>();
	private LayoutInflater mInflater = null;
	private SparseBooleanArray mIsSelected = null;
	
	private FileIconHelper iconHelper;

	public int mMode = ZYConstant.MENU_MODE_NORMAL;
	
	private Context mContext;

	public FileInfoAdapter(Context context, List<FileInfo> list, FileIconHelper iconHelper) {
		mInflater = LayoutInflater.from(context);
		this.mList = list;
		mIsSelected = new SparseBooleanArray();
		this.iconHelper = iconHelper;
		mContext = context;
	}

	/**
	 * Select All or not
	 * 
	 * @param isSelected
	 *            true or false
	 */
	public void selectAll(boolean isSelected) {
		int count = this.getCount();
		Log.d(TAG, "selectALl.count=" + count);
		for (int i = 0; i < count; i++) {
			setSelected(i, isSelected);
		}
	}

	/**
	 * set selected or not
	 * 
	 * @param position
	 *            the position that clicked
	 * @param isSelected
	 *            checked or not
	 */
	public void setSelected(int position, boolean isSelected) {
		mIsSelected.put(position, isSelected);
	}

	public void setSelected(int position) {
		mIsSelected.put(position, !isSelected(position));
	}

	public void clearSelected() {
		for (int i = 0; i < mIsSelected.size(); i++) {
			if (mIsSelected.valueAt(i)) {
				setSelected(i, false);
			}
		}
	}

	/**
	 * return current position checked or not
	 * 
	 * @param position
	 *            current position
	 * @return checked or not
	 */
	public boolean isSelected(int position) {
		return mIsSelected.get(position);
	}

	/**
	 * get how many item that has cheked
	 * 
	 * @return checked items num.
	 */
	public int getSelectedItems() {
		int count = 0;
		for (int i = 0; i < mIsSelected.size(); i++) {
			if (mIsSelected.valueAt(i)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * get selected items fileinfo list
	 * @return
	 */
	public List<FileInfo> getSelectedFileInfos() {
		List<FileInfo> fileList = new ArrayList<FileInfo>();
		for (int i = 0; i < mIsSelected.size(); i++) {
			if (mIsSelected.valueAt(i)) {
				fileList.add(mList.get(i));
			}
		}
		return fileList;
	}

	/**
	 * get selected items filepath list
	 * @return
	 */
	public List<String> getSelectedFilePaths() {
		List<String> pathList = new ArrayList<String>();
		for (int i = 0; i < mIsSelected.size(); i++) {
			if (mIsSelected.valueAt(i)) {
				pathList.add(mList.get(i).filePath);
			}
		}

		return pathList;
	}

	/**
	 * get selected items position list
	 * @return
	 */
	public List<Integer> getSelectedItemPositions() {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < mIsSelected.size(); i++) {
			if (mIsSelected.valueAt(i)) {
				list.add(mIsSelected.keyAt(i));
			}
		}
		return list;
	}
	
	/**
	 * check if there is dir selected in selected list
	 * @return
	 */
	public boolean hasDirSelected(){
		for (int i = 0; i < mIsSelected.size(); i++) {
			if (mIsSelected.valueAt(i)) {
				if (mList.get(i).isDir) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * set scroll is idle or not
	 * 
	 * @param flag
	 */
	public void setFlag(boolean flag) {
//		this.mIdleFlag = flag;
	}

	/**
	 * This method changes the display mode of adapter between MODE_NORMAL,
	 * MODE_EDIT
	 * 
	 * @param mode
	 *            the mode which will be changed to be.
	 */
	public void changeMode(int mode) {
		mMode = mode;
	}

	/**
	 * This method checks that current mode equals to certain mode, or not.
	 * 
	 * @param mode
	 *            the display mode of adapter
	 * @return true for equal, and false for not equal
	 */
	public boolean isMode(int mode) {
		return mMode == mode;
	}

	public List<FileInfo> getList() {
		return mList;
	}

	public void setList(List<FileInfo> fileList) {
		mList = fileList;
	}

	/**
	 * This method gets index of certain fileInfo(item) in fileInfoList
	 * 
	 * @param fileInfo
	 *            the fileInfo which wants to be located.
	 * @return the index of the item in the listView.
	 */
	public int getPosition(FileInfo fileInfo) {
		Log.d(TAG, "getPosition:" + fileInfo.filePath);
		for (int i = 0; i < mList.size(); i++) {
			if (fileInfo.filePath.equals(mList.get(i).filePath)) {
				return i;
			}
		}
		return mList.indexOf(fileInfo);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public FileInfo getItem(int position) {
		if (mList.size() <= 0) {
			return null;
		}
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public class ViewHolder {
		public ImageView iconView;
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
					.findViewById(R.id.file_name_textview);
			holder.dateAndSizeView = (TextView) view
					.findViewById(R.id.file_info_textview);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}

		FileInfo fileInfo = mList.get(position);
		
		//20131128 yuri:use new way to load file icon
		FileListItem.setupFileListItemInfo(mContext, view, fileInfo, iconHelper);

		if (mMode == ZYConstant.MENU_MODE_EDIT) {
			updateListViewBackground(position, view);
		} else {
			view.setBackgroundResource(Color.TRANSPARENT);
		}

		return view;
	}

	private void updateListViewBackground(int position, View view) {
		if (isSelected(position)) {
			view.setBackgroundResource(R.color.holo_blue1);
		} else {
			view.setBackgroundResource(Color.TRANSPARENT);
		}
	}
}
