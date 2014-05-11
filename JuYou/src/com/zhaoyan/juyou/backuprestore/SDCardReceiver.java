package com.zhaoyan.juyou.backuprestore;

import java.util.HashSet;

import com.zhaoyan.common.util.Log;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;


public class SDCardReceiver extends BroadcastReceiver {
	private BackupFileScanner mFileScanner;
	private static final String AT_MOUNT_ACTION = "com.mediatek.autotest.mount";
	private static final String AT_UNMOUNT_ACTION = "com.mediatek.autotest.unmount";
    public interface OnSDCardStatusChangedListener{
        public void onSDCardStatusChanged(boolean mount);
    }
    private static final String TAG = "SDCardReceiver";
    private static SDCardReceiver sInstance;
    private static HashSet<OnSDCardStatusChangedListener>mListenerList;
    @Override
    public void onReceive(Context context, Intent intent) {
    	if(mFileScanner == null ){
    		mFileScanner = new BackupFileScanner(context,null);
    	}
        String action = intent.getAction();
        Log.i(TAG, "  SDCardReceiver -> onReceive: " + action);
        if(action.equals(Intent.ACTION_MEDIA_UNMOUNTED)||action.equals(AT_UNMOUNT_ACTION)){
            if(mListenerList != null){
                for(OnSDCardStatusChangedListener listener : mListenerList){
                	Log.i(TAG, "  listener : " + listener);
                    listener.onSDCardStatusChanged(false);
                }
            }
            mFileScanner.quitScan();
            NotifyManager.getInstance(context).clearNotification();
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.cancel(NotifyManager.NOTIFY_NEW_DETECTION);
            manager.cancel(NotifyManager.NOTIFY_BACKUPING);
            manager.cancel(NotifyManager.NOTIFY_RESTORING);
        }else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)||action.equals(AT_MOUNT_ACTION)){
            if(mListenerList != null){
                for(OnSDCardStatusChangedListener listener : mListenerList){
                	Log.i(TAG, "  listener : " + listener);
                    listener.onSDCardStatusChanged(true);
                }
            }
            startScanSDCard(context);
        }else if(action.equals(Constants.INTENT_SD_SWAP)){
            boolean sd_card_exist = intent.getBooleanExtra(Constants.ACTION_SD_EXIST, false);
            if(mListenerList != null){
                for(OnSDCardStatusChangedListener listener : mListenerList){
                	Log.i(TAG, "  listener : " + listener);
                    listener.onSDCardStatusChanged(sd_card_exist);
                }
            }
            if(sd_card_exist){
            	startScanSDCard(context);
            }
        }else if (Constants.ACTION_NEW_DATA_DETECTED_TRANSFER.equals(intent.getAction())) {
			Log.d(TAG,  "NotificationReceiver ----->ACTION_NEW_DATA_DETECTED_TRANSFER");
			Intent orderIntent = new Intent(Constants.ACTION_NEW_DATA_DETECTED);
			int type = intent.getIntExtra(Constants.NOTIFY_TYPE, 0);
			String folder = intent.getStringExtra(Constants.FILENAME);
			orderIntent.putExtra(Constants.FILENAME, folder);
			orderIntent.putExtra(Constants.NOTIFY_TYPE, type);
			context.sendOrderedBroadcast(orderIntent, null);
		}
    }



	private void startScanSDCard(Context context) {
		if(!mFileScanner.isRunning()){
			mFileScanner.addScanHandler(new CosmosBackupHandler());
			mFileScanner.addScanHandler(new PlutoBackupHandler());
			mFileScanner.addScanHandler(new SmartPhoneBackupHandler());
			mFileScanner.addScanHandler(new DataTransferBackupHandler());
			mFileScanner.startScan();
		}
	}
    
    

    public static SDCardReceiver getInstance() {
        if (sInstance == null) {
            sInstance = new SDCardReceiver();
        }
        return sInstance;
    }
    
    
    public void registerOnSDCardChangedListener(
            OnSDCardStatusChangedListener listener){
        if(mListenerList == null){
            mListenerList = new HashSet<OnSDCardStatusChangedListener>();
        }
        Log.v(TAG, "registerOnSDCardChangedListener:" + listener);
        mListenerList.add(listener);
    }
    
    public void unRegisterOnSDCardChangedListener(
            OnSDCardStatusChangedListener listener){
        Log.v(TAG, "unRegisterOnSDCardChangedListener:" + listener);
        mListenerList.remove(listener);
    }
}
