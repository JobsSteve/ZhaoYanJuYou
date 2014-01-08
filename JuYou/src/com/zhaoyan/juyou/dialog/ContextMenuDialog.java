package com.zhaoyan.juyou.dialog;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.ActionMenuInterface.OnMenuItemClickListener;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ContextMenuDialog extends ZyAlertDialog implements OnItemClickListener {

	private ActionMenu mActionMenu;
	private ListView mListView;
	private Context mContext;
	private OnMenuItemClickListener mListener;
	
	private boolean mShowIcon = false;
	
	public ContextMenuDialog(Context context, ActionMenu actionMenu) {
		super(context);
		mContext = context;
		mActionMenu = actionMenu;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View view  = getLayoutInflater().inflate(R.layout.dialog_contextmenu, null);
		mListView = (ListView) view.findViewById(R.id.lv_contextmenu);
		mListView.setOnItemClickListener(this);
		
		if (mActionMenu.getItem(0).getIcon() == 0) {
			mShowIcon = false;
		}else {
			mShowIcon = true;
		}
		
		ContextMenuAdapter adapter = new ContextMenuAdapter();
		mListView.setAdapter(adapter);
		
		setCanceledOnTouchOutside(true);
		setCustomView(view);
		setCustomViewMargins(0, 0, 0, 0);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void show() {
		super.show();
//		WindowManager windowManager = getWindow().getWindowManager();
//		Display display = windowManager.getDefaultDisplay();
//		WindowManager.LayoutParams lp = getWindow().getAttributes();
//		lp.width = (int)display.getWidth() - 40;
//		getWindow().setAttributes(lp);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ActionMenuItem item = mActionMenu.getItem(position);
		mListener.onMenuItemClick(item);
		dismiss();
	}
	
	public void setOnMenuItemClickListener(OnMenuItemClickListener listener){
		mListener = listener;
	}
	
	class ContextMenuAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mActionMenu.size();
		}

		@Override
		public ActionMenuItem getItem(int position) {
			return mActionMenu.getItem(position);
		}

		@Override
		public long getItemId(int position) {			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.dialog_contextmenu_item, null);
			if (mShowIcon) {
				ImageView imageView = (ImageView) view.findViewById(R.id.iv_menu_icon);
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(mActionMenu.getItem(position).getIcon());
			}
			
			TextView textView = (TextView) view.findViewById(R.id.tv_menu_title);
			textView.setText(mActionMenu.getItem(position).getTitle());
			
			return view;
		}
		
	}

}
