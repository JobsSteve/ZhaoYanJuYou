package com.zhaoyan.juyou.dialog;

import java.util.ArrayList;
import java.util.List;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ContextMenuDialog extends ZyAlertDialog implements OnItemClickListener {

	private ActionMenu mActionMenu;
	private ListView mListView;
	private Context mContext;
	private OnMenuItemClickListener mListener;
	
	public ContextMenuDialog(Context context, ActionMenu actionMenu) {
		super(context);
		mContext = context;
		mActionMenu = actionMenu;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view  = getLayoutInflater().inflate(R.layout.dialog_contextmenu, null);
		mListView = (ListView) view.findViewById(R.id.lv_contextmenu);
		mListView.setOnItemClickListener(this);
		
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < mActionMenu.size(); i++) {
			try {
				list.add(mActionMenu.getItem(i).getTitle());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, 
				android.R.layout.simple_list_item_1, android.R.id.text1, list);
		mListView.setAdapter(adapter);
		
		setCancelable(true);
		setCustomView(view);
		setCustomViewMargins(0, 0, 0, 0);
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void show() {
		super.show();
		WindowManager windowManager = getWindow().getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.width = (int)display.getWidth() - 110;
		getWindow().setAttributes(lp);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		try {
			ActionMenuItem item = mActionMenu.getItem(position);
			mListener.onMenuItemClick(item);
			dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public interface OnMenuItemClickListener{
		public void onMenuItemClick(ActionMenuItem actionMenuItem);
	}
	
	public void setOnMenuItemClickListener(OnMenuItemClickListener listener){
		mListener = listener;
	}

}
