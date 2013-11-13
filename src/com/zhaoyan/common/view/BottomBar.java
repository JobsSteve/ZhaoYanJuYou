package com.zhaoyan.common.view;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class BottomBar extends LinearLayout implements OnClickListener {
	private ArrayList<BottomBarItem> mItems = new ArrayList<BottomBarItem>();
	private OnBottomBarItemSelectChangeListener mListener;
	private int mLastSelectedPostion = 0;

	public BottomBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BottomBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BottomBar(Context context) {
		super(context);
	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < mItems.size(); i++) {
			BottomBarItem item = mItems.get(i);
			if (v == item.getView()) {
				if (i != mLastSelectedPostion) {
					setSelectedItem(i, item);

					if (mListener != null) {
						mListener.onBottomBarItemSelectChanged(i, item);
					}
				}
			}
		}
	}

	public void setSelectedPosition(int position) {
		BottomBarItem item = mItems.get(position);
		setSelectedItem(position, item);
	}

	private void setSelectedItem(int position, BottomBarItem item) {
		// Update selected item and previous selected item.
		BottomBarItem previousItem = mItems.get(mLastSelectedPostion);
		previousItem.setSelected(false);
		item.setSelected(true);

		mLastSelectedPostion = position;
	}

	public void addItem(BottomBarItem item) {
		if (mItems.contains(item)) {
			return;
		}
		mItems.add(item);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, 1);
		addView(item.getView(), params);
		item.getView().setOnClickListener(this);
	}

	public void removeItem(BottomBarItem item) {
		if (mItems.contains(item)) {
			removeView(item.getView());
			item.getView().setOnClickListener(null);
			mItems.remove(item);
		}
	}

	public void setOnBottomBarItemSelectChangeListener(
			OnBottomBarItemSelectChangeListener listener) {
		mListener = listener;
	}

	public interface OnBottomBarItemSelectChangeListener {
		void onBottomBarItemSelectChanged(int position, BottomBarItem item);

	}

}
