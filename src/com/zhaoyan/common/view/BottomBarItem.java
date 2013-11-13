package com.zhaoyan.common.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.zhaoyan.juyou.R;

public class BottomBarItem {
	private static final String TAG = "BottomBarItem";
	private TextView mNameTextView;
	private View mView;
	private int mIconRes;
	private int mIconSelectedRes;
	private boolean mIsSelected = false;
	private Context mContext;

	public BottomBarItem(Context context, int iconRes, int nameRes,
			int iconSelecteddRes) {
		mContext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		mView = inflater.inflate(R.layout.bottom_bar_item, null);
		mIconRes = iconRes;
		mIconSelectedRes = iconSelecteddRes;

		mNameTextView = (TextView) mView.findViewById(R.id.tv_bbi_text);
		mNameTextView.setText(nameRes);
		mNameTextView.setBackgroundResource(iconRes);
	}

	public View getView() {
		return mView;
	}

	public void setSelected(boolean selected) {
		if (mIsSelected == selected) {
			return;
		} else {
			mIsSelected = selected;
			updateSelection(mIsSelected);
		}

	}

	private void updateSelection(boolean selected) {
		if (selected) {
			mNameTextView.setBackgroundResource(mIconSelectedRes);
			mNameTextView.setTextColor(mContext.getResources().getColor(
					R.color.bottom_bar_selected));
		} else {
			mNameTextView.setBackgroundResource(mIconRes);
			mNameTextView.setTextColor(mContext.getResources().getColor(
					R.color.bottom_bar_unselected));
		}
	}
}
