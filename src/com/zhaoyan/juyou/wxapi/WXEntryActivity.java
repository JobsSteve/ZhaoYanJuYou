package com.zhaoyan.juyou.wxapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.app.ProgressDialog;
import com.zhaoyan.juyou.activity.BaseActivity;
import com.zhaoyan.juyou.activity.InviteBluetoothActivity;
import com.zhaoyan.juyou.activity.InviteHttpActivity;

import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.InviteAPI;
import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.mm.sdk.openapi.BaseReq;
import com.tencent.mm.sdk.openapi.BaseResp;
import com.tencent.mm.sdk.openapi.ConstantsAPI;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.open.SocialConstants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tencent.weibo.sdk.android.api.WeiboAPI;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.tencent.weibo.sdk.android.component.Authorize;
import com.tencent.weibo.sdk.android.component.sso.AuthHelper;
import com.tencent.weibo.sdk.android.component.sso.OnAuthListener;
import com.tencent.weibo.sdk.android.component.sso.WeiboToken;
import com.tencent.weibo.sdk.android.model.AccountModel;
import com.tencent.weibo.sdk.android.model.BaseVO;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;

import com.zhaoyan.communication.SocketCommunicationManager;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.communication.connect.ServerCreator;
import com.zhaoyan.communication.search.ServerSearcher;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.R;

public class WXEntryActivity extends BaseActivity implements OnClickListener, 
        IWXAPIEventHandler, HttpCallback, IWeiboHandler.Response{
	private static final String TAG = "WXEntryActivity";
	
	// For Test
	private static int mFlag = 0;
	
	// Tencent WeiXin Call Interface
	private IWXAPI api;
	
	private ProgressDialog dialog;
	
	private static final int MSG_TTWB_AUTH_SUCCESS = 0;
	
	private InviteRequestListener mInviteRequestListener = new InviteRequestListener();
	private IWeiboShareAPI  mWeiboShareAPI = null;
	
	// Tencent QQ Interface
    private Tencent mTencentQQ = null;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_TTWB_AUTH_SUCCESS:
				Log.d(TAG, "handleMessage: id = " + msg.what);
				addContentToTTWB();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite);
		
		dialog = new ProgressDialog(WXEntryActivity.this);
		dialog.setMessage("正在发送请稍后......");
		
		// Get the WeiXin api base on the APP ID
		api = WXAPIFactory.createWXAPI(this, ZYConstant.TT_WX_APP_ID);
		api.handleIntent(getIntent(), this);
		
		// Get the Sina WeiBo api
		mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, ZYConstant.SINA_WB_APP_KEY);
		if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }
		
		// Gain the Tencent QQ interface
		mTencentQQ = Tencent.createInstance(ZYConstant.TENCENT_QQ_APP_ID, getApplicationContext());
	
		initTitle(R.string.invite_install);
		initView();
		
		Log.d(TAG, "onCreate!!!");
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        api.handleIntent(intent, this);
        
        mWeiboShareAPI.handleWeiboResponse(intent, this);
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
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	@Override
	public void onReq(BaseReq req) {
		switch (req.getType()) {
		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
            // For Later
			break;
		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
            // For Later
			break;
		default:
			break;
		}
	}

	@Override
	public void onResp(BaseResp resp) {
		int result = 0;
		
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = R.string.errcode_success;
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = R.string.errcode_cancel;
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = R.string.errcode_deny;
			break;
		default:
			result = R.string.errcode_unknown;
			break;
		}
		
		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
	}
	
	private void sendToWXMoments() {
		if (api.isWXAppInstalled() == false) {
			// Please install WeiXin client
			Toast.makeText(this, R.string.please_install_weixin, Toast.LENGTH_LONG).show();
			
			return;
		}
		
        // Init the WXTextObject instance
		WXTextObject textObj = new WXTextObject();
		textObj.text = ZYConstant.SHARE_STRING;

        // Init the WXMediaMessage instance
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = textObj;
		// Title is ignored, when send the text message
		// msg.title = "Will be ignored";
		msg.description = ZYConstant.SHARE_STRING;

		// Create the send request
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = buildTransaction("text");
		req.message = msg;
		// Send to WeiXin Moments
		int wxSdkVersion = api.getWXAppSupportAPI();
		if (wxSdkVersion >= ZYConstant.TIMELINE_SUPPORTED_VERSION) {
			// Send to WeiXin Moments
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		} else {
			// Send to WeiXin Friends
			req.scene = SendMessageToWX.Req.WXSceneSession;
		}
		// req.scene = SendMessageToWX.Req.WXSceneTimeline; // SendMessageToWX.Req.WXSceneSession;
		
		api.sendReq(req);
		
		// Must be finish, WeiXin can reopen this activity
		finish();
	}
	
	@Override
	public void onResult(Object object) {
		{
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			
			Log.d(TAG, "onResult!!!");

			if (object != null) {
				ModelResult result = (ModelResult) object;
				if (result.isExpires()) {
					Toast.makeText(WXEntryActivity.this,
							result.getError_message(), Toast.LENGTH_SHORT)
							.show();
				} else {
					if (result.isSuccess()) {
						Toast.makeText(WXEntryActivity.this, "发送成功", Toast.LENGTH_LONG)
								.show();
						Log.d(TAG, object.toString());
					} else {
						Toast.makeText(WXEntryActivity.this,
								((ModelResult) object).getError_message(), Toast.LENGTH_LONG)
								.show();
					}
				}

			}

		}

	}
	
	private void addContentToTTWB(String accessToken) {
		AccountModel account = new AccountModel(accessToken);
		WeiboAPI weiboAPI = new WeiboAPI(account);
		
		Log.d(TAG, "addContentToTTWB: accessToken = " + accessToken);
		
		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
		}
		
		double longitude = 0d;
		double latitude = 0d;

		mFlag++;
		weiboAPI.addWeibo(getApplicationContext(), ZYConstant.SHARE_STRING + mFlag, "json", longitude,
		 		latitude, 0, 0, this, null, BaseVO.TYPE_JSON);
	}
	
	private void addContentToTTWB() {
		String accessToken = Util.getSharePersistent(getApplicationContext(), "ACCESS_TOKEN");
		
		if (accessToken == null || "".equals(accessToken)) {
			Toast.makeText(WXEntryActivity.this, "accessToken is empty", Toast.LENGTH_LONG).show();
		} else {
			addContentToTTWB(accessToken);
		}
	}
	
	private void authTTWB() {
		long appid = Long.valueOf(Util.getConfig().getProperty("APP_KEY"));
		String app_secket = Util.getConfig().getProperty("APP_KEY_SEC");
		
		Log.d(TAG, "authTTWB: id = " + appid + ", sct = " + app_secket);
		
		final Context context = this.getApplicationContext();
		AuthHelper.register(this, appid, app_secket, new OnAuthListener() {

			@Override
			public void onWeiBoNotInstalled() {
				Toast.makeText(WXEntryActivity.this, "onWeiBoNotInstalled", Toast.LENGTH_LONG).show();
				Intent i = new Intent(WXEntryActivity.this,Authorize.class);
				startActivity(i);
			}

			@Override
			public void onWeiboVersionMisMatch() {
				Toast.makeText(WXEntryActivity.this, "onWeiboVersionMisMatch", Toast.LENGTH_LONG).show();
				Intent i = new Intent(WXEntryActivity.this, Authorize.class);
				startActivity(i);
			}

			@Override
			public void onAuthFail(int result, String err) {
				Toast.makeText(WXEntryActivity.this, "result : " + result, Toast.LENGTH_LONG) .show();
			}

			//授权成功，走这里
			//授权成功后，所有的授权信息是存放在WeiboToken对象里面的，可以根据具体的使用场景，将授权信息存放到自己期望的位置，
			//在这里，存放到了applicationcontext中
			@Override
			public void onAuthPassed(String name, WeiboToken token) {
				Toast.makeText(WXEntryActivity.this, "passed", Toast.LENGTH_LONG).show();
				//
				Util.saveSharePersistent(context, "ACCESS_TOKEN", token.accessToken);
				Util.saveSharePersistent(context, "EXPIRES_IN", String.valueOf(token.expiresIn));
				Util.saveSharePersistent(context, "OPEN_ID", token.openID);
//				Util.saveSharePersistent(context, "OPEN_KEY", token.omasKey);
				Util.saveSharePersistent(context, "REFRESH_TOKEN", "");
//				Util.saveSharePersistent(context, "NAME", name);
//				Util.saveSharePersistent(context, "NICK", name);
				Util.saveSharePersistent(context, "CLIENT_ID", Util.getConfig().getProperty("APP_KEY"));
				Util.saveSharePersistent(context, "AUTHORIZETIME",
						String.valueOf(System.currentTimeMillis() / 1000l));
				
				// Begin to send text to TTWB
				Message message = mHandler.obtainMessage();
				message.what = MSG_TTWB_AUTH_SUCCESS;
				message.sendToTarget();
			}
		});

		// Begin to auth
		AuthHelper.auth(this, "");
	}
	
	private void sendToTTWB() {
		String accessToken = Util.getSharePersistent(getApplicationContext(), "ACCESS_TOKEN");
		
		Log.d(TAG, "sendToTTWB: accessToken = " + accessToken);
		
		if (accessToken == null || "".equals(accessToken)) {
			// Pleanse Auth
			authTTWB();
		} else {
			// Send String to Tencent WeiBo
			addContentToTTWB(accessToken);
		}
	}
	
	private void shareToQQ() {
		Log.d(TAG, "shareToQQ: share QQ!");
		final Bundle params = new Bundle();
		
		// 这里可能和授权有关系，所以在QQ上显示不完整，而换成测试中的TARGET_URL就不同了,
		// 对方显示的内容，完全是根据这个地址来的，其他的Title/Summary等等设置没有效果了。
		params.putString(Tencent.SHARE_TO_QQ_APP_NAME, "雪山");
		params.putInt(Tencent.SHARE_TO_QQ_KEY_TYPE, Tencent.SHARE_TO_QQ_TYPE_DEFAULT);
		params.putInt(Tencent.SHARE_TO_QQ_EXT_INT, 0);
		params.putString(Tencent.SHARE_TO_QQ_TITLE, "雪山");
		params.putString(Tencent.SHARE_TO_QQ_TARGET_URL, "http://app.baidu.com/app/enter?appid=106056");
		params.putString(Tencent.SHARE_TO_QQ_SUMMARY, "一款移动互联网综合平台应用。");
		// 暂时用这种图片，实际上用本地图片最好了，后面再替换吧
		// params.putString(Tencent.SHARE_TO_QQ_IMAGE_URL, "http://img3.douban.com/lpic/s3635685.jpg");
		
		/*
		int shareType = Tencent.SHARE_TO_QQ_TYPE_DEFAULT;
		if (shareType != Tencent.SHARE_TO_QQ_TYPE_IMAGE) {
        	params.putString(Tencent.SHARE_TO_QQ_TITLE, "Title");
            params.putString(Tencent.SHARE_TO_QQ_TARGET_URL, "http://douban.fm/?start=8508g3c27g-3&cid=-3");
            params.putString(Tencent.SHARE_TO_QQ_SUMMARY, "Summary");
        }
        if (shareType == Tencent.SHARE_TO_QQ_TYPE_IMAGE) {
            params.putString(Tencent.SHARE_TO_QQ_IMAGE_LOCAL_URL, "http://img3.douban.com/lpic/s3635685.jpg");
        } else {
            params.putString(Tencent.SHARE_TO_QQ_IMAGE_URL, "http://img3.douban.com/lpic/s3635685.jpg");
        }
        params.putString(shareType == Tencent.SHARE_TO_QQ_TYPE_IMAGE ? Tencent.SHARE_TO_QQ_IMAGE_LOCAL_URL 
        		: Tencent.SHARE_TO_QQ_IMAGE_URL, "http://img3.douban.com/lpic/s3635685.jpg");
        params.putString(Tencent.SHARE_TO_QQ_APP_NAME, "Test");
        params.putInt(Tencent.SHARE_TO_QQ_KEY_TYPE, shareType);
        params.putInt(Tencent.SHARE_TO_QQ_EXT_INT, 0x00);
        if (shareType == Tencent.SHARE_TO_QQ_TYPE_AUDIO) {
            params.putString(Tencent.SHARE_TO_QQ_AUDIO_URL, "http://img3.douban.com/lpic/s3635685.jpg");
        }
        */
		
		// Start Share Thread
		final Activity activity = WXEntryActivity.this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                mTencentQQ.shareToQQ(activity, params, new IUiListener() {

                    @Override
                    public void onComplete(JSONObject response) {
                        // TODO Auto-generated method stub
                    	Log.d(TAG, "shareToQQ: onComplete: " + response.toString());
                        Toast.makeText(activity, "onComplete: " + response.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(UiError e) {
                    	Log.d(TAG, "shareToQQ: onError: " + e.errorMessage);
                        Toast.makeText(activity, "onError: " + e.errorMessage, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                    	Log.d(TAG, "shareToQQ: onCancel!");
                        Toast.makeText(activity, "onCancel!", Toast.LENGTH_LONG).show();
                    }

                });
            }
        }).start();
	}
	
	private void inviteToQQ() {
		Log.d(TAG, "inviteToQQ: invite QQ!");
		
		Bundle params = new Bundle();
        // TODO keywords.
        params.putString(SocialConstants.PARAM_APP_ICON,
                "http://imgcache.qq.com/qzone/space_item/pre/0/66768.gif");
        params.putString(SocialConstants.PARAM_APP_DESC ,
                "AndroidSdk_2_0: voice description!");
        params.putString(SocialConstants.PARAM_ACT, "进入应用");
        
        //mSocialApi.voice(MainActivity.this, params, new BaseUiListener());
        mTencentQQ.invite(WXEntryActivity.this, params, new InviteQQUIListener(WXEntryActivity.this));
	}
	
	private void sendToQQZero() {
		if (mTencentQQ != null) {
			if (mTencentQQ.isSessionValid()) {
				// Invite QQ after login QQ
				// inviteToQQ();
				shareToQQ();
			} else {
				// Share QQ when QQ can't login
				shareToQQ();
			}
		} else {
		    Log.d(TAG, "QQConnect: mTencentQQ is null!");
		    Toast.makeText(this, "QQ Interface Error!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		switch (v.getId()) {
		case R.id.ll_invite_weixin:
			// Send SHARE_STRING to WeiXin Moments
			sendToWXMoments();
			break;
		case R.id.ll_invite_qq:
			sendToQQZero();
			break;
		case R.id.ll_invite_bluetooth:
			intent.setClass(this, InviteBluetoothActivity.class);
			startActivity(intent);
			overridePendingTransition(R.anim.activity_right_in, 0);
			break;
		case R.id.ll_invite_weibo_sina:
			sendToSinaWB();
			break;
		case R.id.ll_invite_weibo_tencent:
			// Send SHARE_STRING to Tencent WeiBo
			Log.d(TAG, "onClick: invite_weibo_tencent");
			sendToTTWB();
			break;
		case R.id.ll_invite_zero_gprs:
			zeroGprsInviteCheck();
			break;
		default:
			break;
		}
	}
	
	private void showToast(boolean bSuccess, String message) {
		String finalMsg = bSuccess ? "Invite Success": "Invite Fail";
        if (!TextUtils.isEmpty(message)) {
            finalMsg = finalMsg + "：" + message;
        }
                    
        Toast.makeText(this, finalMsg, Toast.LENGTH_LONG).show();
    }
	
	private class InviteRequestListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            LogUtil.d(TAG, "Invite Response: " + response);

            if (TextUtils.isEmpty(response) || response.contains("error_code")) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String errorMsg = obj.getString("error");
                    String errorCode = obj.getString("error_code");
                    String message = "error_code: " + errorCode + "error_message: " + errorMsg;
                    LogUtil.e(TAG, "Invite Failed: " + message);
                    showToast(false, message);
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                showToast(true, null);
            }
        }

        @Override
        public void onComplete4binary(ByteArrayOutputStream responseOS) {
            // Do nothing
        }

        @Override
        public void onIOException(IOException e) {
            showToast(false, e.getMessage());
        }

        @Override
        public void onError(WeiboException e) {
            showToast(false, e.getMessage());
        }
    }
	
	private void inviteSinaWBFans(Oauth2AccessToken accessToken) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(InviteAPI.KEY_TEXT,        "这个游戏太好玩了，加入一起玩吧");
            jsonObject.put(InviteAPI.KEY_URL,         "http://app.sina.com.cn/appdetail.php?appID=770915");
            jsonObject.put(InviteAPI.KEY_INVITE_LOGO, "http://hubimage.com2us.com/hubweb/contents/123_499.jpg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        // Test UID
        String uid = "2785593564";
        // Invite Sina WeiBo Fans
        new InviteAPI(accessToken).sendInvite(uid, jsonObject, mInviteRequestListener);
	}
	
	private void addContentToSinaWB() {
		if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351 /*ApiUtils.BUILD_INT_VER_2_2*/) {
            	WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
            	TextObject textObject = new TextObject();
                textObject.text = ZYConstant.SHARE_STRING;
                weiboMessage.textObject = textObject;
                SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
                request.transaction = String.valueOf(System.currentTimeMillis());
                request.multiMessage = weiboMessage;
                
                // Enter Sina WeiBo Client
                mWeiboShareAPI.registerApp();
                mWeiboShareAPI.sendRequest(request);
            } else {
            	WeiboMessage weiboMessage = new WeiboMessage();
            	TextObject textObject = new TextObject();
                textObject.text = ZYConstant.SHARE_STRING;
                weiboMessage.mediaObject = textObject;
                SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
                request.transaction = String.valueOf(System.currentTimeMillis());
                request.message = weiboMessage;
                
                // Enter Sina WeiBo Client
                mWeiboShareAPI.registerApp();
                mWeiboShareAPI.sendRequest(request);
            }
        } else {
            Toast.makeText(this, "API Error!", Toast.LENGTH_SHORT).show();
        }
	}
	
	private void sendToSinaWB() {
		Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(WXEntryActivity.this);
        if (accessToken != null && accessToken.isSessionValid()) {
            // Invite Sina WeiBo fans, when Access Token is valid
        	// Temp Cancel
        	// inviteSinaWBFans(accessToken);
        	Log.d(TAG, "Invite Sina WeiBo Fans!");
        	addContentToSinaWB();
        } else {
            // Send SHARE_STRING to Sina WeiBo
        	Log.d(TAG, "Share Sina WeiBo!");
        	addContentToSinaWB();
        }
	}
	
	@Override
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
        case WBConstants.ErrorCode.ERR_OK:
            Toast.makeText(this, "Share Success!", Toast.LENGTH_LONG).show();
            break;
        case WBConstants.ErrorCode.ERR_CANCEL:
            Toast.makeText(this, "Share Cancel!", Toast.LENGTH_LONG).show();
            break;
        case WBConstants.ErrorCode.ERR_FAIL:
            Toast.makeText(this, 
                    "Share Failed, " + "Error Message: " + baseResp.errMsg, 
                    Toast.LENGTH_LONG).show();
            break;
        }
    }

	private void zeroGprsInviteCheck() {
		final SocketCommunicationManager manager = SocketCommunicationManager
				.getInstance();

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
		intent.setClass(WXEntryActivity.this, InviteHttpActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_right_in, 0);
	}

	private void disconnectCurrentNetwork() {
		ServerSearcher serverSearcher = ServerSearcher
				.getInstance(getApplicationContext());
		serverSearcher.stopSearch(ServerSearcher.SERVER_TYPE_ALL);

		ServerCreator serverCreator = ServerCreator
				.getInstance(getApplicationContext());
		serverCreator.stopServer();

		SocketCommunicationManager manager = SocketCommunicationManager
				.getInstance();
		manager.closeAllCommunication();
		manager.stopServer();
		UserManager.getInstance().resetLocalUser();
	}

}
