package com.zhaoyan.juyou;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.util.Log;
import com.zhaoyan.communication.FileTransferService;
import com.zhaoyan.communication.ProtocolCommunication;
import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.TrafficStatics;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.connect.ServerConnector;
import com.zhaoyan.communication.connect.ServerCreator;
import com.zhaoyan.communication.search.SearchUtil;
import com.zhaoyan.communication.search.ServerSearcher;

public class JuYouApplication extends Application {
	private static final String TAG = "JuYouApplication";
	private static boolean mIsInit = false;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		initApplication(getApplicationContext());
	}

	/**
	 * Notice, call this not only in application's {@link #onCreate()}, but also
	 * in the first activity's onCreate(). Because application's
	 * {@link #onCreate()} will not be call every time when we launch first
	 * activity.
	 * 
	 * @param context
	 */
	public static synchronized void initApplication(Context context) {
		if (mIsInit) {
			return;
		}
		Log.d(TAG, "initApplication");
		mIsInit = true;

		initImageLoader(context);
		// Start save log to file.
		Log.startSaveToFile();
		// Initialize TrafficStatics
		TrafficStatics.getInstance().init(context);
		// Initialize SocketCommunicationManager
		SocketCommunicationManager.getInstance().init(context);
		// Initialize ProtocolCommunication
		ProtocolCommunication.getInstance().init(context);
	}

	public static synchronized void quitApplication(Context context) {
		if (!mIsInit) {
			return;
		}
		Log.d(TAG, "quitApplication");
		mIsInit = false;
		logout(context);
		stopServerSearch(context);
		stopServerCreator(context);
		stopCommunication(context);
		stopFileTransferService(context);
		// Release ProtocolCommunication
		ProtocolCommunication.getInstance().release();
		// Release SocketCommunicationManager
		SocketCommunicationManager.getInstance().release();
		// Release TrafficStatics
		TrafficStatics.getInstance().quit();
		// Logout account
		logoutAccount(context);
		// Stop record log and close log file.
		Log.stopAndSave();
		releaseStaticInstance(context);
	}

	private static void logoutAccount(Context context) {
		AccountHelper.logoutCurrentAccount(context);
	}

	private static void logout(Context context) {
		ProtocolCommunication protocolCommunication = ProtocolCommunication
				.getInstance();
		protocolCommunication.logout();
	}

	private static void releaseStaticInstance(Context context) {
		ServerConnector serverConnector = ServerConnector.getInstance(context);
		serverConnector.release();
	}

	private static void stopServerCreator(Context context) {
		ServerCreator serverCreator = ServerCreator.getInstance(context);
		serverCreator.stopServer();
		serverCreator.release();
	}

	private static void stopServerSearch(Context context) {
		ServerSearcher serverSearcher = ServerSearcher.getInstance(context);
		serverSearcher.stopSearch(ServerSearcher.SERVER_TYPE_ALL);
		serverSearcher.release();
	}

	private static void stopFileTransferService(Context context) {
		Intent intent = new Intent();
		intent.setClass(context, FileTransferService.class);
		context.stopService(intent);
	}

	private static void stopCommunication(Context context) {
		UserManager.getInstance().resetLocalUser();
		SocketCommunicationManager manager = SocketCommunicationManager
				.getInstance();
		manager.closeAllCommunication();
		manager.stopServer();

		// Disable wifi AP.
		NetWorkUtil.setWifiAPEnabled(context, null, false);
		// Clear wifi connect history.
		SearchUtil.clearWifiConnectHistory(context);
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // remove it when release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}
