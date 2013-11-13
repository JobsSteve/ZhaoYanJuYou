package com.zhaoyan.juyou.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SimpleFramgentPagerAdapter extends FragmentPagerAdapter {
	List<Fragment> mFragments = new ArrayList<Fragment>();

	public SimpleFramgentPagerAdapter(FragmentManager fm, List<Fragment> list) {
		super(fm);
		this.mFragments = list;
	}

	@Override
	public Fragment getItem(int position) {
		return mFragments.get(position);
	}

	public void addItem(Fragment fragment) {
		mFragments.add(fragment);
	}

	@Override
	public int getCount() {
		return mFragments.size();
	}
}
