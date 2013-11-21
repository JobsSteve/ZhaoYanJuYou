package com.zhaoyan.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.Scroller;

import com.zhaoyan.common.util.Log;

/**
 * 文件路径水平导航栏
 */
public class SlowHorizontalScrollView extends HorizontalScrollView {
	private static final String TAG = "SlowHorizontalScrollView";
    private static final int SCROLL_DURATION = 2000;
    private final Scroller mScroller = new Scroller(getContext());

	public SlowHorizontalScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SlowHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public SlowHorizontalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void startHorizontalScroll(int startX, int dx) {
        Log.d(TAG, "start scroll");
        mScroller.startScroll(startX, 0, dx, 0, SCROLL_DURATION);
        invalidate();
    }
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }
		super.computeScroll();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		 mScroller.abortAnimation();
		return super.onTouchEvent(ev);
	}
	
}
