package com.zhaoyan.juyou.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.Window;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.common.view.BottomBar;
import com.zhaoyan.common.view.BottomBar.OnBottomBarItemSelectChangeListener;
import com.zhaoyan.common.view.BottomBarItem;
import com.zhaoyan.juyou.JuYouApplication;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.adapter.SimpleFramgentPagerAdapter;
import com.zhaoyan.juyou.fragment.GuanJiaFragment;
import com.zhaoyan.juyou.fragment.GuangChangFragment;
import com.zhaoyan.juyou.fragment.JuYouFragment;
import com.zhaoyan.juyou.fragment.WoFragment;

public class MainActivity extends FragmentActivity implements
		OnPageChangeListener, OnBottomBarItemSelectChangeListener {
	private static final String TAG = "ZhaoYanActivity";

	private BottomBar mBottomBar;
	private BottomBarItem mBarItem1, mBarItem2, mBarItem3, mBarItem4;

	private ViewPager mViewPager;
	private SimpleFramgentPagerAdapter mPagerAdapter;
	private ArrayList<Fragment> mFragments;
	private ArrayList<String> mFragmentTags;
	private static final String FRAGMENT_TAG_JU_YOU = "JuYou";
	private static final String FRAGMENT_TAG_GUAN_JIA = "GuanJia";
	private static final String FRAGMENT_TAG_GUANG_CHANG = "GuangChang";
	private static final String FRAGMENT_TAG_WO = "Wo";
	private Fragment mJuYouFragment;
	private Fragment mGuanJiaFragment;
	private Fragment mGuangChangFragment;
	private Fragment mWoFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		PreviewPagesActivity.skipPreviewPagesForever(getApplicationContext());

		initView();
	}

	@Override
	public void onBottomBarItemSelectChanged(int position, BottomBarItem item) {
		Log.d(TAG, "onBottomBarItemSelectChanged " + position);
		setCurrentItem(position);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int position) {
		mBottomBar.setSelectedPosition(position);
	}

	private void initView() {
		initBottomBar();
		initViewPager();
	}

	private void initViewPager() {
		mViewPager = (ViewPager) findViewById(R.id.vp_zhaoyan);
		mViewPager.setOffscreenPageLimit(0);
		FragmentManager fragmentManager = getSupportFragmentManager();
		mJuYouFragment = (JuYouFragment) fragmentManager
				.findFragmentByTag(FRAGMENT_TAG_JU_YOU);
		mGuanJiaFragment = (GuanJiaFragment) fragmentManager
				.findFragmentByTag(FRAGMENT_TAG_GUAN_JIA);
		mGuangChangFragment = (GuangChangFragment) fragmentManager
				.findFragmentByTag(FRAGMENT_TAG_GUANG_CHANG);
		mWoFragment = (WoFragment) fragmentManager
				.findFragmentByTag(FRAGMENT_TAG_WO);

		if (mJuYouFragment == null) {
			mJuYouFragment = new JuYouFragment();
		}
		if (mGuanJiaFragment == null) {
			mGuanJiaFragment = new GuanJiaFragment();
		}
		if (mGuangChangFragment == null) {
			mGuangChangFragment = new GuangChangFragment();
		}
		if (mWoFragment == null) {
			mWoFragment = new WoFragment();
		}

		mFragments = new ArrayList<Fragment>();
		mFragments.add(mJuYouFragment);
		mFragments.add(mGuanJiaFragment);
		mFragments.add(mGuangChangFragment);
		mFragments.add(mWoFragment);

		mFragmentTags = new ArrayList<String>();
		mFragmentTags.add(FRAGMENT_TAG_JU_YOU);
		mFragmentTags.add(FRAGMENT_TAG_GUAN_JIA);
		mFragmentTags.add(FRAGMENT_TAG_GUANG_CHANG);
		mFragmentTags.add(FRAGMENT_TAG_WO);

		mPagerAdapter = new SimpleFramgentPagerAdapter(
				getSupportFragmentManager(), mFragments, mFragmentTags);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	private void initBottomBar() {
		mBottomBar = (BottomBar) findViewById(R.id.bottom_bar);
		mBottomBar.setOnBottomBarItemSelectChangeListener(this);

		mBarItem1 = new BottomBarItem(this,
				R.drawable.bottom_bar_juyou_selector,
				R.string.bottom_bar_juyou, R.drawable.bottom_bar_juyou_pressed);
		mBottomBar.addItem(mBarItem1);
		mBarItem2 = new BottomBarItem(this,
				R.drawable.bottom_bar_guanjia_selector,
				R.string.bottom_bar_guanjia,
				R.drawable.bottom_bar_guanjia_pressed);
		mBottomBar.addItem(mBarItem2);
		mBarItem3 = new BottomBarItem(this,
				R.drawable.bottom_bar_guangchang_selector,
				R.string.bottom_bar_guangchang,
				R.drawable.bottom_bar_guangchang_pressed);
		mBottomBar.addItem(mBarItem3);
		mBarItem4 = new BottomBarItem(this, R.drawable.bottom_bar_wo_selector,
				R.string.bottom_bar_wo, R.drawable.bottom_bar_wo_pressed);
		mBottomBar.addItem(mBarItem4);

		mBottomBar.setSelectedPosition(0);
	}

	public void setCurrentItem(int position) {
		mViewPager.setCurrentItem(position);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		JuYouApplication.quitApplication(getApplicationContext());
		super.onDestroy();
	}

}
