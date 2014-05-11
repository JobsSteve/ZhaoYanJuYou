package com.zhaoyan.juyou.backuprestore;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.juyou.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class BRItemAdapter extends BaseAdapter {

	private LayoutInflater mInflater = null;
	private List<PersonalItemData> mDataList = new ArrayList<PersonalItemData>();
	
	public BRItemAdapter(Context context, List<PersonalItemData> list){
		mInflater = LayoutInflater.from(context);
		mDataList = list;
	}
	
	public void changeData(List<PersonalItemData> list){
		mDataList = list;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
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
		return mDataList.get(position).getType();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = null;
		ViewHolder holder = null;
		
		if (convertView == null) {
			view = mInflater.inflate(R.layout.br_data_item, null);
			holder = new ViewHolder();
			holder.iconView = (ImageView) view.findViewById(R.id.iv_item_icon);
			holder.titleView = (TextView) view.findViewById(R.id.tv_item_title);
			holder.infoView = (TextView) view.findViewById(R.id.tv_item_info);
			holder.checkBox = (CheckBox) view.findViewById(R.id.cb_item_check);
			
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) convertView.getTag();
		}
		
		PersonalItemData item = mDataList.get(position);
		boolean bEnabled = item.isEnable();
		holder.iconView.setImageResource(item.getIconId());
		holder.titleView.setText(item.getTextId());
		holder.infoView.setText(item.getCount() + "");
		holder.checkBox.setChecked(item.isEnable());
		
		return view;
	}
	
	private class ViewHolder{
		ImageView iconView;
		TextView titleView;
		TextView infoView;
		CheckBox checkBox;
	}

}
