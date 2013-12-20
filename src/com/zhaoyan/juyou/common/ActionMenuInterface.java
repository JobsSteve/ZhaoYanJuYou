package com.zhaoyan.juyou.common;

import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;

public interface ActionMenuInterface {
	
	public interface OnMenuItemClickListener{
		public void onMenuItemClick(ActionMenuItem item);
	}
}
