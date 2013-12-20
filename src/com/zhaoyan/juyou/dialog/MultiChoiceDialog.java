package com.zhaoyan.juyou.dialog;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.ActionMenuInterface.OnMenuItemClickListener;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MultiChoiceDialog extends ZyAlertDialog implements OnItemClickListener {

	private ActionMenu mActionMenu;
	private ListView mListView;
	private Context mContext;
	private OnMenuItemClickListener mListener;
	
	private boolean mShowIcon = false;
	private MultiChoiceAdapter mAdapter;
	
	private boolean mCheckedAll = false;
	
	public MultiChoiceDialog(Context context, ActionMenu actionMenu) {
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
		
		mAdapter = new MultiChoiceAdapter();
		mListView.setAdapter(mAdapter);
		
		setCanceledOnTouchOutside(true);
		setCustomView(view);
		setCustomViewMargins(0, 0, 0, 0);
		
		super.onCreate(savedInstanceState);
	}
	
	public SparseBooleanArray getChoiceArray(){
		return mAdapter.checkArray;
	}
	
	public void setCheckedAll(boolean checkedAll){
		mCheckedAll = checkedAll;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mAdapter.setChecked(position);
		mAdapter.notifyDataSetChanged();
		
		if (null != mListener) {
			ActionMenuItem item = mActionMenu.getItem(position);
			mListener.onMenuItemClick(item);
		}
	}
	
	public void setOnMenuItemClickListener(OnMenuItemClickListener listener){
		mListener = listener;
	}
	
	@Override
	public void show() {
		super.show();
	}
	
	class MultiChoiceAdapter extends BaseAdapter{
		SparseBooleanArray checkArray;
		
		MultiChoiceAdapter(){
			checkArray = new SparseBooleanArray(mActionMenu.size());
			checkedAll(mCheckedAll);
		}
		
		public void checkedAll(boolean checkedAll){
			for (int i = 0; i < mActionMenu.size(); i++) {
				setChecked(i, checkedAll);
			}
		}
		
		public void setChecked(int position, boolean checked){
			checkArray.put(position, checked);
		}
		
		public void setChecked(int position){
			checkArray.put(position, !isChecked(position));
		}
		
		public boolean isChecked(int position){
			return checkArray.get(position);
		}
		
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
			View view = getLayoutInflater().inflate(R.layout.dialog_multichoice_item, null);
			if (mShowIcon) {
				ImageView imageView = (ImageView) view.findViewById(R.id.iv_menu_icon);
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(mActionMenu.getItem(position).getIcon());
			}
			
			TextView textView = (TextView) view.findViewById(R.id.tv_menu_title);
			textView.setText(mActionMenu.getItem(position).getTitle());
			
			CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_menu);
			if (checkArray.valueAt(position)) {
				checkBox.setChecked(true);
			}else {
				checkBox.setChecked(false);
			}
			
			if (!mActionMenu.getItem(position).isEnable()) {
				textView.setTextColor(mContext.getResources().getColor(R.color.disable_color));
				checkBox.setEnabled(false);
				checkBox.setFocusable(true);
			}
			
			return view;
		}
		
	}

}
