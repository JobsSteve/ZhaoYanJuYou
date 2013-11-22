package com.zhaoyan.juyou;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.FileTransferService;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.TrafficStatics;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.search.ConnectHelper;
import com.zhaoyan.communication.search.SearchUtil;

public class JuYouApplication extends Application {
	private static final String TAG = "JuYouApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		TrafficStatics.getInstance().init(getApplicationContext());
	}

	public static void quitApplication(Context context) {
		Log.d(TAG, "quitApplication");
		stopCommunication(context);
		TrafficStatics.getInstance().quit();
		stopFileTransferService(context);
	}

	private static void stopFileTransferService(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, FileTransferService.class);
		context.stopService(intent);
	}

	private static void stopCommunication(Context context) {
		ConnectHelper connectHelper = ConnectHelper.getInstance(context);
		connectHelper.stopSearch();

		UserManager.getInstance().resetLocalUserID();
		SocketCommunicationManager manager = SocketCommunicationManager
				.getInstance(context);
		manager.closeAllCommunication();
		manager.stopServer();

		// Disable wifi AP.
		NetWorkUtil.setWifiAPEnabled(context, null, false);
		// Clear wifi connect history.
		SearchUtil.clearWifiConnectHistory(context);
		// Stop record log and close log file.

		Log.stopAndSave();
	}
}
