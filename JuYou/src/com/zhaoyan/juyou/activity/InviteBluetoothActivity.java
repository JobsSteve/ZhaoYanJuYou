package com.zhaoyan.juyou.activity;

import java.io.File;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.zhaoyan.common.util.Log;
import com.zhaoyan.juyou.R;

public class InviteBluetoothActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "InviteBluetoothActivity";

	private Button mSendBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.invite_bluetooth);

		initTitle(R.string.invite_bluetooth);
		initView();
	}

	private void initView() {
		mSendBtn = (Button) findViewById(R.id.bluetooth_send_btn);
		mSendBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bluetooth_send_btn:
			// tranfer file by bluetooth
			Intent intent = new Intent();
			intent.setType("*/*");
			intent.setAction(Intent.ACTION_SEND);
			ApplicationInfo packageInfo = getApplicationInfo();
			Uri uri = Uri.fromFile(new File(packageInfo.sourceDir));
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			try {
				// set send by bluetooth ,only bluetooth
				intent.setClassName("com.android.bluetooth",
						"com.android.bluetooth.opp.BluetoothOppLauncherActivity");
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				Log.d(TAG,
						"Can not find BluetoothOppLauncherActivity. Try mediatek's BluetoothShareGatewayActivity"
								+ e.toString());
				try {
					intent.setClassName("com.mediatek.bluetooth",
							"com.mediatek.bluetooth.BluetoothShareGatewayActivity");
					startActivity(intent);
				} catch (ActivityNotFoundException e2) {
					Log.d(TAG,
							"Can not find BluetoothShareGatewayActivity. Do not set class name."
									+ e.toString());
					intent.setComponent(null);
					startActivity(intent);
				}
			}
			break;

		default:
			break;
		}
	}

	public void exitActivity() {
		finish();
	}
}
