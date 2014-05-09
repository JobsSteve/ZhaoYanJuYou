package com.zhaoyan.juyou.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;

import com.zhaoyan.juyou.DirectLogin;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.GuanJiaLauncherUtil;

public class GuanJiaLauncherActivity extends BaseActivity implements
		OnItemClickListener, OnClickListener {
	private static final String TAG = GuanJiaLauncherActivity.class
			.getSimpleName();
	private Context mContext;
	private GridView mGridView;
	private ListAdapter mAdapter;
	private ArrayList<HashMap<String, Object>> mData;
	private final String KEY_ITEM_ICON = "icon";
	private final String KEY_ITEM_TEXT = "text";
	private final String KEY_ITEM_CLASS_NAME = "class";
	private ImageView mConnectImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.guanjia_launcher);

		mContext = this;
		PreviewPagesActivity.skipPreviewPagesForever(mContext);
		// TODO move login in the future.
		DirectLogin directLogin = new DirectLogin(mContext);
		directLogin.login();
		
		initLaunchers();
		initView();
		disableFinishAnimation();
	}

	private void initLaunchers() {
		mData = new ArrayList<HashMap<String, Object>>();
		String[] names = GuanJiaLauncherUtil.getMainLauncherName(mContext);
		String[] classname = GuanJiaLauncherUtil
				.getMainLauncherClassName(mContext);
		int[] icons = GuanJiaLauncherUtil.getMainLauncherIcons(mContext);
		for (int i = 0; i < names.length; i++) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put(KEY_ITEM_ICON, icons[i]);
			item.put(KEY_ITEM_TEXT, names[i]);
			item.put(KEY_ITEM_CLASS_NAME, classname[i]);
			mData.add(item);
		}
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
		
		mConnectImageView = (ImageView) findViewById(R.id.iv_gj_invite_connect);
		mConnectImageView.setOnClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		openActivity(GuanJiaLauncherUtil.getLaunchIntent(mContext,
				mData.get(position).get(KEY_ITEM_CLASS_NAME).toString()));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_gj_invite_connect:
			openActivity(ConnectFriendsActivity.class);
			break;

		default:
			break;
		}
	}

}
