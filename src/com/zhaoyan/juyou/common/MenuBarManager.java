package com.zhaoyan.juyou.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;

public class MenuBarManager implements OnClickListener {
	private static final String TAG  = "MenuTabManager";
	
	private LinearLayout mMenuHolders;
	private LayoutInflater mInflater;
	private List<ActionMenuItem> items = new ArrayList<ActionMenu.ActionMenuItem>();
	private onMenuItemClickListener mListener; 
	private int enable_color;
	private int disable_color;
	
	public MenuBarManager(Context context, View rootView){
		mInflater = LayoutInflater.from(context);
		mMenuHolders = (LinearLayout) rootView.findViewById(R.id.ll_menutabs_holder);
	}
	
	public MenuBarManager(Context context, LinearLayout menuHodlers){
		mInflater = LayoutInflater.from(context);
		mMenuHolders = menuHodlers;
		
		enable_color = context.getResources().getColor(R.color.black);
		disable_color = context.getResources().getColor(R.color.disable_color);
	}
	
	public void setOnMenuItemClickListener(onMenuItemClickListener listener){
		this.mListener = listener;
	}
	
	public void refreshMenus(ActionMenu actionMenu){
		Log.d(TAG, "refreshMenus:" + actionMenu.size());
		int count = mMenuHolders.getChildCount();
		mMenuHolders.removeViews(0, count);
		items.clear();
		
		for (int i = 0; i < actionMenu.size(); i++) {
			ActionMenuItem item = null;
			try {
				item = actionMenu.getItem(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			addMenuItem(item);
		}
	}
	
	public void addMenuItem(ActionMenuItem item){
//		Log.d(TAG, "addMenuItem.item:" + item.getTitle());
		View view = null;
		view = mInflater.inflate(R.layout.menubar_bottom_item, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1.0f));
		ImageView imageView = (ImageView) view.findViewById(R.id.iv_menu_icon);
		TextView textView = (TextView) view.findViewById(R.id.tv_menu_name);
		int icon = item.getIcon();
		String name = item.getTitle().toString();
		imageView.setImageResource(icon);
		textView.setText(name);
		view.setId(items.size());
		view.setOnClickListener(this);
		view.setEnabled(item.isEnable());
		textView.setTextColor(item.isEnable() ? enable_color : disable_color);
		mMenuHolders.addView(view);
		items.add(item);
	}
	
	public void refresh(int id){
		mMenuHolders.findViewById(id).setEnabled(false);
	}
	
	public void enableMenuBar(boolean enable){
		for (int i = 0; i < items.size(); i++) {
			mMenuHolders.findViewById(i).setEnabled(enable);
		}
	}
	
	@Override
	public void onClick(View v) {
		ActionMenuItem item = items.get(v.getId());
		mListener.onMenuClick(item);
	}
	
	public interface onMenuItemClickListener{
		public void onMenuClick(ActionMenuItem item);
	}
}
