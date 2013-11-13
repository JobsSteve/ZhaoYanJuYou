package com.zhaoyan.juyou.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.search.ConnectHelper;
import com.zhaoyan.juyou.R;

public class InviteActivity extends BaseActivity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite);

		initTitle(R.string.invite_install);
		initView();
	}

	private void initView() {
		View weixinView = findViewById(R.id.ll_invite_weixin);
		View qqView = findViewById(R.id.ll_invite_qq);
		View bluetoothView = findViewById(R.id.ll_invite_bluetooth);
		View weiboSinaView = findViewById(R.id.ll_invite_weibo_sina);
		View weiboTencentView = findViewById(R.id.ll_invite_weibo_tencent);
		View zeroGprsView = findViewById(R.id.ll_invite_zero_gprs);

		weixinView.setOnClickListener(this);
		qqView.setOnClickListener(this);
		bluetoothView.setOnClickListener(this);
		weiboSinaView.setOnClickListener(this);
		weiboTencentView.setOnClickListener(this);
		zeroGprsView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		switch (v.getId()) {
		case R.id.ll_invite_weixin:
			break;
		case R.id.ll_invite_qq:
			break;
		case R.id.ll_invite_bluetooth:
			intent.setClass(this, InviteBluetoothActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_right_in, 0);
			break;
		case R.id.ll_invite_weibo_sina:
			break;
		case R.id.ll_invite_weibo_tencent:
			break;
		case R.id.ll_invite_zero_gprs:
			zeroGprsInviteCheck();
			break;
		default:
			break;
		}
	}

	private void zeroGprsInviteCheck() {
		final SocketCommunicationManager manager = SocketCommunicationManager
				.getInstance(getApplicationContext());

		if (manager.isConnected() || manager.isServerAndCreated()) {
			showDisconnectDialog();
		} else {
			launchZeroGprsInvite();
		}
	}

	private void showDisconnectDialog() {
		Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(R.string.http_share_open_warning_dialog_title)
				.setMessage(R.string.http_share_open_warning_dialog_message)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								disconnectCurrentNetwork();
								launchZeroGprsInvite();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create().show();
	}

	private void launchZeroGprsInvite() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(InviteActivity.this, InviteHttpActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_right_in, 0);
	}

	private void disconnectCurrentNetwork() {
		ConnectHelper connectHelper = ConnectHelper
				.getInstance(getApplicationContext());
		connectHelper.stopSearch();

		SocketCommunicationManager manager = SocketCommunicationManager
				.getInstance(getApplicationContext());
		manager.closeAllCommunication();
		manager.stopServer();
		UserManager.getInstance().resetLocalUserID();
	}

}
