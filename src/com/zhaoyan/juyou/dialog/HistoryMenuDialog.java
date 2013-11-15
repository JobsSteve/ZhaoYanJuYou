package com.zhaoyan.juyou.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;

public class HistoryMenuDialog extends Dialog implements OnItemClickListener {

	private TextView mDialogTitle;
	private ListView mListView;
	
	private Context mContext;
	private ActionMenu mActionMenu;
	private String mTitle;
	
	private OnMenuItemClickListener mClickListener;
	
	public HistoryMenuDialog(Context context, String title, ActionMenu menu){
		super(context, R.style.Custom_Dialog);
		mContext = context;
		mActionMenu = menu;
		mTitle = title;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_history);
		
		mDialogTitle = (TextView) findViewById(R.id.tv_history_title);
		mListView = (ListView) findViewById(R.id.lv_history);
		mListView.setOnItemClickListener(this);
		
		setTitle(mTitle);
		
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
		
		setCanceledOnTouchOutside(true);
	}
	
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		WindowManager windowManager = getWindow().getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.width = (int)display.getWidth() - 100;
		getWindow().setAttributes(lp);
	}
	
	public void setTitle(String title){
		mDialogTitle.setText(title);
	}
	
	public void setTitle(int titleId){
		mDialogTitle.setText(titleId);
	}
	
	
	public interface OnMenuItemClickListener{
		public void onMenuClick(ActionMenuItem menuItem);
	}
	
	public void setOnMenuItemClickListener(OnMenuItemClickListener listener){
		mClickListener = listener;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			ActionMenuItem item = mActionMenu.getItem(position);
			mClickListener.onMenuClick(item);
			dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
