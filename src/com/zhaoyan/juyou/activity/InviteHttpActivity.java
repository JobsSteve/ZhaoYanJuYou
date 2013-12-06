package com.zhaoyan.juyou.activity;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.common.net.WiFiAP;
import com.zhaoyan.common.net.http.HttpShareServer;
import com.zhaoyan.common.util.QRCodeEncoder;
import com.zhaoyan.communication.SocketPort;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.dialog.ZyProgressDialog;

public class InviteHttpActivity extends BaseActivity {
	private static final String TAG = "InviteHttpActivity";

	private String WIFI_AP_NAME;

	private TextView mWiFiAPNameTextView;
	private TextView mAddressTextView;
	private ImageView mQuickResponseCodeImageView;
	private Context mContext;

	private static final int HTTP_SHARE_SERVER_PORT = SocketPort.HTTP_SHARE_SERVER_PORT;
	private HttpShareServer mHttpShareServer;

	private Bitmap mQRCodeBitmap;
	private ZyProgressDialog mZyProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_http);

		mContext = this;
		WIFI_AP_NAME = getString(R.string.app_name);

		initTitle(R.string.http_share_title);
		initView();

		mZyProgressDialog = new ZyProgressDialog(mContext);
		mZyProgressDialog.setMessage(R.string.invite_http_opening);
		mZyProgressDialog.show();

		mHttpShareServer = new HttpShareServer();

		disableWiFiAP();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WiFiAP.ACTION_WIFI_AP_STATE_CHANGED);
		registerReceiver(mWiFiAPBroadcastReceiver, intentFilter);
		enableWiFiAP();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateView();
	}

	private boolean createHttpShareServer() {
		ApplicationInfo packageInfo = getApplicationInfo();
		Uri uri = Uri.fromFile(new File(packageInfo.sourceDir));

		boolean result = mHttpShareServer.createHttpShare(
				getApplicationContext(), HTTP_SHARE_SERVER_PORT, uri);
		if (result) {
			Log.d(TAG, "createHttpShare port: " + HTTP_SHARE_SERVER_PORT
					+ " successs.");
			if (mZyProgressDialog.isShowing()) {
				mZyProgressDialog.dismiss();
			}
		} else {
			Log.e(TAG, "createHttpShare port: " + HTTP_SHARE_SERVER_PORT
					+ " fail.");
		}
		return result;
	}

	private boolean enableWiFiAP() {
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			return true;
		}
		boolean result = NetWorkUtil.setWifiAPEnabled(getApplicationContext(),
				WIFI_AP_NAME, true);
		if (result) {
			Log.d(TAG, "enableWiFiAP: " + WIFI_AP_NAME + " success.");
		} else {
			Log.e(TAG, "enableWiFiAP: " + WIFI_AP_NAME + " fail.");
		}
		return result;
	}

	private void disableWiFiAP() {
		if (NetWorkUtil.isWifiApEnabled(mContext)) {
			NetWorkUtil.setWifiAPEnabled(mContext, null, false);
		}
	}

	private String getHttpShareUrl() {
		String ipAddress = WiFiAP.IP;
		return "http://" + ipAddress + ":" + HTTP_SHARE_SERVER_PORT;
	}

	private void initView() {
		mWiFiAPNameTextView = (TextView) findViewById(R.id.tv_network_http_share_ap_name);
		mAddressTextView = (TextView) findViewById(R.id.tv_network_http_share_address);
		mQuickResponseCodeImageView = (ImageView) findViewById(R.id.iv_network_http_share);
	}

	private void updateView() {
		mWiFiAPNameTextView.setText(getString(
				R.string.http_share_first_step_tip, WIFI_AP_NAME));

		mAddressTextView.setText(getString(
				R.string.http_share_second_step_tip1, getHttpShareUrl()));
		releaseQRCodeBitmap();
		try {
			mQRCodeBitmap = QRCodeEncoder.createQRCode(getHttpShareUrl(), 350);

			mQuickResponseCodeImageView.setImageBitmap(mQRCodeBitmap);
		} catch (WriterException e) {
			Log.e(TAG, "createQRCode fail." + e);
		}
	}

	private void releaseQRCodeBitmap() {
		if (mQRCodeBitmap != null) {
			mQuickResponseCodeImageView.setImageDrawable(null);
			mQRCodeBitmap.recycle();
			mQRCodeBitmap = null;
		}
	}

	@Override
	protected void onDestroy() {
		releaseQRCodeBitmap();
		mHttpShareServer.stopServer();

		try {
			unregisterReceiver(mWiFiAPBroadcastReceiver);
		} catch (Exception e) {
			// ignore.
		}

		disableWiFiAP();
		super.onDestroy();
	}

	private BroadcastReceiver mWiFiAPBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, android.content.Intent intent) {
			String action = intent.getAction();
			if (WiFiAP.ACTION_WIFI_AP_STATE_CHANGED.equals(action)) {
				int wifiApState = intent
						.getIntExtra(WiFiAP.EXTRA_WIFI_AP_STATE,
								WiFiAP.WIFI_AP_STATE_FAILED);
				handleWifiApchanged(wifiApState);
			}
		}

		private void handleWifiApchanged(int state) {
			switch (state) {
			case WiFiAP.WIFI_AP_STATE_ENABLED:
				createHttpShareServer();
				break;
			case WiFiAP.WIFI_AP_STATE_DISABLED:
				enableWiFiAP();
				break;
			default:
				break;
			}
		}
	};

}
