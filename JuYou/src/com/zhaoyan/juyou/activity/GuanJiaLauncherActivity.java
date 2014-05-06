package com.zhaoyan.juyou.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.zhaoyan.juyou.R;

public class GuanJiaLauncherActivity extends BaseActivity implements
		OnItemClickListener {
	private static final String TAG = GuanJiaLauncherActivity.class
			.getSimpleName();
	private Context mContext;
	private GridView mGridView;
	private ListAdapter mAdapter;
	private ArrayList<HashMap<String, Object>> mData;
	private final String KEY_ITEM_ICON = "icon";
	private final String KEY_ITEM_TEXT = "text";
	private final String KEY_ITEM_CLASS_NAME = "class";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guanjia_launcher);

		mContext = this;
		PreviewPagesActivity.skipPreviewPagesForever(mContext);
		initLaunchers();
		initView();
	}

	private void initLaunchers() {
		mData = new ArrayList<HashMap<String, Object>>();
		String[] texts = getResources().getStringArray(
				R.array.gj_launcher_item_text);
		String[] classname = getResources().getStringArray(R.array.gj_launcher_item_classname);
		TypedArray icons = getResources().obtainTypedArray(
				R.array.gj_launcher_item_icon);
		int count = getResources().getInteger(R.integer.gj_gridview_column)
				* getResources().getInteger(R.integer.gj_gridview_row);
		for (int i = 0; i < count; i++) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put(KEY_ITEM_ICON, icons.getResourceId(i, -1));
			item.put(KEY_ITEM_TEXT, texts[i]);
			item.put(KEY_ITEM_CLASS_NAME, classname[i]);
			mData.add(item);
		}
		icons.recycle();
	}

	private void initView() {
		mGridView = (GridView) findViewById(R.id.gv_gj_launchers);
		final int gridRowNumber = getResources().getInteger(
				R.integer.gj_gridview_row);
		mAdapter = new SimpleAdapter(mContext, mData,
				R.layout.guanjia_launcher_item, new String[] { KEY_ITEM_ICON,
						KEY_ITEM_TEXT }, new int[] { R.id.iv_gj_item_icon,
						R.id.tv_gj_item_text }) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				convertView = super.getView(position, convertView, parent);
				AbsListView.LayoutParams param = (LayoutParams) convertView
						.getLayoutParams();
				param.width = LayoutParams.MATCH_PARENT;
				param.height = mGridView.getHeight() / gridRowNumber;
				convertView.setLayoutParams(param);
				return convertView;
			}
		};
		mGridView.setAdapter(mAdapter);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mGridView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent();
		intent.setClassName(mContext, mData.get(position).get(KEY_ITEM_CLASS_NAME).toString());
		openActivity(intent);
	}
}
