package com.zhaoyan.juyou.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.provider.AppData;

public class AppCursorAdapter extends BaseCursorAdapter {
	private static final String TAG = "AppCursorAdapter";
	private LayoutInflater inflater = null;
	private PackageManager pm = null;
	
	public AppCursorAdapter(Context context){
		super(context, null, true);
		inflater = LayoutInflater.from(context);
		pm = context.getPackageManager();
	}
	
	@Override
	public void checkedAll(boolean isChecked) {
		int count = this.getCount();
		for (int i = 0; i < count; i++) {
			setChecked(i, isChecked);
		}
	}
	
	/**
	 * get Select item pakcageName list
	 * @return
	 */
	public List<String> getCheckedPkgList(){
		Log.d(TAG, "getSelectedPkgList");
		List<String> list = new ArrayList<String>();
		Cursor cursor = getCursor();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				cursor.moveToPosition(i);
				String url = cursor.getString(cursor
						.getColumnIndex(AppData.App.PKG_NAME));
				list.add(url);
			}
		}
		return list;
	}
	
	/**
	 * get Select item installed path list
	 * @return
	 */
	@Override
	public List<String> getCheckedPathList(){
		Log.d(TAG, "getCheckedPathList");
		List<String> list = new ArrayList<String>();
		Cursor cursor = getCursor();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				cursor.moveToPosition(i);
				String packagename = cursor.getString(cursor
						.getColumnIndex(AppData.App.PKG_NAME));
				ApplicationInfo applicationInfo = null;
				try {
					applicationInfo = pm.getApplicationInfo(packagename, 0);
					list.add(applicationInfo.sourceDir);
				} catch (NameNotFoundException e) {
					Log.e(TAG, "getSelectItemPathList:" + packagename + " name not found.");
				}
			}
		}
		return list;
	}
	
	@Override
	public List<String> getCheckedNameList(){
		List<String> list = new ArrayList<String>();
		Cursor cursor = getCursor();
		for (int i = 0; i < mCheckArray.size(); i++) {
			if (mCheckArray.valueAt(i)) {
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor
						.getColumnIndex(AppData.App.PKG_NAME));
				list.add(name);
			}
		}
		return list;
	}
	
	@Override
	public Object getItem(int position) {
		return super.getItem(position);
	}
	
	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		
		final String packagename = cursor.getString(cursor.getColumnIndex(AppData.App.PKG_NAME));
		ApplicationInfo applicationInfo = null;
		try {
			applicationInfo = pm.getApplicationInfo(packagename, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.toString());
			return;
		}
		
		holder.iconView.setImageDrawable(applicationInfo.loadIcon(pm));
		holder.nameView.setText(applicationInfo.loadLabel(pm));
		long size = new File(applicationInfo.sourceDir).length();
		String version = "";
		try {
			version = pm.getPackageInfo(packagename, 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (Extra.VIEW_TYPE_LIST == mViewType) {
			holder.sizeView.setText("版本:" + version + "  " + ZYUtils.getFormatSize(size));
		} else {
			holder.sizeView.setText(ZYUtils.getFormatSize(size));
		}
		
		boolean isChecked = isChecked(cursor.getPosition());
		updateViewBackground(isChecked, cursor.getPosition(), view);
	}
	
	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup arg2) {
		View view  = null;
		if (Extra.VIEW_TYPE_LIST == mViewType) {
			view = inflater.inflate(R.layout.app_item_list, null);
		} else {
			view = inflater.inflate(R.layout.app_item_grid, null);
		}
		ViewHolder holder = new ViewHolder();
		
		holder.iconView = (ImageView) view.findViewById(R.id.app_icon_text_view);
		holder.nameView = (TextView) view.findViewById(R.id.app_name_textview);
		holder.sizeView = (TextView) view.findViewById(R.id.app_size_textview);
		view.setTag(holder);
		
		return view;
	}
	
	public class ViewHolder{
		public ImageView iconView;
		TextView nameView;
		TextView sizeView;
	}

}
