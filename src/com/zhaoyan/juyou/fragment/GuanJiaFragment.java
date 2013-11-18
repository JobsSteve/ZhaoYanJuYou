package com.zhaoyan.juyou.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.view.BadgeView;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.AudioActivity;
import com.zhaoyan.juyou.activity.HistoryActivity;
import com.zhaoyan.juyou.common.HistoryManager;
import com.zhaoyan.juyou.provider.JuyouData;
import com.zhaoyan.juyou.provider.JuyouData.History;


public class GuanJiaFragment extends BaseFragment implements OnClickListener {
	private static final String TAG = "GuanJiaFragment";
	
	//items
	private View mMusicView;
	private View mHistoryView;
	private BadgeView badgeView;
	private View mHistoryIconView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.guanjia_fragment, container, false);
		
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
	}
	
	public void initView(View view){
		mMusicView = view.findViewById(R.id.rl_guanjia_music);
		mMusicView.setOnClickListener(this);
		
		mHistoryView = view.findViewById(R.id.rl_guanjia_history);
		mHistoryView.setOnClickListener(this);
		
		mHistoryIconView = view.findViewById(R.id.iv_guanjia_history);
		badgeView = new BadgeView(mContext, mHistoryIconView);
		badgeView.setBackgroundResource(R.drawable.assist_block_count_bk);
		badgeView.setPadding(0, 0, 0, 0);
		badgeView.setText("");
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
