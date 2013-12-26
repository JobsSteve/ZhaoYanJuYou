package com.zhaoyan.juyou.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.common.view.BadgeView;
import com.zhaoyan.communication.FileTransferService;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AppActivity;
import com.zhaoyan.juyou.activity.AudioActivity;
import com.zhaoyan.juyou.activity.ConnectFriendsActivity;
import com.zhaoyan.juyou.activity.FileCategoryActivity;
import com.zhaoyan.juyou.activity.FileBrowserActivity;
import com.zhaoyan.juyou.activity.GameActivity;
import com.zhaoyan.juyou.activity.HistoryActivity;
import com.zhaoyan.juyou.activity.ImageActivity;
import com.zhaoyan.juyou.activity.InviteActivity;
import com.zhaoyan.juyou.activity.VideoActivity;
import com.zhaoyan.juyou.common.ActionMenu;
import com.zhaoyan.juyou.dialog.MultiChoiceDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;

public class GuanJiaFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "GuanJiaFragment";
	
	private GuanjiaReceiver mGuanjiaReceiver = null;
	
	//items
	private BadgeView badgeView;
	private SharedPreferences sp;
	private static final String SHOW_BADGEVIEW = "show_badgeview";
	
	class GuanjiaReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			boolean show = intent.getBooleanExtra(FileTransferService.EXTRA_BADGEVIEW_SHOW, false);
			Log.d(TAG, "onReceive:action=" + action);
			if (show) {
				badgeView.show();
			}else {
				badgeView.hide();
			}
			Editor editor = sp.edit();
			editor.putBoolean(SHOW_BADGEVIEW, show);
			editor.commit();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter(FileTransferService.ACTION_NOTIFY_SEND_OR_RECEIVE);
		mGuanjiaReceiver = new GuanjiaReceiver();
		getActivity().getApplicationContext().registerReceiver(mGuanjiaReceiver, filter);
		
		sp = SharedPreferenceUtil.getSharedPreference(getActivity().getApplicationContext());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.guanjia_fragment, container, false);
		initTitle(rootView, R.string.guan_jia);
		initView(rootView);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//need update whether show badgeview at histroy view
		boolean showBadgeView = sp.getBoolean(SHOW_BADGEVIEW, false);
		if (showBadgeView) {
			badgeView.show();
		}else {
			badgeView.hide();
		}
	}
	
	public void initView(View view){
		View musicView = view.findViewById(R.id.rl_guanjia_music);
		musicView.setOnClickListener(this);
		
		View historyView = view.findViewById(R.id.rl_guanjia_history);
		historyView.setOnClickListener(this);
		
		badgeView = new BadgeView(mContext, view.findViewById(R.id.iv_guanjia_history));
		badgeView.setBackgroundResource(R.drawable.assist_block_count_bk);
		badgeView.setPadding(0, 0, 0, 0);
		badgeView.setText("");
		
		View videoView = view.findViewById(R.id.rl_guanjia_video);
		videoView.setOnClickListener(this);
		
		View appView = view.findViewById(R.id.rl_guanjia_application);
		appView.setOnClickListener(this);
		
		View gameView = view.findViewById(R.id.rl_guanjia_game);
		gameView.setOnClickListener(this);
		
		View fileBrowserView = view.findViewById(R.id.rl_guanjia_file_all);
		fileBrowserView.setOnClickListener(this);
		
		View docView = view.findViewById(R.id.rl_guanjia_file_document);
		docView.setOnClickListener(this);
		
		View archiveView = view.findViewById(R.id.rl_guanjia_file_compressed);
		archiveView.setOnClickListener(this);
		
		View apkView = view.findViewById(R.id.rl_guanjia_file_apk);
		apkView.setOnClickListener(this);
		
		View photoView = view.findViewById(R.id.rl_guanjia_photo);
		photoView.setOnClickListener(this);
		
		View galleryView = view.findViewById(R.id.rl_guanjia_picture);
		galleryView.setOnClickListener(this);
		
		View inviteView = view.findViewById(R.id.rl_guanjia_invite);
		inviteView.setOnClickListener(this);
		
		View connectView = view.findViewById(R.id.rl_guanjia_connect);
		connectView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_guanjia_music:
			openActivity(AudioActivity.class);
			break;
		case R.id.rl_guanjia_history:
			openActivity(HistoryActivity.class);
			break;
		case R.id.rl_guanjia_video:
			openActivity(VideoActivity.class);
			break;
		case R.id.rl_guanjia_application:
			openActivity(AppActivity.class);
			break;
		case R.id.rl_guanjia_game:
			openActivity(GameActivity.class);
			break;
		case R.id.rl_guanjia_file_all:
			openActivity(FileBrowserActivity.class);
			break;
		case R.id.rl_guanjia_file_document:
			Bundle docBundle = new Bundle();
			docBundle.putInt(FileCategoryActivity.CATEGORY_TYPE, FileCategoryActivity.TYPE_DOC);
			openActivity(FileCategoryActivity.class, docBundle);
			break;
		case R.id.rl_guanjia_file_compressed:
			Bundle archiveBundle = new Bundle();
			archiveBundle.putInt(FileCategoryActivity.CATEGORY_TYPE, FileCategoryActivity.TYPE_ARCHIVE);
			openActivity(FileCategoryActivity.class, archiveBundle);
			break;
		case R.id.rl_guanjia_file_apk:
			Bundle apkBundle = new Bundle();
			apkBundle.putInt(FileCategoryActivity.CATEGORY_TYPE, FileCategoryActivity.TYPE_APK);
			openActivity(FileCategoryActivity.class, apkBundle);
			break;
		case R.id.rl_guanjia_photo:
			Bundle photoBundle = new Bundle();
			photoBundle.putInt(ImageActivity.IMAGE_TYPE, ImageActivity.TYPE_PHOTO);
			openActivity(ImageActivity.class, photoBundle);
			break;
		case R.id.rl_guanjia_picture:
			Bundle galleryBundle = new Bundle();
			galleryBundle.putInt(ImageActivity.IMAGE_TYPE, ImageActivity.TYPE_GALLERY);
			openActivity(ImageActivity.class, galleryBundle);
			break;
		case R.id.rl_guanjia_invite:
			openActivity(InviteActivity.class);
			break;
			
		case R.id.rl_guanjia_connect:
			openActivity(ConnectFriendsActivity.class);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mGuanjiaReceiver != null) {
			getActivity().getApplicationContext().unregisterReceiver(mGuanjiaReceiver);
			mGuanjiaReceiver = null;
		}
	}
}
