package com.zhaoyan.juyou.activity;

import java.util.ArrayList;
import java.util.HashMap;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.GuanJiaLauncherUtil;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class GuanJiaLaunchMoreActivity extends BaseActivity implements
		OnItemClickListener {
	private static final String TAG = GuanJiaLaunchMoreActivity.class
			.getSimpleName();
	private Context mContext;
	private ListView mListView;
	private ListAdapter mAdapter;
	private ArrayList<HashMap<String, Object>> mData;
	private final String KEY_ITEM_ICON = "icon";
	private final String KEY_ITEM_TEXT = "text";
	private final String KEY_ITEM_CLASS_NAME = "class";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guanjia_launch_more);
		mContext = this;

		initLaunchers();
		initView();
	}

	private void initLaunchers() {
		mData = new ArrayList<HashMap<String, Object>>();
		String[] names = GuanJiaLauncherUtil.getMoreLauncherName(mContext);
		String[] classname = GuanJiaLauncherUtil
				.getMoreLauncherClassName(mContext);
		int[] icons = GuanJiaLauncherUtil.getMoreLauncherIcons(mContext);
		for (int i = 0; i < names.length; i++) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put(KEY_ITEM_ICON, icons[i]);
			item.put(KEY_ITEM_TEXT, names[i]);
			item.put(KEY_ITEM_CLASS_NAME, classname[i]);
			mData.add(item);
		}
	}

	private void initView() {
		mListView = (ListView) findViewById(R.id.gj_more_list);
		
		mAdapter = new SimpleAdapter(mContext, mData,
				R.layout.guanjia_launcher_more_item, new String[] { KEY_ITEM_ICON,
						KEY_ITEM_TEXT }, new int[] { R.id.iv_gj_item_icon,
						R.id.tv_gj_item_text });
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		openActivity(GuanJiaLauncherUtil.getLaunchIntent(mContext,
				mData.get(position).get(KEY_ITEM_CLASS_NAME).toString()));
	}
}
