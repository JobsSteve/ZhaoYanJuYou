package com.zhaoyan.juyou.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.view.BadgeView;
import com.zhaoyan.communication.FileTransferService;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AppActivity;
import com.zhaoyan.juyou.activity.AudioActivity;
import com.zhaoyan.juyou.activity.FileCategoryActivity;
import com.zhaoyan.juyou.activity.FileBrowserActivity;
import com.zhaoyan.juyou.activity.GameActivity;
import com.zhaoyan.juyou.activity.HistoryActivity;
import com.zhaoyan.juyou.activity.ImageActivity;
import com.zhaoyan.juyou.activity.InviteActivity;
import com.zhaoyan.juyou.activity.VideoActivity;


public class GuanJiaFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "GuanJiaFragment";
	
	private GuanjiaReceiver mGuanjiaReceiver = null;
	
	//items
	private BadgeView badgeView;
	
	class GuanjiaReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "onReceive:action=" + action);
			boolean show = intent.getBooleanExtra(FileTransferService.EXTRA_BADGEVIEW_SHOW, false);
			if (show) {
				badgeView.show();
			}else {
				badgeView.hide();
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter(FileTransferService.ACTION_NOTIFY_SEND_OR_RECEIVE);
		mGuanjiaReceiver = new GuanjiaReceiver();
		getActivity().getApplicationContext().registerReceiver(mGuanjiaReceiver, filter);
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
		// TODO Auto-generated method stub
		super.onResume();
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

		default:
			break;
		}
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
