package com.zhaoyan.juyou.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dreamlink.communication.aidl.User;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.UsersAPI;
import com.tencent.open.HttpStatusException;
import com.tencent.open.NetworkUnavailableException;
import com.tencent.tauth.Constants;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.zhaoyan.common.net.NetWorkUtil;
import com.zhaoyan.communication.UserHelper;
import com.zhaoyan.communication.UserInfo;
import com.zhaoyan.communication.UserManager;
import com.zhaoyan.juyou.DirectLogin;
import com.zhaoyan.juyou.JuYouApplication;
import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.common.ZYConstant;
import com.zhaoyan.juyou.dialog.LoginProgressDialog;
import com.zhaoyan.juyou.provider.JuyouData;
import com.zhaoyan.juyou.wxapi.AccessTokenKeeper;

public class LoginActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "LoginActivity";
	private ImageView mTopLogoImageView;
	private Bitmap mTopLogoBitmap;
	private EditText mAccountEditText;
	private EditText mPasswordEditText;
	private Context mContext;
	
	// Sina WeiBo Auth Listener
	private SinaWBAuthListener mSinaWBAuthListener = null;
	// Sina WeiBo Auth Infomation
	private AuthInfo mSinaWBAuthInfo = null;
	// Sina WeiBo SSO Handler
	private SsoHandler mSinaWBSsoHandler = null;
	// Sina WeiBo Button for onActivityResult
	private Button mSinaWBCallBackButton = null;
	private SinaWBShowRequestListener mSinaWBShowRequestListener = new SinaWBShowRequestListener();
	
	// Tencent QQ Interface
	private Tencent mTencentQQ = null;
	private ProgressDialog dialog;
	
	private static final int MSG_TTQQ_LOGIN_USERNAME = 0;
	private static final int MSG_TTQQ_LOGIN_HEADIMAGE = 1;
	private static final int MSG_SINAWB_LOGIN_USERNAME = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.login);
		
		mSinaWBAuthListener = new SinaWBAuthListener();
		mSinaWBAuthInfo = new AuthInfo(this, ZYConstant.SINA_WB_APP_KEY, ZYConstant.SINA_WB_REDIRECT_URL, ZYConstant.SINA_WB_SCOPE);
		
		// Gain the Tencent QQ interface
		mTencentQQ = Tencent.createInstance(ZYConstant.TENCENT_QQ_APP_ID, getApplicationContext());
		dialog = new ProgressDialog(LoginActivity.this);
		dialog.setMessage("正在登录请稍候......");

		initView();
	}
	
	private void thirdEnterMain(String userName) {
		Log.d(TAG, "thirdEnterMain: userName = " + userName);
		
		// Get UserInfo and Set UserInfo
		UserInfo userInfo = UserHelper.loadLocalUser(this);
		if (null == userInfo) {
			userInfo = new UserInfo();
			userInfo.setUser(new User());
		}

		// Set UserName
		userInfo.getUser().setUserName(userName);
		// Set Head Image ID
		userInfo.setHeadId(0);
		userInfo.setHeadBitmap(null);
		// Set ThirdLogin Flag
		userInfo.setThirdLogin(1);
		
		// user type
		userInfo.setType(JuyouData.User.TYPE_LOCAL);
		// Save to database
		UserHelper.saveLocalUser(this, userInfo);

		// Update UserManager.
		UserManager userManager = UserManager.getInstance();
		userManager.setLocalUser(userInfo.getUser());

		// Enter Main Activity
		launchMain();
		finish();
	}
	
	private class SinaWBShowRequestListener implements RequestListener {

        @Override
        public void onComplete(String response) {
            Log.d(TAG, "Show Response: " + response);
            
            if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}

            if (TextUtils.isEmpty(response) || response.contains("error_code")) {
                try {
                    JSONObject obj = new JSONObject(response);
                    String errorMsg = obj.getString("error");
                    String errorCode = obj.getString("error_code");
                    String message = "error_code: " + errorCode + "error_message: " + errorMsg;
                    Log.d(TAG, "Show Failed: " + message);
                    showToast(false, message);
                    
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
            	try {
                    JSONObject obj = new JSONObject(response);
                    String screenName = obj.getString("screen_name");
                    String name = obj.getString("name");
                    String message = "screen_name: " + screenName + ", name: " + name;
                    Log.d(TAG, "Show Success: " + message);
                    showToast(true, message);
                    thirdEnterMain(screenName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
	
	private void showToast(boolean bSuccess, String message) {
		String finalMsg = bSuccess ? "Show Success" : "Show Fail";
        if (!TextUtils.isEmpty(message)) {
            finalMsg = finalMsg + "��" + message;
        }

        // Exception, Why? new thread?
        // Toast.makeText(LoginActivity.this, finalMsg, Toast.LENGTH_LONG).show();
        // For Test
        Message msg = new Message();
		msg.obj = finalMsg;
		msg.what = MSG_SINAWB_LOGIN_USERNAME;
		mHandler.sendMessage(msg);
    }
	
	private class SinaWBAuthListener implements WeiboAuthListener {
        @Override
        public void onComplete(Bundle values) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(values);
            if (accessToken != null && accessToken.isSessionValid()) {
                // String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                //         new java.util.Date(accessToken.getExpiresTime()));
                // String format = getString(R.string.weibosdk_demo_token_to_string_format_1);
                // mTokenView.setText(String.format(format, accessToken.getToken(), date));

            	Toast.makeText(LoginActivity.this, "SinaWB: Login Success!", Toast.LENGTH_LONG).show();
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), accessToken);
            }
            
            Log.d(TAG, "SinaWBAuthListener onComplete: " + values);
            
            // Get the UserName base on Oauth2AccessToken
            gainSinaWBUserName(accessToken);
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this, 
                    "Auth Cancel", Toast.LENGTH_SHORT).show();
        }
    }
	
	private class LoginQQBaseUiListener implements IUiListener {

		@Override
		public void onComplete(JSONObject response) {
			// Util.showResultDialog(LoginActivity.this, response.toString(), "��¼�ɹ�");
			doComplete(response);
		}

		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			Util.toastMessage(LoginActivity.this, "onError: " + e.errorDetail);
			Util.dismissDialog();
		}

		@Override
		public void onCancel() {
			Util.toastMessage(LoginActivity.this, "onCancel: ");
			Util.dismissDialog();
		}
	}

	private void initView() {
		mTopLogoBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.login_top);
		mTopLogoImageView = (ImageView) findViewById(R.id.iv_login_top);
		mTopLogoImageView.setImageBitmap(mTopLogoBitmap);
		Button loginButton = (Button) findViewById(R.id.btn_login);
		loginButton.setOnClickListener(this);
		Button loginDirectButton = (Button) findViewById(R.id.btn_login_direct);
		loginDirectButton.setOnClickListener(this);
		Button registerAccountButton = (Button) findViewById(R.id.btn_register);
		registerAccountButton.setOnClickListener(this);
		Button loginWeiXinButton = (Button) findViewById(R.id.btn_login_weixin);
		loginWeiXinButton.setOnClickListener(this);
		Button loginTencentWeiboButton = (Button) findViewById(R.id.btn_login_tencent_weibo);
		loginTencentWeiboButton.setOnClickListener(this);
		Button loginSinaWeiboButton = (Button) findViewById(R.id.btn_login_sina_weibo);
		loginSinaWeiboButton.setOnClickListener(this);
		Button loginQQButon = (Button) findViewById(R.id.btn_login_qq);
		loginQQButon.setOnClickListener(this);
		Button loginRenRenButton = (Button) findViewById(R.id.btn_login_renren);
		loginRenRenButton.setOnClickListener(this);

		mAccountEditText = (EditText) findViewById(R.id.et_account);
		mPasswordEditText = (EditText) findViewById(R.id.et_psw);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (mSinaWBCallBackButton != null) {
        	mSinaWBCallBackButton = null;
        	
        	Log.d(TAG, "onActivityResult call!");
        	if (mSinaWBSsoHandler != null) {
        		mSinaWBSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }
	}
	
	private void gainSinaWBUserName(Oauth2AccessToken accessToken) {
		// Call UsersAPI interface
		Log.d(TAG, "gainSinaWBUserName: call UsersAPI.show!");
		new UsersAPI(accessToken).show(Integer.parseInt(accessToken.getUid()), mSinaWBShowRequestListener);
	}
	
	private void launchSinaWB(View button) {
		Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(LoginActivity.this);
        if (accessToken != null && accessToken.isSessionValid()) {
            // Get the screen_name
        	Log.d(TAG, "launchSinaWB: begin to show name!");
        	if (dialog != null && !dialog.isShowing()) {
				dialog.show();
			}
        	gainSinaWBUserName(accessToken);
        } else {
        	Log.d(TAG, "launchSinaWB: begin to auth!");
            // Begin to auth Sina WeiBo
        	if (null == mSinaWBSsoHandler && mSinaWBAuthInfo != null) {
    			WeiboAuth weiboAuth = new WeiboAuth(mContext, mSinaWBAuthInfo);
    			mSinaWBSsoHandler = new SsoHandler((Activity)mContext, weiboAuth);
    		}
    		
            if (mSinaWBSsoHandler != null) {
            	Log.d(TAG, "launchSinaWB: begin to authorize!");
            	
            	mSinaWBSsoHandler.authorize(mSinaWBAuthListener);
            	// Save Button for onActivityResult
            	mSinaWBCallBackButton = (Button)button;
            } else {
                Log.d(TAG, "mSinaWBSsoHandler is null!");
            }
        }
	}
	
	private void gainQQLoginName() {
		if (mTencentQQ != null && mTencentQQ.isSessionValid()) {
			IRequestListener requestListener = new IRequestListener() {
				@Override
				public void onUnknowException(Exception e, Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onUnknowException!");
				}

				@Override
				public void onSocketTimeoutException(SocketTimeoutException e,
						Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onSocketTimeoutException!");
				}

				@Override
				public void onNetworkUnavailableException(
						NetworkUnavailableException e, Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onNetworkUnavailableException!");
				}

				@Override
				public void onMalformedURLException(MalformedURLException e,
						Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onMalformedURLException!");
				}

				@Override
				public void onJSONException(JSONException e, Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onJSONException!");
				}

				@Override
				public void onIOException(IOException e, Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onIOException!");
				}

				@Override
				public void onHttpStatusException(HttpStatusException e,
						Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onHttpStatusException!");
				}

				@Override
				public void onConnectTimeoutException(
						ConnectTimeoutException e, Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "QQ Login: onConnectTimeoutException!");
				}

				@Override
				public void onComplete(final JSONObject response, Object state) {
					// TODO Auto-generated method stub
					Log.d(TAG, "gainQQLoginName: onComplete!");
					Message msg = new Message();
					msg.obj = response;
					msg.what = MSG_TTQQ_LOGIN_USERNAME;
					mHandler.sendMessage(msg);
					
					// Gain User Bitmap, Temp Cancel
					/*
					new Thread(){
						@Override
						public void run() {
							if(response.has("figureurl")){
								Bitmap bitmap = null;
								try {
									bitmap = Util.getbitmap(response.getString("figureurl_qq_2"));
								} catch (JSONException e) {
									
								}
								Message msg = new Message();
								msg.obj = bitmap;
								msg.what = MSG_TTQQ_LOGIN_HEADIMAGE;
								mHandler.sendMessage(msg);
							}
						}
						
					}.start();
					*/
				}
			};
			
			Log.d(TAG, "gainQQLoginName: request userName!");
			// Send Request to QQ Server
			mTencentQQ.requestAsync(Constants.GRAPH_SIMPLE_USER_INFO, null,
	                Constants.HTTP_GET, requestListener, null);
		} else {
			Log.d(TAG, "QQ Login Fail!");
		}
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_TTQQ_LOGIN_USERNAME) {
				if (dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				
				JSONObject response = (JSONObject) msg.obj;
				Log.d(TAG, "QQ Login: response = " + response);
				if (response.has("nickname")) {
					try {
						String userName = response.getString("nickname");
						Log.d(TAG, "QQ Login: userName = " + userName);
						Toast.makeText(LoginActivity.this, "QQ Login: " + userName, Toast.LENGTH_LONG).show();
						thirdEnterMain(userName);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (msg.what == MSG_TTQQ_LOGIN_HEADIMAGE){
				// QQ Bitmap
				Bitmap bitmap = (Bitmap)msg.obj;
			} else if (msg.what == MSG_SINAWB_LOGIN_USERNAME) {
				// Display
				Toast.makeText(LoginActivity.this, (String)msg.obj, Toast.LENGTH_LONG).show();
			}
		}

	};
	
	private void launchTencentQQ() {
		if (mTencentQQ != null && mTencentQQ.isSessionValid()) {
			// Gain QQ Login Name
			if (dialog != null && !dialog.isShowing()) {
				dialog.show();
			}
			gainQQLoginName();
		} else {
			// Begin to login QQ
			IUiListener listener = new LoginQQBaseUiListener() {
				@Override
				protected void doComplete(JSONObject values) {
                    // Login Success and Gain User Name
					Log.d(TAG, "launchTencentQQ: doComplete!");
					gainQQLoginName();
				}
			};
			Log.d(TAG, "launchTencentQQ!");
			mTencentQQ.login(this, "all", listener);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			loginZhanYan();
			break;
		case R.id.btn_login_direct:
			loginDirect();
			break;
		case R.id.btn_register:
			launchRegisterAccount();
			break;
		case R.id.btn_login_weixin:
			break;
		case R.id.btn_login_tencent_weibo:
			break;
		case R.id.btn_login_sina_weibo:
			// Launch Sina WeiBo
			launchSinaWB(v);
			break;
		case R.id.btn_login_qq:
			// Launch Tencent QQ
			launchTencentQQ();
			break;
		case R.id.btn_login_renren:
			break;
		default:
			break;
		}
	}

	private void loginZhanYan() {
		String account = mAccountEditText.getText().toString();
		if (TextUtils.isEmpty(account)) {
			Toast.makeText(mContext, R.string.account_empty_please_input,
					Toast.LENGTH_SHORT).show();
			return;
		}
		String password = mPasswordEditText.getText().toString();
		if (TextUtils.isEmpty(password)) {
			Toast.makeText(mContext, R.string.password_empty_please_input,
					Toast.LENGTH_SHORT).show();
			return;
		}
		if (NetWorkUtil.isNetworkConnected(getApplicationContext())) {
			Toast.makeText(this, R.string.login_message_no_network,
					Toast.LENGTH_SHORT).show();
			return;
		}

		LoginZhaoYanTask task = new LoginZhaoYanTask();
		task.execute();
	}

	private void loginDirect() {
		DirectLogin directLogin = new DirectLogin(mContext);
		directLogin.login();
		launchMain();
		finish();
	}

	private void launchRegisterAccount() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(this, RegisterAccountActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.activity_right_in, 0);
	}

	private void launchMain() {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		intent.setClass(this, MainActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	@Override
	protected void onDestroy() {
		releaseResource();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			JuYouApplication.quitApplication(this);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void releaseResource() {
		mTopLogoImageView.setImageDrawable(null);
		mTopLogoBitmap.recycle();
		mTopLogoImageView = null;
		mTopLogoBitmap = null;
		System.gc();
	}

	private class LoginZhaoYanTask extends AsyncTask<Void, Void, Integer> {
		private LoginProgressDialog mLoginDialog;
		private static final int LOGIN_TIMEOUT = 5000;
		private static final int LOGIN_RESULT_SUCCESS = 1;
		private static final int LOGIN_RESULT_FAIL_ACCOUNT_ERROR = 2;
		private Timer mTimer;
		private boolean mIsTimeout = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mLoginDialog = new LoginProgressDialog(LoginActivity.this);
			mLoginDialog.show();
			mIsTimeout = false;
			mTimer = new Timer();
			mTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					LoginZhaoYanTask.this.cancel(true);
					mIsTimeout = true;
				}
			}, LOGIN_TIMEOUT);

		}

		@Override
		protected Integer doInBackground(Void... params) {
			int result = doLoginZhaoyan();
			return result;
		}

		private int doLoginZhaoyan() {
			// TODO
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return LOGIN_RESULT_FAIL_ACCOUNT_ERROR;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (mLoginDialog.isShowing()) {
				mLoginDialog.dismiss();
			}
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
			switch (result) {
			case LOGIN_RESULT_SUCCESS:
				launchMain();
				finish();
				break;
			case LOGIN_RESULT_FAIL_ACCOUNT_ERROR:
				Toast.makeText(mContext, R.string.login_fail_account_error,
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}

		@Override
		protected void onCancelled() {
			if (mLoginDialog.isShowing()) {
				mLoginDialog.dismiss();
			}
			if (mIsTimeout) {
				Toast.makeText(mContext, R.string.login_fail_timeout,
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
