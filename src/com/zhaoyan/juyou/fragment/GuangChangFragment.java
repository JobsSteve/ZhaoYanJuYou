package com.zhaoyan.juyou.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhaoyan.juyou.R;


public class GuangChangFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View viweRoot = inflater.inflate(R.layout.guangchang_fragment, container, false);
		return viweRoot;
	}
}
