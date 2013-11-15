package com.zhaoyan.juyou.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dreamlink.communication.lib.util.Notice;
import com.zhaoyan.common.view.TransportAnimationView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant;

public class BaseFragment extends Fragment{
//	protected UserManager mUserManager = null;
	protected Notice mNotice = null;
//	protected MainFragmentActivity mFragmentActivity;
	protected boolean mIsSelectAll = false;
	protected Context mContext = null;
	
	//title
	protected TextView mTitleNameView,mTitleNumView;
	private ViewGroup mViewGroup;
	
	/**
	 * current fragment file size
	 */
	protected int count = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mNotice = new Notice(mContext);
		setHasOptionsMenu(true);
	}
	
	protected void initTitle(View view, int title_resId){
		mViewGroup = (ViewGroup) view;
		mTitleNameView = (TextView) view.findViewById(R.id.tv_title_name);
		mTitleNameView.setText(title_resId);
		mTitleNumView = (TextView) view.findViewById(R.id.tv_title_num);
		mTitleNumView.setVisibility(View.VISIBLE);
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
	 * get current fragment file count
	 */
	public int getCount(){
		return count;
	}
	
	public int getSelectedCount(){
		return -1;
	}
	
	public int getMenuMode(){
		return ZYConstant.MENU_MODE_NORMAL;
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
	};
}
