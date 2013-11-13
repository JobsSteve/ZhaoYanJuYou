package com.zhaoyan.common.net.http;

import android.content.Context;
import android.net.Uri;

public class HttpShareServer {
	private MyHttpServer mServer;

	public boolean createHttpShare(Context context, int port, Uri fileUri) {
		Util.context = context.getApplicationContext();
		if (mServer != null) {
			return true;
		}
		mServer = new MyHttpServer(port);
		mServer.setFile(fileUri);
		mServer.startServer();

		return true;
	}

	public void stopServer() {
		if (mServer != null) {
			mServer.stopServer();
			mServer = null;
		}
		Util.context = null;
	}

}
