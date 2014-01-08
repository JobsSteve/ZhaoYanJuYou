package com.zhaoyan.juyou.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.zhaoyan.common.util.SharedPreferenceUtil;
import com.zhaoyan.juyou.JuYouApplication;
import com.zhaoyan.juyou.R;

public class PreviewPagesActivity extends Activity implements OnClickListener {
	private static final String TAG = "PreviewPagesActivity";

	private static final String SP_FIRST_LAUNCH = "first_launch";

	private View mPage1, mPage2, mPage3;
	private ImageView mImageView1, mImageView2, mImageView3;
	private Bitmap mBitmap1, mBitmap2, mBitmap3;
	private ViewPager mViewPager;
	private List<View> mPageList;

	private Button mLoginButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preview_pages);

		initView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_pg_login:
			launchLogin();
			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releasImages();
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.vp_preview_pages);
		// page list
		LayoutInflater inflater = getLayoutInflater();
		mPage1 = inflater.inflate(R.layout.preview_page1, null);
		mPage2 = inflater.inflate(R.layout.preview_page2, null);
		mPage3 = inflater.inflate(R.layout.preview_page3, null);
		createImages();
		mPageList = new ArrayList<View>();
		mPageList.add(mPage1);
		mPageList.add(mPage2);
		mPageList.add(mPage3);

		PreviewPagerAdapter adapter = new PreviewPagerAdapter(mPageList);
		mViewPager.setAdapter(adapter);

		mLoginButton = (Button) findViewById(R.id.btn_pg_login);
		mLoginButton.setOnClickListener(this);
	}

	private void createImages() {
		mBitmap1 = BitmapFactory.decodeResource(getResources(),
				R.drawable.preview_page1);
		mBitmap2 = BitmapFactory.decodeResource(getResources(),
				R.drawable.preview_page2);
		mBitmap3 = BitmapFactory.decodeResource(getResources(),
				R.drawable.preview_page3);

		mImageView1 = (ImageView) mPage1.findViewById(R.id.iv_preview_page1);
		mImageView1.setImageBitmap(mBitmap1);
		mImageView2 = (ImageView) mPage2.findViewById(R.id.iv_preview_page2);
		mImageView2.setImageBitmap(mBitmap2);
		mImageView3 = (ImageView) mPage3.findViewById(R.id.iv_preview_page3);
		mImageView3.setImageBitmap(mBitmap3);
	}

	private void releasImages() {
		mImageView1.setImageDrawable(null);
		mBitmap1.recycle();
		mImageView1 = null;
		mBitmap1 = null;
		mImageView2.setImageDrawable(null);
		mBitmap2.recycle();
		mImageView2 = null;
		mBitmap2 = null;
		mImageView3.setImageDrawable(null);
		mBitmap3.recycle();
		mImageView3 = null;
		mBitmap3 = null;
		System.gc();
	}

	private void launchLogin() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(this, LoginActivity.class);
		startActivity(intent);
		finish();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
	}

	/**
	 * Check SharedPreferences whether show preview pages.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNeedShowPreviewPages(Context context) {
		SharedPreferences sharedPreferences = SharedPreferenceUtil
				.getSharedPreference(context);
		return sharedPreferences.getBoolean(SP_FIRST_LAUNCH, true);
	}

	/**
	 * Save to SharedPreferences. Skip preview pages forever.
	 * 
	 * @param context
	 */
	public static void skipPreviewPagesForever(Context context) {
		SharedPreferences sharedPreferences = SharedPreferenceUtil
				.getSharedPreference(context);
		Editor editor = sharedPreferences.edit();
		editor.putBoolean(SP_FIRST_LAUNCH, false);
		editor.commit();
	}

	class PreviewPagerAdapter extends PagerAdapter {
		private List<View> mPages;

		public PreviewPagerAdapter(List<View> pages) {
			mPages = pages;
		}

		@Override
		public int getCount() {
			return mPages.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(mPages.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mPages.get(position), 0);
			return mPages.get(position);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			JuYouApplication.quitApplication(this);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
