package com.zhaoyan.juyou.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.view.BadgeView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AppActivity;
import com.zhaoyan.juyou.activity.AudioActivity;
import com.zhaoyan.juyou.activity.ClassifyActivity;
import com.zhaoyan.juyou.activity.FileBrowserActivity;
import com.zhaoyan.juyou.activity.GameActivity;
import com.zhaoyan.juyou.activity.HistoryActivity;
import com.zhaoyan.juyou.activity.VideoActivity;
import com.zhaoyan.juyou.common.HistoryManager;
import com.zhaoyan.juyou.provider.JuyouData;
import com.zhaoyan.juyou.provider.JuyouData.History;


public class GuanJiaFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "GuanJiaFragment";
	
	//items
	private BadgeView badgeView;
	
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
		Log.d(TAG, "onResume()");
		
		String where = JuyouData.History.STATUS + "="
				+ HistoryManager.STATUS_PRE_SEND + " or " + 
				JuyouData.History.STATUS + "=" + HistoryManager.STATUS_SENDING + " or " + 
				JuyouData.History.STATUS + "="
				+ HistoryManager.STATUS_PRE_RECEIVE + " or "
				+ JuyouData.History.STATUS + "=" + HistoryManager.STATUS_RECEIVING;
		Cursor cursor = getActivity().getContentResolver().query(History.CONTENT_URI, null, where, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			badgeView.show();
		}else {
			badgeView.hide();
		}
		cursor.close();
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
			docBundle.putInt(ClassifyActivity.CLASSIFY_TYPE, ClassifyActivity.TYPE_DOC);
			openActivity(ClassifyActivity.class, docBundle);
			break;
		case R.id.rl_guanjia_file_compressed:
			Bundle archiveBundle = new Bundle();
			archiveBundle.putInt(ClassifyActivity.CLASSIFY_TYPE, ClassifyActivity.TYPE_ARCHIVE);
			openActivity(ClassifyActivity.class, archiveBundle);
			break;
		case R.id.rl_guanjia_file_apk:
			Bundle apkBundle = new Bundle();
			apkBundle.putInt(ClassifyActivity.CLASSIFY_TYPE, ClassifyActivity.TYPE_APK);
			openActivity(ClassifyActivity.class, apkBundle);
			break;

		default:
			break;
		}
	}
	
}
