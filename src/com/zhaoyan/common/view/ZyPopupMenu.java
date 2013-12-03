package com.zhaoyan.common.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;

public class ZyPopupMenu implements OnItemClickListener {
	private static final String TAG = "PopupView";
	private ArrayList<HashMap<String, Object>> itemList = new ArrayList<HashMap<String,Object>>();
    private Context context;
    private PopupWindow popupWindow ;
    private ListView listView;
    private LayoutInflater inflater;
    private PopupViewClickListener mListener;
    
    private List<ActionMenuItem> mMenuItemList = new ArrayList<ActionMenu.ActionMenuItem>();
    
    public interface PopupViewClickListener{
    	void onActionMenuItemClick(ActionMenuItem item);
    }
    
	public ZyPopupMenu(Context context, ActionMenu actionMenu){
		this.context= context;
		inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.popupmenu, null);
		listView = (ListView) view.findViewById(R.id.popup_view_listView);
		listView.setOnItemClickListener(this);
		
		for(int i = 0; i < actionMenu.size(); i++){
			try {
				Log.d(TAG, "title:" + actionMenu.getItem(i).getTitle());
				mMenuItemList.add(actionMenu.getItem(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		listView.setAdapter(new PopupMenuAdapter());
		
//		popupWindow = new PopupWindow(view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int width = context.getResources().getDisplayMetrics().widthPixels;
		int height = LayoutParams.WRAP_CONTENT;
		Log.d(TAG, "width=" + width+ ",heiht=" +height);
		popupWindow = new PopupWindow(view, width / 2, height);
		popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.dialog_full_holo_light));
		popupWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					popupWindow.dismiss();
					return true;
				}
				return false;
			}
		});
	}
	
	public void setOnPopupViewListener(PopupViewClickListener listener){
		mListener = listener;
	}
	
	
	public Object getItem(int position){
		return itemList.get(position);
	}
	
	//下拉式 弹出 pop菜单 parent 
	public void showAsDropDown(View parent, int xOff, int yOff) {
		popupWindow.showAsDropDown(parent, xOff, yOff);
		//focus
		popupWindow.setFocusable(true);
		//allow touchable outside
		popupWindow.setOutsideTouchable(true);
		//refresh state
		popupWindow.update();
	}
	
	/**
	 * @param parent anchor
	 * @param gravity loaction
	 * @param x offset
	 * @param y offset
	 */
	public void showAsLoaction(View parent, int gravity, int x, int y){
		popupWindow.showAtLocation(parent, gravity, x , y);
		
		//focus
		popupWindow.setFocusable(true);
		//allow touchable outside
		popupWindow.setOutsideTouchable(true);
		//refresh state
		popupWindow.update();
	}
    
	public void dismiss() {
		popupWindow.dismiss();
	}
	

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mListener.onActionMenuItemClick(mMenuItemList.get(position));
		popupWindow.dismiss();
	}
	
	class PopupMenuAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mMenuItemList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = inflater.inflate(R.layout.popupmenu_item, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.iv_popupmenu_icon);
			TextView textView = (TextView) view.findViewById(R.id.tv_popupmenu_text);
			
			ActionMenuItem menuItem = mMenuItemList.get(position);
			if (menuItem.getIcon() == 0) {
				imageView.setVisibility(View.GONE);
			}else {
				imageView.setImageResource(menuItem.getIcon());
			}
			
			textView.setText(menuItem.getTitle());
			return view;
		}
		
	}
}
