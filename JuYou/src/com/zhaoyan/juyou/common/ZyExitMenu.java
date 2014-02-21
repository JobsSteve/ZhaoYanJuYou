package com.zhaoyan.juyou.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.zhaoyan.juyou.R;

public class ZyExitMenu extends PopupWindow implements OnClickListener {

	private OnMenuItemClickListener mListener;
	private LinearLayout mLayout;
	private View mExitView;
	private Menu menu;
	
	public void setOnMenuItemClick(OnMenuItemClickListener listener){
		mListener = listener;
	}
	
	/**
	 * @param context
	 * @param menu Menu object
	 * @param myMenuAnim animation
	 */
	public ZyExitMenu(Context context, Menu menu,
			int myMenuAnim) {
		super(context);
		
		this.menu = menu;
		LayoutInflater inflater = LayoutInflater.from(context);
		mLayout = (LinearLayout) inflater.inflate(R.layout.custommenu, null);
		mExitView = mLayout.findViewById(R.id.rl_exit);
		mExitView.setOnClickListener(this);

		setContentView(this.mLayout);
		
		setWidth(LayoutParams.FILL_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
//		setBackgroundDrawable(new ColorDrawable(Color.argb(255, 139, 106, 47)));
		setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		setAnimationStyle(myMenuAnim);
		setFocusable(true);

		mLayout.setFocusableInTouchMode(true);
		mLayout.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_MENU && isShowing()) {
					dismiss();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onClick(View v) {
		mListener.onMenuItemClick(null);
		dismiss();
	}

}
