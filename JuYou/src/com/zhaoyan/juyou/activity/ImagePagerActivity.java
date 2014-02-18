package com.zhaoyan.juyou.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.zhaoyan.common.view.HackyViewPager;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant.Extra;

/**全屏查看图片*/
public class ImagePagerActivity extends Activity implements OnClickListener {

	private static final String STATE_POSITION = "STATE_POSITION";
	private static ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private ViewPager pager;
	private List<String> imageList = new ArrayList<String>();
	
	private View mTitleView,mBottomView;
	private boolean mIsMenuViewVisible = true;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_pager_main);
		
		Bundle bundle = getIntent().getExtras();
		imageList = bundle.getStringArrayList(Extra.IMAGE_INFO);
		int pagerPosition = bundle.getInt(Extra.IMAGE_POSITION, 0);
		
		mTitleView = findViewById(R.id.rl_image_pager_title);
		mBottomView = findViewById(R.id.rl_image_pager_bottom);
		View backView = findViewById(R.id.rl_back);
		backView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(ImagePagerActivity.this, "Back", Toast.LENGTH_SHORT).show();
			}
		});
		
		setMenuViewVisible(false);

		// if (savedInstanceState != null) {
		// pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		// }

		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.photo_l)
				.showImageOnFail(R.drawable.photo_l).resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).displayer(new FadeInBitmapDisplayer(300))
				.build();

		pager = (ViewPager) findViewById(R.id.image_viewpager);
		pager.setAdapter(new ImagePagerAdapter(imageList));
		pager.setCurrentItem(pagerPosition);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private LayoutInflater inflater;
		private List<String> list;

		ImagePagerAdapter(List<String> data) {
			this.list = data;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.image_pager_item, view, false);
			ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image_pager);
			imageView.setOnClickListener(ImagePagerActivity.this);
			//write at 20140218
			//由于使用了photoview这个插件，导致onClick事件失效，尚未找到解决办法
			final ProgressBar loadingBar = (ProgressBar) imageLayout.findViewById(R.id.loading_image);

			String path = "file://" + list.get(position);
			imageLoader.displayImage(path, imageView, options, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
					loadingBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					String message = null;
					switch (failReason.getType()) {
					case IO_ERROR:
						message = "Input/Output error";
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
					}
					Toast.makeText(ImagePagerActivity.this, message, Toast.LENGTH_SHORT).show();

					loadingBar.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
					loadingBar.setVisibility(View.GONE);
				}
			});

			((ViewPager) view).addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}
	}

	protected class ViewOnClick implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			System.out.println("ViewOnClick");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		imageLoader.stop();
		imageLoader.clearMemoryCache();
		imageLoader.clearDiscCache();
	}
	
	private void setMenuViewVisible(boolean visible){
		mIsMenuViewVisible = visible;
		if (visible) {
			mTitleView.setVisibility(View.VISIBLE);
			mBottomView.setVisibility(View.VISIBLE);
		} else {
			mTitleView.setVisibility(View.INVISIBLE);
			mBottomView.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		System.out.println("onClick");
		switch (v.getId()) {
		case R.id.image_pager:
			setMenuViewVisible(!mIsMenuViewVisible);
			break;

		default:
			break;
		}
	}

}