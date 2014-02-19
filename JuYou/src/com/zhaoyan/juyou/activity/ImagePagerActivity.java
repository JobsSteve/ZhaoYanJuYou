package com.zhaoyan.juyou.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnViewTapListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.zhaoyan.common.file.FileManager;
import com.zhaoyan.common.util.ZYUtils;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant.Extra;
import com.zhaoyan.juyou.dialog.InfoDialog;
import com.zhaoyan.juyou.dialog.ZyDeleteDialog;
import com.zhaoyan.juyou.dialog.ZyAlertDialog.OnZyAlertDlgClickListener;

/**全屏查看图片*/
public class ImagePagerActivity extends Activity implements OnClickListener, OnPageChangeListener {

	private static final String STATE_POSITION = "STATE_POSITION";
	private static ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private ViewPager pager;
	private List<String> imageList = new ArrayList<String>();
	
	private View mTitleView,mBottomView;
	private TextView mTextView;
	private boolean mIsMenuViewVisible = true;
	
	private ImagePagerAdapter mAdapter;
	
	private PhotoViewAttacher mAttacher;
	
	private List<Integer> mDeleteList = new ArrayList<Integer>();
	public static final String DELETE_POSITION = "delete_position";
	
	private static final int MSG_INVISIBLE_BAR = 0;
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INVISIBLE_BAR:
				setMenuViewVisible(false);
				break;

			default:
				break;
			}
		};
	};

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_pager_main);
		
		Bundle bundle = getIntent().getExtras();
		imageList = bundle.getStringArrayList(Extra.IMAGE_INFO);
		int pagerPosition = bundle.getInt(Extra.IMAGE_POSITION, 0);
		
		mTextView = (TextView) findViewById(R.id.tv_image_info);
		mTextView.setText((pagerPosition + 1) + "/" + imageList.size());
		
		mTitleView = findViewById(R.id.rl_image_pager_title);
		mBottomView = findViewById(R.id.rl_image_pager_bottom);
		View backView = findViewById(R.id.rl_back);
		backView.setOnClickListener(this);
		
		View infoView = findViewById(R.id.rl_info);
		infoView.setOnClickListener(this);
		
		View deleteView = findViewById(R.id.rl_delete);
		deleteView.setOnClickListener(this);
		
		setMenuViewVisible(true);
		mHandler.sendEmptyMessageDelayed(MSG_INVISIBLE_BAR, 2000);
		
		// if (savedInstanceState != null) {
		// pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		// }

		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.photo_l)
				.showImageOnFail(R.drawable.photo_l).resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).displayer(new FadeInBitmapDisplayer(300))
				.build();

		pager = (ViewPager) findViewById(R.id.image_viewpager);
		mAdapter = new ImagePagerAdapter(imageList);
		pager.setAdapter(mAdapter);
		pager.setCurrentItem(pagerPosition);
		pager.setOnPageChangeListener(this);
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
		public int getItemPosition(Object object) {
			return POSITION_NONE;
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
			mAttacher = new PhotoViewAttacher(imageView);
			mAttacher.setOnViewTapListener(new OnViewTapListener() {
				@Override
				public void onViewTap(View arg0, float arg1, float arg2) {
					setMenuViewVisible(!mIsMenuViewVisible);
				}
			});
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
	
	@Override
	public void onBackPressed() {
		System.out.println("onBackPressed");
		doFinish();
	}
	
	private void doFinish(){
		if (mDeleteList.size() > 0) {
			System.out.println("======================");
			Intent data = new Intent();
			data.putIntegerArrayListExtra(DELETE_POSITION, (ArrayList<Integer>) mDeleteList);
			setResult(RESULT_OK, data);
		} else {
			System.out.println("********************");
			setResult(RESULT_CANCELED);
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		imageLoader.stop();
		imageLoader.clearMemoryCache();
		imageLoader.clearDiscCache();
		
		mAttacher.cleanup();
	}
	
	private void setMenuViewVisible(boolean visible){
		mIsMenuViewVisible = visible;
		if (visible) {
			mTitleView.setVisibility(View.VISIBLE);
			mTitleView.clearAnimation();
			mTitleView.setAnimation((AnimationUtils.loadAnimation(this,R.anim.slide_down_in)));
			
			mBottomView.setVisibility(View.VISIBLE);
			mBottomView.clearAnimation();
			mBottomView.setAnimation((AnimationUtils.loadAnimation(this,R.anim.slide_up_in)));
		} else {
			mTitleView.setVisibility(View.INVISIBLE);
			mTitleView.clearAnimation();
			mTitleView.setAnimation((AnimationUtils.loadAnimation(this,R.anim.slide_up_out)));
			
			mBottomView.setVisibility(View.INVISIBLE);
			mBottomView.clearAnimation();
			mBottomView.setAnimation((AnimationUtils.loadAnimation(this,R.anim.slide_down_out)));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_back:
			doFinish();
			break;
		case R.id.rl_info:
			showInfo();
			break;
		case R.id.rl_delete:
			showDeleteDialog();
			break;
		default:
			break;
		}
	}
	
	private void showInfo(){
		InfoDialog infoDialog = new InfoDialog(this, InfoDialog.SINGLE_FILE);
		infoDialog.setTitle(R.string.info_image_info);
		int position = pager.getCurrentItem();
		String url = imageList.get(position);
		int index = url.lastIndexOf("/");
		String name = url.substring(index + 1, url.length());
		File file = new File(url);
		long size = file.length();
		long date = file.lastModified();
		
		String imageType = FileManager.getExtFromFilename(name);
		if ("".equals(imageType)) {
			imageType = getApplicationContext().getResources().getString(R.string.unknow);
		}
		
		infoDialog.setFileType(InfoDialog.IMAGE, imageType);
		infoDialog.setFileName(name);
		infoDialog.setFilePath(ZYUtils.getParentPath(url));
		infoDialog.setModifyDate(date);
		infoDialog.setFileSize(size);
		infoDialog.show();
		infoDialog.scanOver();
	}
	
	/**
     * show confrim dialog
     * @param path file path
     */
    public void showDeleteDialog() {
    	final int position = pager.getCurrentItem();
		final String url = imageList.get(position);
		int index = url.lastIndexOf("/");
		String name = url.substring(index + 1, url.length());
		
    	ZyDeleteDialog deleteDialog = new ZyDeleteDialog(this);
		deleteDialog.setTitle(R.string.delete_image);
		String msg = getString(R.string.delete_file_confirm_msg, name);
		deleteDialog.setMessage(msg);
		deleteDialog.setPositiveButton(R.string.menu_delete, new OnZyAlertDlgClickListener() {
			@Override
			public void onClick(Dialog dialog) {
				FileManager.deleteFileInMediaStore(getApplicationContext(), 
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, url);
				mDeleteList.add(position);
				dialog.dismiss();
				imageList.remove(position);
				mAdapter.notifyDataSetChanged();
				pager.setCurrentItem(position);
				mTextView.setText((position + 1) + "/" + imageList.size());
			}
		});
		deleteDialog.setNegativeButton(R.string.cancel, null);
		deleteDialog.show();
    }

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPageSelected(int arg0) {
		mTextView.setText((arg0 + 1) + "/" + imageList.size());
	}

}