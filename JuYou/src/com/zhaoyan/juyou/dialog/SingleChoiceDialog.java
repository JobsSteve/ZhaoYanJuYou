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
import android.widget.RadioButton;
import android.widget.TextView;

public class SingleChoiceDialog extends ZyAlertDialog implements OnItemClickListener {

	private ActionMenu mActionMenu;
	private ListView mListView;
	private Context mContext;
	private OnMenuItemClickListener mListener;
	
	private boolean mShowIcon = false;
	private SingleChoiceAdapter mAdapter;
	
	private int mChoiceItemId;
	
	public SingleChoiceDialog(Context context, ActionMenu actionMenu) {
		super(context);
		mContext = context;
		mActionMenu = actionMenu;
		mChoiceItemId = actionMenu.getItem(0).getItemId();
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
		
		mAdapter = new SingleChoiceAdapter();
		mListView.setAdapter(mAdapter);
		
		setCanceledOnTouchOutside(true);
		setCustomView(view);
		setCustomViewMargins(0, 0, 0, 0);
		
		super.onCreate(savedInstanceState);
	}
	
	public int getChoiceItemId(){
		return mChoiceItemId;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mAdapter.setChoicePosition(position);
		mAdapter.notifyDataSetChanged();
		
		ActionMenuItem item = mActionMenu.getItem(position);
		mChoiceItemId = item.getItemId();
		
		if (null != mListener) {
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
	
	class SingleChoiceAdapter extends BaseAdapter{
		static final int default_choice_pos = 0;
		int choice_position = default_choice_pos;
		
		@Override
		public int getCount() {
			return mActionMenu.size();
		}
		
		public void setChoicePosition(int position){
			choice_position = position;
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
			View view = getLayoutInflater().inflate(R.layout.dialog_singlechoice_item, null);
			if (mShowIcon) {
				ImageView imageView = (ImageView) view.findViewById(R.id.iv_menu_icon);
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(mActionMenu.getItem(position).getIcon());
			}
			
			TextView textView = (TextView) view.findViewById(R.id.tv_menu_title);
			textView.setText(mActionMenu.getItem(position).getTitle());
			
			RadioButton radioButton = (RadioButton) view.findViewById(R.id.rb_menu);
			if (position == choice_position) {
				radioButton.setChecked(true);
			}else {
				radioButton.setChecked(false);
			}
			
			if (!mActionMenu.getItem(position).isEnable()) {
				textView.setTextColor(mContext.getResources().getColor(R.color.disable_color));
				radioButton.setEnabled(false);
				radioButton.setFocusable(true);
			}
			
			return view;
		}
		
	}

}
