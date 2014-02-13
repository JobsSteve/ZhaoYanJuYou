package com.zhaoyan.juyou.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.file.ZyMediaScanner;
import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.common.view.TransportAnimationView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ActionMenu.ActionMenuItem;
import com.zhaoyan.juyou.common.ActionMenuInflater;
import com.zhaoyan.juyou.common.ActionMenuInterface.OnMenuItemClickListener;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.common.MenuBarManager;

public class BaseFragment extends Fragment implements OnMenuItemClickListener{
	protected Notice mNotice = null;
	protected boolean mIsSelectAll = false;
	protected Context mContext = null;
	
	//title
	protected TextView mTitleNameView,mTitleNumView;
	private ViewGroup mViewGroup;
	
	//menubar
	protected View mMenuBarView;
	protected LinearLayout mMenuHolder;
	protected MenuBarManager mMenuBarManager;
	protected ActionMenu mActionMenu;
	
	private ActionMenuInflater mActionMenuInflater;
	
	/**
	 * current fragment file size
	 */
	protected int count = 0;
	
	//视图模式
	protected int mViewType = Extra.VIEW_TYPE_DEFAULT;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mNotice = new Notice(mContext);
		
		//get View Type
		SharedPreferences sp = SharedPreferenceUtil
				.getSharedPreference(getActivity().getApplicationContext());
		mViewType = sp.getInt(Extra.View_TYPE, Extra.VIEW_TYPE_DEFAULT);
				
		setHasOptionsMenu(true);
	}
	
	protected void initTitle(View view, int title_resId){
		mViewGroup = (ViewGroup) view;
		mTitleNameView = (TextView) view.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(title_resId);
		mTitleNumView = (TextView) view.findViewById(R.id.tv_title_num);
		mTitleNumView.setVisibility(View.VISIBLE);
	}
	
	protected void initMenuBar(View view){
		mMenuBarView = view.findViewById(R.id.menubar_bottom);
		mMenuBarView.setVisibility(View.GONE);
		mMenuHolder = (LinearLayout) view.findViewById(R.id.ll_menutabs_holder);
		
		mMenuBarManager = new MenuBarManager(getActivity().getApplicationContext(), mMenuHolder);
		mMenuBarManager.setOnMenuItemClickListener(this);
	}
	
	public void startMenuBar(){
		mMenuBarView.setVisibility(View.VISIBLE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.grow_from_bottom));
		mMenuBarManager.refreshMenus(mActionMenu);
	}
	
	public void destroyMenuBar(){
		mMenuBarView.setVisibility(View.GONE);
		mMenuBarView.clearAnimation();
		mMenuBarView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.shrink_from_top));
	}
	
	protected void updateTitleNum(int selected){
		if (isAdded()) {
			if (selected == -1) {
				mTitleNumView.setText(getString(R.string.num_format, count));
			}else {
				mTitleNumView.setText(getString(R.string.num_format2, selected, count));
			}
		}
	}
	
	/**
	 * Show transport animation.
	 * 
	 * @param startViews The transport item image view.
	 */
	public void showTransportAnimation(ImageView... startViews) {
		TransportAnimationView transportAnimationView = new TransportAnimationView(
				mContext);
		transportAnimationView.startTransportAnimation(mViewGroup,
				mTitleNameView, startViews);
	}
	
	/**
	 * start activity by class name
	 * @param pClass
	 */
	protected void openActivity(Class<?> pClass){
		openActivity(pClass, null);
	}
	
	/**
	 * start activity by class name & include data
	 * @param pClass
	 * @param bundle
	 */
	protected void openActivity(Class<?> pClass, Bundle bundle){
		Intent intent = new Intent(getActivity(), pClass);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.activity_right_in, 0);
	}
	
	/**
	 * when user pressed back key
	 */
	public boolean onBackPressed(){
		return true;
	}
	
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onMenuItemClick(ActionMenuItem item) {
		// TODO Auto-generated method stub
		
	};
	
	protected ActionMenuInflater getActionMenuInflater(){
		if (null == mActionMenuInflater) {
			mActionMenuInflater = new ActionMenuInflater(getActivity().getApplicationContext());
		}
		return mActionMenuInflater;
	}
	
	protected boolean isListView(){
		return Extra.VIEW_TYPE_LIST == mViewType;
	}
}
