package com.zhaoyan.juyou.common;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.style.BulletSpan;
import android.widget.ImageView;

import com.zhaoyan.common.file.APKUtil;
import com.zhaoyan.common.util.BitmapUtilities;
import com.zhaoyan.common.util.Log;

public class AsyncFileIconLoader {
	private static final String TAG = "AsyncImageLoader";
	// SoftReference是软引用，是为了更好的为了系统回收变量
	public static  HashMap<String, SoftReference<Bitmap>> bitmapCache;
	private ExecutorService pool ; 
	private Context context;
	
	public AsyncFileIconLoader(Context context) {
		bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
		this.context = context;
		pool = Executors.newCachedThreadPool();
	}
	
	public Bitmap loadImage(final String path, final int type, final ILoadImageCallback callback){
		return loadImage(path, type, null, callback);
	}
	
	/**
	 * @param path
	 * @param type
	 * @param imageView
	 * @param callback
	 * @return
	 */
	public Bitmap loadImage(final String path, final int type, final Map<String, Bitmap> caches, 
			final ILoadImageCallback callback){
		//we use file path as key
		if (bitmapCache.containsKey(path)) {
			// 从缓存中获取
			SoftReference<Bitmap> softReference = bitmapCache.get(path);
			Bitmap bitmap = softReference.get();
			if (null != bitmap) {
				return bitmap;
			}
		}
		
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				callback.onObtainBitmap((Bitmap) message.obj, path);
			}
		};
		
		switch (type) {
		case FileInfoManager.APK:
			pool.execute(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = null;
					Drawable drawable = APKUtil.getApkIcon2(context, path);
					BitmapDrawable bd = (BitmapDrawable) drawable;
					if (null != bd) {
						bitmap = bd.getBitmap();
					}
					
					bitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);
				}
			});
			break;
		case FileInfoManager.IMAGE:
			pool.execute(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = null;
					bitmap = getBitmapFromUrl(path, caches);
					bitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);
				}
			});
			break;
		case FileInfoManager.VIDEO:
			pool.execute(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
					bitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);
				}
			});
			break;
		default:
			break;
		}
		return null;
	}
	
	//new way to loadimage
	public Bitmap loadImage(String path,int type, ImageView imageView, Drawable defaultIcon){
		return loadImage(path, type, null, imageView, defaultIcon);
	}
	
	/**
	 * @param path
	 * @param type
	 * @param imageView
	 * @param callback
	 * @return
	 */
	public Bitmap loadImage(final String path, final int type, final Map<String, Bitmap> caches, 
			final ImageView imageView, final Drawable defaultIcon){
		//we use file path as key
		if (bitmapCache.containsKey(path)) {
			// 从缓存中获取
			SoftReference<Bitmap> softReference = bitmapCache.get(path);
			Bitmap bitmap = softReference.get();
			if (null != bitmap) {
				Log.d(TAG, "bitmapcache:path:" + path + ", is not null");
				imageView.setImageBitmap(bitmap);
				return bitmap;
			}else {
				Log.d(TAG, "bitmapcache:path:" + path + ", is null null,default icon is:" + defaultIcon);
				imageView.setImageDrawable(defaultIcon);
			}
		}
		
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				Bitmap bitmap = (Bitmap) message.obj;
				if (null != bitmap) {
					Log.d(TAG, "handleMessage.path:" + path + ", is not null");
					imageView.setImageBitmap(bitmap);
				}else {
					Log.d(TAG, "handleMessage.path:" + path + ", is null null");
					imageView.setImageDrawable(defaultIcon);
				}
			}
		};
		
		switch (type) {
		case FileInfoManager.APK:
			pool.execute(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = null;
					Drawable drawable = APKUtil.getApkIcon2(context, path);
					BitmapDrawable bd = (BitmapDrawable) drawable;
					if (null != bd) {
						bitmap = bd.getBitmap();
					}
					
					bitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);
				}
			});
			break;
		case FileInfoManager.IMAGE:
			pool.execute(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = null;
					bitmap = getBitmapFromUrl(path, caches);
					bitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);
				}
			});
			break;
		case FileInfoManager.VIDEO:
			pool.execute(new Runnable() {
				@Override
				public void run() {
					Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, Thumbnails.MINI_KIND);
					bitmapCache.put(path, new SoftReference<Bitmap>(bitmap));
					Message msg = handler.obtainMessage(0, bitmap);
					handler.sendMessage(msg);
				}
			});
			break;
		default:
			Message msg = handler.obtainMessage(0, null);
			handler.sendMessage(msg);
			break;
		}
		return null;
	}
	//
	
	private int width = 120;//每个Item的宽度,可以根据实际情况修改
	private int height = 150;//每个Item的高度,可以根据实际情况修改
	private Bitmap getBitmapFromUrl(String url, Map<String, Bitmap> caches){
		Bitmap bitmap = null;
		if (null != caches) {
			bitmap = caches.get(url);
		}
		
		if(bitmap != null){
			return bitmap;
		}
		
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inSampleSize =4;
//		Bitmap bitmap2 = BitmapFactory.decodeFile(url, options);
		
//		bitmap = BitmapUtilities.getBitmapThumbnail(bitmap2,width,height);
		bitmap = BitmapUtilities.getBitmapThumbnail(url, width, height);
		return bitmap;
	}

	/**
	 * 异步加载图片的回调接口
	 */
	public interface ILoadImageCallback {
		public void onObtainBitmap(Bitmap bitmap, String url);
	}
}
