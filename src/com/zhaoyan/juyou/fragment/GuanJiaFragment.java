package com.zhaoyan.juyou.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AudioActivity;
import com.zhaoyan.juyou.activity.HistoryActivity;


public class GuanJiaFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "GuanJiaFragment";
	
	//items
	private View mMusicView;
	private View mHistoryView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.guanjia_fragment, container, false);
		
		initView(rootView);
		return rootView;
	}
	
	public void initView(View view){
		mMusicView = view.findViewById(R.id.rl_guanjia_music);
		mMusicView.setOnClickListener(this);
		
		mHistoryView = view.findViewById(R.id.rl_guanjia_history);
		mHistoryView.setOnClickListener(this);
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

		default:
			break;
		}
	}
}
