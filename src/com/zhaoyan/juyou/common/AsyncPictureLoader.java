package com.zhaoyan.juyou.common;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.widget.ImageView;

import com.zhaoyan.juyou.adapter.ImageItemAdapter;


public class AsyncPictureLoader {
	// SoftReference是软引用，是为了更好的为了系统回收变量
		public static HashMap<Long, SoftReference<Bitmap>> bitmapCaches;
		private ContentResolver contentResolver = null;
		
		public AsyncPictureLoader(Context context) {
			bitmapCaches = new HashMap<Long, SoftReference<Bitmap>>();
			contentResolver = context.getContentResolver();
		}
		
	public Bitmap loadBitmap(final long id, final ILoadImagesCallback videoCallback) {
		if (bitmapCaches.containsKey(id)) {
			// 从缓存中获取
			SoftReference<Bitmap> softReference = bitmapCaches.get(id);
			Bitmap bitmap = softReference.get();
			if (null != bitmap) {
				return bitmap;
			}
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				videoCallback.onObtainBitmap((Bitmap) message.obj, id);
			}
		};

		new Thread() {
			@Override
			public void run() {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;//采用默认值
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;//采用默认值
				// get images thumbail
				Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, Images.Thumbnails.MICRO_KIND, options);
				bitmapCaches.put(id, new SoftReference<Bitmap>(bitmap));
				Message message = handler.obtainMessage(0, bitmap);
				handler.sendMessage(message);
			}
		}.start();

		return null;
	}

	public Bitmap loadBitmap(final long id, final ImageView imageView) {
		if (!ImageItemAdapter.USE_VIEW_CACHE) {
			if (bitmapCaches.containsKey(id)) {
				// 从缓存中获取
				SoftReference<Bitmap> softReference = bitmapCaches.get(id);
				Bitmap bitmap = softReference.get();
				if (null != bitmap) {
					imageView.setImageBitmap(bitmap);
					return bitmap;
				}
			}

		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				Bitmap bitmap = (Bitmap)message.obj;
				imageView.setImageBitmap(bitmap);
			}
		};

		new Thread() {
			@Override
			public void run() {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither = false;//采用默认值
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;//采用默认值
				// get images thumbail
				Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, Images.Thumbnails.MICRO_KIND, options);
				if (!ImageItemAdapter.USE_VIEW_CACHE) {
					bitmapCaches.put(id, new SoftReference<Bitmap>(bitmap));
				}
				Message message = handler.obtainMessage(0, bitmap);
				handler.sendMessage(message);
			}
		}.start();

		return null;
	}	
		
	/**
	 * 异步加载的回调接口
	 */
	public interface ILoadImagesCallback {
		public void onObtainBitmap(Bitmap bitmap, long id);
	}
}
