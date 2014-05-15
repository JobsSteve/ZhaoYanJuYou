package com.zhaoyan.juyou.backuprestore;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import com.zhaoyan.juyou.R;
import com.zhaoyan.juyou.backuprestore.Constants.DialogID;
import com.zhaoyan.juyou.backuprestore.Constants.State;
import com.zhaoyan.juyou.backuprestore.RestoreService.OnRestoreStatusListener;
import com.zhaoyan.juyou.backuprestore.RestoreService.RestoreBinder;
import com.zhaoyan.juyou.backuprestore.ResultDialog.ResultEntity;
import com.zhaoyan.juyou.backuprestore.SDCardReceiver.OnSDCardStatusChangedListener;
import com.zhaoyan.juyou.backuprestore.CheckedListActivity.OnCheckedCountChangedListener;

public abstract class AbstractRestoreActivity extends CheckedListActivity implements
        OnCheckedCountChangedListener { 

    private final String TAG = "AbstractRestoreActivity";
    protected ArrayList<String> mUnCheckedList = new ArrayList<String>();
    protected Handler mHandler;
    BaseAdapter mAdapter;
    private LinearLayout mBackupRestoreButtonBar = null;
    private Button mBtRestore = null;
    private Button mSelectAllBtn = null;
    private CheckBox mChboxSelect = null;
    private View mDivider ;
    private ProgressDialog mProgressDialog;
    protected RestoreBinder mRestoreService;
    OnRestoreStatusListener mRestoreListener;
    OnSDCardStatusChangedListener mSDCardListener;
    
    protected TextView mTitleView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setProgressBarIndeterminateVisibility(false);
        setContentView(R.layout.br_data_main);
        init();

        /*
         * bind Restore Service when activity onCreate, and unBind Service when
         * activity onDestroy
         */
        this.bindService();
        Log.d(TAG,  " onCreate");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,  " onDestroy");
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        unRegisteSDCardListener();

        // when startService when to Restore and stopService when onDestroy if
        // the service in IDLE
        if (mRestoreService != null && mRestoreService.getState() == State.INIT) {
            this.stopService();
        }

        // set listener to null avoid some special case when restart after
        // configure changed
        if (mRestoreService != null) {
            mRestoreService.setOnRestoreChangedListner(null);
        }
        this.unBindService();
        mHandler = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,  " onPasue");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,  " onStop");
        if(confirmDialog!=null&&confirmDialog.isShowing()){
    		confirmDialog.dismiss();
    	}
    }

    @Override
    protected void onStart() {
        Log.d(TAG,  " onStart");
        super.onStart();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG,  " onConfigurationChanged");
    }

    @Override
    public void onCheckedCountChanged() {
        mAdapter.notifyDataSetChanged();
        updateButtonState();
    }

    private void init() {
        registerOnCheckedCountChangedListener(this);
        registerSDCardListener();
        
        View titleView = findViewById(R.id.title);
		View backView = titleView.findViewById(R.id.ll_title);
		mTitleView = (TextView) titleView.findViewById(R.id.tv_custom_title);
		backView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finishWithAnimation();
			}
		});
		
        initButton();
        initHandler();
        initLoadingView();
        initDetailList();
        createProgressDlg();
    }
    LinearLayout loadingContent = null;
    private void initLoadingView() {
		// TODO Auto-generated method stub
//    	loadingContent =  (LinearLayout) findViewById(R.id.loading_container);
	}
    
		
	protected void showLoadingContent(boolean show){
		findViewById(R.id.bar_loading).setVisibility(show?View.VISIBLE:View.GONE);
		getListView().setVisibility(!show?View.VISIBLE:View.GONE);
    }
    protected Dialog onCreateDialog(int id) {
        return onCreateDialog(id, null);
    }

    Dialog confirmDialog = null;
    protected Dialog onCreateDialog(int id, Bundle args) {
        Dialog dialog = null;
        Log.d(TAG,  " oncreateDialog, id = " + id);
        switch (id) {

        case DialogID.DLG_RESTORE_CONFIRM:
        	confirmDialog = dialog = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.notice).setMessage(R.string.restore_confirm_notice)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Log.d(TAG,  " to Restore");
                            startRestore();
                        }
                    }).setCancelable(false).create();
            break;

        case DialogID.DLG_SDCARD_REMOVED:
            dialog = new AlertDialog.Builder(this).setTitle(R.string.error)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.sdcard_removed)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if (mRestoreService != null) {
                                mRestoreService.reset();
                            }
                            stopService();
                        }
                    }).setCancelable(false).create();
            break;
        case DialogID.DLG_RUNNING:
            dialog = new AlertDialog.Builder(AbstractRestoreActivity.this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.state_running)
                    .setPositiveButton(android.R.string.ok, null).create();
            break;
        case DialogID.DLG_SDCARD_FULL:
            dialog = new AlertDialog.Builder(this).setTitle(R.string.error)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.sdcard_is_full)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            if (mRestoreService != null) {
                                mRestoreService.cancelRestore();
                            }
                        }
                    }).setCancelable(false).create();
            break;

        default:
            break;
        }
        return dialog;
    }

    private void unRegisteSDCardListener() {
        if (mSDCardListener != null) {
            SDCardReceiver receiver = SDCardReceiver.getInstance();
            receiver.unRegisterOnSDCardChangedListener(mSDCardListener);
        }
    }

    private void registerSDCardListener() {
        mSDCardListener = new OnSDCardStatusChangedListener() {
            @Override
            public void onSDCardStatusChanged(boolean mount) {
                if (!mount) {

                    AbstractRestoreActivity.this.finish();
                }
            }
        };

        SDCardReceiver receiver = SDCardReceiver.getInstance();
        receiver.registerOnSDCardChangedListener(mSDCardListener);
    }
    
    protected void  setTitle(String title) {
		mTitleView.setText(title);
	}
    
    protected void showButtonBar(boolean show) {
		mBackupRestoreButtonBar.setVisibility(show ? View.VISIBLE : View.GONE);
	}

    private void initHandler() {
        mHandler = new Handler();
    }

    private void initButton() {
    	mBackupRestoreButtonBar = (LinearLayout) findViewById(R.id.ll_backup);
        mBtRestore = (Button) findViewById(R.id.btn_backup);
        mBtRestore.setText("恢复");
        mBtRestore.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (getCheckedCount() > 0) {
                	if(Utils.getWorkingInfo()>0){
                		showDialog(DialogID.DLG_RUNNING);
                		return;
                	}
                    showDialog(DialogID.DLG_RESTORE_CONFIRM);
                }
            }
        });
        
        //select all
        mSelectAllBtn = (Button) findViewById(R.id.btn_selectall);
        mSelectAllBtn.setText("全部取消");
        mSelectAllBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isAllChecked(true)) {
					setAllChecked(false);
					mSelectAllBtn.setText("全部选中");
				} else {
					setAllChecked(true);
					mSelectAllBtn.setText("全部取消");
				}
			}
		});
    }

    protected void updateButtonState() {
        if (getCount() == 0) {
            setButtonsEnable(false);
            return;
        }
        if (isAllChecked(false)) {
            mBtRestore.setEnabled(false);
            mSelectAllBtn.setText("全部选中");
        } else {
            mBtRestore.setEnabled(true);
            mSelectAllBtn.setText("全部取消");
        }
    }

    protected void setButtonsEnable(boolean enabled) {
    	if (mSelectAllBtn != null) {
        	mSelectAllBtn.setEnabled(enabled);
        }

        if (mBtRestore != null) {
            mBtRestore.setEnabled(enabled);
        }
    }

    private void initDetailList() {
        mAdapter = initAdapter();
        setListAdapter(mAdapter);
        this.updateButtonState();
    }

    abstract protected BaseAdapter initAdapter();

    protected void notifyListItemCheckedChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        updateButtonState();
    }

    protected ProgressDialog createProgressDlg() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.restoring));
            mProgressDialog.setCancelable(false);
        }
        return mProgressDialog;
    }

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.show();
    }

    protected void setProgressDialogMax(int max) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.setMax(max);
    }

    protected void setProgressDialogProgress(int value) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.setProgress(value);
    }

    protected void setProgressDialogMessage(CharSequence message) {
        if (mProgressDialog == null) {
            mProgressDialog = createProgressDlg();
        }
        mProgressDialog.setMessage(message);
    }

    protected boolean isProgressDialogShowing() {
        return mProgressDialog.isShowing();
    }

    protected void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected boolean isCanStartRestore() {
        if (mRestoreService == null) {
            Log.e(TAG, " isCanStartRestore : mRestoreService is null");
            return false;
        }

        if (mRestoreService.getState() != State.INIT) {
            Log.e(TAG,
                    " isCanStartRestore :Can not to start Restore. Restore Service is ruuning");
            return false;
        }
        return true;
    }

    protected boolean errChecked() {
        boolean ret = false;
        String path = SDCardUtils.getStoragePath(getApplicationContext());
        if (path == null) {
            // no sdcard
            ret = true;
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        showDialog(DialogID.DLG_SDCARD_REMOVED, null);
                    }
                });
            }
        } else if (SDCardUtils.getAvailableSize(path) <= 512) {
            // no space
            ret = true;
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        showDialog(DialogID.DLG_SDCARD_FULL, null);
                    }
                });
            }
        }
        return ret;
    }

    public void setOnRestoreStatusListener(OnRestoreStatusListener listener) {
        mRestoreListener = listener;
        if (mRestoreService != null) {
            mRestoreService.setOnRestoreChangedListner(mRestoreListener);
        }
    }

    protected abstract void afterServiceConnected();

    protected abstract void startRestore();

    private void bindService() {
        getApplicationContext().bindService(new Intent(this, RestoreService.class), mServiceCon,
                Service.BIND_AUTO_CREATE);
    }

    private void unBindService() {
        if (mRestoreService != null) {
            mRestoreService.setOnRestoreChangedListner(null);
        }
        getApplicationContext().unbindService(mServiceCon);
    }

    protected void startService() {
        startService(new Intent(this, RestoreService.class));
    }

    protected void stopService() {
        if (mRestoreService != null) {
            mRestoreService.reset();
        }
        stopService(new Intent(this, RestoreService.class));
    }

    ServiceConnection mServiceCon = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            mRestoreService = (RestoreBinder) service;
            if (mRestoreService != null) {
                mRestoreService.setOnRestoreChangedListner(mRestoreListener);
                afterServiceConnected();
            }
            Log.d(TAG,  " onServiceConnected");
        }

        public void onServiceDisconnected(ComponentName name) {
            mRestoreService = null;
            Log.d(TAG,  " onServiceDisconnected");
        }
    };

    public class NormalRestoreStatusListener implements OnRestoreStatusListener {

        public void onComposerChanged(final int type, final int max) {
            Log.i(TAG, "onComposerChanged");
        }

        public void onProgressChanged(Composer composer, final int progress) {
            Log.i(TAG, "onProgressChange, p = " + progress);
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    public void run() {
                        if (mProgressDialog != null) {
                            mProgressDialog.setProgress(progress);
                        }
                    }
                });
            }
        }

        public void onRestoreEnd(boolean bSuccess, final ArrayList<ResultEntity> resultRecord) {
            Log.i(TAG, "onRestoreEnd");
        }

        public void onRestoreErr(IOException e) {
            Log.i(TAG, "onRestoreErr");
            if (errChecked()) {
                if (mRestoreService != null && mRestoreService.getState() != State.INIT
                        && mRestoreService.getState() != State.FINISH) {
                    mRestoreService.pauseRestore();
                }
            }
        }
    }
    
    protected void finishWithAnimation() {
		finish();
		overridePendingTransition(0, R.anim.activity_right_out);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
				finish();
				overridePendingTransition(0, R.anim.activity_right_out);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
