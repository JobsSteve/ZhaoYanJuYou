package com.zhaoyan.juyou.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.common.view.TransportAnimationView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenuInflater;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.ActionMenuInterface.OnMenuItemClickListener;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.common.MenuBarManager;

public class BaseActivity extends Activity implements OnMenuItemClickListener {
	private static final String TAG = "BaseActivity";
	// title view
	protected View mCustomTitleView;
	protected TextView mTitleNameView;
	protected TextView mTitleNumView;

	// menubar
	protected View mMenuBarView;
	protected LinearLayout mMenuHolder;
	protected MenuBarManager mMenuBarManager;
	protected ActionMenu mActionMenu;

	private ActionMenuInflater mActionMenuInflater;
	
	//视图模式
	protected int mViewType = Extra.VIEW_TYPE_DEFAULT;
	
	private boolean mHasFinishAnimation = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		SharedPreferences sp = SharedPreferenceUtil.getSharedPreference(getApplicationContext());
		mViewType = sp.getInt(Extra.View_TYPE, Extra.VIEW_TYPE_DEFAULT);
	}

	protected void initTitle(int titleName) {
		mCustomTitleView = findViewById(R.id.title);

		// title name view
		mTitleNameView = (TextView) mCustomTitleView
				.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(titleName);
		mTitleNumView = (TextView) mCustomTitleView
				.findViewById(R.id.tv_title_num);
	}

	protected void initMenuBar() {
		mMenuBarView = findViewById(R.id.menubar_bottom);
		mMenuBarView.setVisibility(View.GONE);
		mMenuHolder = (LinearLayout) findViewById(R.id.ll_menutabs_holder);

		mMenuBarManager = new MenuBarManager(getApplicationContext(),
				mMenuHolder);
		mMenuBarManager.setOnMenuItemClickListener(this);
	}

	public void startMenuBar() {
		mMenuBarView.setVisibility(View.VISIBLE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_up_in));
		mMenuBarManager.refreshMenus(mActionMenu);
	}

	public void destroyMenuBar() {
		mMenuBarView.setVisibility(View.GONE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_down_out));
	}

	protected void setTitleNumVisible(boolean visible) {
		mTitleNumView.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	protected void updateTitleNum(int selected, int count) {
		if (selected == -1) {
			mTitleNumView.setText(getString(R.string.num_format, count));
		} else {
			mTitleNumView.setText(getString(R.string.num_format2, selected,
					count));
		}
	}

	/**
	 * Show transport animation.
	 * 
	 * @param startViews
	 *            The transport item image view.
	 */
	protected void showTransportAnimation(ViewGroup viewGroup,
			ImageView... startViews) {
		Log.d(TAG, "showTransportAnimation");
		TransportAnimationView transportAnimationView = new TransportAnimationView(
				getApplicationContext());
		transportAnimationView.startTransportAnimation(viewGroup,
				mTitleNameView, startViews);
	}
	
	public void disableFinishAnimation() {
		mHasFinishAnimation = false;
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
				if (mHasFinishAnimation) {
					overridePendingTransition(0, R.anim.activity_right_out);
				}
				return true;
			} else {
				return false;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean onBackKeyPressed() {
		return true;
	}

	protected ActionMenuInflater getActionMenuInflater() {
		if (null == mActionMenuInflater) {
			mActionMenuInflater = new ActionMenuInflater(
					getApplicationContext());
		}
		return mActionMenuInflater;
	}

	/**
	 * start activity by class name
	 * 
	 * @param pClass
	 */
	protected void openActivity(Class<?> pClass) {
		openActivity(pClass, null);
	}

	/**
	 * start activity by class name & include data
	 * 
	 * @param pClass
	 * @param bundle
	 */
	protected void openActivity(Class<?> pClass, Bundle bundle) {
		Intent intent = new Intent(this, pClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		openActivity(intent);
	}
	
	protected void openActivity(Intent intent) {
		startActivity(intent);
		overridePendingTransition(R.anim.activity_right_in, 0);
	}

	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		// TODO Auto-generated method stub
	}
	
	public boolean isListView(){
		return Extra.VIEW_TYPE_LIST == mViewType;
	}
}
