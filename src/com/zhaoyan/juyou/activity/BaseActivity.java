package com.zhaoyan.juyou.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoyan.common.view.TransportAnimationView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenuInflater;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.MenuBarManager;
import com.zhaoyan.juyou.common.MenuBarManager.onMenuItemClickListener;

public class BaseActivity extends Activity implements onMenuItemClickListener {

	// title view
	protected View mCustomTitleView;
	protected TextView mTitleNameView;
	protected TextView mTitleNumView;
	
	//menubar
	protected View mMenuBarView;
	protected LinearLayout mMenuHolder;
	protected MenuBarManager mMenuBarManager;
	protected ActionMenu mActionMenu;
	
	private ActionMenuInflater mActionMenuInflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	protected void initTitle(int titleName) {
		mCustomTitleView = findViewById(R.id.title);

		// title name view
		mTitleNameView = (TextView) mCustomTitleView
				.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(titleName);
		mTitleNumView = (TextView) mCustomTitleView.findViewById(R.id.tv_title_num);
	}
	
	protected void initMenuBar(){
		mMenuBarView = findViewById(R.id.menubar_bottom);
		mMenuBarView.setVisibility(View.GONE);
		mMenuHolder = (LinearLayout) findViewById(R.id.ll_menutabs_holder);
		
		mMenuBarManager = new MenuBarManager(getApplicationContext(), mMenuHolder);
		mMenuBarManager.setOnMenuItemClickListener(this);
	}
	
	public void startMenuBar(){
		mMenuBarView.setVisibility(View.VISIBLE);
		mMenuBarManager.refreshMenus(mActionMenu);
	}
	
	public void destroyMenuBar(){
		mMenuBarView.setVisibility(View.GONE);
	}
	
	protected void setTitleNumVisible(boolean visible){
		mTitleNumView.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
	
	protected void updateTitleNum(int selected, int count) {
		if (selected == -1) {
			mTitleNumView.setText(getString(R.string.num_format, count));
		}else {
			mTitleNumView.setText(getString(R.string.num_format2, selected, count));
		}
	}
	
	/**
	 * Show transport animation.
	 * 
	 * @param startViews The transport item image view.
	 */
	protected void showTransportAnimation(ViewGroup viewGroup, ImageView... startViews) {
		TransportAnimationView transportAnimationView = new TransportAnimationView(
				getApplicationContext());
		transportAnimationView.startTransportAnimation(viewGroup, mTitleNameView, startViews);
	}

	protected void finishWithAnimation() {
		finish();
		overridePendingTransition(0, R.anim.activity_right_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (onBackKeyPressed()) {
				finish();
				overridePendingTransition(0, R.anim.activity_right_out);
				return true;
			}else {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onBackKeyPressed(){
		return true;
	}

	@Override
	public void onMenuClick(ActionMenuItem item) {
		// TODO Auto-generated method stub
	}
	
	protected ActionMenuInflater getActionMenuInflater(){
		if (null == mActionMenuInflater) {
			mActionMenuInflater = new ActionMenuInflater(getApplicationContext());
		}
		return mActionMenuInflater;
	}
}
