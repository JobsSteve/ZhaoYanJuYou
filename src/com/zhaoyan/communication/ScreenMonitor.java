package com.zhaoyan.communication;

import java.util.Vector;

import com.zhaoyan.common.util.Log;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class ScreenMonitor extends Service {
	private static final String TAG = "ScreenMonitorService";
	private SocketCommunicationManager mCommunicationManager;
	private ScreenMonitorBroadcastReceiver mReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
		mCommunicationManager = SocketCommunicationManager
				.getInstance(getApplicationContext());
		mReceiver = new ScreenMonitorBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mReceiver, intentFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class ScreenMonitorBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_SCREEN_ON.equals(action)) {
				Log.d(TAG, "ACTION_SCREEN_ON");
				Vector<SocketCommunication> communications = mCommunicationManager.getCommunications();
				for (SocketCommunication communication : communications) {
					communication.setScreenOn();
				}
				
			} else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				Log.d(TAG, "ACTION_SCREEN_OFF");
				Vector<SocketCommunication> communications = mCommunicationManager.getCommunications();
				for (SocketCommunication communication : communications) {
					communication.setScreenOff();
				}
			}
		}

	}

}
