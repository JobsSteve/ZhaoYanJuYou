package com.zhaoyan.juyou.activity;

import com.zhaoyan.juyou.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class GuanJiaLauncherActivity extends Activity{
	private static final String TAG = GuanJiaLauncherActivity.class.getSimpleName();
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guanjia_launcher);
		
		mContext = this;
		PreviewPagesActivity.skipPreviewPagesForever(mContext);
	}
}
