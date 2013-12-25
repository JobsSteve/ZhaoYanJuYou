package com.zhaoyan.common.file;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.util.Log;

public class ZyMediaScanner {
	private static final String TAG = "ZyMediaScanner";
	private MediaScannerConnection mConnection = null;
	private MediaScannerClient mClient = null;
	private String filePath = null;
	private String[] filePaths = null;
	private String mimeType = null;
	
	public ZyMediaScanner(Context context){
		if (null == mClient) {
			mClient = new MediaScannerClient();
		}
		
		if (null == mConnection) {
			mConnection = new MediaScannerConnection(context, mClient);
		}
	}
	
	class MediaScannerClient implements MediaScannerConnectionClient{

		@Override
		public void onMediaScannerConnected() {
			// TODO Auto-generated method stub
			if (null != filePath) {
				mConnection.scanFile(filePath, mimeType);
			}
			
			if (null != filePaths) {
				for(String path : filePaths){
					mConnection.scanFile(path, mimeType);
				}
			}
			
			filePath = null;
			filePaths = null;
			mimeType = null;
		}

		@Override
		public void onScanCompleted(String path, Uri uri) {
			Log.d(TAG, "onScanCompleted.path:" + path);
			Log.d(TAG, "onScanCompleted.uri:" + uri);
			mConnection.disconnect();
		}
	}
	
	public void scanFile(String filepath, String mimeType){
		this.filePath = filepath;
		this.mimeType = mimeType;
		
		mConnection.connect();
	}
	
	public void scanFile(String[] filepaths, String mimeType){
		this.filePaths = filepaths;
		this.mimeType = mimeType;
		
		mConnection.connect();
	}
	
	/**
	 * Request the media scanner to scan a file and add it to the media database
	 * @param context
	 * @param filePath
	 * @throws Exception
	 */
	public static void scanFileAsync(Context context, String filePath) throws Exception{
		File file = new File(filePath);
		scanFileAsync(context, file);
	}
	
	/**
	 * Request the media scanner to scan a file and add it to the media database
	 * @param context
	 * @param file
	 * @throws Exception
	 */
	public static void scanFileAsync(Context context, File file) throws Exception{
		if (!file.isFile()) {
			throw new Exception(file.getAbsolutePath() + " is not a file");
		}
		Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		scanIntent.setData(Uri.fromFile(file));
		context.sendBroadcast(scanIntent);
	}
}
