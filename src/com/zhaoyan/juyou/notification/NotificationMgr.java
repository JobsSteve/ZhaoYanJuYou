package com.zhaoyan.juyou.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.activity.MainActivity;

public class NotificationMgr {
	private Context mContext;
	
	private static final int NOTIFICATION_ID = 111;
	private static final int BACKUP_NOTIFICATION_ID = 112;
	
	public static final int STATUS_DEFAULT = 0;
	public static final int STATUS_UNCONNECTED = 1;
	public static final int STATUS_CONNECTED = 2;
	
	private int mPreStatus = -1;
	private NotificationManager mNotificationManager = null;
	private Notification mNotification;
	private RemoteViews mNotificationViews;
	
	public NotificationMgr(Context context){
		mContext = context;
		mNotificationManager = (NotificationManager) mContext 
                .getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void showNotificaiton(int status){
		mPreStatus = status;
		
		String label = mContext.getString(R.string.app_name);
		String runningString = mContext.getString(R.string.running);
		String unconnected = mContext.getString(R.string.unconnected);
		String connected = mContext.getString(R.string.connected);
		String title = "";
		switch (status) {
		case STATUS_UNCONNECTED:
			 title = runningString + unconnected;
			break;
		case STATUS_CONNECTED:
			 title = runningString + connected;
			break;
		}
		Notification notification = new Notification(R.drawable.icon_notification, title, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_NO_CLEAR; 
		notification.flags |= Notification.FLAG_ONGOING_EVENT; 
		
		//点击通知栏后，返回原Activity
		 Intent intent = new Intent(mContext, mContext.getClass()); 
		 //设置启动activity的启动模式，FLAG_ACTIVITY_SINGLE_TOP作用是在返回的时候用原来的activity的实例而不是建立新的 
		 intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		 PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 
                 PendingIntent.FLAG_UPDATE_CURRENT); 
		 notification.setLatestEventInfo(mContext, label, title, pi);
		 mNotificationManager.notify(NOTIFICATION_ID, notification); 
	}
	
	public void updateNotification(int status){
		if (status == mPreStatus) {
			return;
		}else if (status == STATUS_DEFAULT) {
			cancelNotification();
			showNotificaiton(mPreStatus);
		}else {
			cancelNotification();
			showNotificaiton(status);
		}
	}
	
	/**
	 * app backup notification setting
	 */
	public void startBackupNotification(){
		mNotification = new Notification();
		mNotification.icon = R.drawable.icon_notification;
		mNotification.tickerText = mContext.getString(R.string.backup_tip);
		mNotification.when = System.currentTimeMillis();
		mNotification.flags |= Notification.FLAG_NO_CLEAR; 
		
		mNotificationViews = new RemoteViews(mContext.getPackageName(), R.layout.app_backup_notification);
	}
	
	/**
	 * update app backup notification progress
	 * @param progress current backup app progress
	 * @param max the size of backup app list's size
	 * @param name current backup app label
	 */
	public void updateBackupNotification(int progress, int max, String name){
		mNotificationViews.setProgressBar(R.id.bar_progress, max, progress, false);
		mNotificationViews.setTextViewText(R.id.tv_name, name);
		String progressStr = mContext.getString(R.string.backuping_app, progress + "/" + max);
		mNotificationViews.setTextViewText(R.id.tv_progress, progressStr);
		
		mNotification.contentView = mNotificationViews;
		mNotification.contentIntent = null;
		
		mNotificationManager.notify(BACKUP_NOTIFICATION_ID, mNotification);
	}
	
	/**
	 * when app backup over,update notification
	 * @param duration
	 */
	public void appBackupOver(long duration){
		Notification notification = new Notification();
		notification.icon = R.drawable.icon_notification;
		notification.tickerText = mContext.getString(R.string.backup_over);
		notification.when = System.currentTimeMillis();
		
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		Intent intent = new Intent(mContext, mContext.getClass());
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
		notification.setLatestEventInfo(mContext, "备份完成", 
				"本次备份共花费 " + duration / 1000 + "s", pendingIntent);
		
		mNotificationManager.notify(BACKUP_NOTIFICATION_ID, notification);
	}
	
	public void cancelBackupNotification(){
		mNotificationManager.cancel(BACKUP_NOTIFICATION_ID);
	}
	
	public void cancelNotification() {
		mNotificationManager.cancel(NOTIFICATION_ID);
	} 
}
