package com.zhaoyan.juyou.fragment;

import android.R.integer;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.juyou.AccountHelper;
import com.zhaoyan.juyou.AccountInfo;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AccountSettingActivity;
import com.zhaoyan.juyou.activity.TrafficStatisticsActivity;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.dialog.SingleChoiceDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;
import com.zhaoyan.juyou.provider.JuyouData;

public class WoFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "WoFragment";
	private View mAccountInfoSettingView;
	private Bitmap mHeadBitmap;
	private ImageView mHeadImageView;
	private TextView mNickNameTextView;
	private TextView mAccountTextView;
	private View mQuitView;
	
	//view type
	private View mViewSetting;
	private TextView mViewTip;
	private int mCurrentType = 0;

	private Handler mHandler;
	private static final int MSG_UPDATE_ACCOUNT_INFO = 1;
	private BroadcastReceiver mAccountInfoBroadcastReceiver;
	
	private SharedPreferences sp = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater
				.inflate(R.layout.wo_fragment, container, false);
		
		sp = SharedPreferenceUtil.getSharedPreference(mContext);
		mCurrentType = sp.getInt(Extra.View_TYPE, Extra.VIEW_TYPE_DEFAULT);
		initTitle(rootView, R.string.wo);
		initView(rootView);
		updateAccountInfo();

		mHandler = new UiHandler();

		mAccountInfoBroadcastReceiver = new UserInfoBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ZYConstant.CURRENT_ACCOUNT_CHANGED_ACTION);
		getActivity().registerReceiver(mAccountInfoBroadcastReceiver,
				intentFilter);
		
		return rootView;
	}

	private void initView(View rootView) {
		mAccountInfoSettingView = rootView.findViewById(R.id.rl_wo_head_name);
		mAccountInfoSettingView.setOnClickListener(this);
		mQuitView = rootView.findViewById(R.id.ll_wo_quit);
		mQuitView.setOnClickListener(this);

		View trafficView = rootView.findViewById(R.id.rl_wo_traffic_statistics);
		trafficView.setOnClickListener(this);

		mHeadImageView = (ImageView) rootView
				.findViewById(R.id.iv_wo_head_name);
		mNickNameTextView = (TextView) rootView
				.findViewById(R.id.tv_wo_nick_name);
		mAccountTextView = (TextView) rootView.findViewById(R.id.tv_wo_account);
		
		mViewSetting = rootView.findViewById(R.id.rl_wo_view_setting);
		mViewSetting.setOnClickListener(this);
		mViewTip = (TextView) rootView.findViewById(R.id.tv_wo_view_tip);
		setViewTypeText(mCurrentType);
	}

	private void updateAccountInfo() {
		AccountInfo accountInfo = AccountHelper.getCurrentAccount(mContext);
		int headId = accountInfo.getHeadId();
		if (headId != AccountInfo.HEAD_ID_NOT_PRE_INSTALL) {
			mHeadImageView.setImageResource(AccountHelper
					.getHeadImageResource(headId));
		} else {
			releaseHeadBitmap();
			mHeadBitmap = accountInfo.getHeadBitmap();
			mHeadImageView.setImageBitmap(mHeadBitmap);
		}

		mNickNameTextView.setText(accountInfo.getUserName());
		
		int touristAccount = accountInfo.getTouristAccount();
		if (touristAccount == JuyouData.Account.TOURIST_ACCOUNT_TRUE) {
			mAccountTextView.setText(R.string.wo_account_tourist);
		} else {
			mAccountTextView.setText(accountInfo.getAccountZhaoyan());
		}
	}

	private void releaseHeadBitmap() {
		if (mHeadBitmap != null) {
			mHeadImageView.setImageDrawable(null);
			mHeadBitmap.recycle();
			mHeadBitmap = null;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		releaseHeadBitmap();
		getActivity().unregisterReceiver(mAccountInfoBroadcastReceiver);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_wo_head_name:
			openActivity(AccountSettingActivity.class);
			break;
		case R.id.rl_wo_traffic_statistics:
			openActivity(TrafficStatisticsActivity.class);
			break;
		case R.id.rl_wo_view_setting:
			final Editor editor = sp.edit();
			ActionMenu actionMenu = new ActionMenu(mContext.getApplicationContext());
			actionMenu.addItem(1, 0, R.string.view_default);
			actionMenu.addItem(2, 0, R.string.view_list);
			actionMenu.addItem(3, 0, R.string.view_grid);
			final SingleChoiceDialog choiceDialog = new SingleChoiceDialog(mContext, mCurrentType, actionMenu);
			choiceDialog.setTitle(R.string.view_switch);
			choiceDialog.setPositiveButton(R.string.ok, new OnZyAlertDlgClickListener() {
				@Override
				public void onClick(Dialog dialog) {
					int itemId = choiceDialog.getChoiceItemId();
					switch (itemId) {
					case 1:
						mCurrentType = Extra.VIEW_TYPE_DEFAULT;
						break;
					case 2:
						mCurrentType = Extra.VIEW_TYPE_LIST;
						break;
					case 3:
						mCurrentType = Extra.VIEW_TYPE_GRID;
						break;
					}
					editor.putInt(Extra.View_TYPE, mCurrentType);
					editor.commit();
					setViewTypeText(mCurrentType);
					dialog.dismiss();
				}
			});
			choiceDialog.setNegativeButton(R.string.cancel, null);
			choiceDialog.show();
			break;
		case R.id.ll_wo_quit:
			quit();
			break;

		default:
			break;
		}
	}
	
	private void setViewTypeText(int type){
		switch (type) {
		case Extra.VIEW_TYPE_DEFAULT:
			mViewTip.setText(R.string.view_default);
			break;
		case Extra.VIEW_TYPE_LIST:
			mViewTip.setText(R.string.view_list);
			break;
		case Extra.VIEW_TYPE_GRID:
			mViewTip.setText(R.string.view_grid);
			break;
		default:
			break;
		}
	}

	private void quit() {
		getActivity().finish();
	}

	private class UserInfoBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			mHandler.obtainMessage(MSG_UPDATE_ACCOUNT_INFO).sendToTarget();
		}
	}

	private class UiHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_ACCOUNT_INFO:
				updateAccountInfo();
				break;

			default:
				break;
			}
		}
	}

}
