package com.zhaoyan.juyou.backuprestore;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class NotifyManager {
    private static final String TAG = "NotifyManager:";
    public static final int NOTIFY_NEW_DETECTION = 1;
    public static final int NOTIFY_BACKUPING = 2;
    public static final int NOTIFY_RESTORING = 3;

    public static final int FP_NEW_DETECTION_NOTIFY_TYPE_DEAFAULT = 0;
    public static final int FP_NEW_DETECTION_NOTIFY_TYPE_LIST = 1;
    public static final String FP_NEW_DETECTION_INTENT_LIST = "com.mediatek.backuprestore.intent.MainActivity";
    public static final String BACKUP_PERSONALDATA_INTENT = "com.mediatek.backuprestore.intent.PersonalDataBackupActivity";
    public static final String BACKUP_APPLICATION_INTENT = "com.mediatek.backuprestore.intent.AppBackupActivity";
    public static final String RESTORE_PERSONALDATA_INTENT = "com.mediatek.backuprestore.intent.PersonalDataRestoreActivity";
    public static final String RESTORE_APPLICATION_INTENT = "com.mediatek.backuprestore.intent.AppRestoreActivity";

    private Notification mNotification;
    private int mNotificationType;
    private Context mNotificationContext;
    private NotificationManager mNotificationManager;
    private int mMaxPercent = 100;
    private static NotifyManager sNotifyManager;

    /**
     * Constructor function.
     * 
     * @param context
     *            environment context
     */
    private NotifyManager(Context context) {
        mNotificationContext = context;
        mNotification = null;
        mNotificationManager = (NotificationManager) mNotificationContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotifyManager getInstance(Context context) {
        if (sNotifyManager == null) {
            sNotifyManager = new NotifyManager(context);
        }
        return sNotifyManager;
    }

    public void setMaxPercent(int maxPercent) {
        mMaxPercent = maxPercent;
    }

    public void showNewDetectionNotification(int type,String folder) {
        mNotificationType = NOTIFY_NEW_DETECTION;
        CharSequence contentTitle = mNotificationContext.getText(R.string.detect_new_data_title);
        CharSequence contentText = mNotificationContext.getText(R.string.detect_new_data_text);
        String intentFilter = ((type == FP_NEW_DETECTION_NOTIFY_TYPE_LIST)?FP_NEW_DETECTION_INTENT_LIST:RESTORE_PERSONALDATA_INTENT);
//        Intent contentIntent = new Intent(intentFilter);
//        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if(forder!=null&&forder.length()>0){
//        	Log.d(TAG, "showNewDetectionNotification ----->forder is = " +forder);
//        	contentIntent.putExtra(Constants.FILENAME, forder);
//        }
//        PendingIntent pendingIntent = PendingIntent.getActivity(mNotificationContext, 0,
//        		contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        if(type == FP_NEW_DETECTION_NOTIFY_TYPE_DEAFAULT && (folder==null||folder.trim().equals(""))){
        	Log.d(TAG, "[showNewDetectionNotification] ERROR notification ! folder is null !");
        	return;
        }
        Intent intent = new Intent(Constants.ACTION_NEW_DATA_DETECTED_TRANSFER);
        intent.putExtra(Constants.FILENAME, folder);
        intent.putExtra(Constants.NOTIFY_TYPE, type);
        Log.d(TAG, "[showNewDetectionNotification] Folder = " + folder + " Type = "+type);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mNotificationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        configAndShowNotification(R.drawable.ic_new_data_notify, contentTitle, contentText,pendingIntent);
//        Notification.Builder mNotification = new Notification.Builder(mNotificationContext);
       
//        mNotification.setAutoCancel(true).setContentTitle(contentTitle).setContentText(contentText)
//                .setSmallIcon(R.drawable.ic_new_data_notify).setWhen(System.currentTimeMillis())
//                .setContentIntent(pendingIntent);
//        mNotificationManager.notify(NOTIFY_NEW_DETECTION, mNotification.getNotification());
        Notification mNotification = new Notification(R.drawable.ic_new_data_notify, contentTitle, System.currentTimeMillis());
		mNotification.flags |= Notification.FLAG_NO_CLEAR; 
		mNotification.setLatestEventInfo(mNotificationContext, contentText, contentTitle, pendingIntent);
        mNotificationManager.notify(NOTIFY_NEW_DETECTION, mNotification);
    }

    public void showBackupNotification(String contentText, int type, int currentProgress) {
        if (mMaxPercent == 0) {
            return;
        }
        mNotificationType = NOTIFY_BACKUPING;
        CharSequence contentTitle = mNotificationContext
                .getText(R.string.notification_backup_title);
        String intentFilter = null;
        if (type == ModuleType.TYPE_APP) {
            intentFilter = BACKUP_APPLICATION_INTENT;
        } else {
            intentFilter = BACKUP_PERSONALDATA_INTENT;
        }
        setNotificationProgress(R.drawable.ic_backuprestore_notify, contentTitle, contentText,
                currentProgress, intentFilter);
    }
    
    public void showFinishNotification(int type, boolean success) {
    	clearNotification();
    	String contentTitle = mNotificationContext.getString(R.string.notification_backup_ok);
		switch (type) {
			case NOTIFY_BACKUPING:
				contentTitle =mNotificationContext.getString(success?R.string.notification_backup_ok:R.string.notification_backup_failed);
				break;
			case NOTIFY_RESTORING:
				contentTitle =mNotificationContext.getString(success?R.string.notification_restore_ok:R.string.notification_restore_failed);
				break;
			default:
		}
        mNotificationType = NOTIFY_BACKUPING;
        if (mNotification == null) {
//            mNotification = new Notification.Builder(mNotificationContext);
        	mNotification = new Notification(R.drawable.ic_backuprestore_notify, contentTitle, System.currentTimeMillis());
            if (mNotification == null) {
                return;
            }
        }
        PackageManager pm = mNotificationContext.getPackageManager();
        mNotification.flags |= Notification.FLAG_NO_CLEAR; 
        PendingIntent pIntent = PendingIntent.getActivity(
        		mNotificationContext, 0, pm.getLaunchIntentForPackage(mNotificationContext.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT);
//        mNotification.setAutoCancel(true).setContentTitle(contentTitle)
//        .setContentText(mNotificationContext.getString(type==NOTIFY_BACKUPING?R.string.backup:R.string.restore))
//        .setSmallIcon(R.drawable.ic_backuprestore_notify)
//        .setWhen(System.currentTimeMillis())
//        .setContentIntent(PendingIntent.getActivity(mNotificationContext, 0, pm.getLaunchIntentForPackage(mNotificationContext.getPackageName()), PendingIntent.FLAG_UPDATE_CURRENT));
//        mNotificationManager.notify(mNotificationType, mNotification.getNotification());
        mNotification.setLatestEventInfo(mNotificationContext, contentTitle, mNotificationContext.getString(type==NOTIFY_BACKUPING?R.string.backup:R.string.restore), pIntent);
        mNotificationManager.notify(mNotificationType, mNotification);
    }

    public void showRestoreNotification(String contentText, int type, int currentProgress) {
        if (mMaxPercent == 0) {
            return;
        }
        mNotificationType = NOTIFY_RESTORING;
        CharSequence contentTitle = mNotificationContext
                .getText(R.string.notification_restore_title);
        String intentFilter = null;
        if (type == ModuleType.TYPE_APP) {
            intentFilter = RESTORE_APPLICATION_INTENT;
        } else {
            intentFilter = RESTORE_PERSONALDATA_INTENT;
        }
        setNotificationProgress(R.drawable.ic_backuprestore_notify, contentTitle, contentText,
                currentProgress, intentFilter);
    }

    private void setNotificationProgress(int iconDrawableId, CharSequence contentTitle,
            String contentText, int currentProgress, String intentFilter) {
        if (mNotification == null) {
//            mNotification = new Notification.Builder(mNotificationContext);
        	mNotification = new Notification();
            if (mNotification == null) {
                return;
            }
        }
        mNotification.icon = iconDrawableId;
        mNotification.tickerText = contentTitle;
        mNotification.flags |= Notification.FLAG_NO_CLEAR; 
        mNotification.when = System.currentTimeMillis();
        mNotification.setLatestEventInfo(mNotificationContext, contentTitle, contentText, getPendingIntenActivity(intentFilter));
//        mNotification.setAutoCancel(true).setOngoing(true).setContentTitle(contentTitle)
//        .setContentText(contentText).setSmallIcon(iconDrawableId)
//        .setWhen(System.currentTimeMillis())
//        .setContentIntent(getPendingIntenActivity(intentFilter));
        
        String percent = "" + (currentProgress * 100) / mMaxPercent + "%";
        
//        mNotification.setProgress(mMaxPercent, currentProgress, false).setContentInfo(percent);
        mNotificationManager.notify(mNotificationType, mNotification);
    }

    private PendingIntent getPendingIntenActivity(String intentFilter) {

        Intent notificationIntent = new Intent(intentFilter);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(mNotificationContext, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return contentIntent;
    }

    private void configAndShowNotification(int iconDrawableId, CharSequence contentTitle,
            CharSequence contentText, String intentFilter) {
//        mNotification = new Notification.Builder(mNotificationContext);
//
//        if (mNotification == null) {
//            return;
//        }
//        mNotification.setAutoCancel(true).setContentTitle(contentTitle).setContentText(contentText)
//                .setSmallIcon(iconDrawableId).setWhen(System.currentTimeMillis())
//                .setContentIntent(getPendingIntenActivity(intentFilter));
//
//        mNotificationManager.notify(mNotificationType, mNotification.getNotification());
        
		if (mNotification == null) {
			mNotification = new Notification();
			if (mNotification == null) {
				return;
			}
		}
      mNotification.icon = iconDrawableId;
      mNotification.tickerText = contentTitle;
      mNotification.flags |= Notification.FLAG_NO_CLEAR; 
      mNotification.when = System.currentTimeMillis();
      mNotification.setLatestEventInfo(mNotificationContext, contentTitle, contentText, getPendingIntenActivity(intentFilter));
      mNotificationManager.notify(mNotificationType, mNotification);
      mNotification = null;
    }
    
    private void configAndShowNotification(int iconDrawableId, CharSequence contentTitle,
            CharSequence contentText, PendingIntent intent) {
//        mNotification = new Notification.Builder(mNotificationContext);
//
//        if (mNotification == null) {
//            return;
//        }
//        mNotification.setAutoCancel(true).setContentTitle(contentTitle).setContentText(contentText)
//                .setSmallIcon(iconDrawableId).setWhen(System.currentTimeMillis())
//                .setContentIntent(intent);
//
//        mNotificationManager.notify(mNotificationType, mNotification.getNotification());
//        mNotification = null;
    	if (mNotification == null) {
			mNotification = new Notification();
			if (mNotification == null) {
				return;
			}
		}
      mNotification.icon = iconDrawableId;
      mNotification.tickerText = contentTitle;
      mNotification.flags |= Notification.FLAG_NO_CLEAR; 
      mNotification.when = System.currentTimeMillis();
      mNotification.setLatestEventInfo(mNotificationContext, contentTitle, contentText, intent);
      mNotificationManager.notify(mNotificationType, mNotification);
      mNotification = null;
    }

    public void clearNotification() {
    	Log.d(TAG, "clearNotification");
        if (mNotification != null) {
            mNotificationManager.cancel(mNotificationType);
            Log.d(TAG, "clearNotification+mNotificationType = " +mNotificationType);
            mNotification = null;
        }	
        return;
    }
}
